package com.maplestory.ledger.goal.presentation.dto;

import com.maplestory.ledger.goal.domain.Goal;

import java.time.LocalDateTime;

public record GoalResponse(
        Long id,
        String itemName,
        Long targetAmount,
        Boolean achieved,
        LocalDateTime createdAt,
        LocalDateTime achievedAt
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(), goal.getItemName(), goal.getTargetAmount(),
                goal.getIsAchieved(), goal.getCreatedAt(), goal.getAchievedAt()
        );
    }
}