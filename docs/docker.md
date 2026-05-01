# Docker 실행 가이드

## 준비

루트의 `.env.example` 파일을 `.env`로 복사한 뒤, 파일의 값을 환경에 맞게 수정한다.

- `SPOTIFY_CLIENT_ID`
- `SPOTIFY_CLIENT_SECRET`
- `JWT_SECRET`
- `ORACLE_PASSWORD`
- `DB_PASSWORD`

## 실행

```bash
docker compose --env-file .env up -d --build
```

서비스 주소:

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Backend health: http://localhost:8080/api/health
- Oracle: localhost:1521/XEPDB1

## 초기 DB

Oracle 컨테이너 최초 생성 시 `docker/oracle/init/001_init_schema.sh`가 `db/schema/001_init_schema.sql`을 실행한다.

이미 생성된 볼륨을 재사용하면 초기화 SQL은 다시 실행되지 않는다. 스키마를 처음부터 다시 만들려면 Docker 볼륨을 삭제한 뒤 실행한다.

```bash
docker compose --env-file .env down -v
docker compose --env-file .env up -d --build
```
