package com.maplestory.ledger.favorite.domain;

import com.maplestory.ledger.auth.domain.User;
import com.maplestory.ledger.character.domain.MapleCharacter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Getter
@NoArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private MapleCharacter character;

    @Column(nullable = false, length = 100)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FavoriteType type;

    // ── BOSS: 보스 처치 템플릿 ─────────────────────────────────────────────
    @Column(name = "boss_name", length = 100)
    private String bossName;

    @Column(length = 50)
    private String difficulty;

    @Column(name = "party_size")
    private Integer partySize;

    // ── DOPING: 도핑 비용 템플릿 ──────────────────────────────────────────
    // bossName(위 공유) = null이면 공통 도핑, 값이 있으면 특정 보스 전용 도핑
    private Long amount;

    @Column(length = 300)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum FavoriteType {
        BOSS,   // 보스 처치 템플릿
        DOPING  // 도핑 비용 템플릿 (보스별 or 공통)
    }

    public static Favorite createBoss(User user, MapleCharacter character, String label,
                                      String bossName, String difficulty, Integer partySize) {
        Favorite f = new Favorite();
        f.user = user;
        f.character = character;
        f.label = label;
        f.type = FavoriteType.BOSS;
        f.bossName = bossName;
        f.difficulty = difficulty;
        f.partySize = partySize;
        return f;
    }

    public static Favorite createDoping(User user, MapleCharacter character, String label,
                                        String bossName, Long amount, String description) {
        Favorite f = new Favorite();
        f.user = user;
        f.character = character;
        f.label = label;
        f.type = FavoriteType.DOPING;
        f.bossName = bossName;
        f.amount = amount;
        f.description = description;
        return f;
    }
}
