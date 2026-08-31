# Changelog

All notable changes to this project are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added
* **Design system** (`ui/theme/` + `ui/components/`), documented in `DESIGN.md`.
  * Full Material 3 light **and** dark colour schemes generated from a single
    indigo brand seed (`#4C3BCF`), plus extended `success` / `warning` roles and
    four per-feature "segment" accents.
  * Bundled variable-font pairing — **Space Grotesk** (display) + **Inter**
    (body) — with the M3 type scale re-voiced around them.
  * Rounded shape scale, an 8-pt spacing scale (`MultiTheme.spacing`), and
    motion tokens (`MultiMotion`).
  * Reusable components: `MultiCard`, `Pill`, `SectionHeader`, `EmptyState`,
    `MonogramAvatar`, `StatValue`.
* One-tap light/dark toggle in every screen's app bar.

### Changed
* **Notes list and Note editor redesigned** to a portfolio finish; every other
  screen (except the home screen) inherits the new theme automatically.
* The **home screen keeps its original design** — layout, "sophisticated"
  shortcut buttons, dynamic wallpaper colours and the platform font are
  preserved via `LegacyMultiTheme`; it is the one screen that opts out of the
  design system.
* App bar flattened (no drop shadow / corner clip); cold-start window
  background now matches the Compose canvas so there is no colour flash.
* `MultiTheme` defaults to the brand palette instead of dynamic colour (the
  dynamic-colour hook is retained for a future setting).
* **MVVM + repository architecture for the Notes feature.**
  * `NotesRepository` interface with a Room-backed implementation
    (`RoomNotesRepository`) and an in-memory `FakeNotesRepository` for tests.
  * `NotesViewModel` exposing a single `StateFlow<NotesUiState>`; the notes list
    is now reactive (driven by a Room `Flow`) instead of reloaded in `onResume`.
  * `ServiceLocator` as a lightweight dependency-injection entry point.
* **Continuous integration** (`.github/workflows/ci.yml`): unit tests, Android
  Lint and a debug APK build on every push and pull request, with reports and the
  APK uploaded as artifacts.
* **Lint baseline** (`app/lint-baseline.xml`) capturing pre-existing findings so
  CI gates new regressions without a big-bang cleanup.
* **Developer docs**: `ARCHITECTURE.md` (with diagram), `CONTRIBUTING.md`,
  this changelog, and a pull-request template.
* Expanded unit-test suite: `NotesViewModelTest`, `RoomNotesRepositoryTest`,
  `TextUtilsTest`, `DateUtilsTest`, plus more `EventUtils` / `TextMetrics` cases.

### Changed
* `NotesActivity` reduced to a thin Compose renderer that collects
  `NotesViewModel.uiState` and forwards user intents.
* `TextMetrics.wordCount` now treats URLs and e-mail addresses as a single word
  and short-circuits blank input.

### Fixed
* `TextMetricsTest.urlsEmojisAndPunctuation_notOverCounted` was asserting an
  impossible count and failed the whole `testDebugUnitTest` task; the metric and
  its expectations are now consistent.
