# Tech Stack & Architecture

Aqua is built following Android architecture guidelines (Clean Architecture + Unidirectional Data Flow with MVI/MVVM).

## Tech Stack

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose with Material 3 Expressive UI (`androidx.compose.material3`)
- **Navigation**: Jetpack Navigation 3 (`androidx.navigation3`)
- **Dependency Injection**: Hilt (`com.google.dagger:hilt-android`)
- **Database & Persistence**: Room (`androidx.room`) with schema exports and versioned migrations
- **Serialization**: Kotlinx Serialization (`kotlinx-serialization-json`)
- **Testing**: JUnit 4, Compose UI Testing, Room Migration Testing, and JaCoCo coverage reports

## Architecture Overview

The codebase is organized into distinct layers to enforce separation of concerns and testability:

```
core/model/src/main/java/com/arekb/aqua/
 ├── domain/model/HydrationModels.kt   # Shared pure-Kotlin domain models and ml/oz conversions (module :core:model)
 └── sync/                             # Wear sync contract: paths, IntakeEvent, WatchState, JSON codec

app/src/main/java/com/arekb/aqua/
 ├── data/            # Room database, Entities, DAOs, Migrations, Repository implementations, and data/wear (Wear Data Layer)
 ├── di/              # Hilt Modules (DatabaseModule, HydrationRepositoryModule, UseCaseModule)
 ├── domain/          # Backup models, Repository interfaces, UseCases, and Insights Calculator
 └── ui/              # Navigation, Screens (Today, History, Settings), Components, and Theme
```

### Modules

| Module | Type | Purpose |
|---|---|---|
| `:app` | Android application | Phone app (UI, Room, use cases) |
| `:core:model` | Android library | Domain models and the Wear sync contract shared by the phone app and the planned Wear OS app. Keep it free of UI, Room and Hilt dependencies. |

The planned Wear OS companion (`:wear`) is tracked in [future.md](future.md); the phone/watch protocol is described in [wear-sync.md](wear-sync.md).

### Unidirectional Data Flow (UDF)

```mermaid
flowchart TD
    UI["Compose UI (TodayScreen / HistoryScreen / SettingsScreen)"]
    VM["ViewModel (TodayViewModel / HistoryViewModel / SettingsViewModel)"]
    UC["UseCases (AddWaterUseCase / HistoryInsightsCalculator / ImportDataUseCase)"]
    Repo["HydrationRepository (RoomHydrationRepository)"]
    DAO["Room AquaDao & SQLite (aqua.db)"]

    UI -- "User Actions (e.g. logWater)" --> VM
    VM -- "Invoke UseCases / Repository" --> UC
    UC -- "Data operations" --> Repo
    Repo -- "Observe / Insert / Update" --> DAO
    DAO -- "Flow of Intake & Settings Entities" --> Repo
    Repo -- "Domain Models (WaterIntake, HydrationSettings)" --> VM
    VM -- "StateFlow of UiState" --> UI
```

## Local Backup & Restore Pipeline

```mermaid
flowchart LR
    File["Local JSON File (aqua_backup_*.json)"]
    ImportUC["ImportDataUseCase (Input Validation)"]
    ExportUC["ExportDataUseCase (JSON Encoder)"]
    Repo["HydrationRepository"]
    DB["AquaDatabase (Room)"]

    File -- "Raw InputStream" --> ImportUC
    ImportUC -- "Validate & Restore" --> Repo
    Repo -- "Clear & Restore All Tables" --> DB

    DB -- "Get all entries & settings" --> Repo
    Repo -- "Gather domain data" --> ExportUC
    ExportUC -- "Encoded OutputStream" --> File
```

## Key Architectural Principles

1. **Unidirectional Data Flow (UDF)**: ViewModels expose reactive state via `StateFlow` collected with `collectAsStateWithLifecycle()` in Compose.
2. **Injectable Clock**: `java.time.Clock` is injected via Hilt across ViewModels and UseCases for deterministic date/time handling and testing.
3. **Database Schema Migrations**: Explicit Room migrations (`MIGRATION_1_2` … `MIGRATION_4_5`) ensure seamless data upgrades without data loss.
4. **Data Validation & Privacy**: All local backup exports/imports use type-safe JSON serialization with input validation, keeping 100% of data on-device.
