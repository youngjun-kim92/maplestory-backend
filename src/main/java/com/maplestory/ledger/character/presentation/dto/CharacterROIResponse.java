package com.maplestory.ledger.character.presentation.dto;

public record CharacterROIResponse(
        Long characterId,
        String characterName,
        Long initialInvestment,
        Long cumulativeBossIncome,
        Long weeklyAvgBossIncome,
        Long weeksToBreakEven,
        boolean isBreakEvenReached,
        Long remainingToBreakEven
) {}
