package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.BossKill;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BossKillResponse(
        Long id,
        String bossName,
        String difficulty,
        Long crystalPrice,
        Long income,
        Long totalExpense,
        String resetType,
        LocalDate killDate,
        LocalDate weekStart,
        Long characterId,
        String characterName,
        Integer partySize,
        LocalDateTime createdAt
) {
    public static BossKillResponse from(BossKill kill) {
        int party = kill.getPartySize() != null && kill.getPartySize() > 1 ? kill.getPartySize() : 1;
        long income = kill.getCrystalPrice() / party;
        return new BossKillResponse(
                kill.getId(),
                kill.getBossName(),
                kill.getDifficulty(),
                kill.getCrystalPrice(),
                income,
                kill.getTotalExpense() != null ? kill.getTotalExpense() : 0L,
                kill.getResetType(),
                kill.getKillDate(),
                kill.getWeekStart(),
                kill.getCharacter() != null ? kill.getCharacter().getId() : null,
                kill.getCharacter() != null ? kill.getCharacter().getName() : null,
                kill.getPartySize(),
                kill.getCreatedAt()
        );
    }
}