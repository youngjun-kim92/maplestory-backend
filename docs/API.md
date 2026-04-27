# API 명세서

Base URL: `http://localhost:8080`

인증이 필요한 API는 헤더에 `Authorization: Bearer {token}` 포함 필요.

---

## 인증 (Auth)

### POST /api/auth/register — 회원가입
```json
// Request Body
{
  "nickname": "메이플유저",     // 2~20자
  "password": "pass1234"       // 최소 6자
}

// Response 201
{
  "token": "eyJhbGci...",
  "user": {
    "id": 1,
    "nickname": "메이플유저",
    "solErdaFragmentPrice": 0,
    "createdAt": "2026-04-27T10:00:00"
  }
}
```

### POST /api/auth/login — 로그인
```json
// Request Body
{ "nickname": "메이플유저", "password": "pass1234" }

// Response 200 — register와 동일 구조
```

### GET /api/auth/profile — 내 프로필 조회 🔒
```json
// Response 200
{
  "id": 1,
  "nickname": "메이플유저",
  "solErdaFragmentPrice": 150000,
  "createdAt": "2026-04-27T10:00:00"
}
```

### PUT /api/auth/sol-erda-price?price=150000 — 솔 에르다 조각 단가 설정 🔒
```
Response 204 No Content
```

---

## 가계부 (Ledger)

### GET /api/ledger — 주간 가계부 조회 🔒
```
Query Parameter: week=2026-04-24  (목요일 날짜, 생략 시 현재 주)

// Response 200
{
  "weekStart": "2026-04-24",
  "entries": [
    {
      "id": 5,
      "type": "income",
      "category": "boss",
      "amount": 39600000,
      "description": "루시드 hard 결정석",
      "entryDate": "2026-04-27",
      "weekStart": "2026-04-24"
    }
  ],
  "summary": {
    "totalIncome": 39600000,
    "totalExpense": 0,
    "netProfit": 39600000
  }
}
```

### POST /api/ledger — 가계부 항목 추가 🔒
```json
// Request Body
{
  "type": "expense",               // income | expense
  "category": "cube",              // boss | hunting | sol_erda | cube | starforce | spell_trace | other
  "amount": 100000000,
  "description": "레드 큐브 10개",
  "entryDate": "2026-04-27",
  "characterId": 1                 // nullable
}

// Response 201
{
  "entry": { /* 저장된 가계부 항목 */ },
  "goalWarnings": [                // 지출로 인해 목표 달성이 지연되면 경고 포함 (기능 #10)
    {
      "goalId": 1,
      "itemName": "아케인셰이드 완드",
      "delayWeeks": 2,
      "message": "이번 지출로 인해 '아케인셰이드 완드' 목표 달성이 약 2주 지연되었습니다."
    }
  ]
}
```

### DELETE /api/ledger/{id} — 항목 삭제 🔒
```
Response 204 No Content
```

### GET /api/ledger/weeks — 기록된 주차 목록 🔒
```json
// Response 200 — 주차별 요약 목록
[
  {
    "weekStart": "2026-04-24",
    "totalIncome": 150000000,
    "totalExpense": 50000000,
    "entryCount": 12
  }
]
```

### GET /api/ledger/stats?weeks=4 — 카테고리별 통계 🔒
```
Query Parameter: weeks=4 (최근 N주, 기본값 4)

// Response 200 — [카테고리, 타입, 합계, 건수, 평균] 배열
```

---

## 보스 (Boss)

### GET /api/boss/list — 보스 목록 (결정석 가격 포함)
```json
// Response 200 — 인증 불필요
[
  { "id": 1, "bossName": "자쿰", "difficulty": "easy", "crystalPrice": 1440000, "maxAttemptsPerWeek": 1 },
  { "id": 29, "bossName": "루시드", "difficulty": "hard", "crystalPrice": 39600000, "maxAttemptsPerWeek": 1 }
]
```

### POST /api/boss/kill — 보스 처치 기록 (결정석 가격 자동 계산) 🔒
```json
// Request Body
{
  "bossName": "루시드",
  "difficulty": "hard",
  "killDate": "2026-04-27",
  "characterId": 1           // nullable
}

// Response 201
{
  "id": 10,
  "bossName": "루시드",
  "difficulty": "hard",
  "crystalPrice": 39600000,  // boss_master에서 자동 조회
  "killDate": "2026-04-27",
  "weekStart": "2026-04-24"
}
```

### GET /api/boss/weekly?week=2026-04-24 — 주간 보스 처치 목록 🔒
```json
// Response 200 — 해당 주의 보스 처치 목록
```

### GET /api/boss/stats — 보스별 수익 통계 🔒
```json
// Response 200
[
  {
    "bossName": "루시드",
    "difficulty": "hard",
    "killCount": 4,
    "totalCrystalIncome": 158400000,
    "avgCrystalPrice": 39600000.0
  }
]
```

---

## 사냥 (Hunting)

### POST /api/hunting/session — 사냥 세션 기록 🔒
```json
// Request Body
{
  "mapName": "아르카나 강변",
  "durationMinutes": 60,
  "income": 80000000,           // 사냥으로 얻은 메소
  "solErdaFragments": 30,       // 솔 에르다 조각 개수 (nullable)
  "sessionDate": "2026-04-27",
  "characterId": 1               // nullable
}

// 처리 흐름:
// solErdaMesoValue = 30 × (사용자 설정 단가 150,000) = 4,500,000
// 가계부 등록 총액 = 80,000,000 + 4,500,000 = 84,500,000

// Response 201 — 저장된 HuntingSession 반환
```

### GET /api/hunting/sessions?week=2026-04-24 — 주간 사냥 세션 목록 🔒

### GET /api/hunting/stats — 사냥터별 수익 효율 통계 🔒
```json
// Response 200 — 시간당 수익 내림차순 정렬
[
  {
    "mapName": "아르카나 강변",
    "sessionCount": 5,
    "totalIncome": 425000000,
    "totalMinutes": 300,
    "avgIncomePerHour": 85000000   // 시간당 평균 수익
  }
]
```

---

## 캐릭터 (Characters)

### POST /api/characters — 캐릭터 등록 🔒
```json
// Request Body
{
  "name": "야릇한비틀기",
  "jobClass": "아크메이지(불,독)",
  "level": 260,
  "isMain": true,
  "initialInvestment": 0          // 부캐릭터의 초기 투자 비용 (메소)
}
```

### GET /api/characters — 내 캐릭터 목록 🔒
```
메인 캐릭터가 상단에 표시됨 (isMain=true 우선 정렬)
```

### PUT /api/characters/{id} — 캐릭터 정보 수정 🔒
### DELETE /api/characters/{id} — 캐릭터 삭제 🔒

### GET /api/characters/{id}/roi — 부캐릭터 손익분기점 조회 🔒
```json
// Response 200
{
  "characterId": 2,
  "characterName": "부캐릭터명",
  "initialInvestment": 5000000000,       // 초기 투자 비용 (5억)
  "cumulativeBossIncome": 318000000,     // 지금까지 누적 보스 수익
  "weeklyAvgBossIncome": 106000000,      // 주당 평균 보스 수익
  "weeksToBreakEven": 48,                // 투자 회수까지 남은 주차
  "isBreakEvenReached": false,           // 손익분기점 도달 여부
  "remainingToBreakEven": 4682000000     // 회수까지 남은 금액
}
```

---

## 목표 (Goals)

### POST /api/goals — 목표 아이템 등록 🔒
```json
// Request Body
{ "itemName": "아케인셰이드 완드", "targetAmount": 30000000000 }
```

### GET /api/goals — 목표 목록 🔒
### PUT /api/goals/{id} — 목표 수정 🔒
### DELETE /api/goals/{id} — 목표 삭제 🔒

### PATCH /api/goals/{id}/achieve — 목표 달성 처리 🔒

### GET /api/goals/{id}/estimate — 목표 달성 예측 🔒
```json
// Response 200
{
  "goalId": 1,
  "itemName": "아케인셰이드 완드",
  "targetAmount": 30000000000,
  "currentSavings": 5000000000,        // 현재 순자산 (총수입 - 총지출)
  "remaining": 25000000000,            // 잔여 금액
  "progressPercent": 16,               // 달성률 (%)
  "avgWeeklyNet": 500000000,           // 평균 주당 순수익 (최근 4주)
  "weeksRemaining": 50,                // 남은 예상 주차
  "estimatedDate": "2027-04-22"        // 예상 달성일
}
```

---

## 통계 (Stats)

### GET /api/stats/comparison — 익명 유저 평균 수익 비교 🔒
```json
// Response 200
{
  "userAvgWeeklyIncome": 800000000,     // 내 주당 평균 수익
  "globalAvgWeeklyIncome": 400000000,   // 전체 유저 평균
  "totalUsers": 150,                    // 비교 대상 유저 수
  "percentile": 85,                     // 내 수익 백분위
  "message": "내 수익은 전체 유저 상위 15%입니다."
}
```

### POST /api/stats/exp-calculator — 경험치 계산기
```json
// Request Body (인증 불필요)
{
  "currentLevel": 260,
  "currentExpPercent": 45.5,     // 현재 경험치 퍼센트
  "avgExpPerHour": 12.0,         // 시간당 평균 경험치 획득량 (%)
  "targetLevel": 265             // nullable, 기본값: currentLevel + 1
}

// Response 200
{
  "currentLevel": 260,
  "targetLevel": 265,
  "hoursToTarget": 44.5,         // 목표 레벨까지 예상 시간
  "daysToTarget": 1.9            // 목표 레벨까지 예상 일수
}
```

---

## 에러 응답 형식

```json
// 모든 에러는 아래 형식으로 반환됨
{
  "message": "에러 메시지"
}
```

| HTTP 코드 | 상황 |
|---|---|
| 400 | 입력값 검증 실패 (Validation Error) |
| 401 | 인증 실패 (잘못된 닉네임/비밀번호, 토큰 없음/만료) |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 중복 (닉네임 중복 등) |
| 500 | 서버 내부 오류 |
