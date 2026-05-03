package com.maplestory.ledger.favorite.presentation;

import com.maplestory.ledger.common.security.CustomUserDetails;
import com.maplestory.ledger.favorite.application.FavoriteService;
import com.maplestory.ledger.favorite.domain.Favorite;
import com.maplestory.ledger.favorite.presentation.dto.FavoriteRequest;
import com.maplestory.ledger.favorite.presentation.dto.FavoriteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<FavoriteResponse> createFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FavoriteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.createFavorite(userDetails.getUserId(), req));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Favorite.FavoriteType type,
            @RequestParam(required = false) String bossName,
            @RequestParam(required = false) Long characterId) {
        return ResponseEntity.ok(favoriteService.getFavorites(
                userDetails.getUserId(), type, bossName, characterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        favoriteService.deleteFavorite(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
