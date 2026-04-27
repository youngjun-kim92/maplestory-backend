package com.maplestory.ledger.character.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CharacterRequest(
        @NotBlank(message = "캐릭터 이름을 입력해주세요.") String name,
        String jobClass,
        Integer level,
        Boolean isMain,
        Long initialInvestment
) {}
