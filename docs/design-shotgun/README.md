# Design Shotgun

작성일: 2026-04-26

## Variants

### A. Carbon Pulse

파일: `variant-a-carbon-pulse.html`

가장 안전한 1차 구현 방향. 기존 `DESIGN.md`를 거의 그대로 시각화한 버전이다.

강점:
- Spotify와 비슷한 사용성.
- 플레이리스트 목록과 트랙 리스트 구현이 쉽다.
- 디자인 시스템과 충돌이 적다.

약점:
- 가장 익숙해서 차별성은 약하다.

### B. Community Signal

파일: `variant-b-community-signal.html`

댓글, 좋아요, 사용자 반응을 더 앞에 둔 커뮤니티형 방향.

강점:
- "플레이리스트 공유 커뮤니티"라는 서비스 목적이 더 잘 드러난다.
- 댓글과 한줄 리뷰 기능을 발표할 때 좋다.

약점:
- 음악 앱보다 게시판/매거진처럼 보일 수 있다.

### C. Builder Console

파일: `variant-c-builder-console.html`

플레이리스트 생성/수정 화면에 초점을 둔 작업형 방향.

강점:
- Spotify 곡 검색, 추가, 순서 변경 흐름이 가장 명확하다.
- MVP 핵심 기능인 플레이리스트 생성에 좋다.

약점:
- 공개 탐색 화면에는 너무 도구처럼 보인다.

## Recommendation

메인 방향은 A를 선택한다.

그리고 화면별로 섞는다:
- 홈/공개 목록/상세: A
- 댓글/인기/활동 영역: B 일부
- 생성/수정 화면: C 일부

이 조합이 가장 실용적이다. 1차 구현은 A로 빠르게 만들고, 기능이 붙을수록 B와 C의 장점을 부분 반영한다.

## Files

- `board.html`
- `variant-a-carbon-pulse.html`
- `variant-b-community-signal.html`
- `variant-c-builder-console.html`
