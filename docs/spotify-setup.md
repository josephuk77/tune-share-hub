# Spotify 개발 설정 가이드

## 현재 인증 방식

이 프로젝트는 Spotify 소셜 로그인이나 사용자 OAuth를 사용하지 않는다.

백엔드가 `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET`으로 Spotify Client Credentials Flow를 사용해 곡 검색 API를 호출한다. 그래서 Redirect URI는 실제 서비스 코드에서 사용하지 않지만, Spotify Developer Dashboard에서 앱을 만들 때 필수 입력값이라 등록해 둔다.

## Spotify Developer Dashboard

로컬 개발용 Redirect URI:

```text
http://127.0.0.1:5173/callback
```

주의할 점:

- `localhost` 대신 `127.0.0.1`을 사용한다.
- 배포 환경에서는 실제 HTTPS 도메인의 callback URL을 추가한다.
- 현재 프로젝트에는 `/callback` 화면이나 Spotify 로그인 흐름이 없으므로, 이 값은 Dashboard 생성 요구사항을 만족하기 위한 로컬 개발용 값이다.

참고:

- https://developer.spotify.com/documentation/web-api/concepts/redirect_uri
- https://developer.spotify.com/documentation/web-api/tutorials/client-credentials-flow

## 백엔드 환경변수

백엔드 실행 환경에 아래 값을 설정한다.

```env
SPOTIFY_CLIENT_ID=Spotify Dashboard의 Client ID
SPOTIFY_CLIENT_SECRET=Spotify Dashboard의 Client Secret
```

실제 값은 로컬 실행 환경에만 둔다. `SPOTIFY_CLIENT_SECRET`은 절대 커밋하지 않는다.

Spring Boot는 기본적으로 `.env` 파일을 자동으로 읽지 않는다. 로컬에서 `.env` 값을 쓰려면 운영체제 환경변수, IntelliJ Run Configuration, EnvFile 플러그인 같은 방식으로 백엔드 프로세스에 환경변수를 주입해야 한다.

## 프론트엔드 환경변수

기본값:

```env
VITE_API_BASE_URL=
```

비워두는 경우:

- Vite 개발 서버를 사용할 때 `/api` 요청은 `frontend/vite.config.js`의 proxy를 통해 `http://localhost:8080`으로 전달된다.
- Docker Compose 또는 별도 리버스 프록시 환경에서는 `/api` 요청을 백엔드로 프록시하도록 구성한다.

값을 넣는 경우:

```env
VITE_API_BASE_URL=http://localhost:8080
```

끝에 슬래시(`/`)를 붙이지 않는다.

프론트엔드와 백엔드가 프록시 없이 서로 다른 origin에서 직접 통신해야 할 때만 넣는다. 이 경우 백엔드에 CORS 설정이 필요하다.

## 확인 방법

백엔드를 실행한 뒤 아래 API가 응답하는지 확인한다.

```text
GET /api/spotify/search/tracks?q=iu&page=0&size=5
```

Spotify 설정이 비어 있거나 잘못되면 Spotify API 오류가 반환된다.
