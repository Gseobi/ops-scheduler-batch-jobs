# ops-scheduler-batch-jobs

운영 환경에서 사용되던 **스케줄러 기반 배치 작업 구조**를 포트폴리오 목적에 맞게 재구성한 프로젝트입니다.  
서버 이중화 환경에서의 **중복 실행 방지(시간 분산)**, 외부 연동 책임 분리, 재시도, 실행 결과 가시성을 중심으로 설계했습니다.

<br/>

## 1. Project Overview

이 프로젝트는 운영 환경에서 사용되던 스케줄러 기반 배치 작업 구조를  
포트폴리오 목적에 맞게 재구성한 프로젝트입니다.

실제 운영 환경에서는 주기적으로 외부 데이터를 조회하고,  
필요한 형식으로 정규화한 뒤 저장하거나 후속 처리에 활용하는 배치성 작업이 필요했습니다.

이 과정에서 중요한 것은 단순히 스케줄을 실행하는 것이 아니라 다음과 같은 운영 관점이었습니다.

- 이중화 환경에서의 중복 실행 방지
- 외부 연동 책임 분리
- 실패 시 재시도 가능 구조
- 실행 결과 확인과 운영 가시성 확보

본 프로젝트는 이러한 실무 관점을 바탕으로  
Scheduler, InquiryService, StoreClient, FormService, Repository의 책임을 나누고,  
시간 분산 실행과 기본적인 lock 구조를 포함하는 형태로 재구성했습니다.

<br/>

## 2. Why This Project

배치 작업은 단순히 정해진 시간에 실행되는 코드가 아니라,  
운영 환경에서는 **중복 실행**, **실패 후 재처리**, **외부 연동 불안정성**, **실행 결과 추적**이 함께 고려되어야 합니다.

특히 서버 이중화 환경에서는 동일한 스케줄이 여러 인스턴스에서 동시에 실행될 수 있기 때문에  
실행 제어 방식이 매우 중요합니다.

이 프로젝트는 다음과 같은 설계 의도를 중심으로 구성했습니다.

- 스케줄러 중복 실행 위험을 줄이기 위한 시간 분산 실행
- 외부 연동과 정규화 책임 분리
- 재시도(Backoff)를 고려한 배치 흐름
- 운영자가 상태를 확인할 수 있는 최소한의 API 제공
- Mock 기반으로 구조를 설명할 수 있는 포트폴리오화

<br/>

## 3. What This Project Proves

- 스케줄러 기반 배치 구조를 실행 제어 중심으로 분리할 수 있습니다.
- 이중화 환경에서의 중복 실행 위험을 줄이기 위해 그룹별 시간 분산 실행을 적용할 수 있습니다.
- Scheduler / Inquiry / StoreClient / Form / Repository 책임을 느슨하게 분리할 수 있습니다.
- 외부 연동 실패에 대해 재시도(Backoff) 구조를 고려한 복원력 있는 흐름을 설계할 수 있습니다.
- 운영자가 결과를 확인할 수 있는 최소한의 운영 확인 API를 배치 구조에 포함할 수 있습니다.

<br/>

## 4. Key Design Points

- **서버 그룹 분산 실행**
  - 이중화 환경에서 동일 스케줄이 동시에 실행되는 위험을 줄이기 위해 그룹별 시간 분산 실행 적용

- **Scheduler 기반 배치 처리**
  - Cron 기반 주기 실행
  - 실행 흐름과 결과를 확인할 수 있는 구조 반영

- **책임 분리 설계**
  - `Scheduler` → 실행 트리거 및 그룹 분기
  - `InquiryService` → 대상 반복 처리 및 오케스트레이션
  - `StoreClient` → 외부 호출 책임
  - `FormService` → 표준 포맷 정규화
  - `Repository` → 저장 및 조회

- **재시도(Backoff) 구조**
  - 외부 연동 실패를 고려해 재시도 흐름 반영

- **Lock 적용**
  - 포트폴리오에서는 InMemory Lock 적용
  - 실무 환경에서는 Redis 또는 DB Lock으로 확장 가능한 구조 고려

<br/>

## 5. Supported Flow

1. Scheduler가 정해진 시간에 실행됩니다.
2. 실행 그룹(Group A/B)에 따라 작업이 분산됩니다.
3. InquiryService가 조회 대상 목록을 순회합니다.
4. StoreClient가 외부 스토어 데이터를 조회합니다. (Mock)
5. FormService가 응답 데이터를 내부 표준 형식으로 정규화합니다.
6. Repository가 결과를 저장합니다.
7. 운영 확인용 API를 통해 최근 결과나 단건 데이터를 조회할 수 있습니다.

<br/>

## 6. Verification Summary

| Scenario | Expected Behavior | Result | Evidence |
|---|---|---|---|
| Group-based distributed scheduling | Group A/B가 서로 다른 시간대에 실행 | Pass | `docs/test-report.md` |
| Scheduler to Inquiry flow | Scheduler → InquiryService 흐름 정상 동작 | Pass | `docs/test-report.md` |
| External client integration | StoreClient 책임 분리 및 외부 조회 흐름 확인 | Pass | `docs/test-report.md` |
| Normalization and repository save | FormService 정규화 및 Repository 저장 흐름 확인 | Pass | `docs/test-report.md` |
| Retry with backoff | 외부 실패 시 재시도 구조 동작 | Pass | `docs/test-report.md` |
| Lock-based duplicate prevention | InMemory Lock 기반 중복 실행 방지 흐름 확인 | Pass | `docs/test-report.md` |
| Operational visibility APIs | 수동 실행 / 최근 결과 조회 / 단건 조회 가능 | Pass | `docs/test-report.md` |

<br/>

## 7. Job Execution Strategy

### Group-based Distributed Scheduling

이중화 환경에서 스케줄 작업 중복 실행을 피하기 위해  
서버 그룹별 시간 분산 실행 구조를 적용했습니다.

- Group A: 0 / 6 / 12 / 18 시 실행
- Group B: 3 / 9 / 15 / 21 시 실행

이 방식은 모든 서버가 동일한 시점에 같은 배치를 동시에 수행하는 문제를 줄이는 데 목적이 있습니다.

### Lock Consideration

포트폴리오에서는 InMemory Lock을 적용해 기본적인 중복 실행 방지 구조를 재현했습니다.

다만 실제 운영 환경에서는 단일 인스턴스 메모리 락만으로는 한계가 있으므로  
다음과 같은 확장 방향을 고려할 수 있습니다.

- Redis 기반 distributed lock
- DB lock 또는 scheduler ownership 관리
- 실행 상태 기록 기반 제어

<br/>

## 8. Retry and Recovery

외부 연동은 항상 성공한다고 가정할 수 없으므로  
본 프로젝트에서는 실패 시 재시도 가능한 흐름을 고려했습니다.

### Retry Direction
- 외부 조회 실패 시 즉시 종료하지 않고 일정 기준 내 재시도
- Backoff 개념을 적용해 연속 실패 부담 완화
- 재시도 이후에도 실패한 경우 로그 또는 상태 확인 대상로 남길 수 있는 구조 고려

### Recovery Consideration
- 실패 건과 성공 건을 분리해서 볼 수 있는 흐름 필요
- 재실행 또는 수동 실행 시 운영자가 확인 가능한 구조 필요
- 운영 확인 API를 통해 최소한의 후속 점검 가능

<br/>

## 9. Operational Visibility

배치나 스케줄러는 “돌고 있는지”와 “무엇이 처리됐는지”를  
운영자가 확인할 수 있어야 한다고 판단했습니다.

따라서 본 프로젝트는 최소한의 운영 확인용 API를 제공합니다.

- `POST /ops/reviews/fetch`
  - 수동 1회 실행 트리거

- `GET /ops/reviews?limit=50`
  - 저장된 리뷰 최근 N개 조회

- `GET /ops/reviews/inquiry?reviewId=...&platform=android|ios`
  - 저장 데이터 기준 단건 조회

이 API들은 실제 운영 환경의 전체 Admin 도구를 대체하는 것은 아니지만,  
배치 결과를 “보이게 만드는 것”이 중요하다는 관점을 반영하고 있습니다.

<br/>

## 10. Security and Constraints

- 실제 스토어 API 인증/키/계정 파일은 포함하지 않습니다.
- 외부 연동 파이프라인 구조는 **Mock Client**로 재현했습니다.
- 본 프로젝트는 실제 외부 스토어 연동 완성본이 아니라,  
  운영형 배치 구조와 책임 분리를 설명하기 위한 포트폴리오입니다.

<br/>

## 11. Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Scheduling**
- **REST Controller**
- **Logback**
- **InMemory Repository**
- **Mock Client**

> Repository 및 외부 연동 영역은 실무 환경에서 RDBMS / Redis 또는  
> 실연동 구현체로 확장 가능하도록 책임을 분리해 설계했으며,  
> 포트폴리오에서는 Mock 기반으로 구조를 재현했습니다.

<br/>

## 12. Test and Verification

본 프로젝트에서 확인한 주요 시나리오는 다음과 같습니다.

- 그룹별 시간 분산 실행 구조 확인
- Scheduler → InquiryService → StoreClient → FormService → Repository 흐름 확인
- 외부 연동 실패 시 재시도 흐름 확인
- InMemory Lock 기반 중복 실행 방지 구조 확인
- 운영 확인 API를 통한 결과 조회 가능 여부 확인

상세 검증 결과는 아래 문서를 참고할 수 있습니다.

- [Test Report](docs/test-report.md)

<br/>

## 13. Quick Links

- [Test Report](docs/test-report.md)
- [Design Notes](docs/design-notes.md)
- [Troubleshooting Notes](docs/troubleshooting.md)

<br/>

## 14. Future Improvements

- Redis 또는 DB 기반 distributed lock 적용
- 배치 실행 상태 이력 저장
- 실패 건 재처리 큐 또는 보류 처리 구조 추가
- traceId 또는 executionId 기반 로그 추적 강화
- 운영 메트릭 및 모니터링 연계
- 실제 외부 스토어 연동 구현체 확장

<br/>

## 15. Conclusion

이 프로젝트는 스케줄러 기반 배치 작업에서  
실행 분산, 외부 연동 책임 분리, 재시도, 중복 실행 방지, 운영 가시성을  
어떻게 구조화할 수 있는지 설명하는 포트폴리오 프로젝트입니다.

단순 스케줄 샘플이 아니라,  
운영 환경에서 중요한 실행 제어와 책임 분리를 중심으로  
배치 구조를 재구성했다는 점에 의미가 있습니다.