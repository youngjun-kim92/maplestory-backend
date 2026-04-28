package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossDropRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BossDropRecordRepository extends JpaRepository<BossDropRecord, Long> {
    List<BossDropRecord> findByUserIdAndWeekStartOrderByCreatedAtDesc(Long userId, LocalDate weekStart);
    List<BossDropRecord> findByBossKillId(Long bossKillId);
    Optional<BossDropRecord> findByIdAndUserId(Long id, Long userId);
}
