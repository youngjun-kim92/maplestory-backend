package com.maplestory.ledger.favorite.infrastructure;

import com.maplestory.ledger.favorite.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Favorite> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Favorite.FavoriteType type);
    List<Favorite> findByUserIdAndTypeAndCharacterIdOrderByCreatedAtDesc(
            Long userId, Favorite.FavoriteType type, Long characterId);

    int countByUserIdAndTypeAndCharacterId(Long userId, Favorite.FavoriteType type, Long characterId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.type = 'DOPING' " +
           "AND f.character.id = :characterId " +
           "AND (f.bossName = :bossName OR f.bossName IS NULL) ORDER BY f.createdAt DESC")
    List<Favorite> findDopingByUserIdAndCharacterIdAndBossName(
            @Param("userId") Long userId,
            @Param("characterId") Long characterId,
            @Param("bossName") String bossName);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.type = 'DOPING' " +
           "AND (f.bossName = :bossName OR f.bossName IS NULL) ORDER BY f.createdAt DESC")
    List<Favorite> findDopingByUserIdAndBossName(@Param("userId") Long userId,
                                                  @Param("bossName") String bossName);

    Optional<Favorite> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
