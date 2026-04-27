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

    /** 캐릭터 인벤토리에 현재 보유 중인 메소 */
    @Column(name = "inventory_meso")
    private Long inventoryMeso = 0L;

    /** 창고에 보관 중인 메소 */
    @Column(name = "storage_meso")
    private Long storageMeso = 0L;

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
        user.inventoryMeso = 0L;
        user.storageMeso = 0L;
        return user;
    }

    public void updateSolErdaPrice(Long price) {
        this.solErdaFragmentPrice = price;
    }

    public void updateMesoBalance(Long inventoryMeso, Long storageMeso) {
        this.inventoryMeso = inventoryMeso;
        this.storageMeso = storageMeso;
    }

    public long getTotalMeso() {
        return (inventoryMeso != null ? inventoryMeso : 0L)
             + (storageMeso != null ? storageMeso : 0L);
    }
}