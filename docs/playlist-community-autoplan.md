# 플레이리스트 공유 커뮤니티 Autoplan

작성일: 2026-04-25

## 결론

원안은 CRUD, 커뮤니티, Spotify 검색을 잘 묶은 세미프로젝트 주제다. 다만 핵심 차별화로 잡은 `Audio Features`, `Recommendations`, `Genre Seeds`, `preview_url` 의존 기능은 신규 Spotify 앱 기준으로 위험하다.

Spotify가 2024-11-27 발표한 Web API 변경에 따르면 신규 앱 또는 개발 모드 앱은 Recommendations, Audio Features, Audio Analysis, Genre Seed 기반 추천, 일부 30초 preview URL 기능에 접근하지 못할 수 있다. 따라서 이 프로젝트는 Spotify를 "곡 검색/조회 메타데이터 제공자"로 쓰고, 추천과 분위기 분석은 우리 DB의 태그, 사용자 행동, 아티스트/장르 메타데이터, 플레이리스트 구성 기반으로 구현해야 한다.

## 검증한 외부 전제

- Spotify Client Credentials Flow는 사용자 로그인 없이 서버 간 인증으로 사용할 수 있다. 단, 사용자 개인 데이터는 접근하지 못한다.
- Search for Item, Get Track, Get Artist, Get Artist's Albums 같은 조회 계열은 서비스 목적에 맞는다.
- Get Track 응답의 `preview_url`은 nullable이고 deprecated로 표시된다. 30초 미리듣기는 있으면 보조로만 보여주고, 핵심 기능으로 잡으면 안 된다.
- Spotify 정책상 콘텐츠 다운로드 금지, 앨범 아트 원형 보존, Spotify 링크/로고를 통한 출처 표기가 필요하다.
- 신규 앱은 Recommendations, Audio Features, Audio Analysis, Get Available Genre Seeds 접근이 제한될 수 있다.

참고:
- https://developer.spotify.com/documentation/web-api/tutorials/client-credentials-flow
- https://developer.spotify.com/blog/2024-11-27-changes-to-the-web-api
- https://developer.spotify.com/documentation/web-api/reference/search
- https://developer.spotify.com/documentation/web-api/reference/get-track
- https://developer.spotify.com/documentation/web-api/reference/get-an-artist

## Premise Gate

아래 전제는 사용자 확인이 필요하다.

1. 이 프로젝트는 2024-11-27 이후 생성한 신규 Spotify app으로 개발한다.
2. Spotify 소셜 로그인은 끝까지 쓰지 않는다.
3. 발표/평가에서 "실제 Spotify 추천 알고리즘 연동"보다 "우리 서비스 내부 추천 로직"을 차별화로 설명해도 괜찮다.

추천 결정: 위 3개 전제를 받아들이고 계획을 수정한다. 원안 그대로 가면 2차 기능 상당수가 API 권한 문제로 실패할 가능성이 높다.

## CEO Review

### 문제 재정의

원문 문제:
Spotify에서 곡을 검색하고, 우리 사이트에서 플레이리스트를 만들어 공유하는 음악 커뮤니티.

수정 문제:
사용자가 Spotify 카탈로그에서 곡을 가져와 "내 취향의 플레이리스트 설명, 태그, 코멘트, 복사, 반응"을 남기고 재사용할 수 있는 커뮤니티.

핵심 제품 가치는 Spotify의 추천 엔진을 복제하는 것이 아니다. "사람이 만든 맥락"을 DB에 쌓아서 다시 찾고 복사하고 확산시키는 것이다.

### 12개월 이상적인 상태

```
현재 원안
  Spotify 검색 + CRUD + 댓글/좋아요

수정된 1차 제품
  자체 계정 + 플레이리스트 빌더 + 공개 커뮤니티 + 복사/반응 + 내부 검색

12개월 이상적 상태
  유저 행동 기반 추천 + 태그/아티스트/공통곡 기반 유사 플레이리스트 + 음악 상황별 탐색
```

### Scope Decisions

Accepted:
- 자체 회원가입, 로그인, 로그아웃
- HttpSession 기반 인증
- 플레이리스트 CRUD
- Spotify 곡 검색
- 곡 추가, 삭제, 순서 변경
- 공개 플레이리스트 목록/상세
- 댓글 CRUD
- 좋아요 토글
- 태그/제목/설명/작성자 검색
- 플레이리스트 복사
- 내부 유사 플레이리스트 추천
- 한줄 리뷰

Changed:
- 분위기 자동 분석은 Spotify Audio Features 기반에서 사용자 태그 + 아티스트 장르 + 플레이리스트 설명 키워드 + 좋아요/복사 행동 기반으로 변경
- 자동 추천 곡은 Spotify Recommendations 기반에서 "내부 DB 유사 플레이리스트의 공통곡/공통아티스트 추천"으로 변경
- 장르 기반 시작하기는 Spotify Genre Seeds 기반에서 서비스 자체 고정 장르/상황 태그 기반 템플릿으로 변경
- 미리듣기는 `preview_url`이 있을 때만 보조 제공

Deferred:
- 알림 기능
- 대댓글 2단계 이상
- 실시간 채팅
- Spotify 플레이리스트 생성/동기화
- ML 기반 추천

Rejected:
- Spotify Audio Features 평균값 기반 mood score
- Spotify Recommendations endpoint 기반 자동 추천
- Spotify Genre Seeds endpoint 기반 시작하기

## Design Review

### UX 방향

첫 화면은 랜딩 페이지가 아니라 공개 플레이리스트 탐색 화면이어야 한다. 사용자는 바로 카드 목록, 검색, 태그 필터, 정렬을 보고 들어와야 한다.

주요 화면:
- 홈/탐색: 공개 플레이리스트 카드 목록
- 플레이리스트 상세: 대표 이미지, 제목, 설명, 작성자, 수록곡, 댓글, 좋아요, 복사
- 플레이리스트 생성/수정: 좌측 메타데이터, 우측 곡 검색/선택 또는 모바일에서는 단계형 흐름
- 마이페이지: 내가 만든 목록, 댓글, 좋아요한 목록
- 아티스트 검색/상세: 대표곡 빠른 추가, 단 deprecated 가능성이 있어 보조 기능

### 카드 정보 우선순위

플레이리스트 카드:
1. 대표 앨범 이미지 2~4장
2. 제목
3. 작성자
4. 자동/수동 분위기 배지
5. 좋아요 수, 댓글 수, 조회수
6. 태그

### 필수 상태

- Spotify 검색 로딩
- Spotify API 실패
- 검색 결과 없음
- `preview_url` 없음
- 비로그인 좋아요/댓글 시 로그인 유도
- 비공개 플레이리스트 접근 차단
- 삭제 확인 모달
- 복사 성공 후 새 플레이리스트 이동

### 접근성/반응형

- 앨범 이미지는 자르지 않고 원형 보존
- 모든 이미지에 곡명/앨범명 기반 alt 제공
- 버튼 최소 터치 영역 44px
- 키보드로 곡 검색 결과 선택 가능
- 모바일 생성 화면은 검색 결과와 현재 담은 곡 목록이 겹치지 않게 탭 또는 하단 시트 사용

## Engineering Review

### 권장 아키텍처

```
React
  api client
  pages
  components

Spring Boot
  controller
  interceptor
  service
  repository
  domain
  spotify client

DB
  users
  playlists
  playlist_tracks
  comments
  playlist_likes
  tags
  playlist_tags
  playlist_copies
  short_reviews
```

### 인증

Spring Security는 사용하지 않는다.

- 로그인 성공 시 `session.setAttribute("LOGIN_USER_ID", userId)`
- 인증 필요 API는 `LoginInterceptor`에서 세션 확인
- 작성자 권한은 각 service에서 `sessionUserId == ownerUserId` 비교
- 프론트는 `/api/session/me`로 현재 로그인 상태 조회

주의: 인터셉터는 인증만 맡고, 소유권 검사는 서비스 계층에서 한다. 컨트롤러에서만 검사하면 재사용 로직에서 권한 누락이 생긴다.

### API 설계

Auth:
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/session/me`

Playlists:
- `GET /api/playlists?keyword=&searchType=&tag=&sort=&page=&size=`
- `POST /api/playlists`
- `GET /api/playlists/{id}`
- `PUT /api/playlists/{id}`
- `DELETE /api/playlists/{id}`
- `POST /api/playlists/{id}/copy`
- `GET /api/playlists/{id}/similar`

Tracks:
- `GET /api/spotify/search/tracks?q=&page=&size=`
- `GET /api/spotify/artists/search?q=`
- `GET /api/spotify/artists/{id}/top-tracks`

Interactions:
- `GET /api/playlists/{id}/comments?page=&size=&sort=`
- `POST /api/playlists/{id}/comments`
- `PUT /api/comments/{id}`
- `DELETE /api/comments/{id}`
- `POST /api/playlists/{id}/likes`
- `DELETE /api/playlists/{id}/likes`
- `POST /api/playlists/{id}/short-reviews`

My Page:
- `GET /api/me/playlists`
- `GET /api/me/comments`
- `GET /api/me/liked-playlists`

### Data Model

`users`
- id
- login_id
- password_hash
- nickname
- created_at

`playlists`
- id
- user_id
- title
- description
- is_public
- origin_playlist_id nullable
- origin_user_nickname nullable
- view_count
- like_count
- comment_count
- copy_count
- mood_label nullable
- created_at
- updated_at

`playlist_tracks`
- id
- playlist_id
- spotify_track_id
- track_name
- artist_names
- album_name
- album_image_url
- spotify_url
- preview_url nullable
- position
- duration_ms nullable

`comments`
- id
- playlist_id
- user_id
- parent_id nullable
- content
- created_at
- updated_at
- deleted_at nullable

`playlist_likes`
- playlist_id
- user_id
- created_at
- unique `(playlist_id, user_id)`

`tags`
- id
- name
- unique `name`

`playlist_tags`
- playlist_id
- tag_id
- unique `(playlist_id, tag_id)`

`short_reviews`
- id
- playlist_id
- user_id
- content
- created_at
- unique `(playlist_id, user_id)`

### Important Indexes

- `playlists(is_public, created_at)`
- `playlists(is_public, like_count)`
- `playlists(is_public, view_count)`
- `playlists(user_id, created_at)`
- `playlist_tracks(playlist_id, position)`
- `playlist_tracks(spotify_track_id)`
- `comments(playlist_id, created_at)`
- `playlist_likes(user_id, created_at)`
- `playlist_tags(tag_id, playlist_id)`

### Spotify Client

서버에서만 Spotify API를 호출한다. Client ID와 Secret은 프론트로 절대 보내지 않는다.

필수 구현:
- Client Credentials token 발급
- expires_in 기준 토큰 캐시
- 401이면 토큰 재발급 후 1회 재시도
- 429면 사용자에게 "잠시 후 다시 시도" 반환
- 검색 결과는 필요한 필드만 DTO로 축소

저장 정책:
- 플레이리스트에 담은 곡의 표시용 메타데이터는 DB에 저장
- 검색 결과 전체 캐시는 선택
- 음원 파일 저장 금지
- preview URL 다운로드 금지

### Mood 분석 대체안

Spotify Audio Features 없이 구현한다.

1차:
- 사용자가 직접 고른 태그를 대표 분위기로 사용
- 제목/설명 키워드로 보조 태그 제안
- 예: "운동", "헬스", "러닝" -> 운동
- 예: "새벽", "감성", "잔잔" -> 감성

2차:
- 같은 곡을 포함한 공개 플레이리스트의 태그를 집계
- 많이 같이 쓰인 태그를 추천
- 아티스트 장르가 사용 가능하면 보조 신호로만 사용

발표 문구:
"Spotify가 제한한 음악 특성 API 대신, 우리 서비스 안에서 사용자가 붙인 맥락과 실제 복사/좋아요 행동을 기반으로 플레이리스트 분위기를 계산했습니다."

### Recommendation 대체안

Spotify Recommendations 없이 구현한다.

추천 곡:
- 현재 플레이리스트와 같은 곡을 포함한 다른 공개 플레이리스트 찾기
- 그 플레이리스트에 있는 곡 중 현재 목록에 없는 곡 추천
- 점수 = 공통곡 수 * 5 + 공통아티스트 수 * 2 + 공통태그 수 * 3 + 원본 플레이리스트 좋아요 보정
- 최대 10곡 반환

유사 플레이리스트:
- 공통곡
- 공통아티스트
- 공통태그
- 좋아요/복사 수

장르로 시작하기:
- DB에 미리 정의한 시작 태그 사용
- 예: 공부, 운동, 드라이브, 새벽, 집중, 파티, 감성, K-pop
- 해당 태그의 인기 공개 플레이리스트에서 자주 등장한 곡을 초안 후보로 제시

## Failure Modes Registry

| 위험 | 사용자에게 보이는 문제 | 대응 |
|---|---|---|
| Spotify 추천 API 접근 불가 | 추천 버튼이 실패함 | 내부 추천으로 대체 |
| Audio Features 접근 불가 | 분위기 분석 불가 | 태그/키워드/내부 행동 기반 분석 |
| preview_url null | 재생 버튼이 없음 | 버튼 숨김, Spotify 링크 제공 |
| Spotify 429 | 검색 실패 | 토큰/검색 API 에러 메시지와 재시도 안내 |
| 세션 만료 | 댓글/좋아요 실패 | 401 반환 후 로그인 모달 |
| 중복 좋아요 | 카운트 불일치 | unique 제약 + 트랜잭션 |
| 곡 순서 동시 수정 | 순서 꼬임 | 수정 API에서 전체 순서 재저장 |
| 삭제 후 참조 | 상세 접근 오류 | FK cascade 또는 서비스 트랜잭션 정리 |
| 조회수 중복 증가 | 새로고침으로 과대 집계 | 세션/IP 기반 짧은 중복 방지 선택 |

## DX Review

### 개발 순서

1. Spring Boot 프로젝트 생성, DB 연결
2. users/auth/session/interceptor
3. Spotify token/search client
4. playlist CRUD + playlist_tracks
5. React 라우팅과 API client
6. 플레이리스트 생성/수정 빌더
7. 공개 목록/상세
8. 댓글/좋아요
9. 마이페이지
10. 복사/한줄리뷰/내부추천

### 테스트 계획

Backend:
- 로그인 성공/실패
- 세션 없는 API 401
- 작성자 아닌 수정/삭제 403
- 플레이리스트 생성 시 곡 순서 저장
- 좋아요 중복 방지
- 삭제 시 댓글/좋아요/곡 정리
- Spotify 401 재시도
- Spotify 429 처리

Frontend:
- 비로그인 좋아요 클릭
- 곡 검색 결과 선택
- 곡 순서 변경
- preview_url 없는 곡 렌더링
- 공개/비공개 전환
- 검색/태그/정렬 조합

Manual QA:
- 신규 회원가입부터 플레이리스트 공개까지
- 다른 사용자로 복사/댓글/좋아요
- 모바일에서 생성 화면 사용

## 최종 우선순위

### 1차 구현

- 회원가입/로그인/로그아웃
- HttpSession 인증 + 인터셉터
- Spotify 곡 검색
- 플레이리스트 CRUD
- 곡 추가/삭제/순서 변경
- 공개 목록/상세
- 댓글 CRUD
- 좋아요
- 태그/검색/정렬

### 2차 구현

- 플레이리스트 복사
- 한줄 리뷰
- 내부 유사 플레이리스트 추천
- 내부 추천 곡 제안
- 태그/키워드 기반 분위기 배지
- 장르/상황 태그로 시작하기

### 3차 구현

- 대댓글 1-depth
- 사용자 행동 기반 추천 고도화
- 인기 태그 랭킹
- 알림성 기능

## Not In Scope

- Spotify 소셜 로그인
- Spotify 실제 플레이리스트 생성
- Spotify Audio Features 기반 분석
- Spotify Recommendations 기반 추천
- Spotify Genre Seeds 기반 추천
- 음원 다운로드
- AI 모델 학습용 Spotify 데이터 사용

## Implementation Checklist

- [ ] Spotify API 사용 범위를 검색/조회 중심으로 수정
- [ ] `preview_url`은 선택 필드로 처리
- [ ] 분위기 분석 문구를 "자동 음악 특성 분석"이 아니라 "커뮤니티 태그 기반 분위기 분석"으로 변경
- [ ] 추천 곡은 내부 DB 기반으로 구현
- [ ] ERD 작성
- [ ] API 명세 작성
- [ ] 예외 응답 포맷 통일
- [ ] 세션 인터셉터 작성
- [ ] 권한 체크 서비스 계층에 배치
- [ ] Spotify Client Credentials 토큰 캐시 구현
- [ ] 발표 자료에 Spotify API 제한과 대체 설계를 명확히 설명

## GSTACK REVIEW REPORT

| Phase | Verdict | Main Finding |
|---|---|---|
| CEO | DONE_WITH_CONCERNS | 제품 방향은 좋지만 Spotify 추천/분석 API 전제가 깨져 있음 |
| Design | DONE | 탐색 첫 화면, 생성 빌더, 상세 상호작용 중심으로 가야 함 |
| Eng | DONE_WITH_CONCERNS | Spring Security 없이도 가능하지만 인터셉터와 서비스 권한 체크를 분리해야 함 |
| DX | DONE | 구현 순서는 인증, Spotify 검색, playlist core, 커뮤니티 순서가 맞음 |

STATUS: DONE_WITH_CONCERNS

가장 중요한 수정은 2차 차별화 기능의 기반이다. Spotify의 제한된 endpoint가 아니라 우리 DB의 커뮤니티 데이터로 추천과 분위기 기능을 만들어야 한다.
