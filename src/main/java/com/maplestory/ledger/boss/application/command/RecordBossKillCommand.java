package com.maplestory.ledger.boss.application.command;

import com.maplestory.ledger.boss.presentation.dto.BossKillRequest;

import java.time.LocalDate;
import java.util.List;

public record RecordBossKillCommand(
        String bossName,
        String difficulty,
        LocalDate killDate,
        Long characterId,
        Integer partySize,
        List<InlineExpense> expenses
) {
    public record InlineExpense(String category, Long amount, String description) {}

    public static RecordBossKillCommand from(BossKillRequest req) {
        List<InlineExpense> expenses = req.expenses() == null ? List.of() :
                req.expenses().stream()
                        .map(e -> new InlineExpense(e.category(), e.amount(), e.description()))
                        .toList();
        return new RecordBossKillCommand(
                req.bossName(), req.difficulty(), req.killDate(), req.characterId(), req.partySize(), expenses
        );
    }
}