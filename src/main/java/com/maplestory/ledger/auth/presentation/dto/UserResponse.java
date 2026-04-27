package com.maplestory.ledger.auth.presentation.dto;

import com.maplestory.ledger.auth.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String nickname,
        Long solErdaFragmentPrice,
        Long inventoryMeso,
        Long storageMeso,
        Long totalMeso,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getSolErdaFragmentPrice(),
                user.getInventoryMeso(),
                user.getStorageMeso(),
                user.getTotalMeso(),
                user.getCreatedAt()
        );
    }
}