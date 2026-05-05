package com.maplestory.ledger.boss.presentation.dto;

public record CharacterBossCountResponse(
        Long characterId,
        String characterName,
        int weeklyBossCount
) {}
