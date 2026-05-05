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

    /** 중복 DOPING 즐겨찾기 확인 — (userId, characterId, bossName, amount, description) 기준 */
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.type = 'DOPING' " +
           "AND f.amount = :amount AND f.description = :description AND f.bossName = :bossName " +
           "AND ((:characterId IS NULL AND f.character IS NULL) OR (f.character IS NOT NULL AND f.character.id = :characterId))")
    Optional<Favorite> findExistingDoping(@Param("userId") Long userId,
                                          @Param("characterId") Long characterId,
                                          @Param("bossName") String bossName,
                                          @Param("amount") Long amount,
                                          @Param("description") String description);

    /** 중복 BOSS 즐겨찾기 확인 — (userId, characterId, bossName, difficulty) 기준 */
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.type = 'BOSS' " +
           "AND f.bossName = :bossName AND f.difficulty = :difficulty " +
           "AND ((:characterId IS NULL AND f.character IS NULL) OR (f.character IS NOT NULL AND f.character.id = :characterId))")
    Optional<Favorite> findExistingBoss(@Param("userId") Long userId,
                                        @Param("characterId") Long characterId,
                                        @Param("bossName") String bossName,
                                        @Param("difficulty") String difficulty);

    Optional<Favorite> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
