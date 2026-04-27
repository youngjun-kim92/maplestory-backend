package com.maplestory.ledger.ledger.presentation.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyLedgerResponse(
        LocalDate weekStart,
        List<LedgerEntryResponse> entries,
        Summary summary
) {
    public record Summary(long totalIncome, long totalExpense, long netProfit) {}
}