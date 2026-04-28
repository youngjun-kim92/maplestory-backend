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
@Table(name = "boss_drop_records", indexes = {
        @Index(name = "idx_bdr_user_week", columnList = "user_id, week_start"),
        @Index(name = "idx_bdr_boss_kill", columnList = "boss_kill_id")
})
@Getter
@NoArgsConstructor
public class BossDropRecord {

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
    @JoinColumn(name = "boss_kill_id", nullable = false)
    private BossKill bossKill;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_category", nullable = false, length = 30)
    private BossDropMaster.ItemCategory itemCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DropStatus status;

    @Column(name = "sale_amount")
    private Long saleAmount;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    /** 보스 처치 시점의 주 (목요일 기준). 드랍 기록 조회 그룹핑에 사용. */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntry ledgerEntry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum DropStatus {
        holding,
        sold
    }

    public static BossDropRecord create(User user, MapleCharacter character, BossKill bossKill,
                                        String itemName, BossDropMaster.ItemCategory itemCategory,
                                        LocalDate weekStart) {
        BossDropRecord r = new BossDropRecord();
        r.user = user;
        r.character = character;
        r.bossKill = bossKill;
        r.itemName = itemName;
        r.itemCategory = itemCategory;
        r.status = DropStatus.holding;
        r.weekStart = weekStart;
        return r;
    }

    public void sell(Long saleAmount, LocalDate saleDate, LedgerEntry ledgerEntry) {
        if (this.status == DropStatus.sold) {
            throw new IllegalStateException("이미 판매된 아이템입니다.");
        }
        this.status = DropStatus.sold;
        this.saleAmount = saleAmount;
        this.saleDate = saleDate;
        this.ledgerEntry = ledgerEntry;
    }
}
