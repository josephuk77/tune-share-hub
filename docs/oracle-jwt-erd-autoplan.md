# Oracle JWT ERD Autoplan

작성일: 2026-04-25

## 결론

현재 설계 방향은 맞다. MVP를 `JWT 로그인 + 플레이리스트 CRUD + Spotify 곡 검색/추가`로 줄인 판단이 좋고, 회원가입을 빼고 `USERS`를 사전 등록 계정 테이블로 쓰는 것도 세미프로젝트 범위에 맞다.

다만 DB 설계는 몇 가지를 고치면 훨씬 덜 흔들린다.

핵심 수정:
- `POSITION` 컬럼은 `POSITION_NO`로 바꾼다.
- soft delete를 쓸 테이블에는 전부 `DELETED_AT`을 넣는다.
- soft delete 정책이면 `ON DELETE CASCADE`는 빼고 service에서 연관 데이터를 논리 삭제한다.
- `PLAYLISTS`에 `LIKE_COUNT`, `COMMENT_COUNT`를 추가한다.
- `REFRESH_TOKENS.TOKEN_VALUE`는 unique로 둔다. 가능하면 토큰 원문이 아니라 해시를 저장한다.
- `PUBLIC_YN`, `REVOKED_YN`에는 check 제약을 둔다.

## Accepted Product Scope

### MVP

- 사전 등록 사용자 로그인
- BCrypt hash 비밀번호 검증
- JWT access token 발급
- refresh token 저장/무효화
- 플레이리스트 생성, 조회, 수정, 삭제
- Spotify 곡 검색
- 플레이리스트에 곡 추가, 삭제, 순서 변경

### 1차

- 공개 플레이리스트 목록/상세
- 댓글 작성, 수정, 삭제
- 좋아요, 좋아요 취소
- 최신순/조회순/좋아요순 기초 정렬

### 2차

- 검색 히스토리
- 랭킹
- 트랙 상세 사용성 개선

### 3차

- 태그
- 플레이리스트 복사
- 추천/분위기 분석 확장

## CEO Review

### Scope Calibration

기능 범위를 잘 줄였다. 회원가입, 소셜 로그인, Spotify OAuth, Spotify playlist sync를 빼면서 구현 위험이 크게 줄었다.

사용자에게 보여줄 핵심 가치는 이렇다.

```
로그인한다
Spotify에서 곡을 찾는다
내 플레이리스트를 만든다
공개하면 다른 사람이 본다
댓글과 좋아요로 반응한다
```

이 정도면 세미프로젝트 발표에서 충분히 완성된 서비스로 보인다.

### User Challenge

태그를 3차로 미룬 것은 괜찮다. 원래 명세에는 태그 필터가 중요해 보였지만, 지금 MVP/1차 목표가 안정성이라면 태그는 뒤로 빼는 게 맞다.

단 발표 자료에는 "태그 기반 탐색은 3차 확장"이라고 명확히 적어야 한다. 안 그러면 원래 명세와 구현 범위가 달라 보인다.

## Design Review

### Required Screens

MVP 화면:
- 로그인
- 내 플레이리스트 목록
- 플레이리스트 생성/수정
- Spotify 곡 검색 모달 또는 영역
- 플레이리스트 상세

1차 화면:
- 공개 플레이리스트 목록
- 댓글 영역
- 좋아요 버튼
- 마이페이지 또는 내 활동 요약

### Empty/Error States

반드시 필요한 상태:
- 로그인 실패
- JWT 만료
- refresh token 만료
- 플레이리스트 없음
- Spotify 검색 결과 없음
- Spotify API 실패
- 곡이 없는 플레이리스트
- 삭제된 플레이리스트 접근
- 공개되지 않은 플레이리스트 접근

### UX Decision

좋아요는 optimistic UI로 처리해도 되지만, 세미프로젝트라면 서버 응답 후 카운트를 갱신하는 편이 안전하다. unique 제약 충돌, soft delete 복구, 카운트 갱신이 한 트랜잭션에 묶여야 하기 때문이다.

## Engineering Review

## Final MVP DDL

아래 DDL은 ERDCloud import와 Oracle 실행을 모두 고려한 1차 기준이다. MVP 테이블과 1차 댓글/좋아요까지 포함한다.

```sql
CREATE TABLE USERS (
    USER_ID NUMBER PRIMARY KEY,
    EMAIL VARCHAR2(255) NOT NULL,
    PASSWORD_HASH VARCHAR2(255) NOT NULL,
    NICKNAME VARCHAR2(50) NOT NULL,
    ROLE VARCHAR2(20) DEFAULT 'USER' NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP,
    DELETED_AT TIMESTAMP,

    CONSTRAINT UK_USERS_EMAIL UNIQUE (EMAIL),
    CONSTRAINT UK_USERS_NICKNAME UNIQUE (NICKNAME),
    CONSTRAINT CK_USERS_ROLE CHECK (ROLE IN ('USER', 'ADMIN'))
);

CREATE TABLE REFRESH_TOKENS (
    TOKEN_ID NUMBER PRIMARY KEY,
    USER_ID NUMBER NOT NULL,
    TOKEN_VALUE VARCHAR2(500) NOT NULL,
    EXPIRES_AT TIMESTAMP NOT NULL,
    REVOKED_YN CHAR(1) DEFAULT 'N' NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    REVOKED_AT TIMESTAMP,

    CONSTRAINT FK_REFRESH_TOKENS_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT UK_REFRESH_TOKEN_VALUE UNIQUE (TOKEN_VALUE),
    CONSTRAINT CK_REFRESH_REVOKED_YN CHECK (REVOKED_YN IN ('Y', 'N'))
);

CREATE TABLE PLAYLISTS (
    PLAYLIST_ID NUMBER PRIMARY KEY,
    USER_ID NUMBER NOT NULL,
    TITLE VARCHAR2(100) NOT NULL,
    DESCRIPTION CLOB,
    COVER_IMAGE_URL VARCHAR2(1000),
    PUBLIC_YN CHAR(1) DEFAULT 'Y' NOT NULL,
    VIEW_COUNT NUMBER DEFAULT 0 NOT NULL,
    LIKE_COUNT NUMBER DEFAULT 0 NOT NULL,
    COMMENT_COUNT NUMBER DEFAULT 0 NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP,
    DELETED_AT TIMESTAMP,

    CONSTRAINT FK_PLAYLISTS_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT CK_PLAYLISTS_PUBLIC_YN CHECK (PUBLIC_YN IN ('Y', 'N'))
);

CREATE TABLE PLAYLIST_TRACKS (
    PLAYLIST_TRACK_ID NUMBER PRIMARY KEY,
    PLAYLIST_ID NUMBER NOT NULL,
    SPOTIFY_TRACK_ID VARCHAR2(100) NOT NULL,
    TITLE VARCHAR2(255) NOT NULL,
    ARTIST_NAME VARCHAR2(255) NOT NULL,
    ALBUM_NAME VARCHAR2(255),
    ALBUM_IMAGE_URL VARCHAR2(1000),
    SPOTIFY_URL VARCHAR2(1000) NOT NULL,
    PREVIEW_URL VARCHAR2(1000),
    DURATION_MS NUMBER,
    POSITION_NO NUMBER NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    DELETED_AT TIMESTAMP,

    CONSTRAINT FK_PLAYLIST_TRACKS_PLAYLIST
        FOREIGN KEY (PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT UK_PLAYLIST_TRACK_POSITION
        UNIQUE (PLAYLIST_ID, POSITION_NO)
);

CREATE TABLE COMMENTS (
    COMMENT_ID NUMBER PRIMARY KEY,
    PLAYLIST_ID NUMBER NOT NULL,
    USER_ID NUMBER NOT NULL,
    CONTENT CLOB NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP,
    DELETED_AT TIMESTAMP,

    CONSTRAINT FK_COMMENTS_PLAYLIST
        FOREIGN KEY (PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT FK_COMMENTS_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)
);

CREATE TABLE PLAYLIST_LIKES (
    LIKE_ID NUMBER PRIMARY KEY,
    PLAYLIST_ID NUMBER NOT NULL,
    USER_ID NUMBER NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    DELETED_AT TIMESTAMP,

    CONSTRAINT FK_PLAYLIST_LIKES_PLAYLIST
        FOREIGN KEY (PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT FK_PLAYLIST_LIKES_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT UK_PLAYLIST_LIKES UNIQUE (PLAYLIST_ID, USER_ID)
);

CREATE SEQUENCE SEQ_USERS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_REFRESH_TOKENS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PLAYLISTS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PLAYLIST_TRACKS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_COMMENTS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PLAYLIST_LIKES START WITH 1 INCREMENT BY 1;
```

## Index Plan

```sql
CREATE INDEX IDX_REFRESH_TOKENS_USER
ON REFRESH_TOKENS (USER_ID, REVOKED_YN, EXPIRES_AT);

CREATE INDEX IDX_PLAYLISTS_USER_ID
ON PLAYLISTS (USER_ID, CREATED_AT);

CREATE INDEX IDX_PLAYLISTS_PUBLIC_CREATED
ON PLAYLISTS (PUBLIC_YN, DELETED_AT, CREATED_AT);

CREATE INDEX IDX_PLAYLISTS_PUBLIC_VIEW
ON PLAYLISTS (PUBLIC_YN, DELETED_AT, VIEW_COUNT);

CREATE INDEX IDX_PLAYLISTS_PUBLIC_LIKE
ON PLAYLISTS (PUBLIC_YN, DELETED_AT, LIKE_COUNT);

CREATE INDEX IDX_TRACKS_PLAYLIST_POSITION
ON PLAYLIST_TRACKS (PLAYLIST_ID, DELETED_AT, POSITION_NO);

CREATE INDEX IDX_TRACKS_SPOTIFY_ID
ON PLAYLIST_TRACKS (SPOTIFY_TRACK_ID);

CREATE INDEX IDX_COMMENTS_PLAYLIST_CREATED
ON COMMENTS (PLAYLIST_ID, DELETED_AT, CREATED_AT);

CREATE INDEX IDX_LIKES_PLAYLIST
ON PLAYLIST_LIKES (PLAYLIST_ID, DELETED_AT);

CREATE INDEX IDX_LIKES_USER
ON PLAYLIST_LIKES (USER_ID, DELETED_AT);
```

ERDCloud import가 index를 싫어하면 table DDL만 먼저 넣고, index는 실제 Oracle DB 적용용으로 따로 둔다.

## Transaction Rules

### Playlist Delete

soft delete 정책이므로 물리 삭제하지 않는다.

한 트랜잭션에서:
- `PLAYLISTS.DELETED_AT = SYSTIMESTAMP`
- `PLAYLIST_TRACKS.DELETED_AT = SYSTIMESTAMP`
- `COMMENTS.DELETED_AT = SYSTIMESTAMP`
- `PLAYLIST_LIKES.DELETED_AT = SYSTIMESTAMP`

### Like Toggle

unique `(PLAYLIST_ID, USER_ID)` 때문에 아래 흐름으로 구현한다.

```
row 없음
  INSERT, PLAYLISTS.LIKE_COUNT + 1

row 있음 AND DELETED_AT IS NULL
  UPDATE DELETED_AT = SYSTIMESTAMP, PLAYLISTS.LIKE_COUNT - 1

row 있음 AND DELETED_AT IS NOT NULL
  UPDATE DELETED_AT = NULL, CREATED_AT = SYSTIMESTAMP, PLAYLISTS.LIKE_COUNT + 1
```

### Comment Delete

댓글은 물리 삭제하지 않는다.

```
COMMENTS.DELETED_AT = SYSTIMESTAMP
COMMENTS.UPDATED_AT = SYSTIMESTAMP
PLAYLISTS.COMMENT_COUNT - 1
```

댓글 목록에서는 `DELETED_AT IS NULL`만 보여준다.

## JWT Review

### Required Backend Parts

- `AuthController`
- `AuthService`
- `JwtProvider`
- `JwtInterceptor`
- `AuthUser`
- `UserMapper`
- `RefreshTokenMapper`

Spring Security를 쓰지 않으므로 인증 책임이 전부 직접 구현 코드에 있다.

### Access Token

권장 payload:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "tester",
  "role": "USER",
  "iat": 1710000000,
  "exp": 1710003600
}
```

Access token 만료는 30분에서 2시간 사이를 추천한다.

### Refresh Token

Refresh token은 DB에 저장한다.

권장 정책:
- 원문 대신 SHA-256 hash 저장
- 로그아웃 시 `REVOKED_YN = 'Y'`
- 재발급 시 기존 refresh token 무효화 후 새 refresh token 발급

세미프로젝트 단순화를 원하면 원문 저장도 가능하다. 다만 발표에서는 "실무라면 해시 저장"이라고 설명하는 게 좋다.

## MyBatis Review

### Mapper Naming

- `UserMapper`
- `RefreshTokenMapper`
- `PlaylistMapper`
- `PlaylistTrackMapper`
- `CommentMapper`
- `PlaylistLikeMapper`

### Common SQL Rule

soft delete 테이블 조회는 기본적으로 전부 아래 조건을 포함한다.

```sql
DELETED_AT IS NULL
```

관리자용 조회만 예외를 둔다.

### Oracle Pagination

Oracle 12c 이상이면:

```sql
OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY
```

정렬은 whitelist 방식으로 service에서 허용값만 SQL에 전달한다.

허용 sort:
- `latest`
- `view`
- `like`
- `comment`

## 2차 ERD Additions

2차는 아래만 추가하면 된다.

```sql
CREATE TABLE SEARCH_HISTORIES (
    HISTORY_ID NUMBER PRIMARY KEY,
    USER_ID NUMBER NOT NULL,
    KEYWORD VARCHAR2(100) NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    DELETED_AT TIMESTAMP,

    CONSTRAINT FK_SEARCH_HISTORIES_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT UK_SEARCH_USER_KEYWORD UNIQUE (USER_ID, KEYWORD)
);

CREATE SEQUENCE SEQ_SEARCH_HISTORIES START WITH 1 INCREMENT BY 1;

CREATE INDEX IDX_SEARCH_USER_TIME
ON SEARCH_HISTORIES (USER_ID, DELETED_AT, CREATED_AT);
```

같은 검색어는 insert하지 말고 기존 row의 `CREATED_AT`을 갱신한다. 삭제된 row가 있으면 `DELETED_AT = NULL`로 복구한다.

## 3차 ERD Additions

```sql
CREATE TABLE TAGS (
    TAG_ID NUMBER PRIMARY KEY,
    NAME VARCHAR2(50) NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT UK_TAGS_NAME UNIQUE (NAME)
);

CREATE TABLE PLAYLIST_TAGS (
    PLAYLIST_ID NUMBER NOT NULL,
    TAG_ID NUMBER NOT NULL,

    CONSTRAINT PK_PLAYLIST_TAGS PRIMARY KEY (PLAYLIST_ID, TAG_ID),
    CONSTRAINT FK_PLAYLIST_TAGS_PLAYLIST
        FOREIGN KEY (PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT FK_PLAYLIST_TAGS_TAG
        FOREIGN KEY (TAG_ID) REFERENCES TAGS(TAG_ID)
);

CREATE TABLE PLAYLIST_COPIES (
    COPY_ID NUMBER PRIMARY KEY,
    ORIGINAL_PLAYLIST_ID NUMBER NOT NULL,
    COPIED_PLAYLIST_ID NUMBER NOT NULL,
    USER_ID NUMBER NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT FK_COPIES_ORIGINAL
        FOREIGN KEY (ORIGINAL_PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT FK_COPIES_COPIED
        FOREIGN KEY (COPIED_PLAYLIST_ID) REFERENCES PLAYLISTS(PLAYLIST_ID),
    CONSTRAINT FK_COPIES_USER
        FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)
);

CREATE SEQUENCE SEQ_TAGS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PLAYLIST_COPIES START WITH 1 INCREMENT BY 1;
```

3차에서 복사 기능을 넣을 경우 `PLAYLISTS`에 아래 컬럼도 추가한다.

```sql
ALTER TABLE PLAYLISTS ADD (
    ORIGIN_PLAYLIST_ID NUMBER,
    COPY_COUNT NUMBER DEFAULT 0 NOT NULL
);
```

## Failure Modes

| 위험 | 문제 | 대응 |
|---|---|---|
| JWT 만료 | 사용자가 갑자기 401을 봄 | refresh API로 access token 재발급 |
| refresh token 탈취 | 장기간 계정 접근 가능 | 만료 짧게, 로그아웃 revoke, 가능하면 hash 저장 |
| 좋아요 unique 충돌 | 재좋아요 시 insert 실패 | 기존 row 복구 방식 사용 |
| soft delete 누락 | 삭제된 데이터가 목록에 노출 | 모든 select에 `DELETED_AT IS NULL` |
| playlist 삭제 후 수록곡 노출 | 상세/추천에 삭제 데이터 섞임 | service 트랜잭션으로 연관 soft delete |
| POSITION_NO 중복 | 곡 순서 저장 실패 | 전체 순서 재정렬 후 일괄 update |
| sort SQL injection | 정렬 파라미터로 SQL 조작 | service에서 sort whitelist |
| CLOB 검색 불편 | 설명/댓글 검색 구현 복잡 | MVP 검색은 title 중심으로 제한 |

## Implementation Order

1. Oracle schema 생성
2. 사전 사용자 insert
3. BCrypt 검증 로그인
4. JWT 발급/검증
5. refresh token 저장/재발급/로그아웃
6. playlist CRUD
7. playlist track 추가/삭제/순서 변경
8. Spotify search API 연동
9. 공개 playlist 목록/상세
10. 댓글
11. 좋아요
12. 검색 히스토리
13. 태그/복사

## Final Verdict

STATUS: DONE_WITH_CONCERNS

설계는 충분히 구현 가능하다. 고쳐야 할 것은 기능 방향이 아니라 DB 무결성과 삭제 정책이다.

가장 중요한 원칙:
- soft delete를 쓸 거면 cascade 물리 삭제를 섞지 않는다.
- unique 좋아요는 insert/delete가 아니라 insert/recover/revoke로 다룬다.
- JWT를 직접 구현하므로 interceptor와 service 권한 검사를 분리한다.
- Oracle 예약어처럼 보이는 컬럼명은 피한다.

이렇게 가면 ERD, API, MyBatis 구현까지 일관되게 이어진다.
