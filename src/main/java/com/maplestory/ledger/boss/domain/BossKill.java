package com.maplestory.ledger.boss.domain;

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
@Table(name = "boss_kills", indexes = {
        @Index(name = "idx_bk_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_bk_character", columnList = "character_id, week_start")
})
@Getter
@NoArgsConstructor
public class BossKill {

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

    @Column(name = "boss_name", nullable = false, length = 100)
    private String bossName;

    @Column(nullable = false, length = 20)
    private String difficulty;

    /**
     * 처치 당시의 결정석 가격 스냅샷 (메소).
     * boss_master 가격이 이후 변경되어도 기록 당시 가격이 유지됩니다.
     */
    @Column(name = "crystal_price", nullable = false)
    private Long crystalPrice;

    @Column(name = "kill_date", nullable = false)
    private LocalDate killDate;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /** 파티 인원 수 (1~6). null이면 솔로 처치로 간주. */
    @Column(name = "party_size")
    private Integer partySize;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static BossKill create(User user, MapleCharacter character, LedgerEntry ledgerEntry,
                                   String bossName, String difficulty, Long crystalPrice,
                                   LocalDate killDate, LocalDate weekStart, Integer partySize) {
        BossKill kill = new BossKill();
        kill.user = user;
        kill.character = character;
        kill.ledgerEntry = ledgerEntry;
        kill.bossName = bossName;
        kill.difficulty = difficulty;
        kill.crystalPrice = crystalPrice;
        kill.killDate = killDate;
        kill.weekStart = weekStart;
        kill.partySize = partySize;
        return kill;
    }
}