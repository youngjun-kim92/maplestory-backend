package com.maplestory.ledger.hunting.domain;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hunting_sessions", indexes = {
        @Index(name = "idx_hs_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_hs_map", columnList = "user_id, map_name")
})
@Getter
@NoArgsConstructor
public class HuntingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private MapleCharacter character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntry ledgerEntry;

    @Column(name = "map_name", length = 200)
    private String mapName;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(nullable = false)
    private Long income;

    @Column(name = "sol_erda_fragments")
    private Integer solErdaFragments = 0;

    /** solErdaFragments × User.solErdaFragmentPrice */
    @Column(name = "sol_erda_meso_value")
    private Long solErdaMesoValue = 0L;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void update(Long income, Integer solErdaFragments, Long solErdaMesoValue, LocalDate sessionDate, LocalDate weekStart) {
        this.income = income;
        this.solErdaFragments = solErdaFragments != null ? solErdaFragments : 0;
        this.solErdaMesoValue = solErdaMesoValue != null ? solErdaMesoValue : 0L;
        this.sessionDate = sessionDate;
        this.weekStart = weekStart;
    }

    public static HuntingSession create(User user, MapleCharacter character, LedgerEntry ledgerEntry,
                                        String mapName, Integer durationMinutes, Long income,
                                        Integer solErdaFragments, Long solErdaMesoValue,
                                        LocalDate sessionDate, LocalDate weekStart) {
        HuntingSession s = new HuntingSession();
        s.user = user;
        s.character = character;
        s.ledgerEntry = ledgerEntry;
        s.mapName = mapName;
        s.durationMinutes = durationMinutes;
        s.income = income;
        s.solErdaFragments = solErdaFragments;
        s.solErdaMesoValue = solErdaMesoValue;
        s.sessionDate = sessionDate;
        s.weekStart = weekStart;
        return s;
    }
}