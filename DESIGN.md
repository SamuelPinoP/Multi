# Multi — Design System

> A single source of truth for how Multi looks, moves and reads.
> Everything below is implemented in `app/src/main/java/com/example/multi/ui/`.

---

## 1. Design principles

| Principle | What it means in Multi |
|-----------|------------------------|
| **One brand, every device** | Multi ships a fixed indigo identity instead of leaning on Android's dynamic (wallpaper) color. The hook for "use device colors" is left in the theme, but the default is the brand so the app is recognisable in a portfolio, a screenshot, or a friend's hand. |
| **Tone over shadow** | Elevation is expressed with Material 3 surface *tones* (`surfaceContainerLow → Highest`), not drop shadows. Cards sit on the page, they don't float above it. |
| **Type does the hierarchy** | Two typefaces, a tight display voice and a highly legible body voice, carry most of the visual hierarchy — before color or weight of borders. |
| **Motion is quick, never decorative-only** | 120 ms for feedback, 240 ms for content, 400 ms for hero moves. Every animation maps to a state change the user caused. |
| **Colour-coded map** | Each feature area (Notes, Goals, Events, Calendar) owns an accent. The home screen is literally a colour key to the app. |

---

## 2. Colour

### Seed

The entire Material 3 scheme is generated from one hue:

```
BrandIndigo = #4C3BCF
```

### Roles (implemented in `ui/theme/Color.kt` + `Theme.kt`)

Full light **and** dark tonal schemes are defined for every M3 role
(`primary`, `secondary`, `tertiary`, `error`, `background`, `surface`,
`surfaceVariant`, `surfaceContainer*`, `outline`, `inverse*`, …).

| Role | Light | Dark | Used for |
|------|-------|------|----------|
| `primary` | `#4A3BC4` | `#C7BFFF` | FABs, primary buttons, selected states, cursors |
| `primaryContainer` | `#E4DFFF` | `#3A29B4` | Prominent but non-critical actions |
| `background` / `surface` | `#FCF8FF` | `#131318` | App canvas |
| `surfaceContainerLow` | `#F6F2FA` | `#1B1B21` | Default card |
| `surfaceContainerHigh` | `#EAE7EF` | `#2A292F` | Raised / tonal card |
| `error` / `errorContainer` | `#BA1A1A` / `#FFDAD6` | `#FFB4AB` / `#93000A` | Destructive actions |

### Extended semantic colours (not in the M3 spec)

Defined as an `ExtendedColors` record, provided through
`LocalExtendedColors` and read via `MultiTheme.extended`:

| Token | Purpose |
|-------|---------|
| `success` / `successContainer` | "Done" — goal completion, saved confirmations. Greener than `tertiary`. |
| `warning` / `warningContainer` | Time-sensitive info — "3 days left" on a trashed note. |

### Segment accents

| Segment | Light accent | Feel |
|---------|--------------|------|
| Notes | `#1B6EF3` | ice blue |
| Events | `#B3261E` | lava red |
| Calendar | `#4C5B72` | slate |
| Weekly Goals | `#2E6B4F` | moss green |

Each has a matching `container` tone for chips and list-item avatars. These
line up with the existing textured medallion tiles, so the home wheel and the
rest of the app now speak the same colour language.

### Rule

> No screen hard-codes a hex value. Everything is `MaterialTheme.colorScheme.*`
> or `MultiTheme.extended.*`. The pre-redesign code had ~40 `Color(0xFF…)`
> literals (`Color.Red`, `Color.White`, `Color(0xFF4CAF50)`, `Color.Gray` …),
> many of which broke in dark mode — most notably the home shortcut bar, which
> was painted with a literal white background and near-black text.

---

## 3. Typography

Two variable fonts, bundled (`res/font/`), weights pulled from the `wght`
axis via `FontVariation`:

| Face | File | Role |
|------|------|------|
| **Space Grotesk** | `space_grotesk.ttf` (~130 KB) | `display*`, `headline*`, `titleLarge` — the branding + big-number voice. Slightly quirky, geometric, tight negative tracking. |
| **Inter** | `inter.ttf` (~875 KB) | `titleMedium`–`labelSmall`, all `body*` — everything the user reads or types. Exceptional at small sizes and in dense lists. |

The full M3 type scale is re-voiced in `ui/theme/Type.kt`. Display sizes use
`-1sp` to `-0.5sp` letter-spacing for a modern condensed feel; body/label keep
the open `0.15sp–0.5sp` tracking that keeps Inter readable on a phone.

If Google Play Services / the font provider is unavailable the platform
default is used automatically — no crash, no blank text.

---

## 4. Shape

`ui/theme/Shape.kt` maps the M3 shape slots to a generously rounded scale:

| Slot | Radius | Used for |
|------|--------|----------|
| `extraSmall` | 8 dp | chips (`Pill`), snackbars |
| `small` | 12 dp | text fields, menus |
| `medium` | 16 dp | list cards (`MultiCard`), dialogs |
| `large` | 22 dp | home shortcut tiles, sheets |
| `extraLarge` | 28 dp | full-bleed feature surfaces |

Because these are wired into `MaterialTheme.shapes`, every stock component
(Card, Menu, TextField, Chip, Dialog, FAB) picks them up for free.

---

## 5. Spacing

An 8-point scale (`ui/theme/Spacing.kt`), provided via `LocalSpacing`,
read as `MultiTheme.spacing.*`:

```
xxs 2 · xs 4 · sm 8 · md 12 · lg 16 · xl 24 · xxl 32 · xxxl 48
gutter 16   (screen edge)
fabClearance 96   (bottom padding so a FAB never covers the last list row)
```

Named steps instead of raw `dp` literals keep vertical rhythm consistent and
make intent obvious at the call site.

---

## 6. Motion

`ui/theme/Motion.kt`:

| Token | Value | Used for |
|-------|-------|----------|
| `DurationFast` | 120 ms | press states, checkbox ticks, chip toggles |
| `DurationMedium` | 240 ms | content fades, expand/collapse |
| `DurationSlow` | 400 ms | screen-level & dialog entrances |
| `EasingEmphasized` | `cubic(0.2, 0, 0, 1)` | hero motion — slow start, confident finish |

Press feedback uses a `Spring.DampingRatioMediumBouncy` scale (0.95×) — the
"tactile" half of the brief.

---

## 7. Components (`ui/components/`)

| Component | Replaces | Notes |
|-----------|----------|-------|
| `MultiCard` | ad-hoc `ElevatedCard` / `Surface` blocks | One tonal card. `selected` and `tonal` variants. No shadow. |
| `Pill` | inline `Surface` + `Text` chips, `WordCountChip` | Compact metadata chip; optional icon; optional accent tint. |
| `SectionHeader` | hand-rolled label rows | Small-caps `labelMedium`, optional trailing action. |
| `EmptyState` | 4 divergent "No X yet" blocks | Lottie + `headlineSmall` + supporting line + optional CTA button. |
| `MonogramAvatar` | inline `Surface(CircleShape)` + initial | Circular monogram in a segment accent. |
| `StatValue` | inline big-number columns | Space Grotesk value + caption. |

---

## 8. Screens redesigned in this pass

### Home (`Medallion.kt` + `HomeQuickActions.kt`) — reverted
The home screen was redesigned and then **rolled back to its original design on
request**. It keeps its pre-redesign layout, the original "sophisticated"
shortcut buttons, dynamic (wallpaper) colours and the platform default font, via
`LegacyMultiTheme` (`ui/theme/LegacyTheme.kt`). It is the one screen that does
**not** use the design system described here.

### Notes list (`NotesActivity.kt`)
- `MultiCard` rows: segment-accent monogram avatar, `titleMedium` title,
  two-line `bodyMedium` snippet, and a row of `Pill`s (date, word count, or an
  "Event"/"File" chip).
- Empty state → shared `EmptyState` with a "New note" CTA.
- Selection mode: animated contextual action bar; the delete button shows the
  selected count; share menu labels spell out the format (`Word (.docx)` …).
- FAB and action bar animate in/out (`scaleIn`/`fadeIn`).

### Note editor (`NoteEditorActivity.kt`)
- Metadata moved into `Pill`s at the top (created date; "N days left" as a
  `warning` pill for trashed notes).
- Title field: Space Grotesk, `textSize + 6`, semi-bold, with a real primary
  cursor colour.
- Body field: Inter with a 1.55× line height for comfortable long-form reading.
- Hairline `HorizontalDivider` (`outlineVariant`) instead of the default grey
  rule; generous bottom scroll padding so the last line clears the keyboard.

### Shared chrome (`SegmentBase.kt`)
- Flat `CenterAlignedTopAppBar` on `background` (no more heavy drop shadow and
  rounded-corner clip).
- **One-tap theme toggle** in the app bar (sun / moon), separate from the ⋮
  overflow — which now only appears on screens that actually have menu items.
- Content background is a barely-there `background → surfaceContainerLow`
  vertical wash.

### Every other screen
Inherits the new palette, type, shape and spacing automatically through
`MultiTheme`. Events, the three calendar screens, Weekly Goals, Record and the
trash screens were **not** hand-polished in this pass.

---

## 9. Accessibility

- Every colour pair in the scheme meets WCAG AA for its text size
  (`onX` on `X`, `onXContainer` on `XContainer`).
- Status/nav bar icon contrast is set from `background` luminance in `MultiTheme`
  (the home screen keeps the platform's automatic edge-to-edge scrims).
- Touch targets stay ≥ 48 dp (icon buttons, tiles, list rows).
- Icons that carry meaning have `contentDescription`; purely decorative icons
  pass `null`.
- Text uses `sp` throughout and respects the user's font-scale setting; the
  note editor additionally has an in-app size control (16–32 sp).

---

## 10. File map

```
ui/theme/
  Color.kt      brand seed, all M3 role tokens (light+dark), extended + segment colours
  Type.kt       Space Grotesk + Inter, full re-voiced M3 type scale
  Shape.kt      rounded shape scale → MaterialTheme.shapes
  Spacing.kt    8-pt scale + LocalSpacing
  Motion.kt     duration + easing tokens
  Theme.kt      MultiTheme(): wires schemes, extended colours, spacing, system bars
ui/components/
  MultiComponents.kt   MultiCard, Pill, SectionHeader, MonogramAvatar, StatValue
  EmptyState.kt        shared empty-state treatment
res/font/
  space_grotesk.ttf, inter.ttf   bundled variable fonts (OFL)
```
