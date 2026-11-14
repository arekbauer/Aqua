# Future Features & Roadmap

This document outlines planned features, UI enhancements, and long-term milestones for Aqua.

## Short-Term Milestones

### 1. Home Screen Widgets (Glance AppWidget)
- Quick-log widgets for home screen using `androidx.glance`.
- Real-time progress ring & preset logging buttons directly from the home screen.

### 2. Hydration Reminders & Local Notifications
- Customizable periodic drink reminders.
- Smart notification actions (Quick-add 250ml directly from notification bar).

## Medium-Term Features

### 3. Smart Daily Goal Recommendation & Onboarding Wizard
Interactive goal setup wizard based on personal characteristics:
- Weight & Height
- Biological Sex
- Daily Workout / Activity Level
- Local Climate / Temperature
- Dietary factors & Supplements

### 4. Custom Serving Presets
- User-editable drink types and custom preset icons (e.g. Tea, Coffee, Electrolytes, Juice).

## Long-Term Vision

### 5. Wear OS Companion App
- Wear OS app built with `androidx.wear.compose.material3`.
- Tiles and Complications for quick intake logging from the watch face.

**Goal:** one-tap logging of the user's 3 presets from the wrist; the phone stays the source of truth.

**Design summary**
- Offline-first watch: each tap writes an intake event (client-generated UUID) to a local queue.
- Watch → phone: append-only events over the Wear Data Layer `DataClient` (persisted, delivered on reconnect).
- Phone → watch: a single state snapshot (today's total, goal, presets, unit, acknowledged event ids).
- The phone de-duplicates by a unique `externalId` on `intake_entries` (Room v4 → v5).
- The watch app uses the same `applicationId` (`com.arekb.aqua`) and signing key as the phone app.
- `play-services-wearable` sits behind small interfaces (`WatchStateSink` on the phone; an equivalent gateway on the watch) so the domain layer stays free of proprietary APIs.
- Protocol details: [wear-sync.md](wear-sync.md).

**Progress** (update this table as each phase lands)

| Phase | Scope | Status |
|---|---|---|
| 1 | Extract shared `:core:model` module (no behavior change) | ✅ Done |
| 2 | Phone-side sync: Room v5 migration, `RecordWatchIntakeUseCase`, `WearSyncService`, state publisher, tests | ✅ Done |
| 3 | `:wear` app MVP: progress ring + 3 preset buttons, offline queue | ⬜ Not started |
| 4 | Wear Tile with one-tap presets | ⬜ Not started |
| 5 | Complication (ranged progress) — *outside v1 scope* | ⬜ Not started |
| 6 | Polish: watch undo, phone sync-status row, localization, Play Store setup, docs | ⬜ Not started |

**Phase notes**

- **Phase 1 — done.**
  - New `:core:model` Android library (`core/model`) now holds `HydrationModels.kt` (`WaterIntake`, `HydrationSettings`, `LiquidUnit` + ml/oz conversions, history models). The package is unchanged (`com.arekb.aqua.domain.model`), so no import changes were needed.
  - `:app` depends on it via `implementation(project(":core:model"))`; `android-library` plugin added to the version catalog.
  - Cross-module smart casts on `HistoryDay.goalMl` were replaced with local values in `HistoryInsightsCalculator` and `HistoryDayRow`.
  - Added `HydrationModelsTest` (5 tests). Verified: `testDebugUnitTest` (22 app + 5 core tests), `lintDebug`, `assembleDebug`, `jacocoTestReport`, and `compileDebugAndroidTestKotlin` all pass.
  - `BackupModels.kt` stays in `:app` (phone-only).
  - JaCoCo still reports only `:app` classes; the moved models are covered by `:core:model` tests but not included in that report.

- **Phase 2 — done.**
  - **Contract (`:core:model`, package `com.arekb.aqua.sync`):** `WearSyncContract` (paths, limits, ack window), `IntakeEvent`, `WatchState`, and `WearSyncCodec` (JSON, tolerant of unknown keys, returns null for malformed input). `WaterIntake` gained an optional `externalId`. Documented in [wear-sync.md](wear-sync.md).
  - **Room v4 → v5:** nullable `externalId` column plus a unique index (`MIGRATION_4_5`, schema `5.json` exported). `AquaDao.insertEntryIfAbsent` (IGNORE) and `observeExternalIdsSince`.
  - **Repository:** new `WatchIntakeRepository` interface (kept separate from `HydrationRepository` so existing fakes are unaffected), implemented by `RoomHydrationRepository` and bound in Hilt.
  - **Use cases:** `RecordWatchIntakeUseCase` (validation + de-duplicated insert, returns `STORED` / `DUPLICATE` / `REJECTED`), `ObserveWatchStateUseCase` (snapshot flow with midnight roll-over, `Clock` and `ZoneId` injected), `WatchStatePublisher`.
  - **Android plumbing:** `WearSyncService` (`WearableListenerService`, declared in the manifest for `/aqua/intake`), `DataClientWatchStateSink` (the only class using Play services), `WearSyncModule`, publisher started from `AquaApplication`. Added `play-services-wearable` 20.0.1 and `kotlinx-coroutines-play-services`.
  - **Backups:** `BackupWaterIntake.externalId` (optional, blank treated as missing) so restoring a backup cannot re-admit already-synced watch events; old backups still decode.
  - **Tests:** 11 `:core:model` (codec + models), 45 `:app` JVM tests (23 new: use case validation and de-duplication, snapshot content, ack window, roll-over, publisher resilience, backup mapping), 7 new instrumented tests (migration 4 → 5 incl. unique index, DAO, repository). Verified: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `jacocoTestReport`, and the full `connectedDebugAndroidTest` suite (40 passed on an emulator).
  - **Not verified yet:** an end-to-end watch ↔ phone exchange; that needs the `:wear` app (Phase 3) and paired emulators. On an emulator without the Wearable API the app starts normally and the sink logs a single warning per publish.

---

*Suggestions and contributions are welcome. See [CONTRIBUTING.md](../CONTRIBUTING.md) to get involved.*
