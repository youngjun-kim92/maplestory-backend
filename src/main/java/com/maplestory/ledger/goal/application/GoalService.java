package com.maplestory.ledger.goal.application;

import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.goal.domain.Goal;
import com.maplestory.ledger.goal.infrastructure.GoalRepository;
import com.maplestory.ledger.goal.presentation.dto.GoalEstimateResponse;
import com.maplestory.ledger.goal.presentation.dto.GoalRequest;
import com.maplestory.ledger.goal.presentation.dto.GoalResponse;
import com.maplestory.ledger.ledger.application.LedgerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final LedgerQueryService ledgerQueryService;

    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest req) {
        User user = userRepository.getReferenceById(userId);
        return GoalResponse.from(goalRepository.save(Goal.create(user, req.itemName(), req.targetAmount())));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(GoalResponse::from).toList();
    }

    @Transactional
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest req) {
        Goal goal = findGoal(userId, goalId);
        goal.update(req.itemName(), req.targetAmount());
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        goalRepository.delete(findGoal(userId, goalId));
    }

    @Transactional
    public GoalResponse markAchieved(Long userId, Long goalId) {
        Goal goal = findGoal(userId, goalId);
        goal.achieve();
        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public GoalEstimateResponse getGoalEstimate(Long userId, Long goalId) {
        Goal goal = findGoal(userId, goalId);
        long netSavings = ledgerQueryService.getTotalNetSavings(userId);
        long avgWeeklyNet = ledgerQueryService.getAvgWeeklyNet(userId, 4);
        long remaining = Math.max(0, goal.getTargetAmount() - netSavings);
        int progress = (int) Math.min(100, Math.round((double) netSavings / goal.getTargetAmount() * 100));

        Long weeksRemaining = null;
        LocalDate estimatedDate = null;
        if (avgWeeklyNet > 0 && remaining > 0) {
            weeksRemaining = (long) Math.ceil((double) remaining / avgWeeklyNet);
            estimatedDate = LocalDate.now().plusWeeks(weeksRemaining);
        } else if (remaining == 0) {
            weeksRemaining = 0L;
            estimatedDate = LocalDate.now();
        }

        return new GoalEstimateResponse(
                goal.getId(), goal.getItemName(), goal.getTargetAmount(),
                netSavings, remaining, progress,
                avgWeeklyNet, weeksRemaining, estimatedDate
        );
    }

    /**
     * 지출 추가 시 활성 목표들의 달성 지연 여부를 계산하여 경고 목록을 반환합니다.
     * LedgerService가 expense 항목 추가 시 호출합니다.
     */
    public List<GoalWarning> checkGoalDelays(Long userId, Long newExpenseAmount) {
        List<Goal> activeGoals = goalRepository.findByUserIdAndIsAchievedFalse(userId);
        if (activeGoals.isEmpty()) return List.of();

        long netSavings = ledgerQueryService.getTotalNetSavings(userId);
        long avgWeeklyNet = ledgerQueryService.getAvgWeeklyNet(userId, 4);
        if (avgWeeklyNet <= 0) return List.of();

        List<GoalWarning> warnings = new ArrayList<>();
        for (Goal goal : activeGoals) {
            long delay = goal.calculateDelayWeeks(netSavings, avgWeeklyNet, newExpenseAmount);
            if (delay > 0) {
                warnings.add(new GoalWarning(
                        goal.getId(), goal.getItemName(), delay,
                        "이번 지출로 인해 '" + goal.getItemName() + "' 목표 달성이 약 " + delay + "주 지연되었습니다."
                ));
            }
        }
        return warnings;
    }

    private Goal findGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("목표를 찾을 수 없습니다."));
    }
}