# Android NetTools

A Swiss Army knife of network utilities for Android, starting with SCP/SFTP file transfer.

## Features

- **SCP Transfer** — upload and download files over SSH with streaming progress, resume support, and `.part` file safety
- **SFTP Browser** — navigate remote directories, rename, delete, and create folders
- **Saved Connections** — persist SSH connection profiles (passwords stored via Android Keystore)
- **Transfer History** — view past transfers with status and size
- **Transfer Progress** — live speed, ETA, and cancel support via foreground service
- **Host-Key Verification** — TOFU (Trust On First Use) with SHA-256 fingerprint display and change warnings

## Requirements

- Android 8.0+ (API 26)
- Java 17
- Android SDK (compile/target SDK 36)

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config — see below)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Lint
./gradlew lintDebug
```

## Release Signing

Create a keystore and set these environment variables (or add them to `~/.gradle/gradle.properties`):

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
- **DI:** Hilt
- **Database:** Room (connection profiles, transfer history, known hosts)
- **SSH:** SSHJ (`com.hierynomus:sshj`)
- **Async:** Kotlin Coroutines + Flow
- **Secure Storage:** EncryptedSharedPreferences (Android Keystore-backed)

## Security Notes

- Passwords are stored with `EncryptedSharedPreferences` (AES256-GCM, Android Keystore backed)
- Passwords and key material are never written to logcat — use `sanitizeForLog()` extension
- `android:allowBackup="false"` is set in the manifest
- Host keys are verified via TOFU; key changes trigger a prominent warning dialog
- All SSH traffic is encrypted (no plain-text network communication)
