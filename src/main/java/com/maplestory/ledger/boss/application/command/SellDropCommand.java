package com.maplestory.ledger.boss.application.command;

import com.maplestory.ledger.boss.presentation.dto.SellDropRequest;

import java.time.LocalDate;

public record SellDropCommand(
        Long dropRecordId,
        Long saleAmount,
        LocalDate saleDate
) {
    public static SellDropCommand from(Long dropRecordId, SellDropRequest req) {
        return new SellDropCommand(dropRecordId, req.saleAmount(), req.saleDate());
    }
}
