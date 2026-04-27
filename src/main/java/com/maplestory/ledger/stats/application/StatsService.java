package com.maplestory.ledger.stats.application;

import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import com.maplestory.ledger.stats.infrastructure.projection.UserWeeklyAvgProjection;
import com.maplestory.ledger.stats.presentation.dto.ExpCalculatorRequest;
import com.maplestory.ledger.stats.presentation.dto.ExpCalculatorResponse;
import com.maplestory.ledger.stats.presentation.dto.StatsComparisonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * 기능 #9: 익명 기반 유저 평균 수익 비교.
     * 전체 유저 데이터를 익명화(user_id만 사용)하여 백분위를 계산합니다.
     */
    @Transactional(readOnly = true)
    public StatsComparisonResponse getUserComparison(Long userId) {
        LocalDate weekStart = WeekUtil.getWeekStart();
        LocalDate startDate = weekStart.minusWeeks(4);

        List<UserWeeklyAvgProjection> allUsersData =
                ledgerEntryRepository.findAllUsersAvgWeeklyIncome(startDate, weekStart);

        if (allUsersData.isEmpty()) {
            return new StatsComparisonResponse(0L, 0L, 0, 0, "비교할 데이터가 충분하지 않습니다.");
        }

        double userAvg = allUsersData.stream()
                .filter(p -> p.getUserId() != null && p.getUserId().equals(userId))
                .findFirst()
                .map(UserWeeklyAvgProjection::getAvgWeeklyIncome)
                .orElse(0.0);

        double globalAvg = allUsersData.stream()
                .mapToDouble(p -> p.getAvgWeeklyIncome() != null ? p.getAvgWeeklyIncome() : 0.0)
                .average().orElse(0.0);

        long usersBelow = allUsersData.stream()
                .filter(p -> p.getAvgWeeklyIncome() != null && p.getAvgWeeklyIncome() < userAvg)
                .count();

        int percentile = (int) Math.round((double) usersBelow / allUsersData.size() * 100);

        String message;
        if (allUsersData.size() <= 1) {
            message = "비교할 데이터가 충분하지 않습니다.";
        } else if (percentile >= 50) {
            message = "내 수익은 전체 유저 상위 " + (100 - percentile) + "%입니다.";
        } else {
            message = "내 수익은 전체 유저 하위 " + percentile + "%입니다.";
        }

        return new StatsComparisonResponse(
                (long) userAvg, (long) globalAvg, allUsersData.size(), percentile, message
        );
    }

    /**
     * 기능 #7: 레벨업 경험치 계산기.
     * 현재 경험치%와 시간당 평균 경험치%를 입력받아 목표 레벨까지의 예상 시간을 계산합니다.
     */
    public ExpCalculatorResponse calculateExp(ExpCalculatorRequest req) {
        int targetLevel = req.targetLevel() != null ? req.targetLevel() : req.currentLevel() + 1;

        if (targetLevel <= req.currentLevel()) {
            return new ExpCalculatorResponse(req.currentLevel(), targetLevel, 0.0, 0.0);
        }

        int levelsToGain = targetLevel - req.currentLevel();
        double totalPercent = (100.0 - req.currentExpPercent()) + (levelsToGain - 1) * 100.0;
        double hoursToTarget = Math.round(totalPercent / req.avgExpPerHour() * 10.0) / 10.0;
        double daysToTarget = Math.round(hoursToTarget / 24.0 * 10.0) / 10.0;

        return new ExpCalculatorResponse(req.currentLevel(), targetLevel, hoursToTarget, daysToTarget);
    }
}
