package com.maplestory.ledger.boss.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boss_drop_master", indexes = {
        @Index(name = "idx_bdm_boss_difficulty", columnList = "boss_name, difficulty")
})
@Getter
@NoArgsConstructor
public class BossDropMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boss_name", nullable = false, length = 100)
    private String bossName;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_category", nullable = false, length = 30)
    private ItemCategory itemCategory;

    public enum ItemCategory {
        dark_accessory,    // 칠흑의 보스 장신구
        radiant_accessory, // 광휘의 보스 장신구
        dawn_accessory,    // 여명 장신구
        other              // 기타 (익셉셔널 해머, 연마석, 반지 상자 등)
    }
}
