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
