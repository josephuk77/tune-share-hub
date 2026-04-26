# Design System — Playlist Community

## Product Context

Playlist Community is a Korean playlist sharing service. Users log in with pre-registered accounts, search Spotify tracks, build playlists in this service, and interact through public playlist views, comments, likes, and later copy/recommendation features.

The UI should feel familiar to users of modern music apps, especially Spotify-like dark listening surfaces, but it must remain an independent product. Spotify metadata and album art are content sources, not our brand.

## Aesthetic Direction

**Direction name:** Carbon Pulse

Dark, dense, album-first, and built for repeat use. The product should feel like a music workspace rather than a marketing page.

Core traits:
- Dark surfaces with clear hierarchy.
- Album art provides most of the color.
- Green is used for actions, not as a copied brand identity.
- Layouts prioritize scanning playlists and tracks quickly.
- Community actions, likes, comments, copy, and ownership should be visible without overwhelming the music.

Avoid:
- Spotify logo or logo-like symbols as app branding.
- App names containing "Spotify".
- Green-only visual identity.
- Giant landing-page hero sections.
- Decorative gradients or floating color blobs.
- Cropping or overlaying Spotify album artwork in ways that alter it.

## Typography

### Display/Hero

**Plus Jakarta Sans** — rounded, contemporary, and strong enough for playlist titles without looking like Spotify's brand typography.

Use for:
- Page titles
- Playlist detail title
- Empty state title
- Modal title

Weights:
- 700 for page titles
- 600 for section headers

### Body

**Pretendard** — excellent Korean UI readability, neutral rhythm, and widely used in Korean web products.

Use for:
- Body text
- Forms
- Comments
- Navigation
- Buttons

Weights:
- 400 body
- 500 controls
- 700 important labels

### UI/Labels

**Pretendard** — same as body. Keep the UI compact and consistent.

### Data/Tables

**Roboto Mono** — use only for technical or numeric values where alignment matters, such as duration, counts, API ids in admin/debug views.

Use `font-variant-numeric: tabular-nums;` for counts and durations.

### Code

**Roboto Mono**

### CSS Font Stack

```css
:root {
  --font-display: "Plus Jakarta Sans", "Pretendard", sans-serif;
  --font-body: "Pretendard", "Noto Sans KR", sans-serif;
  --font-mono: "Roboto Mono", monospace;
}
```

## Color

### Palette

| Token | Hex | Usage |
| --- | --- | --- |
| `--color-bg` | `#101010` | App background |
| `--color-bg-elevated` | `#141414` | Sidebar and top bar |
| `--color-surface` | `#1A1A1A` | Cards and panels |
| `--color-surface-hover` | `#252525` | Hovered cards and rows |
| `--color-surface-active` | `#2D2D2D` | Selected nav item or active tab |
| `--color-border` | `#2E2E2E` | Dividers and input borders |
| `--color-text` | `#FFFFFF` | Primary text |
| `--color-text-secondary` | `#B8B8B8` | Secondary text |
| `--color-text-muted` | `#7C7C7C` | Metadata |
| `--color-accent` | `#22C55E` | Primary action |
| `--color-accent-hover` | `#35D46F` | Primary action hover |
| `--color-accent-pressed` | `#16A34A` | Primary action pressed |
| `--color-cyan` | `#38BDF8` | Secondary highlight, links, Spotify attribution |
| `--color-violet` | `#A78BFA` | Mood/recommendation badges |
| `--color-danger` | `#F87171` | Delete and destructive errors |
| `--color-warning` | `#FACC15` | Warnings |
| `--color-success-soft` | `#12351F` | Success background |

### Rules

- Green appears on primary actions, active states, and confirmation feedback.
- Cyan is for metadata links and Spotify attribution. This separates "open in Spotify" from "do action in our app".
- Album covers must remain the dominant visual color in cards and detail pages.
- Do not use green gradients as a background theme.
- Do not use Spotify logo green as the app logo or brand mark.

## Spacing

Use a 4px base grid.

| Token | Value | Usage |
| --- | --- | --- |
| `--space-1` | `4px` | Tight icon gaps |
| `--space-2` | `8px` | Compact row gaps |
| `--space-3` | `12px` | Form field spacing |
| `--space-4` | `16px` | Card padding |
| `--space-5` | `20px` | Card grid gap |
| `--space-6` | `24px` | Page section gap |
| `--space-8` | `32px` | Major vertical rhythm |
| `--space-10` | `40px` | Detail hero gap |

## Radius

| Token | Value | Usage |
| --- | --- | --- |
| `--radius-sm` | `4px` | Inputs, badges |
| `--radius-md` | `8px` | Cards, modals, panels |
| `--radius-lg` | `12px` | Album collage containers |
| `--radius-pill` | `999px` | Primary CTA, play button |

Cards should default to 8px. Do not nest cards inside cards.

## Layout

### Desktop App Shell

```text
┌─────────────────────────────────────────────────────────┐
│ Top Bar: Search / Current User                          │
├───────────────┬─────────────────────────────────────────┤
│ Sidebar 240px │ Main Content                            │
│               │ Playlist grid / detail / editor         │
└───────────────┴─────────────────────────────────────────┘
```

Desktop constants:
- Sidebar width: `240px`
- Top bar height: `64px`
- Main padding: `24px`
- Card grid min width: `180px`
- Card grid ideal width: `minmax(180px, 1fr)`
- Detail content max width: `1180px`

### Mobile App Shell

```text
Top App Bar
Search / Page Actions
Main Content
Bottom Navigation
```

Mobile constants:
- App bar height: `56px`
- Bottom nav height: `64px`
- Page padding: `16px`
- Playlist cards: 2 columns when possible, 1 column below 360px
- Track rows: cover, title/artist, action menu only

## Components

### Playlist Card

Purpose: fast scanning of public playlists.

Structure:
- Square cover or 2x2 album collage.
- Title, max 2 lines.
- Author nickname.
- Metadata row: track count, likes, comments.
- Optional mood/tag badge in later phases.
- Hover play/preview button in lower right.

Behavior:
- Hover background changes from `surface` to `surface-hover`.
- Click card opens detail.
- Play/preview button only appears if a previewable track exists.

### Track Row

Desktop columns:

```text
No | Cover | Title / Artist | Album | Duration | Actions
```

Mobile columns:

```text
Cover | Title / Artist | Actions
```

Rules:
- Do not crop album art aggressively.
- Spotify link is visible in actions or row menu.
- `preview_url` missing means no preview button. Do not show disabled noise unless the user needs to know why.

### Search Result Row

Used in Spotify track search.

Structure:
- Album cover
- Track title
- Artist
- Album
- Add button

States:
- Loading skeleton
- Empty result
- API failure
- Already added

### Buttons

Primary:
- Background `accent`
- Text `#06120A`
- Height 40px desktop, 44px mobile
- Radius pill

Secondary:
- Transparent or `surface`
- Border `border`
- Text `text`

Danger:
- Transparent with danger text by default
- Filled red only inside destructive confirmation modals

Icon buttons:
- Square 40px desktop, 44px mobile
- Use familiar icons for like, comment, copy, edit, delete, play, search, close.

### Forms

Inputs:
- Background `#121212`
- Border `border`
- Focus border `accent`
- Height 44px
- Radius 8px

Textarea:
- Minimum height 112px
- Resize vertical only

Validation:
- Error text below field
- Do not use only red border to communicate errors.

## Screens

### Login

Goal: direct access for pre-registered users.

Layout:
- Centered login panel, max width 420px.
- App name, short subtitle, email, password, login button.
- No signup CTA.

Copy:
- "등록된 계정으로 로그인"
- "계정은 관리자에게 문의하세요"

### Home / Public Playlists

Goal: first screen is actual product usage, not a landing page.

Layout:
- Top search bar.
- Sort segmented control: 최신순, 조회순, 좋아요순, 댓글순.
- Playlist grid.
- Empty state for no public playlists.

### Playlist Detail

Goal: music and community on one page.

Layout:
- Hero with cover/collage, title, author, description, counts.
- Action row: like, copy later, edit/delete if owner.
- Track list.
- Comments panel below or right side on wide desktop.

### Playlist Editor

Goal: build a playlist without losing track context.

Desktop:
- Left panel: title, description, public/private.
- Right panel: Spotify search.
- Bottom/full-width section: selected track list with ordering.

Mobile:
- Step 1: details.
- Step 2: search tracks.
- Step 3: reorder/review.
- Step 4: save.

### My Page

Goal: quick access to personal activity.

Tabs:
- 내 플레이리스트
- 좋아요한 플레이리스트
- 내가 쓴 댓글

## Motion

Keep motion small and fast.

| Interaction | Motion |
| --- | --- |
| Card hover | 120ms background and slight translateY(-2px) |
| Button press | 80ms scale to 0.98 |
| Modal open | 160ms opacity + translateY(8px) |
| Toast | 180ms slide/fade |
| Track add | 160ms row highlight |

Avoid:
- Long page transitions.
- Decorative bouncing.
- Autoplaying visual effects.

## Accessibility

- Minimum touch target: 44px on mobile.
- Text contrast target: 4.5:1 for normal text.
- Focus rings must be visible on keyboard navigation.
- Like state must change icon shape/fill, not only color.
- Preview buttons need aria-label with track title.
- Album images need alt text using track or album name.
- Search results must be keyboard reachable.

## Spotify Compliance

Use Spotify as a data/content source, not as the app's identity.

Required:
- Show "Spotify에서 보기" for Spotify tracks.
- Attribute Spotify content where Spotify metadata or artwork appears.
- Keep album artwork visually intact.
- Do not enable audio download.
- Do not imply official Spotify endorsement.

Forbidden:
- App name containing Spotify.
- Logo that resembles Spotify's circle/waves.
- Spotify logo as our app logo.
- Cropping album art into decorative shapes that alter the visual content.
- Placing our logo or text directly on Spotify album artwork.

Sources:
- https://developer.spotify.com/documentation/design
- https://developer.spotify.com/policy

## CSS Tokens

```css
:root {
  --font-display: "Plus Jakarta Sans", "Pretendard", sans-serif;
  --font-body: "Pretendard", "Noto Sans KR", sans-serif;
  --font-mono: "Roboto Mono", monospace;

  --color-bg: #101010;
  --color-bg-elevated: #141414;
  --color-surface: #1a1a1a;
  --color-surface-hover: #252525;
  --color-surface-active: #2d2d2d;
  --color-border: #2e2e2e;
  --color-text: #ffffff;
  --color-text-secondary: #b8b8b8;
  --color-text-muted: #7c7c7c;
  --color-accent: #22c55e;
  --color-accent-hover: #35d46f;
  --color-accent-pressed: #16a34a;
  --color-cyan: #38bdf8;
  --color-violet: #a78bfa;
  --color-danger: #f87171;
  --color-warning: #facc15;
  --color-success-soft: #12351f;

  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;

  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-pill: 999px;
  --sidebar-width: 240px;
  --topbar-height: 64px;
}
```

## Decisions Log

- Chose a dark music app interface because the core content, album art and track lists, benefits from a low-glare background.
- Chose Plus Jakarta Sans for display to avoid a generic system-font look while staying modern and readable.
- Chose Pretendard for Korean body/UI because the service is Korean-first and dense UI needs strong Hangul rendering.
- Chose `#22C55E` as accent, close enough to feel musical and energetic, different enough to avoid copying Spotify's brand.
- Kept tags, recommendations, and mood badges visually planned but not required for MVP.
- Kept Spotify attribution as an explicit UI rule so implementation does not accidentally imply endorsement.

## Design System

Always read `DESIGN.md` before making visual or UI decisions.

All font choices, colors, spacing, layout, motion, Spotify attribution rules, and aesthetic direction are defined here. In QA, flag any screen that drifts into generic landing-page UI, Spotify brand copying, or album art misuse.
