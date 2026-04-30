# MaplePlanner — 메이플스토리 종합 가계부 백엔드

## 프로젝트 소개

메이플스토리 유저의 인게임 경제 활동을 스마트하게 관리하기 위한 **REST API 백엔드 서비스**입니다.

---

## 핵심 기능

| 번호 | 기능 | 설명 |
|:---:|---|---|
| 1 | **간편 회원가입** | 닉네임 + 비밀번호만으로 가입 |
| 2 | **목요일 기준 누적형 주간 가계부** | 메이플 주간 초기화(목요일) 기준으로 수익/지출 기록 |
| 3 | **사냥터/보스 수익 효율 통계** | 사냥터별 시간당 수익, 보스별 누적 결정석 수익 통계 |
| 4 | **보스 결정석 자동 계산** | 보스 이름+난이도 선택 → 결정석 가격 자동 합산 |
| 5 | **솔 에르다 조각 자동 환산** | 조각 개수 × 사용자 설정 단가 = 자동 메소 합산, 캐릭터별 누적 추적 |
| 6 | **목표 아이템 달성 예측** | 평균 주간 수익 기반 달성까지 남은 주차 계산 |
| 7 | **레벨업 경험치 계산기** | 시간당 평균 경험치%로 목표 레벨까지 시간 계산 |
| 8 | **부캐릭터 손익분기점 계산기** | 초기 투자비 ÷ 주당 보스 수익 = 투자 회수까지 남은 주차 |
| 9 | **익명 기반 유저 평균 수익 비교** | 전체 유저 수익 데이터 익명 집계 후 내 수익 백분위 제공 |
| 10 | **과소비 경고 및 목표 지연 알림** | 지출 추가 시 목표 달성이 몇 주 지연되는지 자동 경고 |
| 11 | **경매장 MVP 수수료 자동 적용** | 캐릭터 MVP 등급(NORMAL~BLACK) 또는 PC방 접속 여부로 3%/5% 수수료 자동 계산 |
| 12 | **즐겨찾기 템플릿** | 보스/사냥/지출 기록을 즐겨찾기로 저장, 반복 입력 최소화 |
| 13 | **캐릭터 일괄 등록** | 여러 캐릭터를 한 번에 등록 가능 |
| 14 | **보스 도핑비 인라인 지출** | 보스 처치 기록 시 도핑비 등 지출을 동시에 기록 |

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JWT (JJWT 0.12.5) |
| ORM | Spring Data JPA (Hibernate 6) |
| Database | MySQL 8.x |
| Build | Gradle 9.2.1 |
| Utility | Lombok |

---

## DDD 도메인 구조

```
com.maplestory.ledger/
├── auth/          — 회원가입, 로그인, 프로필, 메소 잔액, 전체 초기화
├── boss/          — 보스 처치 기록, 드랍 기록, 경매장 판매 (수수료 포함)
├── character/     — 캐릭터 관리, 솔 에르다 조각 추적, 손익분기점, MVP 등급
├── favorite/      — 즐겨찾기 템플릿 (보스/사냥/지출)
├── goal/          — 목표 아이템, 과소비 경고
├── hunting/       — 사냥 세션, 솔 에르다 조각 자동 환산
├── ledger/        — 가계부 항목 CRUD (수입/지출 단일 원천)
├── stats/         — 익명 수익 비교, 경험치 계산기
└── common/        — Security (JWT), 예외 처리, WeekUtil
```

각 도메인은 **domain → infrastructure → application → presentation** 4계층으로 구성됩니다.

---

## 데이터베이스 ERD

```
users
  ├── id, nickname (unique), password_hash
  ├── inventory_meso, storage_meso   ← 보유 메소 (수입/지출 발생 시 자동 반영)
  └── sol_erda_fragment_price        ← 조각 단가 (사용자 설정)

characters
  ├── id, name, job_class, level
  ├── is_main                        ← 메인 1개만 존재 (새 메인 지정 시 기존 자동 해제)
  ├── initial_investment             ← 부캐 초기 투자 비용 (손익분기점 계산)
  ├── sol_erda_fragments             ← 캐릭터별 누적 솔 에르다 조각 수
  ├── mvp_grade                      ← NORMAL|BRONZE|SILVER|GOLD|DIAMOND|RED|BLACK
  └── user_id (FK)

ledger_entries  ← 모든 수입/지출의 단일 원천 (SSOT)
  ├── type: income | expense
  ├── category: boss|hunting|sol_erda|cube|starforce|spell_trace|trade|auction|doping|other
  ├── week_start                     ← 목요일 기준 주차 구분 키
  ├── sol_erda_fragments             ← 사냥 카테고리에서 조각 수 기록
  └── user_id (FK), character_id (FK, nullable)

boss_kills
  ├── boss_name, difficulty, crystal_price, party_size
  ├── kill_date, week_start
  ├── ledger_entry_id (FK)           ← 자동 생성된 income LedgerEntry 참조
  └── user_id (FK), character_id (FK, nullable)

hunting_sessions
  ├── map_name, duration_minutes, income
  ├── sol_erda_fragments, sol_erda_meso_value
  ├── session_date, week_start
  ├── ledger_entry_id (FK)
  └── user_id (FK), character_id (FK, nullable)

favorites  ← 즐겨찾기 템플릿
  ├── type: BOSS | DOPING
  ├── label                          ← 즐겨찾기 표시명
  ├── boss_name, difficulty, party_size (BOSS용)
  ├── boss_name (nullable), amount, description (DOPING용)
  │     boss_name=null: 모든 보스 공통 도핑
  │     boss_name=값:   특정 보스 전용 도핑
  └── user_id (FK)

goals
  ├── item_name, target_amount
  ├── is_achieved, achieved_at
  └── user_id (FK)

boss_master
  ├── boss_name, difficulty (unique)
  ├── crystal_price, max_attempts_per_week
```

---

## 경매장 수수료 정책

| MVP 등급 | 수수료 | 비고 |
|---|---|---|
| 일반 (NORMAL) | 5% | 기본 |
| MVP 브론즈 (BRONZE) | 5% | 혜택 없음 |
| MVP 실버 이상 (SILVER~BLACK) | 3% | MVP 스페셜 혜택 |
| PC방 접속 (isPcCafe=true) | 3% | 등급 무관, 중복 할인 불가 |

수수료는 `PATCH /api/boss/drops/{id}/sell` 요청의 `isPcCafe` 필드와
처치 캐릭터의 `mvpGrade`를 기준으로 자동 계산됩니다.

---

## 주간 계산 알고리즘 (목요일 기준)

```java
// 목요일 ~ 다음 수요일 = 1주
int daysFromThursday = (date.getDayOfWeek().getValue() - THURSDAY.getValue() + 7) % 7;
return date.minusDays(daysFromThursday);
```

---

## 보스 처치 흐름 (도핑비 인라인 지출 포함)

```
POST /api/boss/kill { bossName, difficulty, expenses: [{category, amount, description}] }
  ↓
보스 결정석 수익 LedgerEntry 생성 (income)
  ↓
expenses 항목별 LedgerEntry 생성 (expense)
  ↓
인벤토리 메소 = 기존 + 수익 - 지출 합계
  ↓
BossKill 저장 → 응답
```

---

## 설치 및 실행

```bash
# MySQL DB 생성
CREATE DATABASE maplestory_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 환경 설정 (application.yml)
spring.datasource.url: jdbc:mysql://localhost:3306/maplestory_ledger...
spring.datasource.username / password

# 실행
./gradlew bootRun
```

`data.sql` 자동 실행으로 보스 결정석 가격 마스터 데이터가 삽입됩니다.
