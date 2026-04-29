package com.maplestory.ledger.character.presentation.dto;

public record CharacterROIResponse(
        Long characterId,
        String characterName,
        Long initialInvestment,
        Long cumulativeIncome,
        Long weeklyAvgIncome,
        Long weeksToBreakEven,
        boolean isBreakEvenReached,
        Long remainingToBreakEven
) {}
