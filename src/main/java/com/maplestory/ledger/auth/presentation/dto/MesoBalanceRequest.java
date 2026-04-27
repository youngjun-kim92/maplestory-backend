package com.maplestory.ledger.auth.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MesoBalanceRequest(
        @NotNull @Min(0) Long inventoryMeso,
        @NotNull @Min(0) Long storageMeso
) {}