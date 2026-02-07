# ops-scheduler-batch-jobs

운영 환경에서 사용되던 **스케줄러 기반 배치 작업 구조**를 포트폴리오 목적에 맞게 재구성한 프로젝트입니다.  
서버 이중화 환경에서의 **중복 실행 방지(시간 분산)**, 외부 연동 책임 분리, 실행 결과 가시성을 중심으로 설계했습니다.

---

## 주요 포인트

- **서버 그룹 분산 실행**: 이중화 환경에서 스케줄 작업 중복 실행을 피하기 위해 그룹별 시간 분산 실행
- **Scheduler(Cron) 기반 배치 처리**: 주기적 실행 + 실행 로그/요약
- **책임 분리 설계**
  - `Scheduler` → 실행 트리거/분기
  - `InquiryService` → 대상 루프/오케스트레이션
  - `StoreClient(Mock)` → 외부 호출 책임(포폴에서는 Mock)
  - `FormService` → 표준 포맷(정규화)
  - `Repository(InMemory)` → 저장/조회
- **재시도(Backoff) 적용**: 외부 연동 실패를 고려한 재시도 구조 적용
- **Lock 적용**: 중복 실행 방지를 위해 InMemory Lock을 적용했으며, 실무 환경에서는 Redis 또는 DB Lock으로 확장 가능한 구조입니다.
---

## 아키텍처/흐름
1. Scheduler (Group A/B 분산 실행)
2. ReviewsInquiryService (targets loop)
3. ReviewStoreClient (Mock: Play/App)
4. ReviewsFormService (normalize)
5. ReviewRepository (InMemory)

---

## 스케줄 분산 실행(운영 설계)

- Group A: 0 / 6 / 12 / 18 시 실행
- Group B: 3 / 9 / 15 / 21 시 실행

> 운영 서버 이중화 환경에서 스케줄 작업이 중복 실행되지 않도록 그룹별 실행 시간대를 분리한 구조입니다.

---

## 운영 확인용 API

> 배치/스케줄러는 “보이는 것”이 운영에 중요하다고 판단하여 최소한의 운영 확인 API를 제공합니다.

- `POST /ops/reviews/fetch` : 수동 1회 실행 트리거
- `GET  /ops/reviews?limit=50` : 저장된 리뷰 최근 N개 조회
- `GET  /ops/reviews/inquiry?reviewId=...&platform=android|ios` : 단건 조회(저장 데이터 기준)

---

## 보안/계약 제약 처리

- 실제 스토어 API 인증/키/계정 파일은 포함하지 않습니다.
- 외부 연동 파이프라인 구조는 **Mock Client**로 재현하여 설계 의도를 중심으로 구성했습니다.

---

## 기술 스택

- Java 17
- Spring Boot 3.x (Scheduling)
- REST Controller (운영 확인용)
- Logback
- InMemory Repository (포트폴리오용)

---

> Repository 및 외부 연동 영역은 실무 환경에서 RDBMS/Redis 또는
> 실연동 구현체로 확장 가능하도록 책임을 분리해 설계했으며,
> 포트폴리오에서는 Mock 기반으로 구조를 재현했습니다.
