# Repository guidance

## Architecture & Conventions

- Keep architecture details and tech stack breakdown documented in [docs/architecture.md](docs/architecture.md).
- **Clean Architecture & MVI/MVVM**: Maintain strict layer separation between UI (Compose), ViewModels, UseCases, Domain models, and Data/Repository implementations.
- **Dependency Injection**: Use Hilt (`@HiltViewModel`, `@Inject constructor(...)`, and Dagger modules in `di/`). Always inject `java.time.Clock` for date and time operations to allow deterministic unit testing.
- **Data Persistence & Room**: Keep Room entity definitions, DAOs, and schema exports (`app/schemas`) up to date. Any schema change requires an explicit versioned `Migration` in `DatabaseMigrations.kt` and a corresponding test in `AquaDatabaseMigrationTest.kt`.
- **Backup & Import Validation**: Sanitize and validate JSON backup imports in `ImportDataUseCase.kt` (ensuring positive amounts and non-blank dates) before writing to Room.
- **UI & Theme**: Use Jetpack Compose with Material 3 Expressive UI. Typography styles and font families are defined in `Type.kt` (`GoogleSansFlex`, `GoogleSansCustom`, `FontDisplayRound`, `AquaRounded`, `AquaSquare`).

## Testing & Quality Assurance

- Keep Android verification instructions and current test coverage documented in [docs/testing.md](docs/testing.md).
- Add or update regression tests whenever changing business logic, backup handling, or key user workflows.
- Use instrumented tests for Room behavior and Compose UI interactions; keep ViewModel and UseCase tests on the JVM with coroutine test dispatchers and fakes.
- A successful build alone is not verification. Run the relevant Gradle verification tasks:
  ```powershell
  ./gradlew.bat testDebugUnitTest
  ./gradlew.bat lintDebug
  ./gradlew.bat assembleDebug
  ./gradlew.bat jacocoTestReport
  ```
