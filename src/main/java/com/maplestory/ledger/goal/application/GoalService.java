package com.maplestory.ledger.goal.application;

import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.goal.domain.Goal;
import com.maplestory.ledger.goal.infrastructure.GoalRepository;
import com.maplestory.ledger.goal.presentation.dto.GoalEstimateResponse;
import com.maplestory.ledger.goal.presentation.dto.GoalRequest;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklyNetProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public Goal createGoal(Long userId, GoalRequest req) {
        User user = userRepository.getReferenceById(userId);
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setItemName(req.itemName());
        goal.setTargetAmount(req.targetAmount());
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public List<Goal> getGoals(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Goal updateGoal(Long userId, Long goalId, GoalRequest req) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("목표를 찾을 수 없습니다."));
        goal.setItemName(req.itemName());
        goal.setTargetAmount(req.targetAmount());
        return goalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("목표를 찾을 수 없습니다."));
        goalRepository.delete(goal);
    }

    @Transactional
    public Goal markAchieved(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("목표를 찾을 수 없습니다."));
        goal.setIsAchieved(true);
        goal.setAchievedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public GoalEstimateResponse getGoalEstimate(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("목표를 찾을 수 없습니다."));

        long netSavings = calculateNetSavings(userId);
        long avgWeeklyNet = calculateAvgWeeklyNet(userId);
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
     * 기능 #10: 지출 추가 시 활성 목표들의 달성 지연 여부를 계산하여 경고 목록을 반환합니다.
     */
    public List<GoalWarning> checkGoalDelays(Long userId, Long newExpenseAmount) {
        List<Goal> activeGoals = goalRepository.findByUserIdAndIsAchievedFalse(userId);
        if (activeGoals.isEmpty()) return List.of();

        long netSavings = calculateNetSavings(userId);
        long avgWeeklyNet = calculateAvgWeeklyNet(userId);
        if (avgWeeklyNet <= 0) return List.of();

        List<GoalWarning> warnings = new ArrayList<>();
        for (Goal goal : activeGoals) {
            long remaining = goal.getTargetAmount() - netSavings;
            if (remaining <= 0) continue;

            long weeksWithout = (long) Math.ceil((double) remaining / avgWeeklyNet);
            long weeksWithExpense = (long) Math.ceil((double) (remaining + newExpenseAmount) / avgWeeklyNet);
            long delay = weeksWithExpense - weeksWithout;

            if (delay > 0) {
                warnings.add(new GoalWarning(
                        goal.getId(), goal.getItemName(), delay,
                        "이번 지출로 인해 '" + goal.getItemName() + "' 목표 달성이 약 " + delay + "주 지연되었습니다."
                ));
            }
        }
        return warnings;
    }

    private long calculateNetSavings(Long userId) {
        Long income = ledgerEntryRepository.sumIncome(userId);
        Long expense = ledgerEntryRepository.sumExpense(userId);
        return (income != null ? income : 0L) - (expense != null ? expense : 0L);
    }

    private long calculateAvgWeeklyNet(Long userId) {
        LocalDate weekStart = WeekUtil.getWeekStart();
        LocalDate startDate = weekStart.minusWeeks(4);
        List<WeeklyNetProjection> weeklyNets = ledgerEntryRepository.findWeeklyNets(userId, startDate, weekStart);
        if (weeklyNets.isEmpty()) return 0L;
        long sum = weeklyNets.stream().mapToLong(w -> w.getNetIncome() != null ? w.getNetIncome() : 0L).sum();
        return sum / weeklyNets.size();
    }
}
