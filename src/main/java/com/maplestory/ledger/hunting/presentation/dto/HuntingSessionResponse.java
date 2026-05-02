package com.maplestory.ledger.hunting.presentation.dto;

import com.maplestory.ledger.hunting.domain.HuntingSession;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HuntingSessionResponse(
        Long id,
        Long income,
        Integer solErdaFragments,
        Long solErdaMesoValue,
        Long totalIncome,
        LocalDate sessionDate,
        LocalDate weekStart,
        Long characterId,
        String characterName,
        LocalDateTime createdAt
) {
    public static HuntingSessionResponse from(HuntingSession s) {
        return new HuntingSessionResponse(
                s.getId(),
                s.getIncome(),
                s.getSolErdaFragments(),
                s.getSolErdaMesoValue(),
                s.getIncome() + s.getSolErdaMesoValue(),
                s.getSessionDate(),
                s.getWeekStart(),
                s.getCharacter() != null ? s.getCharacter().getId() : null,
                s.getCharacter() != null ? s.getCharacter().getName() : null,
                s.getCreatedAt()
        );
    }
}