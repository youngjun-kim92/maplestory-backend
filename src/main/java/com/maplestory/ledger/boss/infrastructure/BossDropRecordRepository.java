package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossDropRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BossDropRecordRepository extends JpaRepository<BossDropRecord, Long> {

    @Query("SELECT r FROM BossDropRecord r JOIN FETCH r.bossKill WHERE r.user.id = :userId AND r.weekStart = :weekStart ORDER BY r.createdAt DESC")
    List<BossDropRecord> findByUserIdAndWeekStartOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart);

    @Query("SELECT r FROM BossDropRecord r JOIN FETCH r.bossKill WHERE r.bossKill.id = :bossKillId")
    List<BossDropRecord> findByBossKillId(@Param("bossKillId") Long bossKillId);

    Optional<BossDropRecord> findByIdAndUserId(Long id, Long userId);
}
