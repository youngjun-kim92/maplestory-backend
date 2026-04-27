package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.BossKill;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BossKillResponse(
        Long id,
        String bossName,
        String difficulty,
        Long crystalPrice,
        LocalDate killDate,
        LocalDate weekStart,
        Long characterId,
        String characterName,
        LocalDateTime createdAt
) {
    public static BossKillResponse from(BossKill kill) {
        return new BossKillResponse(
                kill.getId(),
                kill.getBossName(),
                kill.getDifficulty(),
                kill.getCrystalPrice(),
                kill.getKillDate(),
                kill.getWeekStart(),
                kill.getCharacter() != null ? kill.getCharacter().getId() : null,
                kill.getCharacter() != null ? kill.getCharacter().getName() : null,
                kill.getCreatedAt()
        );
    }
}