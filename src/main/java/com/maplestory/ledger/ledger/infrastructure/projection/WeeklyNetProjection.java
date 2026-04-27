package com.maplestory.ledger.ledger.infrastructure.projection;

import java.time.LocalDate;

public interface WeeklyNetProjection {
    LocalDate getWeekStart();
    Long getNetIncome();
}
