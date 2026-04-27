package com.maplestory.ledger.goal.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoalRequest(
        @NotBlank(message = "아이템 이름을 입력해주세요.") String itemName,
        @NotNull @Min(value = 1, message = "목표 금액은 1 이상이어야 합니다.") Long targetAmount
) {}
