package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.BossDropMaster;

public record BossDropMasterResponse(
        Long id,
        String bossName,
        String difficulty,
        String itemName,
        String itemCategory
) {
    public static BossDropMasterResponse from(BossDropMaster m) {
        return new BossDropMasterResponse(
                m.getId(), m.getBossName(), m.getDifficulty(),
                m.getItemName(), m.getItemCategory().name()
        );
    }
}
