package com.maplestory.ledger.hunting.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HuntingSessionRequest(
        @NotNull @Min(value = 0, message = "수익은 0 이상이어야 합니다.") Long income,
        Integer solErdaFragments,
        @NotNull(message = "날짜를 입력해주세요.") LocalDate sessionDate,
        @NotNull(message = "캐릭터를 선택해주세요.") Long characterId
) {}
