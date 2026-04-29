package com.maplestory.ledger.ledger.domain;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.character.domain.MapleCharacter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_user_date", columnList = "user_id, entry_date")
})
@Getter
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private MapleCharacter character;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntryCategory category;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 500)
    private String description;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /**
     * 해당 주의 목요일 날짜 (WeekUtil.getWeekStart()로 계산).
     * 메이플스토리는 목요일 기준으로 주간 초기화되므로 이 값으로 그룹핑합니다.
     */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static LedgerEntry create(User user, MapleCharacter character,
                                     EntryType type, EntryCategory category,
                                     Long amount, String description,
                                     LocalDate entryDate, LocalDate weekStart) {
        LedgerEntry e = new LedgerEntry();
        e.user = user;
        e.character = character;
        e.type = type;
        e.category = category;
        e.amount = amount;
        e.description = description;
        e.entryDate = entryDate;
        e.weekStart = weekStart;
        return e;
    }

    public enum EntryType {
        income,
        expense
    }

    public enum EntryCategory {
        boss,
        hunting,
        trade,
        auction,
        sol_erda,
        cube,
        starforce,
        spell_trace,
        other
    }
}