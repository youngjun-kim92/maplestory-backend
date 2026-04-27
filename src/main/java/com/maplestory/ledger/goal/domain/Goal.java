package com.maplestory.ledger.goal.domain;

import com.maplestory.ledger.auth.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount;

    @Column(name = "is_achieved")
    private Boolean isAchieved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    public static Goal create(User user, String itemName, Long targetAmount) {
        Goal goal = new Goal();
        goal.user = user;
        goal.itemName = itemName;
        goal.targetAmount = targetAmount;
        goal.isAchieved = false;
        return goal;
    }

    public void update(String itemName, Long targetAmount) {
        this.itemName = itemName;
        this.targetAmount = targetAmount;
    }

    public void achieve() {
        this.isAchieved = true;
        this.achievedAt = LocalDateTime.now();
    }

    /** 이 지출이 발생했을 때 목표 달성이 몇 주 지연되는지 계산합니다. */
    public long calculateDelayWeeks(long currentNetSavings, long avgWeeklyNet, long newExpense) {
        if (avgWeeklyNet <= 0) return 0;
        long remaining = targetAmount - currentNetSavings;
        if (remaining <= 0) return 0;
        long weeksWithout = (long) Math.ceil((double) remaining / avgWeeklyNet);
        long weeksWithExpense = (long) Math.ceil((double) (remaining + newExpense) / avgWeeklyNet);
        return weeksWithExpense - weeksWithout;
    }
}