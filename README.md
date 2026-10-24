# 병원 예약 시스템


## 프로젝트 소개

- 환자의 진료 예약과 관리자의 예약 관리를 지원하는 시스템
- 의료 분야에 대한 관심과 Java/Spring 학습 목적으로 제작했습니다.
- 예약 상태 규칙, 취소 후 재예약, 예외 응답과 테스트를 통한 안정성 개선

## 기술 스택

- Java 21, Spring Boot
- Spring Data JPA, PostgreSQL
- Spring Security, Thymeleaf
- Gradle, Docker Compose
- JUnit 5, MockMvc, H2

## 주요 기능

- 환자의 진료 예약 신청
- 진료과 및 의사 조회
- 관리자 로그인 및 접근 권한 제어
- 관리자 예약 목록 조회와 검색·정렬
- 예약 상태 변경 및 관리자 메모 관리

## 실행 방법

**' Windows PowerShell 기준이며, 모든 명령은 프로젝트 루트에서 실행합니다. '**

1. 준비 환경: Java 21, Docker Desktop
2. PostgreSQL 실행: `docker compose up -d`
3. 최초 서버 실행: `.\gradlew.bat bootRun` -> 테이블 생성 확인 후 Ctrl+C
4. 중복 방지 SQL 적용
- `Get-Content -Raw -Encoding UTF8 .\scripts\migrate-reservation-slot-uniqueness.sql | docker compose exec -T postgres psql -U hospital_admin -d hospital_reservation -v ON_ERROR_STOP=1`
- 최초 실행 시 JPA가 테이블을 생성합니다. 취소된 예약을 제외하는 예약 시간 중복 방지 인덱스는 SQL 스크립트로 별도 적용해야 합니다. 스크립트 적용 후 서비스를 사용합니다.
5. 서버 재실행: `.\gradlew.bat bootRun`

### 접속 주소

기본 포트 8080 기준입니다.

- 고객 홈페이지: http://localhost:8080
- 관리자 로그인: http://localhost:8080/admin/login
- 관리자 예약 관리: http://localhost:8080/admin/reservations

### 로컬 개발용 초기 관리자 계정

별도 환경변수 설정 없이 처음 실행한 경우:

- 아이디: `admin`
- 비밀번호: `change-me`

관리자 데이터가 없을 때만 초기 계정이 생성됩니다.
`ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_NAME` 환경변수로
최초 생성할 계정 정보를 지정할 수 있습니다.

이미 생성된 계정은 환경변수를 변경해도 자동으로 갱신되지 않습니다.
기본 계정 정보는 로컬 개발용이며, 외부 공개 환경에서는 사용하지 않습니다.

## 주요 개선 사항

### 예약 중복 방지 개선
1. 문제: 취소된 예약이 남아 있으면 같은 의사, 날짜, 시간으로 다시 예약할 수 없다.
2. 개선 방법: Java의 중복 조회에서 CANCELLED를 제외하고 DB도 취소되지 않은 예약에만 고유 인덱스 적용
3. 수정 이유: Java 조회는 중복 요청에 미리 오류를 안내하고 DB 인덱스는 동시에 들어온 요청으로 중복 데이터가 저장되는 것을 막는다.
4. 검증 범위: 취소 후 동일 시간 재예약은 수동으로 확인, 기존 DB의 스크립트 재실행과 새 DB의 최초 적용 확인, 동시 요청 테스트는 아직 진행되지 않음

### 예약 상태 변경 개선 사례
1. 문제: 상태 변경에 제약을 두지 않으면 신청 상태에서 바로 완료하거나, 취소된 예약을 다시 확정할 수 있었다.
2. 개선 방법: Reservation.changeStatus()에서 현재 상태에 따라 변경 가능한 상태를 제한
3. 예외 처리: 허용되지 않은 전환은 ReservationException을 발생시키고 예외 처리기가 HTTP 409 Conflict로 변환하도록 함
4. 검증: 허용, 금지 전환 단위 테스트와 잘못된 요청의 HTTP 응답 테스트 작성, 실패 시 예약 상태가 유지되는 것 확인

### 예약 상태 전환 규칙

| 현재 상태 | 변경 가능한 상태 |
|---|---|
| `REQUESTED` 신청 | `CONFIRMED` 확정, `CANCELLED` 취소 |
| `CONFIRMED` 확정 | `COMPLETED` 진료 완료, `NO_SHOW` 미방문, `CANCELLED` 취소 |
| `COMPLETED` 진료 완료 | 변경 불가 |
| `CANCELLED` 취소 | 변경 불가 |
| `NO_SHOW` 미방문 | 변경 불가 |

같은 상태로 다시 변경하는 요청도 허용하지 않습니다.

## 테스트

전체 테스트 실행:

```powershell
.\gradlew.bat test
```

### 자동 테스트로 확인한 내용

- 예약 상태의 허용·금지 전환과 실패 시 기존 상태 유지
- 잘못된 상태 변경 요청에 대한 HTTP 409 응답
- 취소된 예약을 제외한 중복 예약 조회

### 수동으로 확인한 내용

- 동일한 의사·날짜·시간의 중복 예약 차단
- 예약 취소 후 동일 시간 재예약
- 기존 DB에서 SQL 스크립트 재실행
- 새 DB에서 테이블 생성 및 조건부 고유 인덱스 적용

### 아직 검증하지 않은 내용

- 동시 예약 요청에 대한 통합 테스트
- PostgreSQL 기반 자동 테스트

현재 DB를 사용하는 자동 테스트는 H2 기반이며,
PostgreSQL의 조건부 고유 인덱스는 수동으로 적용 및 정의를 확인했습니다.

    2026-10-24 기준 `.\gradlew.bat clean build` 성공: 테스트 33개 통과, 실패·오류·건너뜀 0개.
