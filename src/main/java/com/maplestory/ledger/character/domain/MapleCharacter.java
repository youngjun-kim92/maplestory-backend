package com.maplestory.ledger.character.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maplestory.ledger.auth.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 메이플스토리 캐릭터 엔티티 — DB의 characters 테이블과 매핑됩니다.
 *
 * 한 사용자는 여러 캐릭터(메인 + 부캐릭터)를 등록할 수 있습니다.
 * Java 예약어인 "Character"와 충돌하지 않도록 MapleCharacter로 명명했습니다.
 *
 * 기능 #8 (부캐릭터 손익분기점 계산)에서 initialInvestment 필드가 핵심 역할을 합니다.
 */
@Entity
@Table(name = "characters")
@Getter @Setter @NoArgsConstructor
public class MapleCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "job_class", length = 100)
    private String jobClass;

    private Integer level = 1;

    @Column(name = "is_main")
    private Boolean isMain = false;

    /**
     * 부캐릭터 육성에 투입한 초기 투자 비용 (메소).
     * 기능 #8 손익분기점 계산 공식: initialInvestment ÷ 주당평균보스수익 = 회수까지 남은 주차
     */
    @Column(name = "initial_investment")
    private Long initialInvestment = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
