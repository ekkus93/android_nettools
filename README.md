# Android NetTools

A Swiss Army knife of network utilities for Android, with SSH file transfer and an embedded curl runner.

## Features

- **SCP Transfer** — upload and download files over SSH with streaming progress, resume support, and `.part` file atomicity
- **SFTP Browser** — navigate remote directories; rename, delete, and create folders; breadcrumb navigation with sort controls
- **Curl Runner** — paste and run real curl commands with pre-run validation, background execution, separate stdout/stderr panels, exit code, timing metadata, and optional persisted logs
- **Workspace Browser** — manage curl workspace files with import/export via Android pickers, create, rename, move, and delete
- **Saved Connections** — persist SSH connection profiles with password auth or private key; passwords stored via Android Keystore; private keys referenced by path or `content://` URI
- **Transfer History** — view past transfers with status, size, direction, and "Transfer again" shortcut; filterable by status
- **Transfer Progress** — live speed, ETA, bytes transferred, and cancel support via a foreground service
- **Host-Key Verification** — TOFU (Trust On First Use) with SHA-256 fingerprint display; key-change warnings on subsequent connections
- **Material You** — dynamic color scheme on Android 12+ (API 31), with a static teal palette fallback for older devices

## Requirements

- Android 8.0+ (API 26)
- Java 17
- Android SDK (compile SDK 36, target SDK 36)

## Building

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Debug APK (includes x86_64, arm64-v8a, armeabi-v7a)
./gradlew assembleDebug

# Release APK (arm64-v8a and armeabi-v7a only; requires signing config)
./gradlew assembleRelease

# Unit tests (JUnit 5 + MockK; includes MINA SSHD integration tests)
./gradlew test

# Lint (run separately from tests to avoid an AGP/kapt stub conflict)
./gradlew lintDebug
```

> **Note:** Always run `lintDebug` and `test` as separate Gradle invocations — running them together triggers an AGP lint bug with kapt stubs.

## Release Signing

Create a keystore and set these properties in `~/.gradle/gradle.properties` (or as environment variables):

```properties
NETTOOLS_KEYSTORE_PATH=/path/to/nettools.jks
NETTOOLS_KEYSTORE_PASSWORD=<keystore-password>
NETTOOLS_KEY_ALIAS=nettools
NETTOOLS_KEY_PASSWORD=<key-password>
```

Generate a keystore:
```bash
keytool -genkey -v -keystore nettools.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias nettools
```

> **Never commit the keystore file or passwords to version control.**

## Architecture

- **Language:** Kotlin only, Jetpack Compose UI, Material Design 3
- **Pattern:** MVVM with clean layered structure (`ui/`, `domain/`, `data/`, `service/`, `util/`)
- **DI:** Hilt (KSP-based annotation processing)
- **Database:** Room — connection profiles, transfer history, known hosts, curl run logs, curl settings
- **SSH:** SSHJ (`com.hierynomus:sshj`) — SCP upload/download, SFTP directory operations
- **Curl:** bundled native curl 8.8.0 binary (built with OpenSSL 3.3.0 + nghttp2 1.62.1), packaged as `jniLibs/libcurl_exec.so`; JNI metadata bridge (`NativeCurlBridge`) backed by libcurl for version/feature introspection
- **Async:** Kotlin Coroutines + Flow throughout; no RxJava, no raw threads
- **Secure Storage:** `EncryptedSharedPreferences` (AES256-GCM, Android Keystore-backed)
- **Navigation:** Navigation Compose (`NavHost`) with typed routes

## Testing

The project has comprehensive test coverage across two layers:

### Unit Tests (`src/test/`)
- **Framework:** JUnit 5 + MockK + `kotlinx-coroutines-test`
- **Coverage:** All ViewModels, use cases, repository implementations, SSH clients (mocked), domain models, and utility classes
- Notable: `WorkspacePathResolver`, `SftpBrowserViewModel` dialog state machines, `TransferViewModel` SavedStateHandle prefill, `SavedConnectionsViewModel` credential coordination

### Integration Tests (`src/test/`)
- **Framework:** JUnit 5 + Apache MINA SSHD (in-process real SSH server)
- **Coverage:**
  - `SftpClientIntegrationTest` — all SFTP operations (listDirectory, rename, delete, mkdir, stat, getFileSize) against a live MINA server
  - `SshConnectionManagerIntegrationTest` — TOFU flow, host-key change detection, auth failure error mapping, unreachable host
  - `ScpClientIntegrationTest` — SCP upload/download/resume, mid-stream cancellation, large-file progress fidelity
  - `ErrorMapperIntegrationTest` — real SSHJ exceptions mapped to `TransferError` sealed types
  - `TransferFlowIntegrationTest` — end-to-end `SftpClient` + `ScpClient` interop on a single SSH connection

### Instrumentation Tests (`src/androidTest/`)
- Compose UI tests for the Curl Runner, Curl Results, and Workspace Browser screens

## Security Notes

- Passwords are stored with `EncryptedSharedPreferences` (AES256-GCM, Android Keystore backed)
- Passwords and key material are never written to logcat; `sanitizeForLog()` extension strips sensitive fields
- Private keys are read via `ContentResolver` for `content://` URIs and staged to a temp file that is deleted immediately after the SSHJ key provider is constructed
- `android:allowBackup="false"` is set in the manifest; credential files are excluded from cloud backup
- Host keys are verified via TOFU on first connection; key changes on subsequent connections trigger a prominent warning dialog before the transfer proceeds
- All SSH traffic is encrypted; the app makes no plain-text network connections
