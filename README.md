# Hospital Reservation System

병원 예약과 관리자 예약 확정 업무를 지원하는 포트폴리오 프로젝트입니다.

## Development database

Docker Compose로 PostgreSQL을 실행합니다.

```bash
cp .env.example .env
docker compose up -d
docker compose ps
```

기본 접속 정보:

- Host: `localhost`
- Port: `5432`
- Database: `hospital_reservation`
- Username: `hospital_admin`
- Password: `local_dev_password`

종료는 `docker compose down`을 사용합니다. 데이터를 포함해 초기화하려면 `docker compose down -v`를 사용합니다.

## Admin login

관리자 페이지는 `http://localhost:8080/admin/`이며 로그인 후 접근할 수 있습니다.
관리자 계정은 `admins` 테이블에서 관리합니다. 테이블이 비어 있는 최초 실행에만 아래 환경 변수로 최고 관리자 계정이 생성됩니다.

```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD='안전한-비밀번호'
export ADMIN_NAME='최고 관리자'
./gradlew bootRun
```

로컬 초기 기본값은 `admin` / `change-me`입니다. 운영 환경에서는 최초 실행 전 반드시 변경하세요. 이후 로그인은 환경 변수가 아니라 `admins` 테이블에 저장된 BCrypt 비밀번호를 사용합니다.
