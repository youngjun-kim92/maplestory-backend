package com.maplestory.ledger.stats.presentation.dto;

public record StatsComparisonResponse(
        Long userAvgWeeklyIncome,
        Long globalAvgWeeklyIncome,
        int totalUsers,
        int percentile,
        String message
) {}
