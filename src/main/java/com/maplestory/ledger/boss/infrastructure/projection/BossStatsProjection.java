package com.maplestory.ledger.boss.infrastructure.projection;

public interface BossStatsProjection {
    String getBossName();
    String getDifficulty();
    Long getKillCount();
    Long getTotalCrystalIncome();
    Double getAvgCrystalPrice();
}
