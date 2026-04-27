package com.maplestory.ledger.boss.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boss_master", uniqueConstraints = {
        @UniqueConstraint(name = "uk_boss_difficulty", columnNames = {"boss_name", "difficulty"})
})
@Getter
@NoArgsConstructor
public class BossMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boss_name", nullable = false, length = 100)
    private String bossName;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "crystal_price", nullable = false)
    private Long crystalPrice;

    @Column(name = "max_attempts_per_week")
    private Integer maxAttemptsPerWeek = 1;
}