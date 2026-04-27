package com.maplestory.ledger.stats.infrastructure.projection;

public interface UserWeeklyAvgProjection {
    Long getUserId();
    Double getAvgWeeklyIncome();
}
