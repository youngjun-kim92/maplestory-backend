package com.maplestory.ledger.ledger.presentation.dto;

public record CategoryStatResponse(
        String category,
        String type,
        Long total,
        Long count,
        Long average
) {}