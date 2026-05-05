package com.maplestory.ledger.boss.infrastructure;

import com.maplestory.ledger.boss.domain.BossKill;
import com.maplestory.ledger.boss.infrastructure.projection.BossStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BossKillRepository extends JpaRepository<BossKill, Long> {

    Optional<BossKill> findByIdAndUserId(Long id, Long userId);

    List<BossKill> findByUserIdAndWeekStartOrderByKillDateDesc(Long userId, LocalDate weekStart);

    List<BossKill> findByUserIdAndWeekStartAndCharacterIdOrderByKillDateDesc(
            Long userId, LocalDate weekStart, Long characterId);

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

    /** 같은 캐릭터가 같은 주에 동일 보스+난이도를 이미 처치했는지 확인 */
    boolean existsByCharacterIdAndBossNameAndDifficultyAndWeekStart(
            Long characterId, String bossName, String difficulty, LocalDate weekStart);

    /** 같은 캐릭터가 이번 주에 처치한 주간 보스(weekly) 수 */
    @Query(value = """
            SELECT COUNT(*) FROM boss_kills bk
            JOIN boss_master bm ON bk.boss_name = bm.boss_name AND bk.difficulty = bm.difficulty
            WHERE bk.character_id = :characterId
              AND bk.week_start = :weekStart
              AND bm.reset_type = 'weekly'
            """, nativeQuery = true)
    int countWeeklyBossesByCharacter(@Param("characterId") Long characterId,
                                     @Param("weekStart") LocalDate weekStart);

    /** 유저의 모든 캐릭터별 주간 보스 처치 수 */
    @Query(value = """
            SELECT bk.character_id, c.name, COUNT(*) as cnt
            FROM boss_kills bk
            JOIN maple_characters c ON bk.character_id = c.id
            JOIN boss_master bm ON bk.boss_name = bm.boss_name AND bk.difficulty = bm.difficulty
            WHERE bk.user_id = :userId
              AND bk.week_start = :weekStart
              AND bm.reset_type = 'weekly'
            GROUP BY bk.character_id, c.name
            """, nativeQuery = true)
    List<Object[]> findWeeklyBossCountsPerCharacter(@Param("userId") Long userId,
                                                    @Param("weekStart") LocalDate weekStart);

    @Modifying
    @Query("UPDATE BossKill k SET k.ledgerEntry = null WHERE k.ledgerEntry.id = :entryId")
    void clearLedgerEntryRef(@Param("entryId") Long entryId);

    @Modifying
    @Query("UPDATE BossKill k SET k.character = null WHERE k.character.id = :characterId")
    void clearCharacterRef(@Param("characterId") Long characterId);

    void deleteByUserId(Long userId);
}
