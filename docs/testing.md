# Android testing strategy

## Current setup

Aqua is an Android app (`:app`, plus the shared `:core:model` library) built with Kotlin, Jetpack Compose, Room, and Hilt. Before this setup it had no test classes or CI workflow. Tests use JUnit 4. Compose behavior and Room/SQLite tests run as instrumented tests on an Android emulator. ViewModel tests run locally on the JVM with coroutine test dispatchers and a fake repository.

## Coverage added

- `TodayViewModelTest`
  - `loggingUpdatingDeletingAndUndoingEntriesUpdatesTodayState`: logs 250 ml, verifies daily total updates without snackbar popup, updates the entry to 400 ml, deletes it, logs another entry and undoes it, then verifies stale message acknowledgements are ignored and current messages can be cleared.
  - `nonPositiveAmountsAreIgnored`: verifies that logging 0 ml does not create an entry.
- `TodayScreenTest`
  - `quickAddLogsConfiguredAmountAndShowsTodaySummary`: verifies the current intake and remaining goal are shown, and selecting the configured 200 ml quick-add button invokes the add callback with 200.
  - `customDrinkAmountCanBeAddedFromBottomSheet`: opens the drink picker, enters 425 ml, and verifies the add callback receives 425.
  - `logCanBeEditedThroughTheDialog`: opens an existing 150 ml log, changes it to 275 ml, and verifies the edit callback receives 275.
- `AquaDaoTest`
  - `entriesAreOrderedByTimeAndTotalsAreGroupedByDate`: verifies entries are filtered to the requested date and ordered newest-first, while daily totals are grouped and ordered by date.
  - `updatesAndDeletesChangeTheDailyTotal`: verifies updating an entry changes the stored daily total and deleting it removes the total.
- `HistoryInsightsCalculatorTest`
  - Verifies seven-day totals include no-log days, averages include zero-intake days, goal changes apply from their effective local date, and goal status distinguishes met, below, unavailable, no logs, and today in progress.
  - Verifies the 30-day first-drink median and highest-volume time of day appear only after seven logged days, and the longest same-day gap is measured over the last seven completed days.
- `HistoryScreenTest`
  - Verifies summary metrics, no-log chart legend, daily goal statuses, pattern cards, insufficient-history messaging, and the empty daily log.
- `AquaDaoTest` and `AquaDatabaseMigrationTest`
  - Verify effective-dated goal history replacement, inclusive date-range entry queries, and the v1-to-v2 migration preserving existing entries while starting goal history on the migration date.
- `BackupDataTest`
  - `exportAndImportDataRestoresFullBackup`: verifies gathering settings, goal history, and water intake records into an `AquaBackupData` structure, serializing to JSON, and restoring the complete backup into a fresh repository instance.
- `SettingsViewModelTest`
  - `exportImportAndWipeActionsUpdateUiStateAndMessage`: verifies exporting data, wiping all history, and importing data update the ViewModel state and emit appropriate user feedback messages.
- `SettingsScreenTest`
  - `mainScreenShowsAllCategoriesAndNavigates`: verifies all categories are displayed and their navigation callbacks are invoked when clicked.
  - `appearanceScreenThemeSelectionUpdatesState`: verifies selecting a theme invokes the callback with the chosen theme.
  - `preferencesScreenPresetChangeTriggersSave`: verifies modifying a quick add preset triggers a save via `LaunchedEffect`.
- `AquaAppIntegrationTest`
  - `changingGoalAndPresetsUpdatesTodayScreen`: modifies quick add presets in settings and verifies they update on the Today screen.
  - `changingUnitsUpdatesTodayScreen`: switches liquid units from ml to oz in settings and verifies the Today screen goal and quick add calculations reflect oz.
  - `loggingWaterAppearsInHistory`: logs water intake on the Today screen and navigates to History to verify it appears in the daily log.
- `HydrationUseCasesTest`
  - Verifies `AddWaterUseCase` validation and timestamp calculation, `SaveHydrationSettingsUseCase` goal/preset validations, `ResetDailyIntakeUseCase`, and `ClearAllHistoryUseCase`.
- `AquaNavigationStateTest`
  - Verifies multi-backstack state logic, sub-destination navigation, and returning to the Today tab when popping top-level destinations.
- `SettingsDataManagementScreenTest`
  - Verifies UI rendering of backup/restore and danger zone sections, and clicking export, import, wipe, and back buttons.
- `SettingsAboutScreenTest`
  - Verifies UI rendering of app info, developer details, open-source license info, and opening the licenses bottom sheet.
- `HydrationModelsTest` (`:core:model`)
  - Verifies ml/oz conversion rounding in both directions, unit labels, and that default settings expose exactly three presets.
- `WearSyncCodecTest` (`:core:model`)
  - Verifies `IntakeEvent` / `WatchState` JSON round trips, the short `v` version key, tolerance of unknown keys, null results for malformed payloads, and path helpers.
- `RecordWatchIntakeUseCaseTest`
  - Verifies a valid watch event is stored once, re-delivery is reported as a duplicate, and invalid ids, amounts, dates, timestamps and date/timestamp mismatches are rejected without reaching the repository; also covers decoding from raw payloads.
- `ObserveWatchStateUseCaseTest`
  - Verifies snapshot contents (today's total, goal, presets, unit), defaults before settings exist, the 7-day acknowledgement window, re-emission on changes, no duplicate emissions, and midnight roll-over using virtual time.
- `WatchStatePublisherTest`
  - Verifies the initial snapshot and each change are published, a failing publish does not stop later updates, and acknowledgements are published.
- `BackupExternalIdTest`
  - Verifies `externalId` survives backup mapping, blank ids are treated as missing, and pre-sync backups still decode.
- `AquaDatabaseMigrationTest` (v4 → v5)
  - Verifies existing entries are kept with a null `externalId`, duplicate external ids violate the unique index, and multiple null ids are allowed.
- `AquaDaoTest` / `RoomHydrationRepositoryTest` (watch sync)
  - Verify `insertEntryIfAbsent` ignores repeated external ids without changing totals, local entries never collide, `observeExternalIdsSince` skips local and older entries, and restoring a backup keeps external ids so replayed events stay de-duplicated.
- JaCoCo report for local JVM tests (covers `:app` classes only).

The current suite does not include screenshot baselines. The instrumented tests exercise screen components, Room DAO behavior, database migrations, and Hilt-backed end-to-end user flows.

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

## CI & Release Workflows

- **`.github/workflows/ci.yml`**: Runs on pull requests and pushes to `main`. Executes JVM unit tests, Android lint, JaCoCo coverage, and debug assemble.
- **`.github/workflows/release.yml`**: Runs when version tags (`v*`) are pushed. Decodes the base64 release keystore, builds signed release APK (`.apk`) and Android App Bundle (`.aab`), and publishes them to GitHub Releases using `softprops/action-gh-release@v2`.

To sign release builds with a custom production key in GitHub Actions, set these Repository Secrets under **Settings -> Secrets and variables -> Actions**:
- `RELEASE_KEYSTORE_BASE64`: Base64-encoded string of your keystore file (`[Convert]::ToBase64String([System.IO.File]::ReadAllBytes('release.keystore'))`).
- `KEYSTORE_PASSWORD`: Keystore password.
- `KEY_ALIAS`: Key alias name.
- `KEY_PASSWORD`: Key password.

If repository secrets are not configured, local release builds automatically fall back to signing with debug keys so builds never fail.

Visual screenshot references are not configured yet; there is no baseline directory.
