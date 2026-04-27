package com.maplestory.ledger.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자(User) 엔티티 — DB의 users 테이블과 매핑됩니다.
 *
 * 닉네임과 암호화된 비밀번호만으로 가입이 가능한 간편 회원 구조입니다.
 * auth 도메인의 Aggregate Root 역할을 합니다.
 */
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인에 사용하는 닉네임 (UNIQUE) */
    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    /** BCrypt로 해싱된 비밀번호 */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 솔 에르다 조각 낱개 가격 (메소 단위).
     * 기능 #5: 사냥 세션 기록 시 조각 수 × 이 단가 = 자동 메소 환산에 사용됩니다.
     */
    @Column(name = "sol_erda_fragment_price")
    private Long solErdaFragmentPrice = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
