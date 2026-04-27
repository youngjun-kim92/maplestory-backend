package com.maplestory.ledger.boss.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 보스 결정석 가격 마스터 데이터 엔티티 — DB의 boss_master 테이블과 매핑됩니다.
 *
 * 기능 #4(보스 결정석 자동 계산)의 핵심 참조 테이블입니다.
 * 보스 이름 + 난이도 조합에 대한 결정석 가격을 저장합니다.
 *
 * 초기 데이터는 src/main/resources/data.sql에서 INSERT IGNORE로 자동 삽입됩니다.
 */
@Entity
@Table(name = "boss_master", uniqueConstraints = {
        @UniqueConstraint(name = "uk_boss_difficulty", columnNames = {"boss_name", "difficulty"})
})
@Getter @Setter @NoArgsConstructor
public class BossMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boss_name", nullable = false, length = 100)
    private String bossName;

    @Column(nullable = false, length = 20)
    private String difficulty;

    /** 해당 보스의 주간 결정석 가격 (메소 단위) */
    @Column(name = "crystal_price", nullable = false)
    private Long crystalPrice;

    @Column(name = "max_attempts_per_week")
    private Integer maxAttemptsPerWeek = 1;
}
