# Repository guidance

- Keep Android verification instructions and current test coverage documented in [docs/testing.md](docs/testing.md).
- For app changes, run the relevant Gradle checks described there. CI runs unit tests, lint, a debug build, and instrumented Android tests.
- Add or update a regression test when changing business logic or a key user workflow.
- Use instrumented tests for Room behavior and Compose interactions; keep ViewModel tests on the JVM with fakes.
- A successful build alone is not verification. Run the relevant tests and checks from `docs/testing.md`.
