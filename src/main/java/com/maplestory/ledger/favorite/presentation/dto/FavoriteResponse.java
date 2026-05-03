package com.maplestory.ledger.favorite.presentation.dto;

import com.maplestory.ledger.favorite.domain.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long id,
        String label,
        String type,
        Long characterId,
        String characterName,
        String bossName,
        String difficulty,
        Integer partySize,
        Long amount,
        String description,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(Favorite f) {
        return new FavoriteResponse(
                f.getId(),
                f.getLabel(),
                f.getType().name(),
                f.getCharacter() != null ? f.getCharacter().getId() : null,
                f.getCharacter() != null ? f.getCharacter().getName() : null,
                f.getBossName(),
                f.getDifficulty(),
                f.getPartySize(),
                f.getAmount(),
                f.getDescription(),
                f.getCreatedAt()
        );
    }
}
