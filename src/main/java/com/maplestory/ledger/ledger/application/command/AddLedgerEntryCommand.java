package com.maplestory.ledger.ledger.application.command;

import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryCategory;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryRequest;

import java.time.LocalDate;

public record AddLedgerEntryCommand(
        EntryType type,
        EntryCategory category,
        Long amount,
        String description,
        LocalDate entryDate,
        Long characterId
) {
    public static AddLedgerEntryCommand from(LedgerEntryRequest req) {
        return new AddLedgerEntryCommand(
                req.type(), req.category(), req.amount(),
                req.description(), req.entryDate(), req.characterId()
        );
    }
}