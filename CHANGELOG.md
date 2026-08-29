# Changelog

All notable changes to this project are documented here.
The format is loosely based on [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Added
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
