package com.maplestory.ledger.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "sol_erda_fragment_price")
    private Long solErdaFragmentPrice = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static User create(String nickname, String passwordHash) {
        User user = new User();
        user.nickname = nickname;
        user.passwordHash = passwordHash;
        user.solErdaFragmentPrice = 0L;
        return user;
    }

    public void updateSolErdaPrice(Long price) {
        this.solErdaFragmentPrice = price;
    }
}