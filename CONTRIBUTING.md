# Contributing to Aqua 💧

Thank you for your interest in contributing to Aqua! Aqua is a free, open-source, local-first hydration tracking app for Android.

We welcome contributions of all kinds: bug fixes, new features, UI/UX improvements, documentation, and localization.

---

## 🚀 Getting Started

1. **Fork & Clone**: Fork the repository on GitHub and clone your fork locally:
   ```bash
   git clone https://github.com/your-username/Aqua.git
   cd Aqua
   ```
2. **Environment**: Ensure you have Android Studio Ladybug (2024.2.1) or newer installed with JDK 17 and Android SDK 37.
3. **Build**: Open the project in Android Studio or verify the build from PowerShell / Terminal:
   ```powershell
   ./gradlew.bat assembleDebug
   ```

---

## 🛠️ Development Guidelines

### Code Style & Architecture
- **Language**: Idiomatic Kotlin.
- **Architecture**: Follow Clean Architecture and Unidirectional Data Flow (`UI -> ViewModel -> UseCase -> Repository -> Room DAO`).
- **Dependency Injection**: Use Hilt for DI (`@HiltViewModel`, `@Inject constructor(...)`). Always inject `java.time.Clock` when dealing with dates/time.
- **UI**: Write declarative UI using Jetpack Compose and Material 3 Expressive UI components. Keep screens responsive and support light/dark modes and dynamic wallpaper colors.
- **Data Safety**: Keep all user data local. Never introduce external tracking, cloud sync dependencies, or analytics SDKs.

### Testing Requirements
When adding new business logic or updating user flows:
- Add JVM unit tests under `app/src/test/` using coroutine test dispatchers and fakes.
- For Room migrations, add instrumentation tests under `app/src/androidTest/` (`AquaDatabaseMigrationTest`).
- Verify all checks pass before submitting a Pull Request:
  ```powershell
  ./gradlew.bat testDebugUnitTest
  ./gradlew.bat lintDebug
  ./gradlew.bat assembleDebug
  ./gradlew.bat jacocoTestReport
  ```

---

## 📥 Submitting Pull Requests

1. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```
2. **Commit Changes**: Write clear, descriptive commit messages.
3. **Verify Locally**: Ensure unit tests, lint checks, and debug builds pass without errors.
4. **Open a PR**: Push your branch to your fork and submit a Pull Request to the `main` branch of `arekbauer/Aqua`.

---

## 🐛 Reporting Issues

If you find a bug or have a feature suggestion, please open an issue on GitHub:
- Check existing issues before opening a new one to avoid duplicates.
- Provide step-by-step instructions to reproduce bugs, including your device model and Android OS version.

Thank you for helping make Aqua better! 💧
