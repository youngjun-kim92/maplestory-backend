package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.BossMaster;

public record BossMasterResponse(
        Long id,
        String bossName,
        String difficulty,
        Long crystalPrice,
        Integer maxAttemptsPerWeek,
        String resetType
) {
    public static BossMasterResponse from(BossMaster b) {
        return new BossMasterResponse(
                b.getId(), b.getBossName(), b.getDifficulty(),
                b.getCrystalPrice(), b.getMaxAttemptsPerWeek(), b.getResetType()
        );
    }
}