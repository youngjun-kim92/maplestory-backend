package com.maplestory.ledger.goal.application;

/**
 * 과소비 경고 응답 객체 (기능 #10).
 *
 * 지출 추가 시 활성 목표의 달성이 지연되는 경우 해당 정보를 담아 반환합니다.
 * LedgerService가 goal 도메인의 GoalService를 호출한 결과를 이 타입으로 받습니다.
 */
public record GoalWarning(Long goalId, String itemName, Long delayWeeks, String message) {}
