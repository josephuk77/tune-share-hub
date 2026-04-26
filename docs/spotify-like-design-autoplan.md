# Spotify-like Design Autoplan

작성일: 2026-04-26

## 결론

디자인 방향은 "Spotify를 닮은 어두운 음악 감상/탐색 경험"으로 가도 된다. 다만 Spotify 브랜드를 그대로 쓰면 안 된다.

가능:
- 다크 UI
- 앨범 이미지 중심 카드
- 초록 계열 포인트 컬러
- 좌측 사이드바 + 메인 콘텐츠 레이아웃
- 플레이리스트/트랙 중심 탐색 경험
- 둥근 재생 버튼, 좋아요 버튼, 정렬/필터

피해야 함:
- 앱 이름에 Spotify 포함
- Spotify 로고를 우리 로고처럼 사용
- Spotify 초록색을 브랜드 핵심 색상처럼 그대로 복제
- Spotify 원형 로고, 웨이브 심볼과 비슷한 로고 제작
- 앨범 이미지를 자르거나 위에 로고/텍스트를 덮기
- Spotify가 공식 후원/제휴한 것처럼 보이는 표현

공식 기준:
- Spotify 콘텐츠를 표시하면 Spotify 로고로 출처를 표시해야 한다.
- Spotify 메타데이터, 앨범 아트, preview clip은 Spotify 링크와 함께 표시해야 한다.
- Spotify 시각 자료는 원형 보존해야 하며 자르거나 로고를 덮으면 안 된다.
- 앱 이름은 Spotify와 비슷하거나 Spotify를 포함하면 안 된다.

참고:
- https://developer.spotify.com/documentation/design
- https://developer.spotify.com/policy

## Design Positioning

서비스는 "Spotify clone"이 아니라 "Spotify 카탈로그를 활용하는 플레이리스트 커뮤니티"다.

디자인 문장:
> 어두운 배경 위에 앨범 커버와 플레이리스트가 먼저 보이는 음악 커뮤니티. Spotify의 사용성을 참고하되, 우리 서비스는 공유, 댓글, 좋아요, 복사에 초점을 둔다.

## Visual Direction

### Mood

- 어둡고 집중되는 배경
- 앨범 이미지가 가장 강한 시각 요소
- 초록 포인트는 액션 버튼에만 제한
- 텍스트는 흰색/회색 계층으로 정리
- 카드와 리스트는 조밀하지만 답답하지 않게 구성

### Color Tokens

Spotify의 공식 색을 그대로 브랜드 팔레트로 쓰지 않고, 비슷한 음악 앱 톤으로 살짝 비껴간다.

| 용도 | 색상 | 설명 |
| --- | --- | --- |
| App Background | `#121212` | 전체 다크 배경 |
| Surface | `#181818` | 카드/패널 배경 |
| Surface Hover | `#242424` | 카드 hover |
| Border | `#2A2A2A` | 약한 구분선 |
| Primary Text | `#FFFFFF` | 주요 텍스트 |
| Secondary Text | `#B3B3B3` | 보조 텍스트 |
| Muted Text | `#7A7A7A` | 메타 정보 |
| Accent | `#21C45D` | 우리 서비스 액션 컬러 |
| Accent Hover | `#2FDB70` | hover 액션 |
| Danger | `#F87171` | 삭제/오류 |
| Warning | `#FACC15` | 경고/토스트 |

주의:
- Spotify 로고 녹색으로 알려진 색을 우리 로고나 브랜드 identity로 쓰지 않는다.
- 초록은 "재생/저장/완료" 같은 액션 피드백에만 제한한다.

### Typography

공식 가이드도 플랫폼 기본 sans-serif 사용을 권장한다.

권장:
```css
font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
```

폰트 크기:
- Page title: 28px / 700
- Section title: 20px / 700
- Card title: 15px / 700
- Body: 14px / 400
- Meta: 12px / 400
- Button: 14px / 700

## Layout System

### Desktop

```text
┌─────────────────────────────────────────────────────┐
│ Top Bar: Search / User Menu                         │
├──────────────┬──────────────────────────────────────┤
│ Sidebar      │ Main Content                         │
│              │                                      │
│ Home         │ Section Title                         │
│ My Lists     │ Playlist Cards                        │
│ Liked        │                                      │
│ Create       │                                      │
└──────────────┴──────────────────────────────────────┘
```

권장 크기:
- Sidebar: 240px
- Main max width: none, viewport에 맞게 확장
- Page padding: 24px
- Card gap: 20px
- Card radius: 8px

### Mobile

```text
Top App Bar
Search
Content
Bottom Navigation
```

모바일에서는 좌측 사이드바를 쓰지 않는다. 하단 탭으로 바꾼다.

하단 탭:
- 홈
- 검색
- 만들기
- 좋아요
- 내 정보

## Screen Plan

## 1. Login

목표:
- 사전 등록 사용자만 로그인
- 회원가입 버튼 없음

구성:
- 서비스 로고/이름
- 이메일 입력
- 비밀번호 입력
- 로그인 버튼
- 에러 메시지

문구:
- "등록된 계정으로 로그인"
- "계정은 관리자에게 문의하세요"

Spotify 느낌은 유지하되, Spotify 로고나 이름은 쓰지 않는다.

## 2. Home / Public Playlists

첫 화면은 랜딩 페이지가 아니라 플레이리스트 탐색 화면이다.

구성:
- 상단 검색바
- 정렬 탭: 최신순, 조회순, 좋아요순, 댓글순
- 공개 플레이리스트 카드 그리드

카드 구성:
- 대표 앨범 이미지 1장 또는 2x2 collage
- 제목
- 작성자 닉네임
- 수록곡 수
- 좋아요 수
- 댓글 수

카드 hover:
- 배경 `#242424`
- 우측 하단 원형 play/preview 버튼 노출

## 3. Playlist Detail

상단 hero:
- 좌측 앨범 이미지 또는 collage
- 우측 제목, 작성자, 설명, 공개 여부, 생성일
- 좋아요, 복사, 수정, 삭제 버튼

본문:
- 트랙 리스트
- 댓글

트랙 row:
- 순번
- 앨범 이미지
- 곡명
- 아티스트
- 앨범명
- 재생 시간
- Spotify 링크
- preview 버튼, `PREVIEW_URL`이 있을 때만

Spotify 정책:
- Spotify에서 온 앨범/곡 메타데이터는 Spotify 링크와 함께 표시한다.
- 앨범 이미지는 원본 비율을 유지하고, 이미지 위에 텍스트나 로고를 덮지 않는다.

## 4. Playlist Create / Edit

권장 레이아웃:

```text
┌───────────────────┬───────────────────────────────┐
│ Playlist Form     │ Spotify Track Search           │
│ - title           │ - search input                 │
│ - description     │ - search results               │
│ - public/private  │                               │
├───────────────────┴───────────────────────────────┤
│ Selected Tracks                                    │
│ drag/sort/delete                                   │
└───────────────────────────────────────────────────┘
```

모바일:
- Step 1: 제목/설명
- Step 2: 곡 검색
- Step 3: 순서 확인
- Step 4: 공개 여부와 저장

필수 상태:
- 곡 검색 로딩
- 검색 결과 없음
- Spotify API 실패
- 곡 0개인 상태
- 저장 중
- 저장 실패

## 5. My Page

구성:
- 내 플레이리스트
- 내가 좋아요한 플레이리스트
- 내가 작성한 댓글

1차에서는 탭 UI만 있으면 충분하다.

## Component Rules

### Buttons

Primary:
- 초록 배경
- 검은 텍스트
- pill 형태 가능

Secondary:
- 투명 배경
- 회색 border
- 흰색 텍스트

Danger:
- 빨간색 텍스트 또는 border
- 삭제 확인 모달과 함께 사용

### Cards

- radius 8px
- background `#181818`
- hover `#242424`
- 내부 padding 16px
- 앨범 이미지는 정사각형, object-fit contain 또는 cover를 쓰더라도 원본 주요 영역을 훼손하지 않게 처리

### Track Row

리스트형이 카드형보다 낫다. 음악 서비스에서는 곡 목록이 반복 사용 영역이므로 밀도 있게 보여야 한다.

Desktop columns:
```text
No | Cover | Title/Artist | Album | Duration | Actions
```

Mobile:
```text
Cover | Title/Artist | Actions
```

## Accessibility

- 버튼 최소 높이 40px 이상
- 모바일 터치 영역 44px 이상
- 텍스트 대비 4.5:1 이상
- 키보드로 검색 결과와 트랙 row 이동 가능
- 좋아요 버튼은 색만 바꾸지 말고 아이콘 상태도 바꾼다
- preview 재생 버튼에는 곡명 포함 aria-label 사용

## Brand Safety

반드시 지킬 것:
- 앱 이름에 "Spotify" 넣지 않기
- "Spotify 공식", "Spotify 연동 공식 서비스"처럼 보이는 표현 금지
- Spotify 로고는 출처 표시에만 사용
- Spotify 로고 색/형태 변경 금지
- 앨범 이미지는 crop/overlay 최소화
- Spotify 링크를 항상 제공

권장 문구:
- "Spotify 제공 음원 정보를 바탕으로 표시됩니다."
- "Spotify에서 보기"

피할 문구:
- "Spotify 플레이리스트 만들기"
- "Spotify 커뮤니티"
- "Spotify 공식 공유 서비스"

## UI Priority By Phase

### 1차

- 로그인
- 공개 플레이리스트 목록
- 플레이리스트 상세
- 플레이리스트 생성/수정
- Spotify 곡 검색
- 댓글/좋아요

### 2차

- 검색 히스토리
- 랭킹
- 트랙 상세 UX
- 최근 본 곡/최근 검색

### 3차

- 태그 필터
- 플레이리스트 복사
- 분위기 분석 배지
- 추천 플레이리스트

## Implementation Notes

React 구조:

```text
src/
  components/
    layout/
      Sidebar.jsx
      TopBar.jsx
      BottomNav.jsx
    playlist/
      PlaylistCard.jsx
      PlaylistGrid.jsx
      TrackRow.jsx
      TrackSearchPanel.jsx
    common/
      Button.jsx
      Modal.jsx
      EmptyState.jsx
      ErrorState.jsx
  pages/
    LoginPage.jsx
    HomePage.jsx
    PlaylistDetailPage.jsx
    PlaylistEditorPage.jsx
    MyPage.jsx
  styles/
    tokens.css
    globals.css
```

CSS variables:

```css
:root {
  --color-bg: #121212;
  --color-surface: #181818;
  --color-surface-hover: #242424;
  --color-border: #2a2a2a;
  --color-text: #ffffff;
  --color-text-secondary: #b3b3b3;
  --color-text-muted: #7a7a7a;
  --color-accent: #21c45d;
  --color-accent-hover: #2fdb70;
  --color-danger: #f87171;
  --radius-card: 8px;
  --sidebar-width: 240px;
}
```

## Final Verdict

STATUS: DONE

Spotify와 비슷한 방향으로 가도 된다. 단, "Spotify 브랜드 복제"가 아니라 "음악 앱 UX 패턴 차용"이어야 한다.

가장 안전한 방향:
- dark music app
- album-first layout
- playlist card grid
- dense track list
- green action accent
- Spotify attribution only where Spotify metadata appears

이렇게 잡으면 사용자는 익숙하게 느끼고, 프로젝트는 독립 서비스처럼 보인다.
