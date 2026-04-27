package com.maplestory.ledger.ledger.presentation.dto;

import com.maplestory.ledger.goal.application.GoalWarning;

import java.util.List;

public record AddEntryResponse(
        LedgerEntryResponse entry,
        List<GoalWarning> goalWarnings
) {}