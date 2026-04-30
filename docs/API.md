# API 명세서

Base URL: `http://localhost:8080`

인증이 필요한 API는 헤더에 `Authorization: Bearer {token}` 포함 필요.

---

## 인증 (Auth)

### POST /api/auth/register — 회원가입
```json
// Request Body
{ "nickname": "메이플유저", "password": "pass1234" }

// Response 201
{ "token": "eyJhbGci...", "user": { "id": 1, "nickname": "메이플유저", "solErdaFragmentPrice": 0, "createdAt": "..." } }
```

### POST /api/auth/login — 로그인
```json
{ "nickname": "메이플유저", "password": "pass1234" }
// Response 200 — register와 동일 구조
```

### GET /api/auth/profile — 내 프로필 조회 🔒

### PUT /api/auth/meso — 보유 메소 수동 업데이트 🔒
```json
{ "inventoryMeso": 1000000000, "storageMeso": 500000000 }
```

### PUT /api/auth/sol-erda-price?price=150000 — 솔 에르다 조각 단가 설정 🔒

### DELETE /api/auth/reset — 전체 기록 초기화 🔒
```
Response 204 No Content
모든 보스/사냥/가계부/목표/캐릭터/즐겨찾기 기록 삭제, 메소 잔액 0으로 리셋
```

---

## 가계부 (Ledger)

### GET /api/ledger?week=2026-04-24 — 주간 가계부 조회 🔒
```json
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
      "weekStart": "2026-04-24",
      "solErdaFragments": 0
    }
  ],
  "summary": { "totalIncome": 39600000, "totalExpense": 0, "netProfit": 39600000 }
}
```

### POST /api/ledger — 가계부 항목 추가 🔒
```json
// Request Body
{
  "type": "expense",
  "category": "cube",        // boss | hunting | sol_erda | cube | starforce | spell_trace | trade | auction | doping | other
  "amount": 100000000,
  "description": "레드 큐브 10개",
  "entryDate": "2026-04-27",
  "characterId": 1,          // nullable
  "solErdaFragments": 0      // 사냥 카테고리에서만 의미 있음
}

// Response 201
{ "entry": { /* 저장된 가계부 항목 */ }, "goalWarnings": [ ... ] }
```

### PUT /api/ledger/{id} — 가계부 항목 수정 🔒
```json
// Request Body — POST와 동일 구조
// Response 200
```

### DELETE /api/ledger/{id} — 항목 삭제 🔒
```
Response 204 No Content
```

### GET /api/ledger/weeks — 기록된 주차 목록 🔒
```json
// Response 200
[
  {
    "weekStart": "2026-04-24",
    "totalIncome": 150000000,
    "totalExpense": 50000000,
    "entryCount": 12,
    "totalSolErdaFragments": 120
  }
]
```

### GET /api/ledger/stats?weeks=4 — 카테고리별 통계 🔒
### GET /api/ledger/income-trend?weeks=8 — 수입원별 주간 추이 🔒

---

## 보스 (Boss)

### GET /api/boss/list — 보스 목록 (결정석 가격 포함, 인증 불필요)

### POST /api/boss/kill — 보스 처치 기록 (결정석 자동 계산) 🔒
```json
// Request Body
{
  "bossName": "루시드",
  "difficulty": "hard",
  "killDate": "2026-04-27",
  "characterId": 1,
  "partySize": 2,
  "expenses": [
    {
      "category": "other",
      "amount": 2000000,
      "description": "세이람의 영약"
    }
  ]
}
// expenses: 도핑비 등 즉시 지출 항목 목록 (null/빈 배열 허용)
// → 보스 결정석 수익 + 지출 차액이 인벤토리에 즉시 반영됨

// Response 201 — BossKillResponse
```

### GET /api/boss/weekly?week=2026-04-24 — 주간 보스 처치 목록 🔒
### GET /api/boss/stats — 보스별 수익 통계 🔒

### POST /api/boss/kills/{killId}/drops — 드랍 아이템 기록 🔒
### PATCH /api/boss/drops/{id}/list — 경매장 등록 🔒
### PATCH /api/boss/drops/{id}/sell — 경매장 판매 완료 🔒
```json
// Request Body
{
  "saleAmount": 50000000,
  "saleDate": "2026-04-27",
  "isPcCafe": false           // PC방 접속 여부 (3% 수수료 적용)
}

// 수수료 정책:
//   isPcCafe=true  → 3% (캐릭터 MVP 등급 무관)
//   MVP 실버 이상  → 3%
//   일반/브론즈    → 5%
// 실제 수익 = saleAmount × (1 - 수수료율), 설명에 수수료 % 표기됨
```

### GET /api/boss/drops/weekly?week= — 주간 드랍 기록 🔒
### GET /api/boss/kills/{killId}/drops — 특정 처치의 드랍 목록 🔒

---

## 사냥 (Hunting)

### POST /api/hunting/session — 사냥 세션 기록 🔒
```json
// Request Body
{
  "mapName": "아르카나 강변",
  "durationMinutes": 60,
  "income": 80000000,
  "solErdaFragments": 30,
  "sessionDate": "2026-04-27",
  "characterId": 1
}
```

### GET /api/hunting/sessions?week= — 주간 사냥 세션 목록 🔒
### GET /api/hunting/stats — 사냥터별 수익 통계 🔒

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
  "initialInvestment": 0,
  "solErdaFragments": 0,
  "mvpGrade": "NORMAL"        // NORMAL | BRONZE | SILVER | GOLD | DIAMOND | RED | BLACK
}
// 메인 캐릭터는 1개만 존재 — 새 메인 지정 시 기존 메인 자동 해제
```

### POST /api/characters/bulk — 캐릭터 일괄 등록 🔒
```json
// Request Body — CharacterRequest 배열
[
  { "name": "캐릭터1", "jobClass": "아크", "level": 260, "isMain": true },
  { "name": "캐릭터2", "jobClass": "패스파인더", "level": 240, "isMain": false }
]
// Response 201 — CharacterResponse 배열
```

### GET /api/characters — 내 캐릭터 목록 🔒
```json
// Response 200
[
  {
    "id": 1,
    "name": "야릇한비틀기",
    "jobClass": "아크메이지(불,독)",
    "level": 260,
    "isMain": true,
    "initialInvestment": 0,
    "solErdaFragments": 120,
    "mvpGrade": "SILVER",
    "createdAt": "..."
  }
]
```

### PUT /api/characters/{id} — 캐릭터 정보 수정 🔒
### DELETE /api/characters/{id} — 캐릭터 삭제 🔒
### GET /api/characters/{id}/roi — 부캐릭터 손익분기점 조회 🔒
### GET /api/characters/stats — 캐릭터별 통계 🔒

---

## 즐겨찾기 (Favorites)

보스 처치·도핑 비용 기록 시 매번 입력하지 않도록 템플릿을 저장합니다.
프론트엔드는 즐겨찾기 데이터를 폼에 pre-fill하여 사용합니다.

**즐겨찾기 유형:**
- `BOSS` — 보스 처치 템플릿 (보스명 + 난이도 + 인원)
- `DOPING` — 도핑 비용 템플릿 (영약 등). `bossName`이 있으면 특정 보스 전용, null이면 공통

### POST /api/favorites — 즐겨찾기 등록 🔒
```json
// BOSS 템플릿 예시
{
  "label": "루시드 하드 (2인 파티)",
  "type": "BOSS",
  "bossName": "루시드",
  "difficulty": "hard",
  "partySize": 2
}

// DOPING 템플릿 예시 — 특정 보스 전용
{
  "label": "세이람의 영약",
  "type": "DOPING",
  "bossName": "루시드",      // null이면 모든 보스에 공통으로 표시
  "amount": 2000000,
  "description": "세이람의 영약 (15분)"
}
```

### GET /api/favorites — 즐겨찾기 목록 🔒
```
Query Parameters:
  type=BOSS|DOPING        (생략 시 전체)
  bossName=루시드          (type=DOPING일 때만 유효)
                           → 해당 보스 전용 도핑 + 공통(bossName=null) 도핑 함께 반환

예시: GET /api/favorites?type=DOPING&bossName=루시드
```

### DELETE /api/favorites/{id} — 즐겨찾기 삭제 🔒

---

## 목표 (Goals)

### POST /api/goals — 목표 등록 🔒
### GET /api/goals — 목표 목록 🔒
### PUT /api/goals/{id} — 목표 수정 🔒
### DELETE /api/goals/{id} — 목표 삭제 🔒
### PATCH /api/goals/{id}/achieve — 목표 달성 처리 🔒
### GET /api/goals/{id}/estimate — 목표 달성 예측 🔒

---

## 통계 (Stats)

### GET /api/stats/comparison — 익명 유저 평균 수익 비교 🔒
### POST /api/stats/exp-calculator — 경험치 계산기 (인증 불필요)

---

## 에러 응답 형식

```json
{ "message": "에러 메시지" }
```

| HTTP 코드 | 상황 |
|---|---|
| 400 | 입력값 검증 실패 |
| 401 | 인증 실패 (토큰 없음/만료) |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 중복 (닉네임 중복 등) |
| 500 | 서버 내부 오류 |
