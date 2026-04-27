package com.maplestory.ledger.ledger.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.character.domain.MapleCharacter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 가계부 항목(LedgerEntry) 엔티티 — DB의 ledger_entries 테이블과 매핑됩니다.
 *
 * 이 서비스의 핵심 테이블로, 모든 수입과 지출의 단일 원천(Single Source of Truth) 역할을 합니다.
 * 보스 결정석 수익, 사냥 수익, 큐브 지출 등 모든 금전 거래가 여기에 기록됩니다.
 *
 * weekStart 필드를 통해 메이플스토리 주간 초기화(목요일) 기준으로 데이터를 그룹화합니다.
 */
@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_user_date", columnList = "user_id, entry_date")
})
@Getter @Setter @NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
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
     * 주간 가계부 조회 시 이 값으로 그룹핑합니다.
     */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum EntryType {
        income,
        expense
    }

    public enum EntryCategory {
        boss,
        hunting,
        sol_erda,
        cube,
        starforce,
        spell_trace,
        other
    }
}
