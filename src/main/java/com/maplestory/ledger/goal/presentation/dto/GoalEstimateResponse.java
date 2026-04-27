package com.maplestory.ledger.goal.presentation.dto;

import java.time.LocalDate;

public record GoalEstimateResponse(
        Long goalId,
        String itemName,
        Long targetAmount,
        Long currentSavings,
        Long remaining,
        int progressPercent,
        Long avgWeeklyNet,
        Long weeksRemaining,
        LocalDate estimatedDate
) {}
