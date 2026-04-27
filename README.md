# MaplePlanner — 메이플스토리 종합 가계부 백엔드

> 메이플스토리 유저의 인게임 경제 활동을 스마트하게 관리하는 REST API 백엔드 서비스

단순한 수입/지출 기록을 넘어 **보스 결정석 자동 계산**, **부캐릭터 투자 회수 예측**, **유저 간 익명 수익 비교** 등 10가지 독창적인 기능을 제공하는 종합 유틸리티 서비스입니다.

---

## 목차

- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [데이터베이스 구조](#데이터베이스-구조)
- [시작하기](#시작하기)
- [API 사용 방법](#api-사용-방법)
- [인증 방식](#인증-방식)
- [에러 응답 형식](#에러-응답-형식)

---

## 핵심 기능

| # | 기능 | 설명 |
|:---:|---|---|
| 1 | **간편 회원가입** | 닉네임 + 비밀번호만으로 가입. MySQL에 영구 저장 |
| 2 | **목요일 기준 누적형 주간 가계부** | 메이플 주간 초기화(목요일) 기준으로 한 주를 묶어 수익/지출 기록 |
| 3 | **사냥터/보스 수익 효율 통계** | 사냥터별 시간당 수익, 보스별 누적 결정석 수익 통계 |
| 4 | **보스 결정석 자동 계산** | 보스 이름 + 난이도 선택만으로 결정석 가격 자동 합산 |
| 5 | **솔 에르다 조각 자동 환산** | 조각 개수 × 사용자 설정 단가 = 자동 메소 합산 |
| 6 | **목표 아이템 달성 예측** | 평균 주간 수익 기반 목표 달성까지 남은 주차 계산 |
| 7 | **레벨업 경험치 계산기** | 현재 경험치%와 시간당 평균 경험치%로 목표 레벨까지 시간 계산 |
| 8 | **부캐릭터 손익분기점 계산기** | 초기 투자비 ÷ 주당 보스 수익 = 투자 회수까지 남은 주차 |
| 9 | **익명 기반 유저 평균 수익 비교** | 전체 유저 수익 데이터 익명 집계 후 내 수익 백분위 제공 |
| 10 | **과소비 경고 및 목표 지연 알림** | 지출 추가 시 활성 목표 달성이 몇 주 지연되는지 자동 경고 |

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security 6 + JWT (JJWT 0.12.5) |
| ORM | Spring Data JPA (Hibernate 6) |
| Database | MySQL 8.x |
| Build | Gradle |
| Utility | Lombok |

---

## 아키텍처

### DDD 패키지 구조

**DDD(Domain-Driven Design)** 기반의 바운디드 컨텍스트별 패키지 구조를 사용합니다.
각 도메인 패키지는 `domain → infrastructure → application → presentation` 4계층으로 구성됩니다.

```
src/main/java/com/maplestory/ledger/
│
├── MapleStoryApplication.java
│
├── common/                         ← 공통 관심사 (JWT, Security, 예외처리, 유틸)
│   ├── exception/
│   ├── security/
│   └── util/WeekUtil.java          ← 목요일 기준 주차 계산
│
├── auth/                           ← 인증 바운디드 컨텍스트
│   ├── domain/User.java
│   ├── infrastructure/UserRepository.java
│   ├── application/AuthService.java
│   └── presentation/AuthController.java + dto/
│
├── ledger/                         ← 가계부 바운디드 컨텍스트 (핵심 SSOT)
│   ├── domain/LedgerEntry.java
│   ├── infrastructure/LedgerEntryRepository.java + projection/
│   ├── application/
│   │   ├── LedgerService.java
│   │   ├── LedgerQueryService.java  ← 타 도메인용 읽기 전용 서비스
│   │   └── command/AddLedgerEntryCommand.java
│   └── presentation/LedgerController.java + dto/
│
├── boss/                           ← 보스 바운디드 컨텍스트
│   ├── domain/BossKill.java, BossMaster.java
│   ├── infrastructure/BossKillRepository.java, BossMasterRepository.java + projection/
│   ├── application/
│   │   ├── BossService.java
│   │   └── command/RecordBossKillCommand.java
│   └── presentation/BossController.java + dto/
│
├── hunting/                        ← 사냥 바운디드 컨텍스트
├── character/                      ← 캐릭터 바운디드 컨텍스트
├── goal/                           ← 목표 바운디드 컨텍스트
└── stats/                          ← 통계 바운디드 컨텍스트
```

### DDD 설계 원칙

| 원칙 | 적용 내용 |
|---|---|
| **엔티티 불변성** | 도메인 엔티티에 `@Setter` 없음. 생성은 정적 팩토리(`Entity.create()`), 변경은 도메인 메서드(`achieve()`, `update()`) |
| **레이어 분리** | Controller → Command/Request DTO → Application Service → Domain → Infrastructure. 엔티티가 Controller 응답으로 직접 노출되지 않음 |
| **Response DTO** | 모든 Controller는 전용 Response DTO 반환. 엔티티에 `@JsonIgnore` 불필요 |
| **Command 객체** | Application Service는 Presentation DTO 대신 Command 객체를 파라미터로 받음 |
| **도메인 행위** | 비즈니스 로직은 Service에만 있지 않고 도메인 엔티티에도 포함 (`Goal.achieve()`, `Goal.calculateDelayWeeks()` 등) |
| **크로스 도메인 읽기** | `GoalService`는 `LedgerEntryRepository`에 직접 의존하지 않고 `LedgerQueryService`를 통해 집계 데이터를 조회 |

### 의도적 결합 (설계 결정)

`BossService`와 `HuntingService`는 `LedgerEntryRepository`에 직접 의존하여 단일 트랜잭션 내에서 `LedgerEntry`를 함께 생성합니다.
이는 도메인 이벤트 방식 대신 실용성을 택한 의도적 설계입니다.
Boss/Hunting 기록은 반드시 가계부 항목과 동시에 생성되는 불가분의 관계이기 때문입니다.

---

## 데이터베이스 구조

```
users
  ├── id (PK)
  ├── nickname (UNIQUE)
  ├── password_hash
  └── sol_erda_fragment_price     ← 솔 에르다 조각 단가 (기능 #5)

characters
  ├── id (PK)
  ├── user_id (FK → users)
  ├── name, job_class, level
  ├── is_main
  └── initial_investment          ← 부캐 초기 투자 비용 (기능 #8)

ledger_entries                    ← 모든 수입/지출의 단일 원천 (SSOT)
  ├── id (PK)
  ├── user_id (FK), character_id (FK, nullable)
  ├── type: income | expense
  ├── category: boss | hunting | sol_erda | cube | starforce | spell_trace | other
  ├── amount, description
  ├── entry_date
  └── week_start                  ← 해당 주의 목요일 날짜 (주간 그룹핑 키)

boss_kills
  ├── id (PK)
  ├── user_id (FK), character_id (FK, nullable)
  ├── ledger_entry_id (FK)        ← 가계부 자동 등록 연결
  ├── boss_name, difficulty
  ├── crystal_price               ← 처치 당시 가격 스냅샷 (이후 가격 변경과 무관)
  ├── kill_date, week_start

hunting_sessions
  ├── id (PK)
  ├── user_id (FK), character_id (FK, nullable)
  ├── ledger_entry_id (FK)
  ├── map_name, duration_minutes, income
  ├── sol_erda_fragments, sol_erda_meso_value
  ├── session_date, week_start

goals
  ├── id (PK)
  ├── user_id (FK)
  ├── item_name, target_amount
  ├── is_achieved, achieved_at

boss_master                       ← 보스 결정석 가격 마스터 (43개 초기 데이터)
  ├── id (PK)
  ├── boss_name + difficulty (UNIQUE)
  ├── crystal_price
  └── max_attempts_per_week
```

---

## 시작하기

### 사전 요구사항

- **Java 21** 이상
- **MySQL 8.x**
- **Gradle** (또는 IDE 내장 빌드 시스템)

### 1. 데이터베이스 생성

```sql
CREATE DATABASE maple_planner
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. 환경 설정

`src/main/resources/application.yml`을 열어 DB 접속 정보를 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maple_planner?characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: 본인_DB_사용자명
    password: 본인_DB_비밀번호

jwt:
  secret: 최소_32자_이상의_시크릿_키   # 운영 환경에서는 반드시 변경
  expiration: 604800000               # 토큰 유효기간 (7일, 밀리초)
```

> **보안 주의**: 운영 환경에서는 `jwt.secret`과 DB 비밀번호를 환경 변수로 관리하세요.

### 3. 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 빌드 후 JAR 실행
./gradlew build
java -jar build/libs/maplestory-backend-0.0.1-SNAPSHOT.jar
```

### 4. 초기 데이터 확인

애플리케이션 최초 실행 시 `data.sql`이 자동 실행되어 `boss_master` 테이블에 43개 보스 결정석 가격이 삽입됩니다.

---

## API 사용 방법

### Base URL

```
http://localhost:8080
```

### 공개 엔드포인트 (인증 불필요)

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| GET | `/api/boss/list` | 보스 목록 조회 |
| POST | `/api/stats/exp-calculator` | 경험치 계산기 |

### 인증 필요 엔드포인트

모든 요청 헤더에 토큰 포함:

```
Authorization: Bearer {발급받은_토큰}
```

---

### 인증 API

#### POST /api/auth/register — 회원가입

```json
// Request
{ "nickname": "메이플유저", "password": "pass1234" }

// Response 201
{
  "token": "eyJhbGci...",
  "user": { "id": 1, "nickname": "메이플유저", "solErdaFragmentPrice": 0, "createdAt": "..." }
}
```

#### POST /api/auth/login — 로그인

```json
// Request
{ "nickname": "메이플유저", "password": "pass1234" }
// Response 200 — register와 동일 구조
```

#### GET /api/auth/profile 🔒 — 내 프로필 조회

#### PUT /api/auth/sol-erda-price?price=150000 🔒 — 솔 에르다 단가 설정

```
Response 204 No Content
```

---

### 가계부 API

#### GET /api/ledger 🔒 — 주간 가계부 조회

```
Query: ?week=2026-04-24  (목요일 날짜, 생략 시 현재 주)
```

```json
// Response 200
{
  "weekStart": "2026-04-24",
  "entries": [
    {
      "id": 5, "type": "income", "category": "boss", "amount": 39600000,
      "description": "루시드 hard 결정석", "entryDate": "2026-04-27",
      "weekStart": "2026-04-24", "characterId": 1, "characterName": "야릇한비틀기",
      "createdAt": "..."
    }
  ],
  "summary": { "totalIncome": 39600000, "totalExpense": 0, "netProfit": 39600000 }
}
```

#### POST /api/ledger 🔒 — 가계부 항목 추가

```json
// Request
{ "type": "expense", "category": "cube", "amount": 100000000,
  "description": "레드 큐브 10개", "entryDate": "2026-04-27", "characterId": 1 }

// Response 201
{
  "entry": { /* LedgerEntryResponse */ },
  "goalWarnings": [
    {
      "goalId": 1, "itemName": "아케인셰이드 완드", "delayWeeks": 2,
      "message": "이번 지출로 인해 '아케인셰이드 완드' 목표 달성이 약 2주 지연되었습니다."
    }
  ]
}
```

#### DELETE /api/ledger/{id} 🔒 — 항목 삭제 → `204 No Content`

#### GET /api/ledger/weeks 🔒 — 기록된 주차 목록

```json
[{ "weekStart": "2026-04-24", "totalIncome": 150000000, "totalExpense": 50000000, "entryCount": 12 }]
```

#### GET /api/ledger/stats?weeks=4 🔒 — 카테고리별 통계

```json
[{ "category": "boss", "type": "income", "total": 158400000, "count": 4, "average": 39600000 }]
```

---

### 보스 API

#### GET /api/boss/list — 보스 목록 (인증 불필요)

```json
[{ "id": 1, "bossName": "루시드", "difficulty": "hard", "crystalPrice": 39600000, "maxAttemptsPerWeek": 1 }]
```

#### POST /api/boss/kill 🔒 — 보스 처치 기록

```json
// Request
{ "bossName": "루시드", "difficulty": "hard", "killDate": "2026-04-27", "characterId": 1 }

// Response 201
{
  "id": 10, "bossName": "루시드", "difficulty": "hard", "crystalPrice": 39600000,
  "killDate": "2026-04-27", "weekStart": "2026-04-24",
  "characterId": 1, "characterName": "야릇한비틀기", "createdAt": "..."
}
```

#### GET /api/boss/weekly?week=2026-04-24 🔒 — 주간 보스 처치 목록

#### GET /api/boss/stats 🔒 — 보스별 수익 통계

```json
[{ "bossName": "루시드", "difficulty": "hard", "killCount": 4, "totalCrystalIncome": 158400000, "avgCrystalPrice": 39600000.0 }]
```

---

### 사냥 API

#### POST /api/hunting/session 🔒 — 사냥 세션 기록

```json
// Request
{ "mapName": "아르카나 강변", "durationMinutes": 60, "income": 80000000,
  "solErdaFragments": 30, "sessionDate": "2026-04-27", "characterId": 1 }

// Response 201
{
  "id": 3, "mapName": "아르카나 강변", "durationMinutes": 60,
  "income": 80000000, "solErdaFragments": 30, "solErdaMesoValue": 4500000,
  "totalIncome": 84500000,
  "sessionDate": "2026-04-27", "weekStart": "2026-04-24",
  "characterId": 1, "characterName": "야릇한비틀기", "createdAt": "..."
}
```

#### GET /api/hunting/sessions?week=2026-04-24 🔒 — 주간 사냥 세션 목록

#### GET /api/hunting/stats 🔒 — 사냥터별 수익 효율 통계

```json
[{ "mapName": "아르카나 강변", "sessionCount": 5, "totalIncome": 425000000,
   "totalMinutes": 300, "avgIncomePerHour": 85000000 }]
```

---

### 캐릭터 API

#### POST /api/characters 🔒 — 캐릭터 등록

```json
{ "name": "야릇한비틀기", "jobClass": "아크메이지(불,독)", "level": 260, "isMain": true, "initialInvestment": 0 }
```

#### GET /api/characters 🔒 — 내 캐릭터 목록 (메인 캐릭터 상단 정렬)

```json
[{ "id": 1, "name": "야릇한비틀기", "jobClass": "아크메이지(불,독)", "level": 260, "isMain": true, "initialInvestment": 0, "createdAt": "..." }]
```

#### PUT /api/characters/{id} 🔒 — 캐릭터 수정

#### DELETE /api/characters/{id} 🔒 — 캐릭터 삭제

#### GET /api/characters/{id}/roi 🔒 — 부캐릭터 손익분기점 조회

```json
{
  "characterId": 2, "characterName": "부캐릭터명",
  "initialInvestment": 5000000000, "cumulativeBossIncome": 318000000,
  "weeklyAvgBossIncome": 106000000, "weeksToBreakEven": 48,
  "isBreakEvenReached": false, "remainingToBreakEven": 4682000000
}
```

> 계산 공식: `weeksToBreakEven = ceil(initialInvestment / weeklyAvgBossIncome)`

---

### 목표 API

#### POST /api/goals 🔒 — 목표 아이템 등록

```json
{ "itemName": "아케인셰이드 완드", "targetAmount": 30000000000 }
// Response 201: GoalResponse (id, itemName, targetAmount, isAchieved, createdAt, achievedAt)
```

#### GET /api/goals 🔒 / PUT /api/goals/{id} 🔒 / DELETE /api/goals/{id} 🔒

#### PATCH /api/goals/{id}/achieve 🔒 — 목표 달성 처리

#### GET /api/goals/{id}/estimate 🔒 — 목표 달성 예측

```json
{
  "goalId": 1, "itemName": "아케인셰이드 완드", "targetAmount": 30000000000,
  "currentSavings": 5000000000, "remaining": 25000000000, "progressPercent": 16,
  "avgWeeklyNet": 500000000, "weeksRemaining": 50, "estimatedDate": "2027-04-22"
}
```

> 계산 기준: 최근 4주간 평균 주당 순수익 (`LedgerQueryService.getAvgWeeklyNet(userId, 4)`)

---

### 통계 API

#### GET /api/stats/comparison 🔒 — 익명 유저 수익 비교

```json
{
  "userAvgWeeklyIncome": 800000000, "globalAvgWeeklyIncome": 400000000,
  "totalUsers": 150, "percentile": 85,
  "message": "내 수익은 전체 유저 상위 15%입니다."
}
```

#### POST /api/stats/exp-calculator — 경험치 계산기 (인증 불필요)

```json
// Request
{ "currentLevel": 260, "currentExpPercent": 45.5, "avgExpPerHour": 12.0, "targetLevel": 265 }
// Response 200
{ "currentLevel": 260, "targetLevel": 265, "hoursToTarget": 44.5, "daysToTarget": 1.9 }
```

---

## 인증 방식

**JWT Bearer Token** 방식을 사용합니다.

```
1. POST /api/auth/register 또는 /api/auth/login → token 발급
2. 이후 모든 요청 헤더에 포함:
   Authorization: Bearer {token}
3. 토큰 유효기간: 7일 (604,800,000ms)
```

### 주간 계산 알고리즘

메이플스토리는 **매주 목요일 00:00 KST**에 주간 미션이 초기화됩니다. 한 주는 **목요일 ~ 다음 수요일**로 정의됩니다.

```
목요일(4): (4-4+7)%7 = 0  → 당일이 주 시작
금요일(5): (5-4+7)%7 = 1  → 전날(목)이 주 시작
수요일(3): (3-4+7)%7 = 6  → 6일 전(목)이 주 시작
```

모든 `LedgerEntry`, `BossKill`, `HuntingSession`에는 `week_start` 컬럼이 있어 이 값으로 주간 데이터를 그룹핑합니다.

---

## 에러 응답 형식

```json
{ "message": "에러 메시지" }
```

| HTTP 코드 | 상황 |
|---|---|
| 400 | 입력값 검증 실패 |
| 401 | 인증 실패 (잘못된 닉네임/비밀번호, 토큰 없음/만료) |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없습니다 |
| 409 | 중복 (닉네임 중복 등) |
| 500 | 서버 내부 오류 |