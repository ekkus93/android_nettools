# Android NetTools — TODO

Detailed task and subtask list derived from `SPECS.md`. Tasks are grouped by area. Work through sections roughly top-to-bottom; later sections may depend on earlier ones.

---

## 1. Project Setup

- [x] Create new Android project (Kotlin, minimum SDK 26)
- [x] Configure `build.gradle` / `settings.gradle`
  - [x] Set `minSdk 26`, `targetSdk` to latest stable
  - [x] Enable Kotlin and `viewBinding` / `compose` (decide UI toolkit)
  - [x] Add dependency: SSHJ or JSch SSH library
  - [x] Add dependency: Room (local database)
  - [x] Add dependency: Hilt (dependency injection)
  - [x] Add dependency: Kotlin Coroutines + Flow
  - [x] Add dependency: WorkManager or Foreground Service support
  - [x] Add dependency: AndroidX Security (EncryptedSharedPreferences / Keystore)
  - [x] Add dependency: Material Design 3
- [x] Set up version control ignore rules (`.gitignore`)
- [x] Configure ProGuard / R8 rules to keep SSH library classes
- [x] Add required permissions to `AndroidManifest.xml`
  - [x] `INTERNET`
  - [x] `FOREGROUND_SERVICE`
  - [x] `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (API ≤ 32)
  - [x] `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` (API 33+)
  - [x] `POST_NOTIFICATIONS` (API 33+, for transfer notifications)

---

## 2. Architecture & Core Infrastructure

- [x] Define package structure (e.g., `ui`, `data`, `domain`, `service`, `util`)
- [x] Set up Hilt application class and modules
- [x] Create Room database
  - [x] `ConnectionProfileEntity` table
  - [x] `TransferHistoryEntity` table
  - [x] `KnownHostEntity` table (for host-key verification)
  - [x] DAOs for each table
  - [x] Database migrations strategy
- [x] Define domain models
  - [x] `ConnectionProfile` (host, port, username, authType, keyPath)
  - [x] `TransferJob` (direction, localPath, remotePath, profile, status)
  - [x] `TransferHistoryEntry` (timestamp, direction, host, fileName, size, status)
  - [x] `RemoteFileEntry` (name, size, permissions, isDirectory, modifiedAt)
- [x] Create repository interfaces and implementations
  - [x] `ConnectionProfileRepository`
  - [x] `TransferHistoryRepository`
  - [x] `KnownHostRepository`

---

## 3. SSH / SCP / SFTP Core

- [x] Evaluate and select SSH library (SSHJ vs JSch) — document choice
- [x] Create `SshConnectionManager`
  - [x] Establish SSH session (host, port, username)
  - [x] Password authentication
  - [x] Private key authentication (PEM / OpenSSH key parsing)
  - [x] Host-key verification callback (TOFU + known-hosts database)
  - [x] Connection timeout and retry logic
  - [x] Session teardown / resource cleanup
- [x] Create `ScpClient`
  - [x] Upload single file (streaming, no full in-memory load)
  - [x] Upload directory recursively
  - [x] Download single file (streaming)
  - [x] Download directory recursively
  - [x] Progress callback (bytes transferred, total bytes)
  - [x] Cancellation support (cooperative cancellation token)
- [x] Create `SftpClient`
  - [x] List directory contents (`ls`)
  - [x] Stat a single remote path (size, permissions, type)
  - [x] Create remote directory (`mkdir`)
  - [x] Rename remote file/directory
  - [x] Delete remote file/directory
  - [x] Get remote file size (used for resume logic)
- [x] Resumable transfer logic
  - [x] Download resume: seek local file to current size, request offset from remote
  - [x] Upload resume: stat remote `.part` file size, seek local file to that offset
  - [x] `.part` suffix management on remote during upload
  - [x] Atomic rename on successful upload completion (`.part` → final name)
  - [x] Detect stale/corrupted partial files and offer restart
- [x] Error mapping layer
  - [x] Auth failure → user-friendly message
  - [x] Host unreachable / timeout → user-friendly message
  - [x] Permission denied (remote) → user-friendly message
  - [x] Disk full (local or remote) → user-friendly message
  - [x] Unknown host key → prompt user to trust or reject

---

## 4. Background Transfer Service

- [x] Implement `TransferForegroundService`
  - [x] Start foreground service with persistent notification on transfer start
  - [x] Bind service to UI for live progress updates (via Flow / LiveData)
  - [x] Handle multiple concurrent or queued transfers
  - [x] Stop service cleanly when all transfers complete or are cancelled
- [x] Notification management
  - [x] Create notification channel (`TransferChannel`)
  - [x] Per-transfer progress notification (indeterminate → determinate)
  - [x] Success notification (with file name and size)
  - [x] Failure notification (with brief error reason)
  - [x] Cancel action button in notification
- [x] Transfer queue
  - [x] Queue incoming transfer requests
  - [x] Execute one at a time (or configurable concurrency)
  - [x] Persist queued/interrupted jobs across process death (via Room)

---

## 5. Credential & Key Storage

- [x] Implement `CredentialStore`
  - [x] Save password securely using `EncryptedSharedPreferences` (Android Keystore backed)
  - [x] Retrieve password by profile ID
  - [x] Delete password on profile deletion
  - [x] Save private key file path (key data stays on filesystem; path stored in profile)
  - [x] Validate private key file is readable at transfer time
- [x] Ensure passwords and keys are never written to logcat or crash reports
- [x] (Optional / decision pending) Biometric unlock gate before credential access

---

## 6. Known-Hosts Verification

- [x] Implement `KnownHostsManager`
  - [x] Check incoming host key against stored known hosts
  - [x] TOFU: on first connection, prompt user to accept or reject host key
  - [x] Display host key fingerprint (SHA-256) in confirmation dialog
  - [x] Persist accepted host keys in Room (`KnownHostEntity`)
  - [x] Warn user if host key changes for a known host
  - [x] Allow user to remove a stored host key from Saved Connections screen

---

## 7. UI — Home / Utility Launcher Screen

- [x] Design home screen layout (grid or list of tools)
- [x] Add SCP/SFTP tile as first entry
- [x] Implement navigation to SCP Transfer Screen
- [x] Apply Material Design 3 theming (light + dark)
- [x] Add app icon and branding

---

## 8. UI — SCP Transfer Screen

- [x] Layout
  - [x] Saved-profile dropdown / picker
  - [x] Host field (with input validation)
  - [x] Port field (numeric, default 22)
  - [x] Username field
  - [x] Auth method selector (Password / Private Key)
    - [x] Password: masked text field
    - [x] Private Key: file picker button + selected path label
  - [x] Direction toggle (Upload / Download)
  - [x] Local path selector
    - [x] Upload: button to open system file picker or in-app browser
    - [x] Download: button to choose destination folder
  - [x] Remote path field with "Browse…" button (opens SFTP Browser)
  - [x] Transfer button
  - [x] Save connection profile checkbox / button
- [x] Input validation
  - [x] Required fields: host, username, local path, remote path
  - [x] Port in range 1–65535
  - [x] Show inline error messages before allowing transfer to start
- [x] Wire up to `TransferForegroundService` on Transfer button tap

---

## 9. UI — Transfer Progress Screen

- [x] Full-screen or bottom sheet layout
- [x] Current file name being transferred
- [x] Per-file progress bar
- [x] Overall progress bar (for directory transfers)
- [x] Bytes transferred / total bytes
- [x] Transfer speed (KB/s or MB/s), rolling average
- [x] Estimated time remaining
- [x] Resume indicator (show "Resuming from X MB" if applicable)
- [x] Cancel button (with confirmation dialog)
- [x] Auto-dismiss or navigate to history on completion

---

## 10. UI — SFTP Browser Screen

- [x] Remote directory listing (`RecyclerView` / `LazyColumn`)
  - [x] Show file name, size, permissions, last-modified date
  - [x] Distinguish files vs. directories (icons)
  - [x] Sort options (name, size, date)
- [x] Navigation
  - [x] Tap directory to navigate into it
  - [x] Back button / breadcrumb trail to navigate up
  - [x] Home button to jump to `~`
- [x] File operations (long-press or context menu)
  - [x] Rename
  - [x] Delete (with confirmation dialog)
  - [x] Create new directory
- [x] File selection mode
  - [x] When launched as remote-path picker, tapping a file/directory selects it and returns to Transfer Screen
- [x] Loading states and error handling (directory not found, permission denied)
- [x] Pull-to-refresh to reload current directory

---

## 11. UI — Saved Connections Screen

- [x] List all saved connection profiles
- [x] Create new profile (same fields as Transfer Screen connection section)
- [x] Edit existing profile
- [x] Delete profile (with confirmation; also removes stored password)
- [x] Tap profile to pre-fill Transfer Screen
- [x] Show host-key fingerprint for each saved host (link to Known Hosts management)

---

## 12. UI — Transfer History Screen

- [x] List past transfers (newest first)
  - [x] Timestamp
  - [x] Direction icon (upload/download)
  - [x] Remote host and file/directory name
  - [x] File size
  - [x] Status badge (Success / Failed / Cancelled / Resumed)
- [x] Tap entry to see full detail (error message if failed)
- [x] Clear all history button (with confirmation)
- [x] Filter / search by host or filename (nice-to-have)

---

## 13. Theming & Polish

- [x] Define Material Design 3 color scheme (light and dark)
- [x] Apply theme to all screens
- [x] Ensure all text has sufficient contrast in both themes
- [x] Add transition animations between screens
- [x] Handle edge cases: empty states for history, saved connections, SFTP listings
- [x] Accessibility
  - [x] Content descriptions on icon buttons
  - [x] Minimum touch target sizes
  - [x] TalkBack compatibility pass

---

## 14. Testing

- [x] Unit tests
  - [x] `ScpClient` upload/download logic (mock SSH session)
  - [x] `SftpClient` listing and operations (mock SSH session)
  - [x] Resume logic (partial file detection, offset calculation)
  - [x] Error mapping (assert correct user-facing messages)
  - [x] `CredentialStore` (encrypt/decrypt round-trip)
  - [x] `KnownHostsManager` (TOFU flow, changed key detection)
  - [x] Repository layer (Room in-memory database)
- [x] Integration tests
  - [x] Full upload flow against a local SSH test server (e.g., Apache MINA SSHD)
  - [x] Full download flow
  - [x] Resume download after simulated interruption
  - [x] Resume upload after simulated interruption
  - [x] Directory transfer (recursive)
- [x] UI / instrumentation tests
  - [x] Transfer Screen form validation
  - [x] SFTP Browser navigation
  - [x] Saved Connections CRUD
  - [x] History screen renders and clears correctly
- [x] Manual test checklist
  - [x] Test against real Linux host (OpenSSH)
  - [x] Test password auth and key auth
  - [x] Test with large file (>1 GB)
  - [x] Test with deeply nested directory tree
  - [x] Test cancellation mid-transfer and verify cleanup
  - [x] Test on API 26, 31, and 34 emulators/devices

---

## 15. Security Review

- [x] Confirm no credentials appear in logcat (audit log statements)
- [x] Confirm no credentials appear in crash reports
- [x] Verify EncryptedSharedPreferences is used for all saved passwords
- [x] Verify `android:allowBackup` considerations (exclude credential files from backup)
- [x] Review network security config (`network_security_config.xml`) — no plain-text traffic needed (all SSH)
- [x] Test host-key change warning flow end-to-end

---

## 16. Build & Release

- [x] Set up signing config (keystore)
- [x] Configure build variants (`debug` / `release`)
- [x] Enable R8 full-mode minification for release
- [x] Write release notes / changelog
- [x] Update `README.md` with build instructions and feature overview
