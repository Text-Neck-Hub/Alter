이 서비스는 MSA 환경에서의 원활한 로그 수집을 위한 Alter(WebFlux) 서비스이다.

작성자(최지웅)는 관심사의 분리를 통해 핵심 비즈니스 로직을 외부 기술(웹, DB, 메시징 등)의 변화로부터 보호하여, 유연하고
테스트하기 쉬운 코드를 만들고자 하였음

# Quick Start

_오케스트라 레포지토리에서 미리 도커 파일 가져온 후_

```bash
docker compose up -d && docker compose exec <서비스> bash || docker compose exec <서비스> sh
```

```bash
make run
```

# alter 프로젝트 구조

```bash

alter
    ├── AlterApplication.java
    ├── adapter
    │   ├── in
    │   │   ├── messaging
    │   │   │   └── KafkaLogConsumer.java
    │   │   └── web
    │   │       └── controller
    │   │           ├── MongoDbTest.java
    │   │           └── SlackTest.java
    │   └── out
    │       ├── messaging
    │       │   └── SlackNotifier.java
    │       └── persistence
    │           ├── LogMessageEntry.java
    │           ├── LogMessageEntryRepository.java
    │           └── LogMessageRecorder.java
    ├── application
    │   ├── dto
    │   ├── service
    │   └── usecase
    ├── common
    ├── config
    │   └── AppConfig.java
    └── domain
        ├── model
        │   └── LogMessage.java
        └── port
            ├── in
            └── out
                └── NotifierPort.java
```

### 1) 개요

- 헥사고날 아키텍처 따름
- 도메인/유스케이스 중심 설계임
- 웹/메시징/DB/외부 API는 어댑터로 분리됨
- 교체 용이성, 테스트 용이성 지향함

### 2) 디렉터리 개관

- AlterApplication.java

  - 스프링 부트 엔트리포인트임
  - 부팅/컴포넌트 스캔 시작 역할만 함

- config/AppConfig.java

  - 의존성 조립부임
  - 유스케이스에 포트 구현 주입함
  - 트랜잭션/Clock/IdGenerator 같은 공용 빈 정의 적합함

- domain/

  - model/LogMessage.java
    - 핵심 도메인 모델임
    - 불변식/검증/상태 전이 보유함
  - port/
    - in/
      - 유스케이스 진입 인터페이스 모음임
      - 컨트롤러/컨슈머가 여기 인터페이스 통해 호출함
    - out/
      - NotifierPort.java
        - 외부 알림(예: Slack) 추상화 포트임
        - out/messaging 어댑터가 구현함

- application/

  - dto/
    - 유스케이스 입출력 DTO 모음임
    - 도메인 모델 직접 노출 안 함
  - usecase/
    - 유스케이스 인터페이스/구현 위치임
    - 트랜잭션 경계 설정 가능함
  - service/
    - 팀 컨벤션 따라 유스케이스 구현 배치 가능함
    - 도메인 조립/포트 호출 순서/예외 변환 담당함

- adapter/

  - in/
    - web/controller/
      - MongoDbTest.java
        - HTTP 엔드포인트 예시임
        - 저장/조회 동작 검증 목적임
      - SlackTest.java
        - Slack 알림 트리거용 엔드포인트 예시임
    - messaging/
      - KafkaLogConsumer.java
        - Kafka 인바운드 어댑터임
        - 메시지 역직렬화 → 유스케이스 호출 수행함
  - out/
    - persistence/
      - LogMessageEntry.java
        - 영속 엔티티임
        - DB 매핑 전용, 도메인과 분리됨
      - LogMessageEntryRepository.java
        - 데이터 접근 인터페이스임
      - LogMessageRecorder.java
        - 저장 어댑터임
        - 도메인→엔티티 변환 및 저장 처리함
    - messaging/
      - SlackNotifier.java
        - NotifierPort 구현체임
        - Slack 전송/에러 매핑/재시도 정책 담당함

- common/
  - 공용 예외/유틸/응답 포맷/Clock/IdGen 등 배치 추천함

### 3) 요청·이벤트 흐름

- HTTP → 유스케이스 → 저장/알림
  1. Controller가 요청 DTO 검증 및 변환함
  2. UseCase가 도메인 모델 생성/검증함
  3. LogMessageRecorder/NotifierPort 호출함
  4. 결과 DTO로 응답 반환함
- Kafka → 유스케이스 → 저장/알림
  1. Consumer가 수신/역직렬화함
  2. 유스케이스 입력 DTO로 변환 후 호출함
  3. 저장/알림 수행함
  4. 오프셋 커밋/에러 처리 정책 적용함

### 4) 의존성 방향

- domain, application은 adapter 모름
- application은 domain 포트/모델만 의존함
- adapter는 in 포트 호출 또는 out 포트 구현함
- config는 조립만 함, 비즈니스 로직 넣지 않음

### 5) 주요 파일 역할 요약

- AlterApplication.java: 부팅만 함
- AppConfig.java: 포트 구현 주입/트랜잭션 등 공용 빈 관리함
- LogMessage.java: 도메인 규칙/불변식 보유함
- NotifierPort.java: 알림 행위 추상화함
- MongoDbTest.java / SlackTest.java: 샘플/검증용 엔드포인트임
- KafkaLogConsumer.java: Kafka 인바운드 처리함
- LogMessageEntry.java / LogMessageEntryRepository.java / LogMessageRecorder.java: 영속화 계층 담당함
- SlackNotifier.java: 외부 알림 전송 담당함

### 6) DTO/유스케이스/서비스 가이드

- DTO: 입출력 경계 전용임, 도메인 은닉함
- UseCase(입력 포트 구현): 비즈니스 시나리오 표현함, 트랜잭션 경계 설정 적합함
- Service: 포트 호출 순서/예외 변환/메트릭 기록 등 협력 관리함

### 7) 확장/변경 패턴

- 새 알림 채널 추가
  - out 포트 재사용 또는 새 포트 정의함
  - 어댑터 추가 후 AppConfig 주입 변경함
  - in 어댑터(컨트롤러/컨슈머) 변경 최소화됨
- 저장소 교체(Mongo ↔ RDB 등)
  - 새 persistence 어댑터 구현함
  - 포트 계약 유지함
  - AppConfig 주입만 교체하면 됨

### 8) 테스트 가이드

- 단위: 도메인/유스케이스 위주, 외부 I/O는 페이크/스텁 사용함
- 계약: NotifierPort 등 공통 스펙 정의해서 모든 구현 통과 요구함
- 어댑터: 컨트롤러 매핑/에러 변환, Kafka 역직렬화/에러 흐름 검증함
- 통합: Testcontainers로 DB/브로커 붙여 경계 검증 권장함
- E2E: 핵심 플로우 소수만 유지함

### 9) 네이밍/컨벤션

- 포트: 동사형+Port 권장함 (NotifierPort, LogStorePort)
- 어댑터: 기술명+기능 사용함 (SlackNotifier, JpaLogRecorder)
- DTO: UseCaseInput/Output 또는 Request/Response 접미사 사용함
- 예외/에러코드: common 하위에 통합 관리함

### 10) 권장 보완

- 저장 포트 분리 추천함
  - domain/port/out에 LogStorePort 추가 권장함
  - LogMessageRecorder가 구현하도록 하면 교체 용이성↑ 됨
- Clock/IdGenerator 인터페이스 도입 권장함
  - 테스트 결정성 확보됨
- AppConfig에서 트랜잭션 경계 명시 권장함

<h1>Hexagonal Architecture</h1>
<img src="images/Hexagonal Architecture.png"></img>
