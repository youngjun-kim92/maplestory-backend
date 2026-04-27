package com.maplestory.ledger.stats.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExpCalculatorRequest(
        @NotNull @Min(1) @Max(300) Integer currentLevel,
        @NotNull @Min(0) @Max(99) Double currentExpPercent,
        @NotNull @Min(1) Double avgExpPerHour,
        @Min(1) @Max(300) Integer targetLevel
) {}
