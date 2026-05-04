package com.maplestory.ledger.boss.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BossKillUpdateRequest(
        @NotNull @Min(1) @Max(6) Integer partySize
) {}
