# Test Report

## 1. Overview

본 문서는 `ops-scheduler-batch-jobs` 프로젝트에서 검증한 주요 시나리오를 정리한 문서입니다.

이 프로젝트는 스케줄러 기반 배치 작업에서  
실행 분산, 외부 연동 책임 분리, 재시도, 중복 실행 방지, 운영 확인 가능성을  
어떻게 구조화했는지 보여주는 데 목적이 있습니다.

검증 목적은 다음과 같습니다.

- 그룹별 시간 분산 실행 구조가 의도대로 동작하는지 확인
- Scheduler → InquiryService → StoreClient → FormService → Repository 흐름이 정상 동작하는지 확인
- 외부 연동 실패 시 재시도 구조가 동작하는지 확인
- Lock 기반 중복 실행 방지 흐름을 확인
- 운영 확인 API를 통해 결과 조회가 가능한지 확인

---

## 2. Test Environment

- Java 17
- Spring Boot 3.x
- Spring Scheduling
- Mock Store Client
- InMemory Repository
- Local Profile

---

## 3. Test Scenarios

### 3.1 Group-based Distributed Scheduling

**Scenario**  
서버 그룹에 따라 배치 실행 시점을 분산했을 때,  
동일한 시점에 모든 그룹이 동시에 실행되지 않는 구조인지 확인

**Expected**
- Group A / Group B가 서로 다른 시간대에 실행됨
- 동일 배치의 시간 충돌 가능성이 줄어듦

**Result**
- 정상 동작 확인

---

### 3.2 Scheduler to Inquiry Flow

**Scenario**  
Scheduler가 실행되었을 때 InquiryService가 호출되고,  
조회 대상 루프가 정상적으로 진행되는지 확인

**Expected**
- Scheduler가 실행 트리거 역할 수행
- InquiryService가 대상 반복 처리 흐름을 담당
- 상위 실행 흐름이 분리된 책임 구조로 동작

**Result**
- 정상 동작 확인

---

### 3.3 External Client Integration Flow

**Scenario**  
InquiryService가 외부 조회 책임을 StoreClient에 위임하고,  
Mock 응답을 기준으로 후속 처리가 이어지는지 확인

**Expected**
- 외부 연동 책임이 StoreClient에 한정됨
- 상위 계층은 전체 흐름만 조정함

**Result**
- 정상 동작 확인

---

### 3.4 Normalization and Repository Save

**Scenario**  
외부 응답 데이터가 FormService를 통해 내부 표준 형식으로 정규화되고,  
Repository에 저장되는지 확인

**Expected**
- FormService가 응답 데이터를 정규화
- Repository가 저장 및 조회 책임 수행
- 저장 후 운영 확인 API를 통해 조회 가능

**Result**
- 정상 동작 확인

---

### 3.5 Retry with Backoff

**Scenario**  
외부 연동 실패가 발생했을 때 즉시 종료하지 않고  
재시도 흐름이 적용되는지 확인

**Expected**
- 실패 시 재시도 흐름 진입
- 재시도 정책이 전체 배치 구조 안에서 동작
- 일시적 실패에 대해 복원력 있는 흐름 유지

**Result**
- 정상 동작 확인

---

### 3.6 Lock-based Duplicate Execution Prevention

**Scenario**  
동일 배치가 중복 실행될 가능성이 있는 상황에서  
Lock이 중복 실행 방지 흐름에 반영되는지 확인

**Expected**
- Lock 획득 여부에 따라 중복 실행 제어 가능
- 동일 작업이 동시에 반복 실행되지 않음

**Result**
- 기본 구조 정상 동작 확인

---

### 3.7 Operational Visibility APIs

**Scenario**  
운영 확인용 API를 통해 수동 실행, 최근 결과 조회, 단건 조회가 가능한지 확인

**Expected**
- `POST /ops/reviews/fetch` 수동 실행 가능
- `GET /ops/reviews?limit=50` 최근 결과 조회 가능
- `GET /ops/reviews/inquiry?...` 단건 조회 가능

**Result**
- 정상 동작 확인

---

## 4. Verification Summary

본 프로젝트에서는 다음 항목을 확인했습니다.

- 그룹별 시간 분산 실행 구조
- Scheduler 기반 배치 실행 흐름
- 외부 연동 책임 분리
- 응답 데이터 정규화 및 저장 흐름
- 재시도(Backoff) 구조
- Lock 기반 중복 실행 방지 구조
- 운영 확인 API를 통한 결과 가시성

이를 통해 `ops-scheduler-batch-jobs`는  
단순한 스케줄러 샘플이 아니라,  
운영 환경에서 중요한 실행 제어와 배치 구조를 고려한 포트폴리오로 동작함을 확인했습니다.

---

## 5. Notes

- 본 프로젝트는 실제 외부 스토어 연동이 아닌 Mock 기반 구조를 기준으로 검증했습니다.
- Lock은 포트폴리오용 InMemory 구조를 사용했습니다.
- 검증 목적은 실제 운영 환경의 완전한 재현보다는  
  스케줄 실행 제어와 책임 분리 구조를 설명하는 데 있습니다.
