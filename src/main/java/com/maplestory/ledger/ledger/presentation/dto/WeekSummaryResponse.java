package com.maplestory.ledger.ledger.presentation.dto;

import com.maplestory.ledger.ledger.infrastructure.projection.WeeklySummaryProjection;

import java.time.LocalDate;

public record WeekSummaryResponse(
        LocalDate weekStart,
        Long totalIncome,
        Long totalExpense,
        Long entryCount
) {
    public static WeekSummaryResponse from(WeeklySummaryProjection p) {
        return new WeekSummaryResponse(
                p.getWeekStart(), p.getTotalIncome(), p.getTotalExpense(), p.getEntryCount()
        );
    }
}