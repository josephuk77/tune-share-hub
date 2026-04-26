# Design Review Report

작성일: 2026-04-26

## Scope

Reviewed:
- `DESIGN.md`
- `docs/design-system-preview.html`

No React app or live site exists yet, so this review focused on the design system source of truth and the static preview artifact.

## Verdict

STATUS: DONE_WITH_CONCERNS

The design direction is good: dark, dense, album-first, and close enough to a modern music app to feel familiar. It avoids direct Spotify branding in the core identity.

The preview needed polish because it was starting to drift into generic AI mockup territory: unloaded Korean font, decorative gradient covers, missing mobile navigation, and weak keyboard focus states.

## Findings Fixed

### 1. Pretendard was declared but not loaded

Impact:
Korean text would fall back to whatever the browser has available. That makes the preview less reliable and weakens the typography decision in `DESIGN.md`.

Fix:
Added the Pretendard webfont stylesheet to `docs/design-system-preview.html`.

### 2. Cover placeholders looked like decorative gradients

Impact:
The design system says album art should carry the color, but the preview used broad gradient blocks. That reads like generic generated UI instead of an album-first music product.

Fix:
Changed playlist covers and the detail hero cover into 2x2 album-collage placeholders. These are still synthetic, but they model the final layout more honestly.

### 3. Keyboard focus states were missing

Impact:
Keyboard users could tab through controls without a strong visible focus indicator.

Fix:
Added `:focus-visible` outlines for buttons, links, and inputs.

### 4. Mobile shell was incomplete

Impact:
`DESIGN.md` specifies mobile bottom navigation, but the preview only hid the sidebar. That left mobile without the intended navigation pattern.

Fix:
Added a fixed bottom navigation in the preview and adjusted mobile topbar layout.

### 5. Mobile track actions were too wide

Impact:
The full "Spotify에서 보기" label competes with the track title on narrow screens.

Fix:
Shortened the mobile action label to "열기" while preserving the desktop label.

## Remaining Concerns

- The preview still uses CSS-generated placeholder covers because no real album images are available yet. Once the React app is implemented, use actual Spotify album art and keep it unmodified.
- I could not produce screenshot evidence because there is no running app and the local browser automation workflow is not set up for this static file in this session.
- The final app should be reviewed again after React screens exist, especially the playlist editor and detail page.

## Updated Files

- `docs/design-system-preview.html`
- `docs/design-review-report.md`

## Score

Before: 7/10

After: 8/10

The biggest remaining jump will come from using real album imagery and testing the real React implementation across desktop and mobile.
