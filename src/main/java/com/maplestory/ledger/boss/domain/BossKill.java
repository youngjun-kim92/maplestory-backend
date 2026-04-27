package com.maplestory.ledger.boss.domain;

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
 * 보스 처치 기록 엔티티 — DB의 boss_kills 테이블과 매핑됩니다.
 *
 * BossService.recordBossKill() 호출 시 이 레코드와 LedgerEntry 레코드가 함께 생성됩니다.
 * ledger_entry_id를 통해 가계부 항목과 1:1로 연결되어 있어, 보스 수익 출처를 추적할 수 있습니다.
 *
 * 기능 #3(보스 수익 통계), #4(결정석 자동 계산), #8(부캐 손익분기점) 계산에 사용됩니다.
 */
@Entity
@Table(name = "boss_kills", indexes = {
        @Index(name = "idx_bk_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_bk_character", columnList = "character_id, week_start")
})
@Getter @Setter @NoArgsConstructor
public class BossKill {

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

    @Column(name = "boss_name", nullable = false, length = 100)
    private String bossName;

    @Column(nullable = false, length = 20)
    private String difficulty;

    /**
     * 처치 당시의 결정석 가격 (메소).
     * boss_master에서 조회한 가격을 복사 저장 — 이후 가격 변경과 무관하게 당시 가격 유지.
     */
    @Column(name = "crystal_price", nullable = false)
    private Long crystalPrice;

    @Column(name = "kill_date", nullable = false)
    private LocalDate killDate;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
