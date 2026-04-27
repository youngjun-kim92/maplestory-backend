package com.maplestory.ledger.ledger.application;

import com.maplestory.ledger.common.util.WeekUtil;
import com.maplestory.ledger.ledger.infrastructure.LedgerEntryRepository;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklyNetProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 원장 데이터 읽기 전용 서비스.
 * GoalService, StatsService 등 다른 바운디드 컨텍스트가 원장 집계 데이터를 필요로 할 때
 * LedgerEntryRepository에 직접 의존하는 대신 이 서비스를 통해 접근합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerQueryService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public long getTotalNetSavings(Long userId) {
        Long income = ledgerEntryRepository.sumIncome(userId);
        Long expense = ledgerEntryRepository.sumExpense(userId);
        return (income != null ? income : 0L) - (expense != null ? expense : 0L);
    }

    public long getAvgWeeklyNet(Long userId, int recentWeeks) {
        LocalDate weekStart = WeekUtil.getWeekStart();
        LocalDate startDate = weekStart.minusWeeks(recentWeeks);
        List<WeeklyNetProjection> nets = ledgerEntryRepository.findWeeklyNets(userId, startDate, weekStart);
        if (nets.isEmpty()) return 0L;
        long sum = nets.stream().mapToLong(w -> w.getNetIncome() != null ? w.getNetIncome() : 0L).sum();
        return sum / nets.size();
    }
}