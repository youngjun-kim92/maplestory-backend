package com.maplestory.ledger.boss.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doping_master")
@Getter
@NoArgsConstructor
public class DopingMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String effect;

    @Column(nullable = false)
    private Long price;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
