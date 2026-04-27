package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BossMasterRepository extends JpaRepository<BossMaster, Long> {
    Optional<BossMaster> findByBossNameAndDifficulty(String bossName, String difficulty);
    List<BossMaster> findAllByOrderByBossNameAscDifficultyAsc();
}
