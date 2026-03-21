# Troubleshooting Notes

## 1. Overview

`ops-scheduler-batch-jobs`는  
스케줄러 기반 배치 작업에서 실행 분산, 재시도, 외부 연동 책임 분리,  
중복 실행 방지, 운영 가시성을 고려한 구조를 정리한 프로젝트입니다.

이 문서는 프로젝트 구현 및 검증 과정에서 중요하게 본 이슈와  
그에 대한 대응 방향을 정리한 문서입니다.

<br/>

## 2. Duplicate Execution in a Redundant Server Environment

### Problem
이중화 환경에서는 동일한 스케줄이 여러 서버에서 동시에 실행될 수 있습니다.

배치 작업이 중복 실행되면 다음과 같은 문제가 발생할 수 있습니다.

- 동일 데이터 중복 저장
- 외부 시스템 중복 호출
- 운영 로그 중복 기록
- 처리 결과 혼선

### Why It Matters
배치 작업은 요청 기반 API와 달리  
한 번 실행될 때 처리 범위가 넓을 수 있으므로  
중복 실행은 운영 부담이 커질 수 있습니다.

### Response
본 프로젝트에서는 다음 두 가지 방향을 반영했습니다.

- 서버 그룹별 시간 분산 실행
- InMemory Lock을 통한 기본적인 중복 실행 제어

### Limitation
포트폴리오의 InMemory Lock은 단일 인스턴스 기준에서는 단순하지만,  
실제 분산 환경에서는 다음과 같은 보완이 필요합니다.

- Redis distributed lock
- DB lock
- execution ownership 관리
- 배치 실행 상태 테이블 기반 제어

<br/>

## 3. External Integration Failure

### Problem
외부 스토어 또는 외부 시스템 연동은  
응답 지연, 일시 실패, 네트워크 문제 등이 발생할 수 있습니다.

### Why It Matters
배치 구조에서 외부 호출 실패를 즉시 전체 실패로 처리하면  
일시적인 문제에도 전체 작업 안정성이 낮아질 수 있습니다.

### Response
본 프로젝트에서는 외부 연동 책임을 StoreClient에 분리하고,  
재시도(Backoff) 구조를 고려한 흐름을 반영했습니다.

- 외부 연동 실패를 별도 책임 영역으로 격리
- 재시도 흐름 반영
- 전체 구조가 외부 호출 실패에 직접 종속되지 않도록 설계

### Future Improvement
운영 환경을 더 반영하려면 다음과 같은 보완이 가능합니다.

- timeout 세분화
- retryable / non-retryable 구분
- provider 또는 store별 오류 코드 매핑
- circuit breaker 성격의 보호 구조 검토

<br/>

## 4. Tight Coupling Between Scheduler and Business Logic

### Problem
Scheduler 내부에 비즈니스 처리, 외부 연동, 데이터 가공, 저장을 모두 넣으면  
구조가 빠르게 복잡해지고 수정 범위가 커질 수 있습니다.

### Why It Matters
배치 작업은 시간이 지나면서  
조회 대상, 외부 시스템, 저장 방식, 재처리 방식이 바뀔 가능성이 높습니다.

### Response
본 프로젝트에서는 책임을 다음과 같이 나눴습니다.

- Scheduler: 실행 트리거
- InquiryService: 오케스트레이션
- StoreClient: 외부 호출
- FormService: 정규화
- Repository: 저장/조회

이를 통해 실행 제어와 비즈니스 처리를 느슨하게 분리했습니다.

<br/>

## 5. Lack of Operational Visibility

### Problem
배치 작업은 동작하더라도  
운영자가 결과를 확인할 수 없으면 실제 운영에서는 다루기 어렵습니다.

### Why It Matters
운영 환경에서는 아래가 중요합니다.

- 실행됐는지
- 무엇이 저장됐는지
- 특정 대상이 정상 처리됐는지
- 수동 재실행이 가능한지

### Response
본 프로젝트에서는 최소한의 운영 확인용 API를 포함했습니다.

- 수동 실행 트리거
- 최근 결과 조회
- 단건 조회

이를 통해 배치 결과를 코드 밖에서도 확인할 수 있도록 했습니다.

### Limitation
실제 운영 환경을 기준으로 하면  
추가적으로 다음이 필요할 수 있습니다.

- 실행 이력 저장
- 실행 상태 대시보드
- 실패 건 목록 및 재처리 기능
- traceId / executionId 기반 추적

<br/>

## 6. Retry Without Clear Failure Classification

### Problem
재시도는 복원력을 높일 수 있지만,  
모든 실패에 동일하게 적용하면 비효율적이거나 오히려 문제를 키울 수 있습니다.

### Why It Matters
일시적인 외부 지연에는 재시도가 의미 있지만,  
요청 데이터 자체가 잘못된 경우에는 반복 호출이 무의미합니다.

### Response Direction
본 프로젝트는 기본적인 Backoff 재시도 구조를 반영했지만,  
실제 운영 수준에서는 다음과 같은 구분이 필요하다고 보았습니다.

- retryable failure
  - 일시적 네트워크 실패
  - 외부 응답 지연
  - 일시적 연결 오류

- non-retryable failure
  - 잘못된 입력값
  - 인증 실패
  - 구조적으로 잘못된 요청

### Future Improvement
- 실패 유형 분류
- 재시도 횟수 정책 고도화
- dead-letter 성격의 보류 처리
- 실패 건 후속 재처리 구조

<br/>

## 7. InMemory Repository and Lock Limitations

### Problem
포트폴리오에서는 InMemory Repository와 InMemory Lock을 사용했지만,  
이 구조는 실제 운영 환경과 동일하지 않습니다.

### Why It Matters
단일 프로세스 메모리 기반 구조는  
재시작, 다중 인스턴스, 장기 상태 보관에 한계가 있습니다.

### Response
본 프로젝트에서는 의도적으로 InMemory 구조를 사용해  
핵심 흐름과 책임 분리에 집중했습니다.

즉, 저장소와 락 구현체 자체보다  
아래를 보여주는 데 목적이 있었습니다.

- 실행 제어 필요성
- 외부 연동 책임 분리
- 정규화와 저장 흐름 분리
- 운영 확인 수단 필요성

### Future Improvement
- RDBMS 기반 저장 구조 적용
- Redis 기반 lock 적용
- 실행 상태 영속화
- 다중 인스턴스 환경 검증

<br/>

## 8. What This Project Focuses On

이 프로젝트는 배치 프레임워크 전체를 완성하는 것이 목적이 아닙니다.  
대신 다음 설계 포인트를 명확히 보여주는 데 초점을 두고 있습니다.

- 스케줄러 기반 배치 실행 구조
- 그룹별 시간 분산 실행
- 중복 실행 방지 고려
- 외부 연동과 정규화 책임 분리
- 재시도와 운영 가시성 반영

즉, 이 문서에서 다루는 troubleshooting 포인트들은  
단순 예외 처리 목록이 아니라,  
운영형 배치 구조에서 실제로 중요할 수 있는 이슈를 기준으로 정리한 것입니다.

포트폴리오에서는 InMemory 기반으로 단순화했지만, 실제 운영 환경으로 확장 가능한 방향을 함께 설명하는 데 목적이 있습니다.

<br/>

## 9. Summary

본 프로젝트에서 중요하게 본 이슈는 다음과 같습니다.

- 이중화 환경에서의 중복 실행 위험
- 외부 연동 실패와 재시도 필요성
- Scheduler와 비즈니스 로직의 결합 문제
- 운영 가시성 부족 문제
- InMemory 구조의 한계

이를 통해 `ops-scheduler-batch-jobs`는  
단순한 스케줄 샘플이 아니라,  
운영 환경에서의 실행 제어와 배치 구조를 고려한 포트폴리오로 구성되었습니다.
