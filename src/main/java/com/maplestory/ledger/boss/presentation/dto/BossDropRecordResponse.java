package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.BossDropRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BossDropRecordResponse(
        Long id,
        Long bossKillId,
        String bossName,
        String difficulty,
        String itemName,
        String itemCategory,
        String status,
        Long saleAmount,
        LocalDate saleDate,
        LocalDate weekStart,
        Long characterId,
        String characterName,
        LocalDateTime createdAt
) {
    public static BossDropRecordResponse from(BossDropRecord r) {
        return new BossDropRecordResponse(
                r.getId(),
                r.getBossKill().getId(),
                r.getBossKill().getBossName(),
                r.getBossKill().getDifficulty(),
                r.getItemName(),
                r.getItemCategory().name(),
                r.getStatus().name(),
                r.getSaleAmount(),
                r.getSaleDate(),
                r.getWeekStart(),
                r.getCharacter() != null ? r.getCharacter().getId() : null,
                r.getCharacter() != null ? r.getCharacter().getName() : null,
                r.getCreatedAt()
        );
    }
}
