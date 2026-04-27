package com.maplestory.ledger.stats.presentation.dto;

public record ExpCalculatorResponse(
        Integer currentLevel,
        Integer targetLevel,
        Double hoursToTarget,
        Double daysToTarget
) {}
