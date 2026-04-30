package com.maplestory.ledger.ledger.application.command;

import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryCategory;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;
import com.maplestory.ledger.ledger.presentation.dto.LedgerEntryRequest;

import java.time.LocalDate;

public record UpdateLedgerEntryCommand(
        EntryType type,
        EntryCategory category,
        Long amount,
        String description,
        LocalDate entryDate,
        Long characterId,
        Integer solErdaFragments
) {
    public static UpdateLedgerEntryCommand from(LedgerEntryRequest req) {
        return new UpdateLedgerEntryCommand(
                req.type(), req.category(), req.amount(),
                req.description(), req.entryDate(), req.characterId(),
                req.solErdaFragments()
        );
    }
}
