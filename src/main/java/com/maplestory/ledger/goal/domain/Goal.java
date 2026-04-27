package com.maplestory.ledger.goal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maplestory.ledger.auth.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 목표 아이템 엔티티 — DB의 goals 테이블과 매핑됩니다.
 *
 * 기능 #6(목표 아이템 달성 예측)과 기능 #10(과소비 경고)의 핵심 데이터입니다.
 *
 * 지출 추가 시 이 목표들의 달성 지연 여부를 자동으로 검사하여 경고를 발생시킵니다.
 */
@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount;

    /**
     * 달성 여부.
     * false: 진행 중 (과소비 경고 대상)
     * true: 달성 완료 (경고 대상에서 제외)
     */
    @Column(name = "is_achieved")
    private Boolean isAchieved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;
}
