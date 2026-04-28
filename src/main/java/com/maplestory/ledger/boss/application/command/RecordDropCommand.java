package com.maplestory.ledger.boss.application.command;

import com.maplestory.ledger.boss.presentation.dto.RecordDropRequest;

public record RecordDropCommand(
        Long bossKillId,
        String itemName
) {
    public static RecordDropCommand from(Long bossKillId, RecordDropRequest req) {
        return new RecordDropCommand(bossKillId, req.itemName());
    }
}
