package com.maplestory.ledger.hunting.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HuntingSessionRequest(
        @NotBlank(message = "사냥터 이름을 입력해주세요.") String mapName,
        @NotNull @Min(value = 1, message = "사냥 시간은 1분 이상이어야 합니다.") Integer durationMinutes,
        @NotNull @Min(value = 0, message = "수익은 0 이상이어야 합니다.") Long income,
        Integer solErdaFragments,
        @NotNull(message = "날짜를 입력해주세요.") LocalDate sessionDate,
        Long characterId
) {}
