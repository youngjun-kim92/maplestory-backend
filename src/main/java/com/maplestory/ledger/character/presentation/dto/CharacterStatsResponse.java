package com.maplestory.ledger.character.presentation.dto;

public record CharacterStatsResponse(
        Long characterId,
        String characterName,
        String jobClass,
        Boolean isMain,
        Long totalIncome,
        Long totalExpense,
        Long netProfit,
        Long entryCount
) {
    public static CharacterStatsResponse from(Object[] row) {
        long income = ((Number) row[4]).longValue();
        long expense = ((Number) row[5]).longValue();
        return new CharacterStatsResponse(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                row[3] != null && ((Number) row[3]).intValue() == 1,
                income,
                expense,
                income - expense,
                ((Number) row[6]).longValue()
        );
    }
}
