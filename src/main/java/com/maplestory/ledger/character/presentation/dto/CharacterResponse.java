package com.maplestory.ledger.character.presentation.dto;

import com.maplestory.ledger.character.domain.MapleCharacter;

import java.time.LocalDateTime;

public record CharacterResponse(
        Long id,
        String name,
        String jobClass,
        Integer level,
        Boolean isMain,
        Long initialInvestment,
        Integer solErdaFragments,
        String mvpGrade,
        LocalDateTime createdAt
) {
    public static CharacterResponse from(MapleCharacter c) {
        return new CharacterResponse(
                c.getId(), c.getName(), c.getJobClass(),
                c.getLevel(), c.getIsMain(), c.getInitialInvestment(),
                c.getSolErdaFragments() != null ? c.getSolErdaFragments() : 0,
                c.getMvpGrade() != null ? c.getMvpGrade().name() : "NORMAL",
                c.getCreatedAt()
        );
    }
}