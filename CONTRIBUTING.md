# Contributing to Multi

## Prerequisites

* Android Studio Ladybug or newer
* JDK 21 (bundled with recent Android Studio as the JBR)
* Android SDK with API 35 installed

## Building & testing from the command line

```bash
# Unit tests (fast, no device needed)
./gradlew testDebugUnitTest

# Static analysis
./gradlew lintDebug

# Debug APK -> app/build/outputs/apk/debug/
./gradlew assembleDebug
```

CI (`.github/workflows/ci.yml`) runs all three on every push and pull request.

### Lint baseline

Pre-existing lint findings (deprecated APIs, etc.) are recorded in
`app/lint-baseline.xml`. Lint fails the build only on **new** issues. When you
knowingly clear an existing one, regenerate the baseline:

```bash
./gradlew updateLintBaseline
```

Do not add new entries to the baseline to silence your own code.

## Branching & PRs

* Branch off `master`, e.g. `feature/…`, `fix/…`, `chore/…`.
* Keep `main` deployable; open a pull request rather than pushing to it.
* A PR should build green in CI before review.

## Code conventions

* Kotlin official style (`kotlin.code.style=official`), 4-space indent.
* **New features follow MVVM**: `Activity` renders, `ViewModel` holds state and
  logic, a `Repository` interface owns data access. See [ARCHITECTURE.md](ARCHITECTURE.md).
* No data access from composables or `Activity` bodies — go through a repository.
* Every new piece of non-UI logic ships with a unit test. Prefer pure-JVM tests
  with a fake over Robolectric where possible.
* Room schema changes require a bumped `version` and a `Migration` (never rely on
  `fallbackToDestructiveMigration` for shipped schema).

## Commit messages

Short imperative subject line ("Add notes repository", not "Added"/"Adds").
Reference the area touched when useful ("notes:", "calendar:", "ci:").
