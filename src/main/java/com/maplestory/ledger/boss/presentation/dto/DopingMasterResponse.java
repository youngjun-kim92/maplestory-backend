package com.maplestory.ledger.boss.presentation.dto;

import com.maplestory.ledger.boss.domain.DopingMaster;

public record DopingMasterResponse(
        Long id,
        String name,
        String effect,
        Long price
) {
    public static DopingMasterResponse from(DopingMaster d) {
        return new DopingMasterResponse(d.getId(), d.getName(), d.getEffect(), d.getPrice());
    }
}
