package com.maplestory.ledger.favorite.application;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.auth.infrastructure.UserRepository;
import com.maplestory.ledger.character.domain.MapleCharacter;
import com.maplestory.ledger.character.infrastructure.CharacterRepository;
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
    private final CharacterRepository characterRepository;

    @Transactional
    public FavoriteResponse createFavorite(Long userId, FavoriteRequest req) {
        User user = userRepository.getReferenceById(userId);
        MapleCharacter character = resolveCharacter(userId, req.characterId());

        if (req.type() == Favorite.FavoriteType.BOSS && character != null) {
            int count = favoriteRepository.countByUserIdAndTypeAndCharacterId(
                    userId, Favorite.FavoriteType.BOSS, character.getId());
            if (count >= 12) {
                throw new IllegalStateException(
                        character.getName() + "의 즐겨찾기는 최대 12개까지 저장할 수 있습니다.");
            }
        }

        Favorite favorite = switch (req.type()) {
            case BOSS -> Favorite.createBoss(user, character, req.label(),
                    req.bossName(), req.difficulty(), req.partySize());
            case DOPING -> Favorite.createDoping(user, character, req.label(),
                    req.bossName(), req.amount(), req.description());
        };
        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(Long userId, Favorite.FavoriteType type,
                                                String bossName, Long characterId) {
        List<Favorite> favorites;
        if (type == Favorite.FavoriteType.DOPING && characterId != null && bossName != null) {
            favorites = favoriteRepository.findDopingByUserIdAndCharacterIdAndBossName(
                    userId, characterId, bossName);
        } else if (type == Favorite.FavoriteType.DOPING && bossName != null) {
            favorites = favoriteRepository.findDopingByUserIdAndBossName(userId, bossName);
        } else if (type != null && characterId != null) {
            favorites = favoriteRepository.findByUserIdAndTypeAndCharacterIdOrderByCreatedAtDesc(
                    userId, type, characterId);
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

    private MapleCharacter resolveCharacter(Long userId, Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findByIdAndUserId(characterId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("캐릭터를 찾을 수 없습니다."));
    }
}
