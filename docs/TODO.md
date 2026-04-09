# Android NetTools — TODO

Detailed task and subtask list derived from `SPECS.md`. Tasks are grouped by area. Work through sections roughly top-to-bottom; later sections may depend on earlier ones.

---

## 1. Project Setup

- [ ] Create new Android project (Kotlin, minimum SDK 26)
- [ ] Configure `build.gradle` / `settings.gradle`
  - [ ] Set `minSdk 26`, `targetSdk` to latest stable
  - [ ] Enable Kotlin and `viewBinding` / `compose` (decide UI toolkit)
  - [ ] Add dependency: SSHJ or JSch SSH library
  - [ ] Add dependency: Room (local database)
  - [ ] Add dependency: Hilt (dependency injection)
  - [ ] Add dependency: Kotlin Coroutines + Flow
  - [ ] Add dependency: WorkManager or Foreground Service support
  - [ ] Add dependency: AndroidX Security (EncryptedSharedPreferences / Keystore)
  - [ ] Add dependency: Material Design 3
- [ ] Set up version control ignore rules (`.gitignore`)
- [ ] Configure ProGuard / R8 rules to keep SSH library classes
- [ ] Add required permissions to `AndroidManifest.xml`
  - [ ] `INTERNET`
  - [ ] `FOREGROUND_SERVICE`
  - [ ] `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (API ≤ 32)
  - [ ] `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` (API 33+)
  - [ ] `POST_NOTIFICATIONS` (API 33+, for transfer notifications)

---

## 2. Architecture & Core Infrastructure

- [ ] Define package structure (e.g., `ui`, `data`, `domain`, `service`, `util`)
- [ ] Set up Hilt application class and modules
- [ ] Create Room database
  - [ ] `ConnectionProfileEntity` table
  - [ ] `TransferHistoryEntity` table
  - [ ] `KnownHostEntity` table (for host-key verification)
  - [ ] DAOs for each table
  - [ ] Database migrations strategy
- [ ] Define domain models
  - [ ] `ConnectionProfile` (host, port, username, authType, keyPath)
  - [ ] `TransferJob` (direction, localPath, remotePath, profile, status)
  - [ ] `TransferHistoryEntry` (timestamp, direction, host, fileName, size, status)
  - [ ] `RemoteFileEntry` (name, size, permissions, isDirectory, modifiedAt)
- [ ] Create repository interfaces and implementations
  - [ ] `ConnectionProfileRepository`
  - [ ] `TransferHistoryRepository`
  - [ ] `KnownHostRepository`

---

## 3. SSH / SCP / SFTP Core

- [ ] Evaluate and select SSH library (SSHJ vs JSch) — document choice
- [ ] Create `SshConnectionManager`
  - [ ] Establish SSH session (host, port, username)
  - [ ] Password authentication
  - [ ] Private key authentication (PEM / OpenSSH key parsing)
  - [ ] Host-key verification callback (TOFU + known-hosts database)
  - [ ] Connection timeout and retry logic
  - [ ] Session teardown / resource cleanup
- [ ] Create `ScpClient`
  - [ ] Upload single file (streaming, no full in-memory load)
  - [ ] Upload directory recursively
  - [ ] Download single file (streaming)
  - [ ] Download directory recursively
  - [ ] Progress callback (bytes transferred, total bytes)
  - [ ] Cancellation support (cooperative cancellation token)
- [ ] Create `SftpClient`
  - [ ] List directory contents (`ls`)
  - [ ] Stat a single remote path (size, permissions, type)
  - [ ] Create remote directory (`mkdir`)
  - [ ] Rename remote file/directory
  - [ ] Delete remote file/directory
  - [ ] Get remote file size (used for resume logic)
- [ ] Resumable transfer logic
  - [ ] Download resume: seek local file to current size, request offset from remote
  - [ ] Upload resume: stat remote `.part` file size, seek local file to that offset
  - [ ] `.part` suffix management on remote during upload
  - [ ] Atomic rename on successful upload completion (`.part` → final name)
  - [ ] Detect stale/corrupted partial files and offer restart
- [ ] Error mapping layer
  - [ ] Auth failure → user-friendly message
  - [ ] Host unreachable / timeout → user-friendly message
  - [ ] Permission denied (remote) → user-friendly message
  - [ ] Disk full (local or remote) → user-friendly message
  - [ ] Unknown host key → prompt user to trust or reject

---

## 4. Background Transfer Service

- [ ] Implement `TransferForegroundService`
  - [ ] Start foreground service with persistent notification on transfer start
  - [ ] Bind service to UI for live progress updates (via Flow / LiveData)
  - [ ] Handle multiple concurrent or queued transfers
  - [ ] Stop service cleanly when all transfers complete or are cancelled
- [ ] Notification management
  - [ ] Create notification channel (`TransferChannel`)
  - [ ] Per-transfer progress notification (indeterminate → determinate)
  - [ ] Success notification (with file name and size)
  - [ ] Failure notification (with brief error reason)
  - [ ] Cancel action button in notification
- [ ] Transfer queue
  - [ ] Queue incoming transfer requests
  - [ ] Execute one at a time (or configurable concurrency)
  - [ ] Persist queued/interrupted jobs across process death (via Room)

---

## 5. Credential & Key Storage

- [ ] Implement `CredentialStore`
  - [ ] Save password securely using `EncryptedSharedPreferences` (Android Keystore backed)
  - [ ] Retrieve password by profile ID
  - [ ] Delete password on profile deletion
  - [ ] Save private key file path (key data stays on filesystem; path stored in profile)
  - [ ] Validate private key file is readable at transfer time
- [ ] Ensure passwords and keys are never written to logcat or crash reports
- [ ] (Optional / decision pending) Biometric unlock gate before credential access

---

## 6. Known-Hosts Verification

- [ ] Implement `KnownHostsManager`
  - [ ] Check incoming host key against stored known hosts
  - [ ] TOFU: on first connection, prompt user to accept or reject host key
  - [ ] Display host key fingerprint (SHA-256) in confirmation dialog
  - [ ] Persist accepted host keys in Room (`KnownHostEntity`)
  - [ ] Warn user if host key changes for a known host
  - [ ] Allow user to remove a stored host key from Saved Connections screen

---

## 7. UI — Home / Utility Launcher Screen

- [ ] Design home screen layout (grid or list of tools)
- [ ] Add SCP/SFTP tile as first entry
- [ ] Implement navigation to SCP Transfer Screen
- [ ] Apply Material Design 3 theming (light + dark)
- [ ] Add app icon and branding

---

## 8. UI — SCP Transfer Screen

- [ ] Layout
  - [ ] Saved-profile dropdown / picker
  - [ ] Host field (with input validation)
  - [ ] Port field (numeric, default 22)
  - [ ] Username field
  - [ ] Auth method selector (Password / Private Key)
    - [ ] Password: masked text field
    - [ ] Private Key: file picker button + selected path label
  - [ ] Direction toggle (Upload / Download)
  - [ ] Local path selector
    - [ ] Upload: button to open system file picker or in-app browser
    - [ ] Download: button to choose destination folder
  - [ ] Remote path field with "Browse…" button (opens SFTP Browser)
  - [ ] Transfer button
  - [ ] Save connection profile checkbox / button
- [ ] Input validation
  - [ ] Required fields: host, username, local path, remote path
  - [ ] Port in range 1–65535
  - [ ] Show inline error messages before allowing transfer to start
- [ ] Wire up to `TransferForegroundService` on Transfer button tap

---

## 9. UI — Transfer Progress Screen

- [ ] Full-screen or bottom sheet layout
- [ ] Current file name being transferred
- [ ] Per-file progress bar
- [ ] Overall progress bar (for directory transfers)
- [ ] Bytes transferred / total bytes
- [ ] Transfer speed (KB/s or MB/s), rolling average
- [ ] Estimated time remaining
- [ ] Resume indicator (show "Resuming from X MB" if applicable)
- [ ] Cancel button (with confirmation dialog)
- [ ] Auto-dismiss or navigate to history on completion

---

## 10. UI — SFTP Browser Screen

- [ ] Remote directory listing (`RecyclerView` / `LazyColumn`)
  - [ ] Show file name, size, permissions, last-modified date
  - [ ] Distinguish files vs. directories (icons)
  - [ ] Sort options (name, size, date)
- [ ] Navigation
  - [ ] Tap directory to navigate into it
  - [ ] Back button / breadcrumb trail to navigate up
  - [ ] Home button to jump to `~`
- [ ] File operations (long-press or context menu)
  - [ ] Rename
  - [ ] Delete (with confirmation dialog)
  - [ ] Create new directory
- [ ] File selection mode
  - [ ] When launched as remote-path picker, tapping a file/directory selects it and returns to Transfer Screen
- [ ] Loading states and error handling (directory not found, permission denied)
- [ ] Pull-to-refresh to reload current directory

---

## 11. UI — Saved Connections Screen

- [ ] List all saved connection profiles
- [ ] Create new profile (same fields as Transfer Screen connection section)
- [ ] Edit existing profile
- [ ] Delete profile (with confirmation; also removes stored password)
- [ ] Tap profile to pre-fill Transfer Screen
- [ ] Show host-key fingerprint for each saved host (link to Known Hosts management)

---

## 12. UI — Transfer History Screen

- [ ] List past transfers (newest first)
  - [ ] Timestamp
  - [ ] Direction icon (upload/download)
  - [ ] Remote host and file/directory name
  - [ ] File size
  - [ ] Status badge (Success / Failed / Cancelled / Resumed)
- [ ] Tap entry to see full detail (error message if failed)
- [ ] Clear all history button (with confirmation)
- [ ] Filter / search by host or filename (nice-to-have)

---

## 13. Theming & Polish

- [ ] Define Material Design 3 color scheme (light and dark)
- [ ] Apply theme to all screens
- [ ] Ensure all text has sufficient contrast in both themes
- [ ] Add transition animations between screens
- [ ] Handle edge cases: empty states for history, saved connections, SFTP listings
- [ ] Accessibility
  - [ ] Content descriptions on icon buttons
  - [ ] Minimum touch target sizes
  - [ ] TalkBack compatibility pass

---

## 14. Testing

- [ ] Unit tests
  - [ ] `ScpClient` upload/download logic (mock SSH session)
  - [ ] `SftpClient` listing and operations (mock SSH session)
  - [ ] Resume logic (partial file detection, offset calculation)
  - [ ] Error mapping (assert correct user-facing messages)
  - [ ] `CredentialStore` (encrypt/decrypt round-trip)
  - [ ] `KnownHostsManager` (TOFU flow, changed key detection)
  - [ ] Repository layer (Room in-memory database)
- [ ] Integration tests
  - [ ] Full upload flow against a local SSH test server (e.g., Apache MINA SSHD)
  - [ ] Full download flow
  - [ ] Resume download after simulated interruption
  - [ ] Resume upload after simulated interruption
  - [ ] Directory transfer (recursive)
- [ ] UI / instrumentation tests
  - [ ] Transfer Screen form validation
  - [ ] SFTP Browser navigation
  - [ ] Saved Connections CRUD
  - [ ] History screen renders and clears correctly
- [ ] Manual test checklist
  - [ ] Test against real Linux host (OpenSSH)
  - [ ] Test password auth and key auth
  - [ ] Test with large file (>1 GB)
  - [ ] Test with deeply nested directory tree
  - [ ] Test cancellation mid-transfer and verify cleanup
  - [ ] Test on API 26, 31, and 34 emulators/devices

---

## 15. Security Review

- [ ] Confirm no credentials appear in logcat (audit log statements)
- [ ] Confirm no credentials appear in crash reports
- [ ] Verify EncryptedSharedPreferences is used for all saved passwords
- [ ] Verify `android:allowBackup` considerations (exclude credential files from backup)
- [ ] Review network security config (`network_security_config.xml`) — no plain-text traffic needed (all SSH)
- [ ] Test host-key change warning flow end-to-end

---

## 16. Build & Release

- [ ] Set up signing config (keystore)
- [ ] Configure build variants (`debug` / `release`)
- [ ] Enable R8 full-mode minification for release
- [ ] Write release notes / changelog
- [ ] Update `README.md` with build instructions and feature overview
