package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BossKillRepository extends JpaRepository<BossKill, Long> {

    List<BossKill> findByUserIdAndWeekStartOrderByKillDateDesc(Long userId, LocalDate weekStart);

    List<BossKill> findByUserIdAndCharacterId(Long userId, Long characterId);

    @Query(value = """
            SELECT boss_name as bossName, difficulty,
                   COUNT(*) as killCount,
                   SUM(crystal_price) as totalCrystalIncome,
                   AVG(crystal_price) as avgCrystalPrice
            FROM boss_kills
            WHERE user_id = :userId
            GROUP BY boss_name, difficulty
            ORDER BY totalCrystalIncome DESC
            """, nativeQuery = true)
    List<BossStatsProjection> findBossStats(@Param("userId") Long userId);

    @Query(value = """
            SELECT week_start, SUM(crystal_price) as totalIncome
            FROM boss_kills
            WHERE user_id = :userId AND character_id = :characterId
            GROUP BY week_start
            """, nativeQuery = true)
    List<Object[]> findWeeklyIncomeByCharacter(@Param("userId") Long userId, @Param("characterId") Long characterId);
}
