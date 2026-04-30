package com.maplestory.ledger.ledger.infrastructure;

import com.maplestory.ledger.ledger.domain.LedgerEntry;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklyNetProjection;
import com.maplestory.ledger.ledger.infrastructure.projection.WeeklySummaryProjection;
import com.maplestory.ledger.stats.infrastructure.projection.UserWeeklyAvgProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByUserIdAndWeekStartOrderByEntryDateDescCreatedAtDesc(Long userId, LocalDate weekStart);

    Optional<LedgerEntry> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);

    @Modifying
    @Query("UPDATE LedgerEntry e SET e.character = null WHERE e.character.id = :characterId")
    void clearCharacterRef(@Param("characterId") Long characterId);

    @Query(value = """
            SELECT week_start as weekStart,
                   SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) as totalIncome,
                   SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) as totalExpense,
                   COUNT(*) as entryCount,
                   COALESCE(SUM(sol_erda_fragments), 0) as totalSolErdaFragments
            FROM ledger_entries
            WHERE user_id = :userId
            GROUP BY week_start
            ORDER BY week_start DESC
            """, nativeQuery = true)
    List<WeeklySummaryProjection> findWeeklySummaries(@Param("userId") Long userId);

    @Query(value = """
            SELECT category, type,
                   SUM(amount) as total,
                   COUNT(*) as cnt,
                   AVG(amount) as average
            FROM ledger_entries
            WHERE user_id = :userId AND week_start >= :startDate
            GROUP BY category, type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> findCategoryStats(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM ledger_entries WHERE user_id = :userId AND type = 'income'", nativeQuery = true)
    Long sumIncome(@Param("userId") Long userId);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM ledger_entries WHERE user_id = :userId AND type = 'expense'", nativeQuery = true)
    Long sumExpense(@Param("userId") Long userId);

    @Query(value = """
            SELECT week_start as weekStart,
                   SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) -
                   SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END) as netIncome
            FROM ledger_entries
            WHERE user_id = :userId AND week_start >= :startDate AND week_start < :endDate
            GROUP BY week_start
            """, nativeQuery = true)
    List<WeeklyNetProjection> findWeeklyNets(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT user_id as userId, AVG(weekly_income) as avgWeeklyIncome
            FROM (
                SELECT user_id, week_start,
                       SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) as weekly_income
                FROM ledger_entries
                WHERE week_start >= :startDate AND week_start < :endDate
                GROUP BY user_id, week_start
            ) weekly_data
            GROUP BY user_id
            """, nativeQuery = true)
    List<UserWeeklyAvgProjection> findAllUsersAvgWeeklyIncome(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);

    @Query(value = """
            SELECT week_start,
                   SUM(CASE WHEN category = 'boss'    AND type = 'income' THEN amount ELSE 0 END) as bossIncome,
                   SUM(CASE WHEN category = 'hunting' AND type = 'income' THEN amount ELSE 0 END) as huntingIncome,
                   SUM(CASE WHEN category = 'auction' AND type = 'income' THEN amount ELSE 0 END) as auctionIncome,
                   SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END) as totalIncome
            FROM ledger_entries
            WHERE user_id = :userId AND week_start >= :startDate
            GROUP BY week_start
            ORDER BY week_start ASC
            """, nativeQuery = true)
    List<Object[]> findIncomeSourceTrend(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT week_start,
                   SUM(amount) as totalIncome
            FROM ledger_entries
            WHERE user_id = :userId AND character_id = :characterId AND type = 'income'
            GROUP BY week_start
            ORDER BY week_start ASC
            """, nativeQuery = true)
    List<Object[]> findCharacterBossIncomeByWeek(@Param("userId") Long userId,
                                                  @Param("characterId") Long characterId);

    @Query(value = """
            SELECT le.character_id as characterId,
                   c.name as characterName,
                   c.job_class as jobClass,
                   c.is_main as isMain,
                   SUM(CASE WHEN le.type = 'income' THEN le.amount ELSE 0 END) as totalIncome,
                   SUM(CASE WHEN le.type = 'expense' THEN le.amount ELSE 0 END) as totalExpense,
                   COUNT(*) as entryCount
            FROM ledger_entries le
            JOIN characters c ON le.character_id = c.id
            WHERE le.user_id = :userId AND le.character_id IS NOT NULL
            GROUP BY le.character_id, c.name, c.job_class, c.is_main
            ORDER BY totalIncome DESC
            """, nativeQuery = true)
    List<Object[]> findCharacterStats(@Param("userId") Long userId);
}
