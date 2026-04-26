# Clean Code Rules

작성일: 2026-04-27

이 문서는 플레이리스트 공유 커뮤니티 프로젝트를 개발할 때 지켜야 하는 코드 작성 기준이다.

대상 스택:
- Backend: Spring Boot
- Frontend: React with JavaScript
- DB: Oracle DB
- Persistence: MyBatis 기준
- Auth: JWT, Spring Security 미사용

관련 문서:
- [Git Commit Convention](./git-commit-convention.md)

## Spring Boot Clean Code Rules

### Layer Responsibility

- Controller는 HTTP 요청과 응답 처리만 담당한다.
- Controller에 비즈니스 로직을 작성하지 않는다.
- 비즈니스 로직은 Service 계층에 작성한다.
- DB 접근 로직은 Repository 또는 Mapper 계층에 작성한다.
- Entity 또는 DB row 객체를 API 응답으로 직접 반환하지 않는다.
- 요청/응답 전용 DTO를 사용한다.

### DTO Naming

DTO 이름은 역할이 드러나게 작성한다.

좋은 예:
- `LoginRequest`
- `LoginResponse`
- `PlaylistCreateRequest`
- `PlaylistUpdateRequest`
- `PlaylistResponse`
- `TrackSearchResponse`
- `CommentCreateRequest`
- `CommentResponse`

나쁜 예:
- `DataDto`
- `RequestDto`
- `ResponseDto`
- `TempDto`

### Method Design

- 메서드는 하나의 책임만 가진다.
- 메서드가 길어지면 private 메서드로 분리한다.
- 중복 코드는 공통 메서드로 분리한다.
- 의미 없는 변수명을 사용하지 않는다.
- if 문이 깊어지지 않도록 early return을 사용한다.
- 매직 넘버와 문자열은 상수로 분리한다.

나쁜 예:
```java
String data = mapper.find(id);
```

좋은 예:
```java
Playlist playlist = playlistMapper.findById(playlistId);
```

### Exception Handling

- 예외 상황은 `GlobalExceptionHandler`에서 일관되게 처리한다.
- 에러 응답 형식을 통일한다.
- Service에서 실패 이유가 명확한 커스텀 예외를 던진다.
- Controller에서 try-catch를 반복하지 않는다.

권장 에러 응답:
```json
{
  "code": "PLAYLIST_NOT_FOUND",
  "message": "플레이리스트를 찾을 수 없습니다."
}
```

### Dependency Injection

- 생성자 주입을 사용한다.
- 필드 주입 `@Autowired`는 사용하지 않는다.
- Service는 인터페이스보다 구현 단순성을 우선한다.
- 구현체가 여러 개일 때만 인터페이스를 분리한다.

좋은 예:
```java
@Service
public class PlaylistService {

    private final PlaylistMapper playlistMapper;

    public PlaylistService(PlaylistMapper playlistMapper) {
        this.playlistMapper = playlistMapper;
    }
}
```

### Transaction

- 트랜잭션이 필요한 메서드에는 `@Transactional`을 명확히 붙인다.
- 조회 전용 메서드는 `@Transactional(readOnly = true)`를 사용한다.
- 좋아요 토글, 댓글 작성, 플레이리스트 삭제처럼 여러 테이블이 함께 바뀌는 기능은 하나의 트랜잭션으로 묶는다.

예:
```java
@Transactional
public void deletePlaylist(Long playlistId, Long loginUserId) {
    // playlist, tracks, comments, likes soft delete
}
```

```java
@Transactional(readOnly = true)
public PlaylistResponse getPlaylist(Long playlistId) {
    // 조회 전용
}
```

### Logging And Security

- `System.out.println` 대신 Logger를 사용한다.
- 비밀번호, JWT, Refresh Token, 개인정보는 로그에 남기지 않는다.
- 로그인 실패 로그에도 입력 비밀번호를 남기지 않는다.
- 토큰 검증 실패는 필요한 수준의 메시지만 남긴다.

### Configuration

- 설정값은 `application.yml` 또는 환경변수로 분리한다.
- JWT secret, Spotify client secret, DB password는 코드에 직접 쓰지 않는다.
- 만료 시간, 페이지 사이즈 기본값 같은 값은 상수 또는 설정값으로 분리한다.

### Package Structure

역할 기준으로 패키지를 명확히 나눈다.

```text
src/main/java/com/example/playlist/
  config/
  controller/
  service/
  mapper/
  dto/
  entity/
  exception/
  auth/
  spotify/
```

권장 도메인별 확장:
```text
playlist/
  controller/
  service/
  mapper/
  dto/

comment/
  controller/
  service/
  mapper/
  dto/
```

작은 프로젝트에서는 역할 기준 구조를 먼저 쓰고, 파일이 많아지면 도메인별로 나눈다.

### Playlist Project Specific Rules

- JWT 인증 검증은 Interceptor에서 처리한다.
- 작성자 권한 검사는 Service에서 처리한다.
- SQL만 믿고 권한 처리를 끝내지 않는다.
- Soft delete 테이블 조회에는 `DELETED_AT IS NULL`을 기본 조건으로 넣는다.
- 좋아요는 unique 제약 때문에 insert/delete가 아니라 insert/recover/revoke 흐름으로 처리한다.
- Spotify API 결과는 필요한 필드만 DTO로 변환한다.
- Spotify client id, secret은 프론트로 보내지 않는다.

## React Clean Code Rules

### Language

- React 코드는 JavaScript로 작성한다.
- TypeScript는 사용하지 않는다.
- 파일 확장자는 기본적으로 `.jsx`와 `.js`를 사용한다.
- 컴포넌트 파일은 `.jsx`를 사용한다.
- API, hook, util, constant 파일은 `.js`를 사용한다.

예:
```text
pages/LoginPage.jsx
components/playlist/PlaylistCard.jsx
api/authApi.js
hooks/useAuth.js
utils/tokenStorage.js
constants/routes.js
```

### Component Responsibility

- 컴포넌트는 하나의 책임만 가진다.
- 한 컴포넌트가 너무 커지면 작은 컴포넌트로 분리한다.
- 화면 컴포넌트와 재사용 컴포넌트를 구분한다.
- 반복되는 UI는 재사용 컴포넌트로 분리한다.

권장 구조:
```text
src/
  pages/
  components/
  api/
  hooks/
  utils/
  styles/
  constants/
  contexts/
```

### Pages And Components

`pages`는 라우트 단위 화면을 담당한다.

예:
- `LoginPage`
- `HomePage`
- `PlaylistDetailPage`
- `PlaylistEditorPage`
- `MyPage`

`components`는 재사용 UI를 담당한다.

예:
- `PlaylistCard`
- `PlaylistGrid`
- `TrackRow`
- `TrackSearchPanel`
- `CommentList`
- `LikeButton`
- `Modal`
- `Button`
- `EmptyState`

### API Code

- API 호출 로직은 컴포넌트 안에 많이 작성하지 않는다.
- API 호출 함수는 별도 파일로 분리한다.

예:
```text
api/authApi.js
api/playlistApi.js
api/spotifyApi.js
api/commentApi.js
api/likeApi.js
```

컴포넌트는 API의 세부 URL보다 의도를 읽을 수 있어야 한다.

좋은 예:
```js
const playlist = await getPlaylist(playlistId);
```

나쁜 예:
```js
const response = await fetch(`/api/playlists/${id}`);
```

### Naming

props 이름은 의미 있게 작성한다.

나쁜 예:
```jsx
<PlaylistCard data={data} item={item} />
```

좋은 예:
```jsx
<PlaylistCard playlist={playlist} onDeletePlaylist={handleDeletePlaylist} />
```

상태 이름은 역할이 드러나게 작성한다.

좋은 예:
- `isLoading`
- `errorMessage`
- `selectedPlaylist`
- `playlistTracks`
- `searchKeyword`
- `hasToken`
- `canSubmit`

boolean 값은 `is`, `has`, `can`, `should`로 시작한다.

예:
- `isLogin`
- `isLoading`
- `hasToken`
- `canSubmit`
- `shouldShowModal`

이벤트 핸들러 이름은 `handle`로 시작한다.

예:
- `handleSubmit`
- `handleChange`
- `handleDelete`
- `handleLikeToggle`
- `handleTrackAdd`

### State And Effect

- `useState`는 필요한 곳에만 사용한다.
- 여러 상태가 함께 변하면 객체나 `useReducer` 사용을 고려한다.
- `useEffect`는 목적을 명확히 한다.
- `useEffect` 안에 너무 많은 로직을 작성하지 않는다.
- 중복되는 로직은 custom hook으로 분리한다.

custom hook 예:
- `useInput`
- `useAuth`
- `usePlaylist`
- `useTrackSearch`
- `usePagination`

### Rendering

- 조건부 렌더링은 읽기 쉽게 작성한다.
- `map`의 key에는 index보다 고유 id를 우선 사용한다.
- 불필요한 렌더링을 만드는 상태 구조를 피한다.
- 부모 컴포넌트가 너무 많은 props를 전달하면 구조를 다시 검토한다.

좋은 예:
```jsx
tracks.map((track) => (
  <TrackRow key={track.playlistTrackId} track={track} />
))
```

나쁜 예:
```jsx
tracks.map((track, index) => (
  <TrackRow key={index} data={track} />
))
```

### Styling

- 인라인 스타일을 남발하지 않는다.
- CSS 클래스 이름은 역할이 드러나게 작성한다.
- `DESIGN.md`의 색상, 간격, 폰트 토큰을 따른다.
- Spotify 느낌을 참고하되 Spotify 로고/브랜드를 복제하지 않는다.
- 앨범 이미지는 자르거나 변형하지 않는다.

좋은 클래스명:
- `playlist-card`
- `track-row`
- `track-search-panel`
- `comment-list`
- `like-button`
- `empty-state`

나쁜 클래스명:
- `box`
- `area`
- `temp`
- `style1`

### Frontend Security

- `console.log`는 디버깅 후 제거한다.
- 토큰, 비밀번호, 개인정보를 화면이나 로그에 노출하지 않는다.
- JWT를 사용할 때 API 요청 헤더 설정을 공통 함수에서 처리한다.
- 로그인 만료 응답을 공통으로 처리한다.

## Common Rules

### Before Coding

- 기능을 구현하기 전에 기존 구조를 먼저 파악한다.
- 기존 코드 스타일을 최대한 유지한다.
- 불필요한 리팩토링은 하지 않는다.
- 요구사항과 관련 없는 코드는 수정하지 않는다.

### During Coding

- 함수명과 변수명만 보고도 역할을 이해할 수 있게 작성한다.
- 중복을 줄이되 과한 추상화는 피한다.
- 처음 보는 사람이 읽어도 흐름을 이해할 수 있게 작성한다.
- 주석은 코드로 설명하기 어려운 의도나 이유를 설명할 때만 작성한다.
- 사용하지 않는 import, 변수, 함수는 제거한다.

### After Coding

- 코드 변경 후 영향 범위를 간단히 설명한다.
- 관련 기능을 직접 실행하거나 테스트한다.
- 테스트하지 못한 부분은 명확히 남긴다.
- 커밋 단위는 하나의 변경 목적을 기준으로 작게 나눈다.

## Review Checklist

개발 후 아래 항목을 확인한다.

### Backend

- [ ] Controller에 비즈니스 로직이 없는가?
- [ ] Service에 핵심 로직이 모여 있는가?
- [ ] Mapper는 DB 접근만 담당하는가?
- [ ] Entity/row 객체를 API 응답으로 직접 반환하지 않는가?
- [ ] 요청/응답 DTO 이름이 명확한가?
- [ ] 트랜잭션이 필요한 메서드에 `@Transactional`이 있는가?
- [ ] 조회 메서드에 `@Transactional(readOnly = true)`가 있는가?
- [ ] 예외 응답 형식이 통일되어 있는가?
- [ ] 비밀번호, 토큰, 개인정보가 로그에 남지 않는가?
- [ ] 설정값이 코드에 하드코딩되어 있지 않은가?
- [ ] soft delete 조회 조건이 빠지지 않았는가?

### Frontend

- [ ] 페이지 컴포넌트와 재사용 컴포넌트가 분리되어 있는가?
- [ ] API 호출 함수가 `api/` 폴더로 분리되어 있는가?
- [ ] props 이름이 구체적인가?
- [ ] boolean 상태가 `is`, `has`, `can`, `should`로 시작하는가?
- [ ] 이벤트 핸들러가 `handle`로 시작하는가?
- [ ] `map` key에 고유 id를 사용하는가?
- [ ] 불필요한 `console.log`가 없는가?
- [ ] 토큰이나 개인정보가 화면/로그에 노출되지 않는가?
- [ ] `DESIGN.md`의 색상, 간격, 폰트 규칙을 따르는가?

### Common

- [ ] 요구사항과 관련 없는 코드를 수정하지 않았는가?
- [ ] 사용하지 않는 import, 변수, 함수가 없는가?
- [ ] 중복을 줄였지만 과한 추상화를 하지 않았는가?
- [ ] 처음 보는 사람이 흐름을 이해할 수 있는가?
- [ ] 변경 영향 범위를 설명할 수 있는가?
