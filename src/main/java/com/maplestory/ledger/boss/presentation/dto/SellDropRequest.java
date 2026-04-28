package com.maplestory.ledger.boss.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record SellDropRequest(
        @NotNull @Positive Long saleAmount,
        @NotNull LocalDate saleDate
) {}
