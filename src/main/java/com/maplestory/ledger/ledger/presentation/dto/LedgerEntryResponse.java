package com.maplestory.ledger.ledger.presentation.dto;

import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryCategory;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LedgerEntryResponse(
        Long id,
        EntryType type,
        EntryCategory category,
        Long amount,
        String description,
        LocalDate entryDate,
        LocalDate weekStart,
        Long characterId,
        String characterName,
        LocalDateTime createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId(),
                e.getType(),
                e.getCategory(),
                e.getAmount(),
                e.getDescription(),
                e.getEntryDate(),
                e.getWeekStart(),
                e.getCharacter() != null ? e.getCharacter().getId() : null,
                e.getCharacter() != null ? e.getCharacter().getName() : null,
                e.getCreatedAt()
        );
    }
}