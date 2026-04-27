package com.maplestory.ledger.hunting.infrastructure;

import com.maplestory.ledger.hunting.domain.HuntingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HuntingSessionRepository extends JpaRepository<HuntingSession, Long> {

    List<HuntingSession> findByUserIdAndWeekStartOrderBySessionDateDesc(Long userId, LocalDate weekStart);

    List<HuntingSession> findByUserId(Long userId);

    @Query(value = """
            SELECT map_name,
                   COUNT(*) as sessionCount,
                   SUM(income + sol_erda_meso_value) as totalIncome,
                   SUM(duration_minutes) as totalMinutes,
                   ROUND(SUM(income + sol_erda_meso_value) * 60.0 / NULLIF(SUM(duration_minutes), 0)) as avgIncomePerHour
            FROM hunting_sessions
            WHERE user_id = :userId
            GROUP BY map_name
            ORDER BY avgIncomePerHour DESC
            """, nativeQuery = true)
    List<Object[]> findHuntingStatsByMap(@Param("userId") Long userId);
}
