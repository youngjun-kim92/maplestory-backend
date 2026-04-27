# 메이플스토리 종합 가계부 백엔드 (MapleStory Ledger Backend)

## 프로젝트 소개

메이플스토리 유저의 인게임 경제 활동을 스마트하게 관리하기 위한 **REST API 백엔드 서비스**입니다.
단순 수입/지출 기록을 넘어 보스 결정석 자동 계산, 부캐릭터 투자 회수 예측, 유저 간 익명 수익 비교 등
10가지 독창적인 기능을 제공합니다.

---

## 핵심 기능 10가지

| 번호 | 기능 | 설명 |
|:---:|---|---|
| 1 | **간편 회원가입** | 닉네임 + 비밀번호만으로 가입. MySQL에 영구 저장. |
| 2 | **목요일 기준 누적형 주간 가계부** | 메이플 주간 초기화(목요일) 기준으로 한 주를 묶어 수익/지출 기록 |
| 3 | **사냥터/보스 수익 효율 통계** | 사냥터별 시간당 수익, 보스별 누적 결정석 수익 통계 |
| 4 | **보스 결정석 자동 계산** | 보스 이름+난이도 선택 → 결정석 가격 자동 합산 |
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
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (JJWT 0.12) |
| ORM | Spring Data JPA (Hibernate 6) |
| Database | MySQL 8.x |
| Build | Gradle |
| Utility | Lombok |

---

## 프로젝트 디렉토리 구조

```
maplestory-backend/
│
├── docs/                                     ← 프로젝트 문서
│   ├── PROJECT_OVERVIEW.md                   ← 이 파일 (전체 개요)
│   └── API.md                                ← API 상세 명세
│
├── src/main/java/com/maplestory/ledger/
│   ├── MapleStoryApplication.java            ← 스프링 부트 진입점
│   │
│   ├── config/
│   │   └── SecurityConfig.java               ← Spring Security + CORS 설정
│   │
│   ├── security/                             ← JWT 인증 관련
│   │   ├── JwtTokenProvider.java             ← JWT 토큰 생성/검증
│   │   ├── JwtAuthFilter.java                ← 요청마다 JWT 검사하는 필터
│   │   ├── CustomUserDetails.java            ← 인증된 사용자 정보 래퍼
│   │   └── UserDetailsServiceImpl.java       ← DB에서 사용자 정보 로드
│   │
│   ├── entity/                               ← DB 테이블 매핑 클래스
│   │   ├── User.java                         ← 사용자
│   │   ├── MapleCharacter.java               ← 캐릭터 (메인/부캐)
│   │   ├── LedgerEntry.java                  ← 가계부 항목 (수입/지출)
│   │   ├── BossMaster.java                   ← 보스 결정석 가격 마스터 데이터
│   │   ├── BossKill.java                     ← 보스 처치 기록
│   │   ├── HuntingSession.java               ← 사냥 세션 기록
│   │   └── Goal.java                         ← 목표 아이템
│   │
│   ├── repository/                           ← DB 접근 인터페이스 (JPA)
│   │   ├── UserRepository.java
│   │   ├── CharacterRepository.java
│   │   ├── LedgerEntryRepository.java        ← 복잡한 집계 쿼리 포함
│   │   ├── BossMasterRepository.java
│   │   ├── BossKillRepository.java
│   │   ├── HuntingSessionRepository.java
│   │   ├── GoalRepository.java
│   │   └── projection/                       ← 네이티브 쿼리 결과 매핑용 인터페이스
│   │       ├── WeeklyNetProjection.java
│   │       ├── WeeklySummaryProjection.java
│   │       ├── UserWeeklyAvgProjection.java
│   │       └── BossStatsProjection.java
│   │
│   ├── service/                              ← 핵심 비즈니스 로직
│   │   ├── AuthService.java                  ← 회원가입, 로그인
│   │   ├── LedgerService.java                ← 가계부 CRUD + 과소비 경고 연동
│   │   ├── BossService.java                  ← 보스 결정석 자동 계산
│   │   ├── HuntingService.java               ← 사냥 세션 + 솔 에르다 환산
│   │   ├── CharacterService.java             ← 캐릭터 관리 + 손익분기점 계산
│   │   ├── GoalService.java                  ← 목표 관리 + 과소비 경고 계산
│   │   └── StatsService.java                 ← 익명 수익 비교 + 경험치 계산기
│   │
│   ├── controller/                           ← HTTP 요청 처리
│   │   ├── AuthController.java               ← /api/auth/**
│   │   ├── LedgerController.java             ← /api/ledger/**
│   │   ├── BossController.java               ← /api/boss/**
│   │   ├── HuntingController.java            ← /api/hunting/**
│   │   ├── CharacterController.java          ← /api/characters/**
│   │   ├── GoalController.java               ← /api/goals/**
│   │   └── StatsController.java              ← /api/stats/**
│   │
│   ├── dto/
│   │   ├── request/                          ← 클라이언트 → 서버 입력값
│   │   └── response/                         ← 서버 → 클라이언트 응답값
│   │
│   ├── exception/                            ← 커스텀 예외 + 전역 예외 핸들러
│   └── util/
│       └── WeekUtil.java                     ← 목요일 기준 주차 계산 유틸
│
└── src/main/resources/
    ├── application.yml                       ← DB 연결, JWT 설정
    └── data.sql                              ← 보스 결정석 가격 초기 데이터
```

---

## 데이터베이스 구조 (ERD 설명)

```
users (사용자)
  ├── id, nickname (unique), password_hash
  └── sol_erda_fragment_price  ← 솔 에르다 조각 낱개 가격 (사용자 설정)

characters (캐릭터)
  ├── id, name, job_class, level
  ├── is_main          ← 메인/부캐 구분
  ├── initial_investment  ← 부캐 초기 투자 비용 (손익분기점 계산에 사용)
  └── user_id (FK → users)

ledger_entries (가계부 항목) ← 모든 수입/지출의 단일 원천 (SSOT)
  ├── id, amount, description
  ├── type: income | expense
  ├── category: boss | hunting | sol_erda | cube | starforce | spell_trace | other
  ├── entry_date      ← 실제 발생 날짜
  ├── week_start      ← 해당 주의 목요일 날짜 (주간 구분 키)
  ├── user_id (FK), character_id (FK, nullable)
  └── created_at

boss_kills (보스 처치 기록)
  ├── boss_name, difficulty, crystal_price
  ├── kill_date, week_start
  ├── ledger_entry_id (FK → ledger_entries)  ← 가계부 자동 등록 연결
  └── user_id (FK), character_id (FK, nullable)

hunting_sessions (사냥 세션)
  ├── map_name, duration_minutes, income
  ├── sol_erda_fragments, sol_erda_meso_value  ← 조각 개수 + 자동 환산 메소
  ├── session_date, week_start
  ├── ledger_entry_id (FK → ledger_entries)
  └── user_id (FK), character_id (FK, nullable)

goals (목표 아이템)
  ├── item_name, target_amount
  ├── is_achieved, achieved_at
  └── user_id (FK)

boss_master (보스 결정석 가격 마스터)
  ├── boss_name, difficulty  (unique 복합키)
  ├── crystal_price
  └── max_attempts_per_week
```

---

## 설치 및 실행

### 1. 사전 요구사항
- Java 21 이상
- MySQL 8.x
- Gradle 8.x (또는 Gradle Wrapper 사용)

### 2. 데이터베이스 준비
```sql
CREATE DATABASE maplestory_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 환경 설정
`src/main/resources/application.yml` 파일에서 DB 접속 정보 수정:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maplestory_ledger?characterEncoding=UTF-8&...
    username: 본인_DB_사용자명
    password: 본인_DB_비밀번호

jwt:
  secret: 최소_32자_이상의_시크릿_키  # 운영 환경에서는 반드시 변경할 것
```

### 4. 실행
```bash
# 개발 실행
./gradlew bootRun

# 빌드 후 실행
./gradlew build
java -jar build/libs/maplestory-backend-0.0.1-SNAPSHOT.jar
```

### 5. 초기 데이터
`data.sql`이 자동 실행되어 보스 결정석 가격이 `boss_master` 테이블에 삽입됩니다.
(자쿰 ~ 검은마법사까지 44개 보스 데이터 포함)

---

## 인증 방식

**JWT Bearer Token** 방식을 사용합니다.

1. 회원가입(`POST /api/auth/register`) 또는 로그인(`POST /api/auth/login`) → `token` 발급
2. 이후 모든 요청의 헤더에 포함:
   ```
   Authorization: Bearer {발급받은_토큰}
   ```
3. 토큰 유효기간: **7일**

공개 엔드포인트 (토큰 불필요):
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/boss/list`

---

## 주간 계산 알고리즘 (목요일 기준)

메이플스토리는 **매주 목요일 00:00 KST** 에 주간 미션이 초기화됩니다.
따라서 한 주(Week)는 **목요일 ~ 다음 수요일** 로 정의됩니다.

```java
// WeekUtil.getWeekStart(date)
// 해당 날짜가 속한 주의 시작일(목요일)을 반환
int daysFromThursday = (date.getDayOfWeek().getValue() - THURSDAY.getValue() + 7) % 7;
return date.minusDays(daysFromThursday);
```

| 오늘 | 계산값 | 주 시작일 |
|---|---|---|
| 목 (4) | (4-4+7)%7 = 0 | 당일 목요일 |
| 금 (5) | (5-4+7)%7 = 1 | 전날 목요일 |
| 수 (3) | (3-4+7)%7 = 6 | 6일 전 목요일 |

---

## 기능별 상세 흐름

### 보스 결정석 자동 계산 (기능 #4)
```
클라이언트: POST /api/boss/kill { bossName: "루시드", difficulty: "hard", killDate: "2026-04-27" }
     ↓
BossService: boss_master 테이블에서 결정석 가격 조회 (39,600,000 메소)
     ↓
LedgerEntry 자동 생성: type=income, category=boss, amount=39,600,000
     ↓
BossKill 생성 (ledger_entry_id 참조)
     ↓
응답: 생성된 BossKill 정보 반환
```

### 과소비 경고 (기능 #10)
```
클라이언트: POST /api/ledger { type: "expense", category: "cube", amount: 500,000,000 }
     ↓
LedgerEntry 저장
     ↓
GoalService.checkGoalDelays(userId, 500,000,000) 호출
     ↓
활성 목표별 계산:
  - 현재 순자산: 총수입 - 총지출
  - 평균 주당 순수익: 최근 4주 데이터 평균
  - 이번 지출 없이 달성까지: ceil(잔여금액 / 평균주당수익) 주
  - 이번 지출 포함 달성까지: ceil((잔여금액 + 지출액) / 평균주당수익) 주
  - 지연 = 두 값의 차이
     ↓
응답: { entry: {...}, goalWarnings: [{ message: "X 목표 달성이 3주 지연됩니다." }] }
```

### 부캐 손익분기점 (기능 #8)
```
GET /api/characters/{id}/roi
     ↓
해당 캐릭터의 boss_kills 전체 조회
     ↓
주차별 그룹핑 → 주당 평균 보스 수익 계산
     ↓
손익분기점(주) = ceil(초기투자비 / 주당평균보스수익)
누적보스수익 >= 초기투자비 → isBreakEvenReached = true
```
