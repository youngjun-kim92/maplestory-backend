package com.maplestory.ledger.character.domain;

import com.maplestory.ledger.auth.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
@Getter
@NoArgsConstructor
public class MapleCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * 손익분기점 계산 공식: initialInvestment ÷ 주당평균보스수익 = 회수까지 남은 주차
     */
    @Column(name = "initial_investment")
    private Long initialInvestment = 0L;

    @Column(name = "sol_erda_fragments")
    private Integer solErdaFragments = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static MapleCharacter create(User user, String name, String jobClass,
                                        Integer level, Boolean isMain, Long initialInvestment) {
        MapleCharacter c = new MapleCharacter();
        c.user = user;
        c.name = name;
        c.jobClass = jobClass;
        c.level = level != null ? level : 1;
        c.isMain = isMain != null ? isMain : false;
        c.initialInvestment = initialInvestment != null ? initialInvestment : 0L;
        c.solErdaFragments = 0;
        return c;
    }

    public void update(String name, String jobClass, Integer level, Boolean isMain, Long initialInvestment, Integer solErdaFragments) {
        if (name != null) this.name = name;
        if (jobClass != null) this.jobClass = jobClass;
        if (level != null) this.level = level;
        if (isMain != null) this.isMain = isMain;
        if (initialInvestment != null) this.initialInvestment = initialInvestment;
        if (solErdaFragments != null) this.solErdaFragments = solErdaFragments;
    }

    public void addSolErdaFragments(int count) {
        this.solErdaFragments = (this.solErdaFragments != null ? this.solErdaFragments : 0) + count;
    }
}