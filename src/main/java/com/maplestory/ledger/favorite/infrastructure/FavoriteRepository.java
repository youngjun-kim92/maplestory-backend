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

    /**
     * 특정 보스의 도핑 즐겨찾기 조회 — 해당 보스 전용 + 공통(bossName=null) 모두 반환
     */
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.type = 'DOPING' " +
           "AND (f.bossName = :bossName OR f.bossName IS NULL) ORDER BY f.createdAt DESC")
    List<Favorite> findDopingByUserIdAndBossName(@Param("userId") Long userId,
                                                  @Param("bossName") String bossName);

    Optional<Favorite> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
