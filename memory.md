# Android NetTools — Copilot Memory

## 2026-04-10T12:57:20Z - GPT-5.4 - Added curl error-message mapping slice

Improved curl-facing error handling by adding shared user-message mapping for execution failures, workspace import/export issues, and save-output failures, then covered the mappings with new unit tests. Revalidated with `./gradlew --no-daemon --console=plain lintDebug test assembleDebug` using JDK 17 and updated `docs/CURL_TODO.md` for the completed error-handling and unit-test items.

## 2026-04-10T12:49:20Z - GPT-5.4 - Added curl workspace import-export and path cleanup slice

Implemented the next curl slice: workspace path rewriting before execution, pre-run local path validation, partial local output cleanup with surfaced warnings, save-output-to-workspace support, and workspace browser import/export via Android pickers. Updated `docs/CURL_TODO.md` to mark the newly completed items and revalidated with `./gradlew --no-daemon --console=plain lintDebug test assembleDebug` using JDK 17 after disabling empty unit-test KAPT tasks to remove their warning noise.

## 2026-04-10T12:31:12Z - GPT-5.4 - Completed curl UI phase

Finished the curl UI slice: added the home-card entry and routes, runner/results/logs/settings/workspace screens, active-run reconnect behavior, completion/failure/cancellation notifications, and settings/log-history controls. Updated `docs/CURL_TODO.md` to mark the completed UI and notification items and revalidated with `./gradlew --no-daemon --console=plain lintDebug test assembleDebug` using JDK 17.

## 2026-04-10T12:05:50Z - GPT-5.4 - Completed curl execution scaffold phase

Finished the second curl implementation slice: added the execution request/result seam, process-backed executor placeholder, curl foreground service, live run holder, notification wiring, and start/observe/cancel/clear-log use cases. Updated `docs/CURL_TODO.md` to mark the execution-layer items that are now truly in place, and revalidated with `./gradlew --no-daemon --console=plain lintDebug test assembleDebug` using JDK 17.

## 2026-04-10T11:48:33Z - GPT-5.4 - Completed curl foundation phase and started execution phase

Implemented the first curl foundation slice: NDK/CMake/native-bridge scaffolding, curl domain and repository models, Room persistence for curl runs/settings, workspace path management, parser/validation groundwork, DI bindings, and unit tests. Updated `docs/CURL_TODO.md` to mark the completed foundation items and moved session tracking from phase 1 to phase 2.

## 2026-04-10T10:11:56Z - GPT-5.4 - Added curl spec and implementation TODO docs

Created `docs/CURL_SPECS.md` and `docs/CURL_TODO.md` to capture the agreed libcurl-backed paste/run curl feature, its workspace/logging/output rules, native packaging defaults, and a detailed implementation task breakdown.

## 2026-04-10T10:07:08Z - GPT-5.4 - User chose separate workspace browser and flexible log UI

For the planned curl feature, the workspace file manager should live in a separate browser screen, log-screen organization can start simple and improve later, and output retention should keep as much as practical with a reasonable cap chosen during implementation.

## 2026-04-10T10:03:07Z - GPT-5.4 - User finalized curl logs, workspace, and validation preferences

For the planned curl feature, logs should persist across runs, the workspace should be a single global root, users should be able to browse and manage workspace files/directories in-app, and pre-run validation should at least catch unclosed quotes and misspelled argument names.

## 2026-04-10T09:52:15Z - GPT-5.4 - User fixed remaining curl workspace and ABI choices

For the planned curl feature, user wants a user-selectable workspace root with picker-based import/export, warning surfacing when remote partial-upload cleanup fails, and native packaging defaults of `arm64-v8a` + `armeabi-v7a` for release with `x86_64` added for debug/dev.

## 2026-04-10T09:47:21Z - GPT-5.4 - User clarified curl workspace, logging, and cleanup preferences

For the planned curl feature, user prefers a workspace directory that acts like the root for Unix-style local paths, logging off by default with optional user-enabled logs and manual clearing, deletion of failed/partial downloads, and best-effort cleanup on cancel including remote partial uploads where possible.

## 2026-04-10T09:37:13Z - GPT-5.4 - Captured detailed curl feature decisions

User specified the planned curl feature as a libcurl-backed paste/run tool with full raw command input, multiline continuations, separate stdout/stderr, optional history, Android picker-based file path rewriting, single active run, background execution, cancellation, and partial-download cleanup.

## 2026-04-10T09:24:08Z - GPT-5.4 - Discussing remaining curl feature specs

Reviewed what still needs to be specified for the planned libcurl-backed paste/run curl feature before implementation starts.

## 2026-04-10T09:23:22Z - GPT-5.4 - User approved paste/run curl direction

User agreed with the paste/run-first direction for a future libcurl-backed curl feature.

## 2026-04-10T09:22:12Z - GPT-5.4 - User prefers paste/run first for curl

For the planned curl feature, user wants a paste/run experience first, with a form-based request builder deferred until later.

## 2026-04-10T09:20:11Z - GPT-5.4 - User is open to libcurl for a future curl feature

Discussed adding a curl feature with real parity expectations. User is comfortable using `libcurl`, which points toward a native Android integration path (NDK/JNI) rather than a Kotlin-only HTTP client.

## 2026-04-09T07:48:52Z - GPT-5.4 - Re-ran lint and tests successfully

Validated the current working tree again with the standard Gradle checks used in this repo.

**Verification:** `./gradlew --no-daemon --console=plain lintDebug test` passed with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`.

## 2026-04-09T04:32:46Z - GPT-5.4 - Implemented code review follow-up fixes

**Scope:** Completed the fixes tracked in `docs/CODE_REVIEW1_TODO.md` and marked every item done.

**Key implementation changes:**
- Serialized transfer execution in `TransferForegroundService` with a single queue worker and immediate foreground startup.
- Reworked SAF handling: streaming upload sources via SSHJ `LocalSourceFile`, stable temp files for resumable SAF downloads, explicit SAF finalization failures, and temp-file cleanup rules.
- Added resumed-download progress emission with resume metadata surfaced to UI/notifications.
- Made SFTP delete recursive for directories and fixed SFTP breadcrumb/parent-path generation for `/` and `~/...`.
- Replaced several silent cleanup/history-close paths with explicit debug logging.

**Verification:** `./gradlew lintDebug` and `./gradlew test` both pass after these fixes.

## 2026-04-09T05:07:47Z - GPT-5.4 - Installed debug build on connected device

**Device:** `SM_A546E` over USB via `adb`, package `dev.nettools.android` confirmed installed with `versionName=1.0`.

**Environment note:** Local Android builds needed `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` because `/usr/lib/jvm/java-21-openjdk-amd64` did not provide the required `JAVA_COMPILER` capability for Gradle.

## 2026-04-09T05:40:25Z - GPT-5.4 - Fixed first-connect Remote Path browse host-key flow

**Bug fixed:** The Transfer screen's **Browse** action now runs through the same TOFU host-key preflight as **Transfer**, so first-time SSH connections show the fingerprint trust dialog instead of failing with the generic "An unexpected error".

**Implementation:** `TransferViewModel` now tracks whether a trusted connection should continue into transfer dispatch or SFTP browser navigation, emits a dedicated browser navigation event after trust, and reuses shared connection-field validation for browse preflight. Added `TransferViewModelTest` to cover the first-connect browse acceptance path.

**Verification:** `./gradlew lintDebug test assembleDebug` passed with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`, and the updated debug APK was installed on the connected `SM_A546E` device.

## 2026-04-09T05:49:25Z - GPT-5.4 - Replaced copied launcher icon with custom NetTools icon

**Design:** Replaced the inherited ToDo launcher art with a custom adaptive icon: dark blue network-node background plus an orange toolbox foreground.

**Implementation:** Updated `drawable/ic_launcher_background.xml`, added `drawable/ic_launcher_foreground.xml`, and pointed both adaptive icon XML files at the new drawable foreground so Android 8+ launchers use the custom art.

**Verification:** `./gradlew lintDebug assembleDebug installDebug` succeeded with `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`, and the updated debug build was installed on the connected `SM_A546E`.

## 2026-04-09T04:16:41Z - GPT-5.4 - Added code review follow-up TODO file

**Docs:** Created `docs/CODE_REVIEW1_TODO.md` with a detailed post-review task list covering queue serialization, resumable progress, SFTP directory delete support, breadcrumb fixes, and SAF transfer correctness.

**Purpose:** This records the current follow-up work identified by review so future sessions can pick up the highest-risk correctness issues quickly.

## 2026-04-09T04:00:27Z - Claude Sonnet 4.6 - All 16 phases complete, tests passing

**Commit:** `e4256ba` — Phase 14 comprehensive test suite + all TODO.md items marked done.

**Bugs fixed this session:**
- `ScpClient.downloadResumable`: was using `outputStream()` (truncates file) instead of `RandomAccessFile("rw")` for resumable writes
- `HistoryViewModelTest`: needed `Dispatchers.setMain(testDispatcher)` / `resetMain()` so `viewModelScope` uses the test dispatcher with `stateIn(WhileSubscribed)`
- `ScpClientIntegrationTest`: `PasswordAuthenticator` lambda needed explicit SAM constructor
- `ScpClientTest`: backtick test name with `.part` is illegal; `any()` needed explicit type hint for `SCPFileTransfer.download`
- Test data: query `"a"` matched both entries via `host = "server.local"` (contains 'a'); changed to `"a.zip"`

**Test suite (95 tests, all passing):**
- `ScpClientTest`, `SftpClientTest`, `ScpClientIntegrationTest` (real MINA SSHD)
- `TransferProgressHolderTest`, `TransferHistoryRepositoryImplTest`
- `SftpBrowserViewModelSortTest`, `HistoryViewModelTest`

**Project status:** COMPLETE — all features implemented, lint clean, tests green, pushed to GitHub.

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

## 2026-04-09T02:17:45Z - Claude Sonnet 4.6 - Stub code replaced with real implementations

Commit: 88ac628

All previously-stubbed functionality is now fully implemented:

### Changes made
- **ScpClient**: real `callbackFlow` streaming via SSHJ `TransferListener`; `.part` suffix + atomic rename on upload; SFTP offset-based resumable download; old stubs removed
- **TransferForegroundService**: full rewrite — dequeues `PendingTransferParams` from `TransferProgressHolder`, resolves SAF `content://` URIs to temp files, connects SSH, streams progress updates, records history, handles cancellation with `NonCancellable` cleanup
- **SshConnectionManager**: added `peekHostKey(host, port)` — connects without auth to capture SHA-256 fingerprint for TOFU pre-flight
- **TransferViewModel**: real TOFU pre-flight in `startTransfer()` using `peekHostKey`; added `prepareSftpBrowse()` to store credentials in holder; removed `@Suppress` on `historyRepository`
- **SftpBrowserViewModel**: injected `TransferProgressHolder`; `init {}` reads and clears `pendingSftpConnectionParams` for auto-connect
- **TransferScreen**: Browse button calls `viewModel.prepareSftpBrowse()` before navigating to SFTP browser
- **MainActivity**: added `navArgument("jobId")` on PROGRESS route (was always returning empty string)
- **SftpConnectionParams**: new data class for in-memory credential passing (credentials never go through nav args)

### Build/test status
`./gradlew assembleDebug` → BUILD SUCCESSFUL  
`./gradlew test` → BUILD SUCCESSFUL (all tests pass)

## 2026-04-09T06:26:00Z - GPT-5.4 - Fixed remote path resolution for browse and upload

Investigated the SFTP browser showing an empty home directory and uploads failing when given `/home/phil`.

### Changes made
- **SftpClient**: added canonical remote-path resolution so SFTP uses the server's real home directory instead of literal `~`; filters `.` and `..` from listings; added upload-destination resolution that appends the local filename when the remote path is a directory
- **SftpBrowserViewModel**: resolves the initial browser location to the canonical remote home directory and reuses that resolved home path for the Home action
- **TransferForegroundService**: upload jobs now resolve directory targets like `/home/phil` to `/home/phil/<local filename>` before starting SCP
- **SftpClientTest**: updated mocks for canonical-path resolution and added regression coverage for home expansion and directory upload targets

### Build/test/install status
`./gradlew --no-daemon --console=plain lintDebug test installDebug` → BUILD SUCCESSFUL  
Installed on connected device `SM-A546E`

## 2026-04-09T07:36:13Z - GPT-5.4 - Re-ran lint and unit tests

### Build/test status
`./gradlew --no-daemon --console=plain lintDebug test` → BUILD SUCCESSFUL

## 2026-04-09T07:30:43Z - GPT-5.4 - Switched file downloads to SFTP to handle remote paths with spaces

Diagnosed a failed download where the selected remote file name contained spaces (`Screenshot from 2025-10-25 14-27-03.png`).

### Changes made
- **Root cause**: SSHJ's SCP download path uses `NoEscape` by default, so remote paths with spaces can fail before transfer starts
- **ScpClient**: changed regular file downloads to use the existing SFTP-based download path instead of SCP, while keeping resume support
- **ScpClientTest**: updated download error coverage for SFTP and added a regression test that verifies remote paths with spaces are opened exactly as selected

### Build/test/install status
`./gradlew --no-daemon --console=plain clean lintDebug test installDebug` → BUILD SUCCESSFUL  
Installed on connected device `SM-A546E`

## 2026-04-09T07:45:39Z - GPT-5.4 - Fixed stale completed transfers on the progress screen

Completed transfers no longer remain in the active transfer list, and terminal transfer cards no longer show a misleading cancel affordance.

### Changes made
- **TransferForegroundService**: removes finished jobs from `TransferProgressHolder` during cleanup so completed transfers disappear from the active progress list
- **ProgressScreen**: only shows the cancel button and cancel dialog for cancellable states (`QUEUED`, `IN_PROGRESS`, `PAUSED`)

### Build/test/install status
`./gradlew --no-daemon --console=plain lintDebug test` → BUILD SUCCESSFUL  
`./gradlew --no-daemon --console=plain installDebug` → BUILD SUCCESSFUL  
Installed on connected device `SM-A546E`

## 2026-04-09T07:14:50Z - GPT-5.4 - Fixed remote picker to allow file selection for downloads

Adjusted the remote SFTP browser so download flows can pick files instead of being limited to directories.

### Changes made
- **SftpConnectionParams**: added `RemotePickerMode` so the Transfer screen can tell the SFTP browser whether it should pick a directory or a file
- **TransferViewModel**: remote Browse now requests directory picking for uploads and file picking for downloads
- **SftpBrowserViewModel**: stores the requested picker mode in UI state when the browser is opened
- **SftpBrowserScreen**: replaced the old boolean picker behavior with explicit mode handling so directory taps navigate during downloads and select during uploads
- **TransferViewModelTest**: updated the existing browse/trust-flow expectation for the new picker mode

### Build/test/install status
`./gradlew --no-daemon --console=plain clean lintDebug test installDebug` → BUILD SUCCESSFUL  
Installed on connected device `SM_A546E`

## 2026-04-09T07:01:31Z - GPT-5.4 - Made local path labels human-readable

Improved the SCP transfer UI so Android Storage Access Framework URIs are no longer shown raw to the user.

### Changes made
- **Extensions**: added a pure `toDisplayPath()` formatter that turns SAF URIs like `content://.../document/primary%3ADownload%2Fgiphy.gif` into `Download/giphy.gif`
- **TransferViewModel**: now tracks both the raw local path used for transfers and a friendly display label for the form field
- **TransferScreen**: picker results now keep the raw URI internally while showing the formatted path in the Local file / Download to field
- **ProgressScreen**: queued upload labels now use the formatted path so they show file names instead of raw `content://` URIs
- **ExtensionsTest**: added regression coverage for document and tree URI formatting

### Build/test/install status
`./gradlew --no-daemon --console=plain lintDebug test installDebug` → BUILD SUCCESSFUL  
Installed on connected device `SM-A546E`

## 2026-04-10T10:19:42Z - GPT-5.4 - Researched libcurl Android integration strategy

Produced concrete implementation plan for libcurl integration (no files modified — research only). Key decisions:

- **Build strategy**: `scripts/build_native_deps.sh` downloads pinned curl/OpenSSL/nghttp2 sources, cross-compiles via NDK 28 Clang, deposits static `.a` + headers into `app/src/main/cpp/prebuilt/{abi}/`. Prebuilts committed to git (~1.5 MB/ABI).
- **Compile full curl tool sources** (not just libcurl C API) to avoid reimplementing the argv parser. Rename `main()` to `curl_tool_main()` via `-Dmain=curl_tool_main` at compile time. Link `libcurl_tool.a` + `libcurl.a`.
- **JNI bridge**: `nativeInit`, `nativeGetVersion`, `nativeGetProtocols`, `nativeIsHttp2Supported`, `nativeStart(args[], workspaceDir, callback)`, `nativeCancel`, `nativeFreeHandle`. Uses pipe pairs + pthreads for streaming stdout/stderr to a Java callback interface wrapped in a Kotlin `callbackFlow`.
- **CA bundle**: ship Mozilla `cacert.pem` as asset, extract to `filesDir` on first run, pass `--cainfo` as synthetic arg.
- **Key pitfalls**: `CURLOPT_NOSIGNAL=1` required; `curl_global_init` once from Application.onCreate (not JNI_OnLoad); `_FORTIFY_SOURCE` conflicts building OpenSSL; curl tool global state requires Kotlin Mutex to enforce one-job-at-a-time.
- **NDK**: Both NDK 27 and 28 present locally. Use NDK 28.2.13676358. ABI filters set per buildType (not defaultConfig) to avoid replacement-vs-merge issue in AGP Kotlin DSL.
