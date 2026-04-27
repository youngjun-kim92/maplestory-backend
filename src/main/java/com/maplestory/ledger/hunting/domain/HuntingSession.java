package com.maplestory.ledger.hunting.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.ledger.domain.LedgerEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사냥 세션 기록 엔티티 — DB의 hunting_sessions 테이블과 매핑됩니다.
 *
 * 기능 #3(사냥터별 효율 통계)과 기능 #5(솔 에르다 조각 자동 환산)의 핵심 데이터 저장소입니다.
 *
 * 솔 에르다 조각 처리 흐름:
 *   입력: solErdaFragments (개수)
 *   자동 계산: solErdaMesoValue = fragments × User.solErdaFragmentPrice
 *   가계부 등록 총액: income + solErdaMesoValue
 */
@Entity
@Table(name = "hunting_sessions", indexes = {
        @Index(name = "idx_hs_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_hs_map", columnList = "user_id, map_name")
})
@Getter @Setter @NoArgsConstructor
public class HuntingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private MapleCharacter character;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntry ledgerEntry;

    @Column(name = "map_name", nullable = false, length = 200)
    private String mapName;

    /** 사냥 시간 (분 단위). 시간당 수익 계산의 분모로 사용됩니다. */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** 사냥으로 직접 획득한 메소 수익 (솔 에르다 제외) */
    @Column(nullable = false)
    private Long income;

    @Column(name = "sol_erda_fragments")
    private Integer solErdaFragments = 0;

    /** 솔 에르다 조각을 메소로 환산한 금액: solErdaFragments × User.solErdaFragmentPrice */
    @Column(name = "sol_erda_meso_value")
    private Long solErdaMesoValue = 0L;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
