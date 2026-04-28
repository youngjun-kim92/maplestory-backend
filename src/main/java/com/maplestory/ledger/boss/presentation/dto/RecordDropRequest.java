package com.maplestory.ledger.boss.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RecordDropRequest(
        @NotBlank(message = "아이템 이름을 입력해주세요.") String itemName
) {}
