package com.maplestory.ledger.ledger.presentation.dto;

import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryCategory;
import com.maplestory.ledger.ledger.domain.LedgerEntry.EntryType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LedgerEntryRequest(
        @NotNull(message = "타입을 선택해주세요.") EntryType type,
        @NotNull(message = "카테고리를 선택해주세요.") EntryCategory category,
        @NotNull @Min(value = 1, message = "금액은 1 이상이어야 합니다.") Long amount,
        String description,
        @NotNull(message = "날짜를 입력해주세요.") LocalDate entryDate,
        Long characterId
) {}
