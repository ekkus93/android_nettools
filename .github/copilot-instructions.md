# GitHub Copilot Instructions — Android NetTools

## Project Overview

Android NetTools is a Swiss Army knife of network utilities for Android. The first and primary feature is SCP/SFTP file transfer. See `docs/SPECS.md` for the full feature specification and `docs/TODO.md` for the task list.

---

## Language & Platform

- **Language:** Kotlin only — do not generate Java code.
- **Minimum SDK:** 26 (Android 8.0). Do not use APIs below API 26 without a compat fallback.
- **Target SDK:** Latest stable Android release.
- **Build system:** Gradle (Kotlin DSL preferred — `.gradle.kts`).

---

## Architecture

- Follow **MVVM** with a clean layered structure:
  - `ui/` — Fragments/Composables + ViewModels
  - `domain/` — Use cases, domain models, repository interfaces
  - `data/` — Room entities/DAOs, repository implementations, SSH/SCP/SFTP clients
  - `service/` — Foreground service for background transfers
  - `util/` — Extensions and helpers
- Use **Hilt** for dependency injection throughout. Annotate ViewModels with `@HiltViewModel`.
- Use **Kotlin Coroutines and Flow** for all async work — no RxJava, no raw threads.
- Use **Room** for all local persistence (connection profiles, transfer history, known hosts).

---

## UI

- Use **Jetpack Compose** with **Material Design 3**.
- Support both **light and dark themes** — never hardcode colors; always use `MaterialTheme.colorScheme`.
- Navigation via **Navigation Compose** (`NavHost`).
- Show inline validation errors on form fields — do not rely solely on Toast/Snackbar for input errors.
- All icon-only buttons must have a `contentDescription`.
- Minimum touch target size: 48dp.

---

## SSH / Networking

- Use **SSHJ** (`com.hierynomus:sshj`) as the SSH library — do not implement SSH/SCP/SFTP from scratch.
- All SSH sessions must be properly closed in `finally` blocks or `use {}` patterns.
- Transfers must be **streaming** — never read an entire file into a `ByteArray` in memory.
- Support **cancellation** via coroutine cancellation (`isActive` checks in transfer loops).
- Resumable transfers: uploads use a `.part` suffix on the remote until complete; downloads resume from local file size as byte offset.

---

## Security

- Passwords and private key material must **never** appear in log output (`Log.*`) or exception messages surfaced to the user.
- Save passwords using **`EncryptedSharedPreferences`** (Android Keystore-backed). Never store plaintext passwords in Room or SharedPreferences.
- Private keys are stored on the filesystem; only the path is persisted in the profile.
- Enforce **host-key verification**: on first connect, show the SHA-256 fingerprint and prompt TOFU acceptance. Warn the user if a known host's key changes.
- Set `android:allowBackup="false"` for the application or exclude credential files from backup.

---

## Error Handling

- Map low-level SSH exceptions to user-friendly messages (e.g., auth failure, host unreachable, permission denied, disk full).
- Use a sealed `Result`/`TransferError` type to propagate errors up to the ViewModel — do not let raw exceptions reach the UI layer.
- Never silently swallow exceptions; at minimum log them at `DEBUG` level without sensitive data.

---

## Code Style

- Follow the [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).
- Prefer `val` over `var`; prefer immutable data classes.
- Use named arguments for functions with more than two parameters of the same type.
- Extension functions are encouraged for concise, readable code — place them in `util/`.
- Keep ViewModels free of Android framework imports (`Context`, `View`, etc.) wherever possible.
- Write KDoc comments on all public functions, classes, and interfaces.

---

## Testing

- Unit tests go in `src/test/`. Use **JUnit 5** and **MockK** for mocking.
- Instrumentation tests go in `src/androidTest/`. Use **Compose testing APIs** for UI tests.
- SSH layer tests should mock the SSHJ session — do not make real network calls in unit tests.
- Integration tests for the full transfer flow should use **Apache MINA SSHD** as a local test server.
- Aim for meaningful coverage on: transfer logic, resume logic, error mapping, credential storage, and known-hosts verification.

---

## Dependencies (approved)

| Purpose | Library |
|---------|---------|
| SSH/SCP/SFTP | `com.hierynomus:sshj` |
| Dependency Injection | `com.google.dagger:hilt-android` |
| Local Database | `androidx.room` |
| Async | `org.jetbrains.kotlinx:kotlinx-coroutines-android` |
| Secure Storage | `androidx.security:security-crypto` |
| UI | Jetpack Compose + Material3 |
| Navigation | `androidx.navigation:navigation-compose` |
| Testing | JUnit 5, MockK, Compose Test, MINA SSHD |

Do not introduce new third-party dependencies without discussion.
