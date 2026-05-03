package com.maplestory.ledger.favorite.presentation.dto;

import com.maplestory.ledger.favorite.domain.Favorite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(
        @NotBlank(message = "즐겨찾기 이름을 입력해주세요.") String label,
        @NotNull(message = "즐겨찾기 유형을 선택해주세요.") Favorite.FavoriteType type,
        Long characterId,

        // BOSS 템플릿
        String bossName,
        String difficulty,
        Integer partySize,

        // DOPING 템플릿 (bossName 공유 — null이면 공통 도핑)
        Long amount,
        String description
) {}
