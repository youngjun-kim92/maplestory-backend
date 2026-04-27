package com.maplestory.ledger.ledger.infrastructure.projection;

import java.time.LocalDate;

public interface WeeklySummaryProjection {
    LocalDate getWeekStart();
    Long getTotalIncome();
    Long getTotalExpense();
    Long getEntryCount();
}
