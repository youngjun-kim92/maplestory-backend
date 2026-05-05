package com.maplestory.ledger.hunting.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HuntingSessionUpdateRequest(
        @NotNull @Min(value = 0) Long income,
        Integer solErdaFragments,
        @NotNull LocalDate sessionDate
) {}
