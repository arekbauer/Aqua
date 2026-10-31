# Android testing strategy

## Current setup

Aqua is a single-module Android app built with Kotlin, Jetpack Compose, Room, and Hilt. Before this setup it had no test classes or CI workflow. Tests use JUnit 4. Compose behavior and Room/SQLite tests run as instrumented tests on an Android emulator. ViewModel tests run locally on the JVM with coroutine test dispatchers and a fake repository.

## Coverage added

- `TodayViewModelTest`
  - `loggingUpdatingDeletingAndUndoingEntriesUpdatesTodayState`: logs 250 ml, checks the daily total and undo message, updates the entry to 400 ml, deletes it, logs another entry and undoes it, then verifies stale message acknowledgements are ignored and the current message can be cleared.
  - `nonPositiveAmountsAreIgnored`: verifies that logging 0 ml does not create an entry.
- `TodayScreenTest`
  - `quickAddLogsConfiguredAmountAndShowsTodaySummary`: verifies the current intake and remaining goal are shown, and selecting the configured 200 ml quick-add button invokes the add callback with 200.
  - `customDrinkAmountCanBeAddedFromBottomSheet`: opens the drink picker, enters 425 ml, and verifies the add callback receives 425.
  - `logCanBeEditedThroughTheDialog`: opens an existing 150 ml log, changes it to 275 ml, and verifies the edit callback receives 275.
- `AquaDaoTest`
  - `entriesAreOrderedByTimeAndTotalsAreGroupedByDate`: verifies entries are filtered to the requested date and ordered newest-first, while daily totals are grouped and ordered by date.
  - `updatesAndDeletesChangeTheDailyTotal`: verifies updating an entry changes the stored daily total and deleting it removes the total.
- JaCoCo report for local JVM tests.

The current suite does not include screenshot baselines, full application navigation tests, or dedicated regression tests for Settings and History. The instrumented tests exercise the Today screen component and Room DAO directly; they do not yet run a complete Hilt-backed app journey.

## Run locally

From the repository root in PowerShell:

```powershell
./gradlew.bat testDebugUnitTest
./gradlew.bat lintDebug
./gradlew.bat assembleDebug
./gradlew.bat jacocoTestReport
./gradlew.bat connectedDebugAndroidTest
```

The emulator command needs a running Android emulator or connected device. JaCoCo HTML output is written under `app/build/reports/jacoco/jacocoTestReport/html/`.

## CI

`.github/workflows/android-ci.yml` runs on pull requests and pushes to any branch. It runs JVM tests, Android lint, a debug build, and a coverage report. The debug APK and quality reports are uploaded as workflow artifacts. Compose behavior and Room database tests are intentionally excluded from CI and can be run locally with `connectedDebugAndroidTest` on an emulator or connected device.

Visual screenshot references are not configured yet; there is no baseline directory.
