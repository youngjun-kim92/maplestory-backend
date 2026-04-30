package com.maplestory.ledger.ledger.presentation.dto;

import com.maplestory.ledger.ledger.infrastructure.projection.WeeklySummaryProjection;

import java.time.LocalDate;

public record WeekSummaryResponse(
        LocalDate weekStart,
        Long totalIncome,
        Long totalExpense,
        Long entryCount,
        Long totalSolErdaFragments
) {
    public static WeekSummaryResponse from(WeeklySummaryProjection p) {
        return new WeekSummaryResponse(
                p.getWeekStart(), p.getTotalIncome(), p.getTotalExpense(), p.getEntryCount(),
                p.getTotalSolErdaFragments() != null ? p.getTotalSolErdaFragments() : 0L
        );
    }
}