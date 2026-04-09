# Android NetTools — Copilot Memory

## 2026-04-09T00:30:41Z - Claude Sonnet 4.5 - Initial project scaffold completed

**Build status:** `./gradlew assembleDebug` succeeds (BUILD SUCCESSFUL).

### Project structure created
All source files placed directly in `/home/phil/work/android_nettools/`.

**Build infrastructure:**
- Gradle wrapper copied from reference project at `/home/phil/AndroidStudioProjects/tiny_android_todo/ToDo/`
- Gradle 8.13, AGP 8.12.0, Kotlin 2.2.10
- Hilt 2.56.2 with kapt (not KSP)
- Room 2.7.2 with kapt
- SSHJ 0.39.0
- Security Crypto 1.1.0-alpha06

**Kotlin source files created:**
- `NetToolsApp.kt` — @HiltAndroidApp Application class
- `MainActivity.kt` — @AndroidEntryPoint, NavHost with home route
- `ui/HomeScreen.kt` — Scaffold placeholder screen
- `ui/theme/Theme.kt` — Material3 light/dark theme
- `domain/model/ConnectionProfile.kt` — profile data class + AuthType enum
- `domain/model/TransferJob.kt` — job data class + TransferDirection/Status enums
- `domain/model/TransferHistoryEntry.kt` — history entry + HistoryStatus enum
- `domain/model/RemoteFileEntry.kt` — remote filesystem entry
- `domain/model/TransferError.kt` — sealed class error hierarchy
- `domain/repository/ConnectionProfileRepository.kt` — interface
- `domain/repository/TransferHistoryRepository.kt` — interface
- `domain/repository/KnownHostRepository.kt` — interface
- `data/db/Entities.kt` — Room entities + toDomain()/toEntity() functions
- `data/db/Daos.kt` — DAOs for all three entities
- `data/db/AppDatabase.kt` — Room database class
- `data/repository/ConnectionProfileRepositoryImpl.kt`
- `data/repository/TransferHistoryRepositoryImpl.kt`
- `data/repository/KnownHostRepositoryImpl.kt`
- `data/ssh/SshConnectionManager.kt` — SSHJ connection handling
- `data/ssh/ScpClient.kt` — SCP upload/download via SSHJ
- `data/ssh/SftpClient.kt` — SFTP operations via SSHJ
- `data/ssh/TransferProgress.kt` — progress data class
- `data/ssh/ErrorMapper.kt` — maps exceptions to TransferError
- `data/security/CredentialStore.kt` — EncryptedSharedPreferences password storage
- `data/security/KnownHostsManager.kt` — TOFU host key verification
- `di/DatabaseModule.kt` — Hilt module for Room + repositories
- `di/SshModule.kt` — Hilt module for SSH/security components
- `service/TransferForegroundService.kt` — foreground service with job queue
- `service/NotificationHelper.kt` — notification factory
- `util/Extensions.kt` — size/speed/ETA formatting + sanitizeForLog()

### Key notes / issues encountered
1. SSHJ `TransferListener.file()` returns `StreamCopier.Listener` — avoid implementing it with anonymous objects that return wrong type; removed usage entirely in ScpClient.
2. `TransferError.Unknown(cause)` — `cause` must use `override` since Throwable already has a `cause` property.
3. Mipmap resources copied from reference project to satisfy AAPT launcher icon requirement.
4. `packaging { resources { excludes += "META-INF/INDEX.LIST" } }` needed for SSHJ/Bouncy Castle.

### What's not yet implemented (future phases)
- Actual UI screens (profile management, file browser, transfer UI)
- ViewModel layer
- Use cases / domain logic
- Real transfer orchestration (service wires to SSH clients)
- Tests (unit, integration, UI)
- Permission request handling at runtime

## 2026-04-09T01:51:50Z - GPT-5.4 - Added code review follow-up TODO

Created `docs/CODE_REVIEW1_TODO.md` as a detailed remediation list based on code review findings.

Primary reviewed issues captured there:
- `TransferForegroundService` does not execute real transfers yet
- `TransferProgressHolder` needs atomic state updates and should be the single progress source
- `SftpBrowserScreen` is not wired to pass/connect with transfer credentials
- local picker results are SAF URIs, but transfer code currently assumes `java.io.File`
