package com.maplestory.ledger.boss.application.command;

import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;

import java.time.LocalDate;

public record RecordBossKillCommand(
        String bossName,
        String difficulty,
        LocalDate killDate,
        Long characterId
) {
    public static RecordBossKillCommand from(BossKillRequest req) {
        return new RecordBossKillCommand(
                req.bossName(), req.difficulty(), req.killDate(), req.characterId()
        );
    }
}