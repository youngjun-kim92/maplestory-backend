package com.maplestory.ledger.boss.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record BossKillRequest(
        @NotBlank(message = "보스 이름을 입력해주세요.") String bossName,
        @NotBlank(message = "난이도를 선택해주세요.") String difficulty,
        @NotNull(message = "날짜를 입력해주세요.") LocalDate killDate,
        Long characterId,
        @Min(1) @Max(6) Integer partySize,
        List<InlineExpense> expenses
) {
    public record InlineExpense(
            @NotBlank String category,
            @NotNull @Positive Long amount,
            String description
    ) {}
}
