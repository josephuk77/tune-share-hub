# Git Commit Convention

작성일: 2026-04-27

이 프로젝트의 모든 커밋 메시지는 아래 규칙을 따른다.

## 기본 구조

```text
<type>(<scope>): <subject>

<body>

<footer>
```

`type`은 필수다.

`scope`는 선택이지만, 변경 영역이 명확하면 사용하는 것을 권장한다.

`body`와 `footer`는 선택이다.

## Type

| type | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정, README, 주석 등 |
| `style` | 코드 포맷팅, 세미콜론, 공백 등 로직 변화 없는 수정 |
| `refactor` | 기능 변화 없는 리팩토링 |
| `test` | 테스트 코드 추가 또는 수정 |
| `chore` | 빌드, 설정, 패키지 등 기타 작업 |
| `perf` | 성능 개선 |
| `ci` | CI/CD 설정 변경 |
| `build` | 빌드 관련 변경 |
| `revert` | 이전 커밋 되돌리기 |

## Scope

`scope`는 변경된 영역을 짧게 적는다.

예시:
- `auth`
- `member`
- `playlist`
- `comment`
- `like`
- `spotify`
- `api`
- `config`
- `ui`
- `db`
- `security`
- `docs`

좋은 예:
```text
feat(auth): add jwt login
fix(playlist): fix track order update
docs(db): add oracle erd guide
refactor(ui): split playlist card component
```

## Subject Rules

`subject`는 가장 중요하다.

규칙:
- 한 줄로 작성한다.
- 마침표를 찍지 않는다.
- 첫 글자는 소문자로 시작한다.
- 명령문 형태로 작성한다.
- `add`, `fix`, `change`, `remove`, `refactor` 같은 동사로 시작한다.
- 50자 이내로 작성한다.
- 무엇을 했는지 짧고 명확하게 적는다.

좋은 예:
```text
feat(auth): add jwt login logic
fix(board): fix pagination bug
docs(db): add first phase erd
refactor(member): separate dto and entity
```

나쁜 예:
```text
feat: 로그인 기능을 추가했습니다.
fix: 버그 수정 완료.
update
수정
변경
```

## Body

`body`는 선택이다.

작성 기준:
- 무엇을 왜 변경했는지 설명한다.
- 어떻게보다 왜에 집중한다.
- subject 아래 한 줄을 띄우고 작성한다.
- 여러 줄 작성 가능하다.

예:
```text
feat(auth): add jwt login

사용자 로그인 시 access token과 refresh token을 발급하도록 구현
```

## Footer

`footer`는 선택이다.

이슈 번호 연결이나 breaking change를 적을 때 사용한다.

예:
```text
Closes #12
Fixes #23
```

전체 예:
```text
feat(auth): add jwt login

사용자 로그인 시 JWT 토큰을 발급하도록 구현

Closes #12
```

## Commit Unit Rules

- 하나의 커밋은 하나의 목적만 가진다.
- 기능 추가와 리팩토링을 한 커밋에 같이 넣지 않는다.
- 커밋은 작게 나눈다.
- 동작 가능한 상태에서 커밋한다.
- 문서 수정은 코드 변경과 분리하는 것을 권장한다.
- DB 스키마 변경과 API 구현은 가능하면 분리한다.

## Forbidden

금지:
- 의미 없는 커밋 메시지
- 여러 기능을 한 커밋에 넣기
- 디버깅 코드가 포함된 커밋
- `console.log`가 남은 프론트엔드 커밋
- `System.out.println`이 남은 백엔드 커밋
- 비밀번호, 토큰, 개인정보가 포함된 커밋

금지 메시지 예:
```text
update
수정
변경
작업
마무리
fix
feat
```

## Project Examples

인증:
```text
feat(auth): add jwt login
fix(auth): handle expired refresh token
refactor(auth): separate jwt provider
```

플레이리스트:
```text
feat(playlist): add playlist create api
fix(playlist): fix track position update
refactor(playlist): split playlist service methods
```

Spotify:
```text
feat(spotify): add track search api
fix(spotify): handle token refresh failure
```

댓글/좋아요:
```text
feat(comment): add comment create api
feat(like): add playlist like toggle
fix(like): restore deleted like row
```

DB:
```text
docs(db): add oracle erd tables
chore(db): add first phase schema
```

UI:
```text
feat(ui): add playlist card component
refactor(ui): split track row component
style(ui): adjust playlist grid spacing
```

## Default Rule

앞으로 모든 커밋 메시지는 이 형식을 따른다.

```text
<type>(<scope>): <subject>
```

명확하고 짧게 작성한다.
