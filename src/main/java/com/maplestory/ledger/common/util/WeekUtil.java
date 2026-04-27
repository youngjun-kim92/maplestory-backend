package com.maplestory.ledger.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * 메이플스토리 주간 초기화 기준 날짜 계산 유틸리티.
 *
 * 메이플스토리는 매주 목요일 00:00 KST에 주간 미션이 초기화됩니다.
 * 따라서 한 주(Week)는 목요일 ~ 다음 수요일로 정의됩니다.
 *
 * 이 클래스는 임의의 날짜가 속한 주의 시작일(목요일)을 계산합니다.
 * week_start 컬럼에 이 값을 저장하여 주간 데이터를 그룹핑합니다.
 *
 * 인스턴스화 불필요 — 모든 메서드가 static입니다.
 */
public final class WeekUtil {

    private WeekUtil() {}

    /**
     * 주어진 날짜가 속한 주의 시작일(목요일)을 반환합니다.
     *
     * 계산 알고리즘:
     *   Java DayOfWeek: MONDAY=1, TUESDAY=2, WEDNESDAY=3, THURSDAY=4, FRIDAY=5, SATURDAY=6, SUNDAY=7
     *   목요일(4)로부터 지난 일수 = (오늘의 DayOfWeek 값 - 4 + 7) % 7
     *
     * 검증 예시:
     *   - 목요일(4): (4-4+7)%7 = 0  → 당일이 목요일
     *   - 금요일(5): (5-4+7)%7 = 1  → 전날이 목요일
     *   - 수요일(3): (3-4+7)%7 = 6  → 6일 전이 목요일
     *   - 일요일(7): (7-4+7)%7 = 3  → 3일 전이 목요일
     *
     * @param date 기준 날짜
     * @return 해당 주의 목요일 날짜
     */
    public static LocalDate getWeekStart(LocalDate date) {
        int daysFromThursday = (date.getDayOfWeek().getValue() - DayOfWeek.THURSDAY.getValue() + 7) % 7;
        return date.minusDays(daysFromThursday);
    }

    /**
     * 오늘 날짜 기준으로 현재 주의 시작일(목요일)을 반환합니다.
     *
     * @return 현재 주의 목요일 날짜
     */
    public static LocalDate getWeekStart() {
        return getWeekStart(LocalDate.now());
    }
}
