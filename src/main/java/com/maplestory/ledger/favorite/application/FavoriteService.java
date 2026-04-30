package com.maplestory.ledger.favorite.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.common.exception.ResourceNotFoundException;
import com.maplestory.ledger.favorite.domain.Favorite;
import com.maplestory.ledger.favorite.infrastructure.FavoriteRepository;
import com.maplestory.ledger.favorite.presentation.dto.FavoriteRequest;
import com.maplestory.ledger.favorite.presentation.dto.FavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional
    public FavoriteResponse createFavorite(Long userId, FavoriteRequest req) {
        User user = userRepository.getReferenceById(userId);
        Favorite favorite = switch (req.type()) {
            case BOSS -> Favorite.createBoss(user, req.label(),
                    req.bossName(), req.difficulty(), req.partySize());
            case DOPING -> Favorite.createDoping(user, req.label(),
                    req.bossName(), req.amount(), req.description());
        };
        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(Long userId, Favorite.FavoriteType type, String bossName) {
        List<Favorite> favorites;
        if (type == Favorite.FavoriteType.DOPING && bossName != null) {
            favorites = favoriteRepository.findDopingByUserIdAndBossName(userId, bossName);
        } else if (type != null) {
            favorites = favoriteRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        } else {
            favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return favorites.stream().map(FavoriteResponse::from).toList();
    }

    @Transactional
    public void deleteFavorite(Long userId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findByIdAndUserId(favoriteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("즐겨찾기를 찾을 수 없습니다."));
        favoriteRepository.delete(favorite);
    }
}
