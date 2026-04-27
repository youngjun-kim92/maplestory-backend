# DDD 아키텍처 가이드

## 개요

이 문서는 메이플스토리 가계부 백엔드 프로젝트의 **도메인 주도 설계(Domain-Driven Design, DDD)** 리팩토링 내용을 설명합니다.

기존의 계층형 아키텍처(controller / service / entity / repository)를 **도메인 중심의 패키지 구조**로 전면 재편하였습니다.

---

## Before vs After

### Before — 계층형 아키텍처 (Layered Architecture)

```
com.maplestory.ledger/
├── config/           ← Spring Security 설정
├── security/         ← JWT 관련
├── util/             ← 유틸리티
├── entity/           ← 모든 JPA 엔티티 혼재
│   ├── User.java
│   ├── LedgerEntry.java
│   ├── BossMaster.java
│   ├── BossKill.java
│   ├── HuntingSession.java
│   ├── MapleCharacter.java
│   └── Goal.java
├── repository/       ← 모든 Repository 혼재
├── service/          ← 모든 Service 혼재
├── controller/       ← 모든 Controller 혼재
├── dto/
│   ├── request/
│   └── response/
└── exception/
```

**문제점:**
- Auth(인증)와 Boss(보스)가 같은 `controller/`, `service/` 폴더에 섞여 있음
- 파일이 늘어날수록 **기술 계층**으로만 묶여 도메인 경계가 불분명해짐
- 특정 도메인 코드 수정 시 여러 폴더를 오가며 파일을 찾아야 함
- 새로운 팀원이 "보스 기능"을 찾으려면 entity/service/controller/dto/repository 5개 폴더를 모두 뒤져야 함

---

### After — DDD 기반 도메인 중심 구조

```
com.maplestory.ledger/
│
├── MapleStoryApplication.java
│
├── common/                          ← 공통 관심사 (Shared Kernel)
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── DuplicateNicknameException.java
│   │   └── InvalidCredentialsException.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthFilter.java
│   │   ├── CustomUserDetails.java
│   │   └── UserDetailsServiceImpl.java
│   └── util/
│       └── WeekUtil.java
│
├── auth/                            ← 인증 도메인
│   ├── domain/
│   │   └── User.java                        (Aggregate Root)
│   ├── infrastructure/
│   │   └── UserRepository.java              (Spring Data JPA)
│   ├── application/
│   │   └── AuthService.java
│   └── presentation/
│       ├── AuthController.java
│       └── dto/
│           ├── RegisterRequest.java
│           ├── LoginRequest.java
│           ├── AuthResponse.java
│           └── UserResponse.java
│
├── ledger/                          ← 가계부 도메인 (핵심 SSOT)
│   ├── domain/
│   │   └── LedgerEntry.java                 (Aggregate Root)
│   ├── infrastructure/
│   │   ├── LedgerEntryRepository.java
│   │   └── projection/
│   │       ├── WeeklyNetProjection.java
│   │       └── WeeklySummaryProjection.java
│   ├── application/
│   │   └── LedgerService.java
│   └── presentation/
│       ├── LedgerController.java
│       └── dto/
│           └── LedgerEntryRequest.java
│
├── boss/                            ← 보스 도메인
│   ├── domain/
│   │   ├── BossMaster.java                  (Aggregate Root)
│   │   └── BossKill.java
│   ├── infrastructure/
│   │   ├── BossMasterRepository.java
│   │   ├── BossKillRepository.java
│   │   └── projection/
│   │       └── BossStatsProjection.java
│   ├── application/
│   │   └── BossService.java
│   └── presentation/
│       ├── BossController.java
│       └── dto/
│           └── BossKillRequest.java
│
├── hunting/                         ← 사냥 도메인
│   ├── domain/
│   │   └── HuntingSession.java              (Aggregate Root)
│   ├── infrastructure/
│   │   └── HuntingSessionRepository.java
│   ├── application/
│   │   └── HuntingService.java
│   └── presentation/
│       ├── HuntingController.java
│       └── dto/
│           ├── HuntingSessionRequest.java
│           └── HuntingStatsResponse.java
│
├── character/                       ← 캐릭터 도메인
│   ├── domain/
│   │   └── MapleCharacter.java              (Aggregate Root)
│   ├── infrastructure/
│   │   └── CharacterRepository.java
│   ├── application/
│   │   └── CharacterService.java
│   └── presentation/
│       ├── CharacterController.java
│       └── dto/
│           ├── CharacterRequest.java
│           └── CharacterROIResponse.java
│
├── goal/                            ← 목표 도메인
│   ├── domain/
│   │   └── Goal.java                        (Aggregate Root)
│   ├── infrastructure/
│   │   └── GoalRepository.java
│   ├── application/
│   │   ├── GoalService.java
│   │   └── GoalWarning.java                 (Application Output DTO)
│   └── presentation/
│       ├── GoalController.java
│       └── dto/
│           ├── GoalRequest.java
│           └── GoalEstimateResponse.java
│
└── stats/                           ← 통계 도메인 (Entity 없음)
    ├── infrastructure/
    │   └── projection/
    │       └── UserWeeklyAvgProjection.java
    ├── application/
    │   └── StatsService.java
    └── presentation/
        ├── StatsController.java
        └── dto/
            ├── ExpCalculatorRequest.java
            ├── ExpCalculatorResponse.java
            └── StatsComparisonResponse.java
```

---

## DDD 4계층 구조 설명

각 도메인 내부는 다음 4개의 계층으로 구성됩니다.

```
┌─────────────────────────────────────────────────┐
│  presentation/   (표현 계층)                      │
│  Controller, Request/Response DTO                 │
│  - HTTP 요청/응답 처리만 담당                       │
│  - 비즈니스 로직 없음                               │
├─────────────────────────────────────────────────┤
│  application/    (응용 계층)                      │
│  Service                                          │
│  - 도메인 객체들을 조합하여 Use Case를 구현          │
│  - 트랜잭션 경계 관리                               │
├─────────────────────────────────────────────────┤
│  domain/         (도메인 계층)                    │
│  Entity, Value Object                             │
│  - 핵심 비즈니스 규칙을 담당                        │
│  - 외부 기술(JPA, Spring)에 의존하지 않는 것이 이상적 │
├─────────────────────────────────────────────────┤
│  infrastructure/ (인프라 계층)                    │
│  Repository (Spring Data JPA), Projection         │
│  - DB 접근, 쿼리 등 기술 구현체                    │
└─────────────────────────────────────────────────┘
```

---

## 도메인 경계 (Bounded Context) 정의

| 도메인 | 책임 | Aggregate Root |
|:---:|---|:---:|
| **auth** | 회원가입, 로그인, JWT 발급, 솔 에르다 단가 설정 | `User` |
| **ledger** | 모든 수입/지출의 단일 원천(SSOT), 주간 가계부, 카테고리 통계 | `LedgerEntry` |
| **boss** | 보스 결정석 자동 계산, 보스 처치 기록, 보스별 수익 통계 | `BossMaster`, `BossKill` |
| **hunting** | 사냥 세션 기록, 솔 에르다 자동 환산, 사냥터별 효율 통계 | `HuntingSession` |
| **character** | 캐릭터 CRUD, 부캐릭터 손익분기점(ROI) 계산 | `MapleCharacter` |
| **goal** | 목표 아이템 관리, 달성 예측, 과소비 경고 | `Goal` |
| **stats** | 익명 유저 수익 비교, 경험치 계산기 | *(Entity 없음)* |
| **common** | JWT 필터, Security 설정, 예외 처리, WeekUtil | *(공유 커널)* |

---

## 도메인 간 의존 관계 (Cross-Domain Dependencies)

DDD에서 도메인 간 의존은 **도메인 레이어가 아닌 응용 레이어(application layer)** 에서만 허용됩니다.

```
                      ┌────────────┐
                      │   common   │ ← 모든 도메인이 의존
                      │ (security, │
                      │  exception,│
                      │   util)    │
                      └────────────┘
                            ↑
         ┌──────────────────┼──────────────────┐
         │                  │                  │
    ┌────┴────┐        ┌────┴────┐       ┌────┴────┐
    │  auth   │        │ ledger  │       │  boss   │
    │(User)   │        │(LedgerE)│       │(BossKill│
    └────┬────┘        └────┬────┘       │BossM.)  │
         │                  │↑           └────┬────┘
         │           ┌──────┘│                │
         │           │  goal │←───────────────┘
         │           │(Goal) │
         │           └───────┘
         │
    ┌────┴────┐   ┌──────────┐   ┌──────────┐
    │character│   │ hunting  │   │  stats   │
    │(Maple   │   │(Hunting  │   │(no entity│
    │ Char.)  │   │ Session) │   │  stats   │
    └─────────┘   └──────────┘   └──────────┘
```

### 주요 의존 관계 상세

| 의존하는 도메인 | 의존 대상 | 이유 |
|---|---|---|
| `ledger.application.LedgerService` | `goal.application.GoalService` | 지출 추가 시 과소비 경고 계산 (기능 #10) |
| `boss.application.BossService` | `ledger.infrastructure.LedgerEntryRepository` | 보스 처치 시 가계부 자동 생성 (기능 #4) |
| `hunting.application.HuntingService` | `ledger.infrastructure.LedgerEntryRepository` | 사냥 세션 시 가계부 자동 생성 (기능 #5) |
| `character.application.CharacterService` | `boss.infrastructure.BossKillRepository` | 캐릭터별 보스 수익 집계 (기능 #8) |
| `goal.application.GoalService` | `ledger.infrastructure.LedgerEntryRepository` | 순자산/주간 평균 계산에 가계부 데이터 필요 |
| `stats.application.StatsService` | `ledger.infrastructure.LedgerEntryRepository` | 전체 유저 익명 수익 집계 (기능 #9) |
| `common.security.UserDetailsServiceImpl` | `auth.infrastructure.UserRepository` | JWT 인증 시 사용자 조회 |

> **설계 원칙**: 도메인 엔티티끼리의 JPA 연관은 객체 참조를 유지하고, 서비스 간 협력은 application 레이어에서만 수행합니다.

---

## common (공유 커널) 상세

`common` 패키지는 모든 도메인이 공통으로 사용하는 컴포넌트를 담습니다.

### common/security
| 파일 | 역할 |
|---|---|
| `SecurityConfig.java` | Spring Security 필터 체인, CORS, BCrypt 빈 설정 |
| `JwtTokenProvider.java` | JWT 생성(generateToken) / 검증(validateToken) / 파싱(getUserId) |
| `JwtAuthFilter.java` | 모든 요청에서 Bearer 토큰을 추출하여 SecurityContext에 등록 |
| `CustomUserDetails.java` | 인증된 사용자 정보(userId, nickname)를 담는 래퍼 |
| `UserDetailsServiceImpl.java` | JWT 검증 후 userId로 DB에서 사용자 로드 |

### common/exception
| 파일 | HTTP 상태 |
|---|---|
| `GlobalExceptionHandler.java` | 전역 예외 핸들러 (`@RestControllerAdvice`) |
| `ResourceNotFoundException.java` | 404 Not Found |
| `DuplicateNicknameException.java` | 409 Conflict |
| `InvalidCredentialsException.java` | 401 Unauthorized |

### common/util
| 파일 | 역할 |
|---|---|
| `WeekUtil.java` | 임의 날짜 → 해당 주의 목요일(week_start) 계산 |

---

## GoalWarning의 위치 결정 이유

`GoalWarning`은 `goal/application/GoalWarning.java`에 위치합니다.

```
goal/
└── application/
    ├── GoalService.java
    └── GoalWarning.java   ← 여기
```

`GoalWarning`은 `GoalService.checkGoalDelays()`의 반환 타입으로, goal 도메인의 **응용 계층 출력 객체(Application Output DTO)** 입니다. `LedgerService`가 이 타입을 import하는 것은 application 계층 간 협력이므로 허용됩니다.

---

## 확장성 고려 사항

이 DDD 구조는 다음과 같은 확장 시 이점을 제공합니다.

### 1. 새로운 기능 추가

예시: **길드 공동 자산 관리** 기능 추가 시

```
guild/                           ← 새 도메인 추가
├── domain/
│   └── Guild.java
├── infrastructure/
│   └── GuildRepository.java
├── application/
│   └── GuildService.java
└── presentation/
    ├── GuildController.java
    └── dto/
        └── GuildRequest.java
```

다른 도메인 코드를 **전혀 수정하지 않고** 새 폴더만 추가합니다.

---

### 2. 마이크로서비스 분리

각 도메인이 독립적인 패키지로 격리되어 있어, 향후 MSA로 분리할 때 해당 도메인 폴더를 그대로 새 프로젝트로 이동할 수 있습니다.

```
현재 (모놀리식):              향후 MSA 분리:
maplestory-backend/          maplestory-auth-service/
├── auth/                    maplestory-ledger-service/
├── ledger/       →          maplestory-boss-service/
├── boss/                    maplestory-character-service/
└── ...                      ...
```

---

### 3. 팀 단위 개발 분리

각 도메인이 독립된 패키지이므로 팀원별로 도메인을 나눠 개발할 때 충돌이 최소화됩니다.

```
팀원 A → auth/, common/
팀원 B → boss/, hunting/
팀원 C → goal/, stats/
팀원 D → ledger/, character/
```

---

## 리팩토링 변경 사항 요약

### 패키지 이동 매핑표

| 기존 경로 (계층형) | 신규 경로 (DDD) |
|---|---|
| `entity/User.java` | `auth/domain/User.java` |
| `entity/LedgerEntry.java` | `ledger/domain/LedgerEntry.java` |
| `entity/BossMaster.java` | `boss/domain/BossMaster.java` |
| `entity/BossKill.java` | `boss/domain/BossKill.java` |
| `entity/HuntingSession.java` | `hunting/domain/HuntingSession.java` |
| `entity/MapleCharacter.java` | `character/domain/MapleCharacter.java` |
| `entity/Goal.java` | `goal/domain/Goal.java` |
| `repository/UserRepository.java` | `auth/infrastructure/UserRepository.java` |
| `repository/LedgerEntryRepository.java` | `ledger/infrastructure/LedgerEntryRepository.java` |
| `repository/BossMasterRepository.java` | `boss/infrastructure/BossMasterRepository.java` |
| `repository/BossKillRepository.java` | `boss/infrastructure/BossKillRepository.java` |
| `repository/HuntingSessionRepository.java` | `hunting/infrastructure/HuntingSessionRepository.java` |
| `repository/CharacterRepository.java` | `character/infrastructure/CharacterRepository.java` |
| `repository/GoalRepository.java` | `goal/infrastructure/GoalRepository.java` |
| `repository/projection/WeeklyNetProjection.java` | `ledger/infrastructure/projection/WeeklyNetProjection.java` |
| `repository/projection/WeeklySummaryProjection.java` | `ledger/infrastructure/projection/WeeklySummaryProjection.java` |
| `repository/projection/BossStatsProjection.java` | `boss/infrastructure/projection/BossStatsProjection.java` |
| `repository/projection/UserWeeklyAvgProjection.java` | `stats/infrastructure/projection/UserWeeklyAvgProjection.java` |
| `service/AuthService.java` | `auth/application/AuthService.java` |
| `service/LedgerService.java` | `ledger/application/LedgerService.java` |
| `service/BossService.java` | `boss/application/BossService.java` |
| `service/HuntingService.java` | `hunting/application/HuntingService.java` |
| `service/CharacterService.java` | `character/application/CharacterService.java` |
| `service/GoalService.java` | `goal/application/GoalService.java` |
| `service/StatsService.java` | `stats/application/StatsService.java` |
| `controller/AuthController.java` | `auth/presentation/AuthController.java` |
| `controller/LedgerController.java` | `ledger/presentation/LedgerController.java` |
| `controller/BossController.java` | `boss/presentation/BossController.java` |
| `controller/HuntingController.java` | `hunting/presentation/HuntingController.java` |
| `controller/CharacterController.java` | `character/presentation/CharacterController.java` |
| `controller/GoalController.java` | `goal/presentation/GoalController.java` |
| `controller/StatsController.java` | `stats/presentation/StatsController.java` |
| `dto/request/RegisterRequest.java` | `auth/presentation/dto/RegisterRequest.java` |
| `dto/request/LoginRequest.java` | `auth/presentation/dto/LoginRequest.java` |
| `dto/response/AuthResponse.java` | `auth/presentation/dto/AuthResponse.java` |
| `dto/response/UserResponse.java` | `auth/presentation/dto/UserResponse.java` |
| `dto/request/LedgerEntryRequest.java` | `ledger/presentation/dto/LedgerEntryRequest.java` |
| `dto/request/BossKillRequest.java` | `boss/presentation/dto/BossKillRequest.java` |
| `dto/request/HuntingSessionRequest.java` | `hunting/presentation/dto/HuntingSessionRequest.java` |
| `dto/response/HuntingStatsResponse.java` | `hunting/presentation/dto/HuntingStatsResponse.java` |
| `dto/request/CharacterRequest.java` | `character/presentation/dto/CharacterRequest.java` |
| `dto/response/CharacterROIResponse.java` | `character/presentation/dto/CharacterROIResponse.java` |
| `dto/request/GoalRequest.java` | `goal/presentation/dto/GoalRequest.java` |
| `dto/response/GoalEstimateResponse.java` | `goal/presentation/dto/GoalEstimateResponse.java` |
| `dto/response/GoalWarning.java` | `goal/application/GoalWarning.java` |
| `dto/request/ExpCalculatorRequest.java` | `stats/presentation/dto/ExpCalculatorRequest.java` |
| `dto/response/ExpCalculatorResponse.java` | `stats/presentation/dto/ExpCalculatorResponse.java` |
| `dto/response/StatsComparisonResponse.java` | `stats/presentation/dto/StatsComparisonResponse.java` |
| `config/SecurityConfig.java` | `common/security/SecurityConfig.java` |
| `security/JwtAuthFilter.java` | `common/security/JwtAuthFilter.java` |
| `security/JwtTokenProvider.java` | `common/security/JwtTokenProvider.java` |
| `security/CustomUserDetails.java` | `common/security/CustomUserDetails.java` |
| `security/UserDetailsServiceImpl.java` | `common/security/UserDetailsServiceImpl.java` |
| `exception/GlobalExceptionHandler.java` | `common/exception/GlobalExceptionHandler.java` |
| `exception/ResourceNotFoundException.java` | `common/exception/ResourceNotFoundException.java` |
| `exception/DuplicateNicknameException.java` | `common/exception/DuplicateNicknameException.java` |
| `exception/InvalidCredentialsException.java` | `common/exception/InvalidCredentialsException.java` |
| `util/WeekUtil.java` | `common/util/WeekUtil.java` |

### 코드 변경 사항

| 항목 | 변경 내용 |
|---|---|
| 패키지 선언 | 모든 파일의 `package` 문 업데이트 |
| import 경로 | 모든 파일의 import 경로를 새 패키지 경로로 업데이트 |
| `@Getter(onMethod_ = {@JsonIgnore})` | Java 25 호환을 위해 `@JsonIgnore`를 필드에 직접 적용으로 변경 |
| `GoalWarning` 위치 | `dto/response/` → `goal/application/` (Application Output DTO로 재분류) |
| 로직 변경 없음 | 모든 비즈니스 로직은 동일하게 유지 |

---

## DDD 적용 시 한계 및 의도적 타협

이 프로젝트는 **실용적인 DDD(Pragmatic DDD)** 를 적용하였습니다. 완전한 DDD와 비교하면 다음 부분에서 의도적으로 단순화하였습니다.

| 완전한 DDD | 이 프로젝트의 선택 | 이유 |
|---|---|---|
| Repository 인터페이스를 도메인 계층에 정의 | Repository를 infrastructure 계층에 직접 위치 | Spring Data JPA의 `@Query` 등 기술 어노테이션이 포함되므로 도메인 순수성 대신 편의성 선택 |
| Aggregate 간 참조는 ID(Long)로만 | 일부 엔티티에서 객체 참조 유지 | JPA 연관 관계 특성 상 객체 참조가 실용적 |
| Domain Event로 도메인 간 통신 | Application Service 직접 호출 | 개인 프로젝트 규모에서 이벤트 버스는 과도한 복잡성 |
| Value Object 분리 | enum(EntryType, EntryCategory) 사용 | 단순한 열거형은 엔티티 내부에 유지 |

이러한 타협은 **프로젝트 규모와 팀 크기를 고려한 합리적인 선택**입니다. 향후 프로젝트가 커지고 팀이 늘어나면 위의 항목들을 점진적으로 강화할 수 있습니다.

---

## 새 기능 추가 가이드라인

DDD 구조에서 새 기능을 추가할 때는 다음 순서를 따릅니다.

### 기존 도메인에 기능 추가 예시: 보스 처치 취소 기능

```
1. domain/    → 필요 시 BossKill에 비즈니스 규칙 추가
2. infrastructure/ → BossKillRepository에 쿼리 추가
3. application/ → BossService에 cancelBossKill() 메서드 추가
4. presentation/ → BossController에 DELETE 엔드포인트 추가
                → dto/에 필요한 Request/Response 추가
```

### 새 도메인 추가 예시: 거래소 가격 추적 기능

```
1. marketplace/ 폴더 생성
2. domain/ → MarketItem.java (엔티티)
3. infrastructure/ → MarketItemRepository.java
4. application/ → MarketplaceService.java
5. presentation/ → MarketplaceController.java
                → dto/MarketItemRequest.java, MarketPriceResponse.java
```

기존 도메인을 수정하지 않고 새 폴더만 추가합니다.
