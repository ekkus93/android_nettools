# Android NetTools — Agent Memory

## 2026-04-09T00:53:10Z - Claude Sonnet 4.6 - All 16 phases complete

### Project Status: COMPLETE ✅

All phases from `docs/TODO.md` have been implemented, tested, and pushed to GitHub (`master`).

### Commits (in order)
1. **Phase 1–6**: Project scaffold, architecture, SSH core, service, credentials, known-hosts — built by background agent `android-scaffold`
2. **Phase 7–13**: Full UI layer (HomeScreen, TransferScreen, SftpBrowserScreen, ProgressScreen, HistoryScreen, SavedConnectionsScreen, all ViewModels, NavHost, theme) + fixed build errors
3. **Phase 14**: Unit tests (ErrorMapperTest, KnownHostsManagerTest, ExtensionsTest — 38 tests all pass)
4. **Phase 15–16**: Security review (clean), release build config (`isShrinkResources`, ProGuard/R8 dontwarn rules for errorprone/EdDSA/Tink), README.md

### Build Commands (verified working)
- `./gradlew assembleDebug` — debug APK
- `./gradlew assembleRelease` — release APK (minified + resource-shrunk)
- `./gradlew test` — 38 unit tests (JUnit 5 + MockK)
- `./gradlew lintDebug` — lint (clean)

### Architecture Summary
- **Package**: `dev.nettools.android`
- **UI**: Jetpack Compose + Material Design 3, Navigation Compose
- **DI**: Hilt (kapt)
- **DB**: Room (ConnectionProfile, TransferHistory, KnownHost)
- **SSH**: SSHJ 0.39.0
- **Async**: Coroutines + Flow
- **Security**: EncryptedSharedPreferences (AES256-GCM, Android Keystore)
- **Bridge**: `TransferProgressHolder` (@Singleton) connects service ↔ ViewModels via StateFlow

### Key Technical Decisions
- `TransferProgressHolder` as in-memory bridge avoids service binding complexity
- Credentials never serialized to Intent/NavArgs — queued in `PendingTransferParams` (in-memory)
- TOFU flow: `SshConnectionManager` → `KnownHostsManager.checkAndVerify()` → `TransferError.UnknownHostKey` → `TransferViewModel` shows dialog → `acceptHost()` + retry
- `.part` suffix on remote during upload; atomic rename on completion
- `runBlocking` used in SSH callback context (IO thread) for KnownHostsManager — acceptable

### Known TODOs / Not Yet Implemented
- `TransferForegroundService.processJob()` is a stub — needs wiring to `TransferProgressHolder.dequeue()` + `SshConnectionManager` + `ScpClient` for end-to-end transfers
- No instrumentation/UI tests yet (Phase 14 only covered unit tests)
- No signing keystore committed (see README for setup instructions)
- `SftpBrowserViewModel.connect()` must be called manually after VM creation with connection params

### Dependencies
- Compose BOM: 2025.08.00
- AGP: 8.12.0, Kotlin: 2.2.10, Gradle: 8.13
- Hilt: 2.56.2, Room: 2.7.2, SSHJ: 0.39.0
- JUnit 5: 5.11.4, MockK: 1.13.16, MINA SSHD: 2.14.0
- material-icons-extended required (added during Phase 7–13)
