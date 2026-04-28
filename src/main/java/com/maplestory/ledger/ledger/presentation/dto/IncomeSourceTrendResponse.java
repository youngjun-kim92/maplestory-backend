package com.maplestory.ledger.ledger.presentation.dto;

import java.time.LocalDate;

public record IncomeSourceTrendResponse(
        LocalDate weekStart,
        Long bossIncome,
        Long huntingIncome,
        Long auctionIncome,
        Long totalIncome
) {}
