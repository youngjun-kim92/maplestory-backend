package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossDropMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BossDropMasterRepository extends JpaRepository<BossDropMaster, Long> {
    List<BossDropMaster> findByBossNameAndDifficulty(String bossName, String difficulty);
}
