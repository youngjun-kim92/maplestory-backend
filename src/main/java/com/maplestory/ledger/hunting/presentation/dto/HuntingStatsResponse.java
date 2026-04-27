package com.maplestory.ledger.hunting.presentation.dto;

public record HuntingStatsResponse(
        String mapName,
        Long sessionCount,
        Long totalIncome,
        Long totalMinutes,
        Long avgIncomePerHour
) {}
