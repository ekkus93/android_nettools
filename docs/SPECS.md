# Android NetTools — Feature Specifications

## Overview

**Android NetTools** is a Swiss Army knife of network utilities for Android. It provides power-user networking tools in a clean, unified mobile interface built with Jetpack Compose and Material Design 3.

---

## Feature: SCP (Secure Copy Protocol)

### Summary

Allow the user to securely transfer files between their Android device and a remote host using the SCP protocol (SSH-based file transfer).

---

### Functional Requirements

#### 1. Transfer Directions

| Direction | Description |
|-----------|-------------|
| **Upload (Phone → Remote)** | Copy a file from local Android storage to a remote host |
| **Download (Remote → Phone)** | Copy a file from a remote host to local Android storage |

#### 2. Connection Parameters

The user must be able to specify:

- **Host** — hostname or IP address of the remote machine
- **Port** — SSH port (default: `22`)
- **Username** — remote account username
- **Authentication method** — one of:
  - Password (typed at transfer time or optionally saved and encrypted)
  - Private key file (PEM/OpenSSH format; referenced by file path or `content://` URI)
- **Remote path** — absolute or `~`-relative path on the remote host

#### 3. File Selection

- **Upload:** User picks a file via the Android system file picker.
- **Download:** User types or browses the remote path; destination folder on device is selectable.
- Support single file transfers; directory transfers use SCP recursive mode.

#### 4. Progress & Feedback

- Display per-file progress (bytes transferred, total, percentage).
- Show transfer speed (KB/s or MB/s) and estimated time remaining.
- Transfers must continue in the background (foreground service) if the user navigates away.
- Display a clear success or failure message when the transfer completes.

#### 5. Saved Connections

- Users can save named connection profiles (host, port, username, auth method).
- Saved profiles are listed for quick reuse on the Transfer screen.
- Passwords are stored in `EncryptedSharedPreferences` (Android Keystore-backed AES256-GCM).
- Private key files are referenced by file path or `content://` URI; only the path is persisted.

#### 6. Transfer History

- Log of recent transfers: timestamp, direction, remote host, file name, size, status (success/failure/cancelled/resumed).
- Filterable by status (All / Success / Failed / Resumed / Cancelled).
- "Transfer again" action on any history entry pre-fills the Transfer screen.
- User can clear history.

#### 7. Cancellation

- The user can cancel an in-progress transfer at any time from the Progress screen.
- The foreground service is stopped and the job is marked Cancelled.

#### 8. Resumable Transfers

- **Downloads:** If a transfer is interrupted, resume from the byte offset already received (SFTP `seek` to offset). The UI indicates "Resuming" vs "Starting".
- **Uploads:** Remote file is written to `<remotePath>.part` and atomically renamed to `<remotePath>` on successful completion, ensuring no corrupt file is left visible on interruption.

---

### Non-Functional Requirements

| Requirement | Detail |
|-------------|--------|
| **Security** | All transfers encrypted via SSH. Private keys and passwords never logged. TOFU host-key verification with SHA-256 fingerprint display; key-change warnings block the transfer until the user explicitly accepts. |
| **Compatibility** | Android 8.0 (API 26) and above. |
| **Permissions** | `READ_MEDIA_*` / `READ_EXTERNAL_STORAGE`, `INTERNET`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`. |
| **SSH Library** | SSHJ (`com.hierynomus:sshj`) — do not implement SSH from scratch. |
| **Performance** | Streaming transfer; never read entire files into a `ByteArray` in memory. Support coroutine cancellation via `isActive` checks in transfer loops. |
| **Error handling** | Map SSH exceptions to user-friendly `TransferError` sealed types: `AuthFailure`, `HostUnreachable`, `PermissionDenied`, `DiskFull`, `Unknown`. |

---

### User Interface

#### Screens

1. **Home** — Launcher for all utilities with an active-transfer banner linking to the Progress screen.
2. **SCP Transfer Screen** — Connection section (host, port, username, auth), Transfer section (direction, local path, remote path), Options section (profile picker, save password). Inline validation errors on all fields.
3. **Transfer Progress Screen** — Live progress bar, speed, ETA, bytes transferred, and Cancel button.
4. **Saved Connections Screen** — CRUD for connection profiles via a bottom sheet editor; file picker for private key path.
5. **Transfer History Screen** — List of past transfers, status filter chips, "Transfer again" action.

---

### Out of Scope (v1)

- Multi-hop / jump-host support
- SCP server mode (acting as the remote host)
- Fingerprint/biometric unlock before revealing saved credentials

---

## Feature: SFTP Browser

### Summary

An interactive file browser for the remote host using SFTP over the same SSH connection. Allows the user to navigate the remote directory tree, inspect file metadata, and perform common file management operations.

---

### Functional Requirements

#### 1. Navigation

- Browse the remote filesystem by directory.
- Home directory is resolved on connect (`~` expansion via SFTP canonicalize).
- Breadcrumb bar shows the current path; each crumb is tappable to navigate up.
- Sort entries by Name, Size, or Date (ascending/descending); directories always appear before files.

#### 2. File Operations

| Operation | Description |
|-----------|-------------|
| **Rename** | Rename a file or directory in-place. |
| **Delete** | Delete a file or directory (recursive for directories). Confirmation dialog before deletion. |
| **Create Directory** | Create a new directory at the current path. |

#### 3. Selection & Transfer

- Tapping a file entry selects it; tapping a directory navigates into it.
- Selected file path can be passed back to the SCP Transfer screen as the remote path.

#### 4. Connection

- Launched from the SCP Transfer screen (uses the same connection profile / parameters).
- SFTP session is managed by the same `SshConnectionManager` as SCP; host-key TOFU applies.

---

### Non-Functional Requirements

- All SFTP operations are suspend functions running on `Dispatchers.IO` via coroutines.
- Errors (permission denied, host disconnected) surface as dismissable inline error messages, not crashes.
- Loading state shown while directory listings are in-flight.

---

## Feature: Curl Runner

### Summary

Allow the user to paste and run real curl commands using a bundled native curl binary. The feature stays close to real curl behavior while adapting file handling, lifecycle, and storage to Android.

For the full curl specification see [`docs/CURL_SPECS.md`](CURL_SPECS.md).

---

### Functional Requirements

#### 1. Command Input

- Paste a full raw curl command line; if text does not start with `curl`, it is prepended automatically.
- Support multiline commands with shell-style line continuations (`\`).
- Pre-run validation catches unclosed quotes and unknown option names.

#### 2. Execution

- One curl job runs at a time.
- Runs in the background via a foreground service if the user navigates away.
- User can cancel an in-progress job.

#### 3. Output

- Stdout and stderr shown in separate panels.
- Exit code, timing metadata, and truncation indicators available on the Results screen.
- User can copy stdout, copy stderr, or save output to the workspace.

#### 4. Logs & History

- Detailed run logs are **off by default**; opt-in in Curl Settings.
- Saved command history is **off by default**.
- User can clear all logs.

#### 5. Native Binary

- curl 8.8.0 compiled with OpenSSL 3.3.0 and nghttp2 1.62.1.
- Packaged as `jniLibs/libcurl_exec.so` per ABI (arm64-v8a, armeabi-v7a; x86_64 in debug builds only).
- Launched from `nativeLibraryDir` via `ProcessCurlExecutor`.
- JNI metadata bridge (`NativeCurlBridge`) exposes curl version and built-in protocol/feature lists.

---

### User Interface

#### Screens

1. **Curl Runner Screen** — Command text field, Run / Cancel button, validation error display.
2. **Curl Results Screen** — Stdout panel, stderr panel, exit code, timing, copy/save actions.
3. **Curl Logs Screen** — List of past curl run summaries (when logging enabled), clear-all action.
4. **Curl Settings Screen** — Toggle logging enabled, toggle save history.

---

## Feature: Workspace Browser

### Summary

An app-private file manager for the curl workspace directory. Users move files in and out via Android pickers; all curl file-path arguments are resolved relative to this workspace root.

---

### Functional Requirements

#### 1. Workspace Root

- Single global app-private directory (not user-selectable in v1).
- Absolute-style paths in curl commands (e.g., `/tmp/foo.json`) are mapped to workspace-relative paths by `WorkspacePathResolver` before execution.

#### 2. File Operations

| Operation | Description |
|-----------|-------------|
| **Browse** | Navigate directories within the workspace. |
| **Create Directory** | Create a subdirectory at the current path. |
| **Rename** | Rename a file or directory. |
| **Move** | Move a file or directory to another location within the workspace. |
| **Delete** | Delete a file or directory (recursive for directories). |
| **Import** | Pick an external file via the Android file picker and copy it into the workspace. |
| **Export** | Pick an export destination via the Android file picker and copy a workspace file out. |

#### 3. Path Normalization

- `WorkspacePathResolver.normalize()` handles empty paths, `.` and `..` segments, missing leading slashes, and whitespace.

---

### Non-Functional Requirements

- Workspace operations run on `Dispatchers.IO`.
- The workspace root is created on first access if it does not exist.

---

### User Interface

#### Screens

1. **Workspace Browser Screen** — File/directory list at current path, breadcrumbs, FAB for create directory, per-item context menu (rename, move, delete), import/export toolbar actions.

---

## Feature: Host-Key Verification (TOFU)

### Summary

All SSH connections (SCP, SFTP, and SFTP Browser) use Trust-On-First-Use (TOFU) host-key verification backed by a persistent `KnownHostRepository` (Room).

### Policy

| Scenario | Behavior |
|----------|----------|
| **First connection to a host** | `peekHostKey` fetches the SHA-256 fingerprint without authenticating. A dialog shows the fingerprint and prompts the user to Accept or Reject. |
| **Subsequent connections — key matches** | Connection proceeds silently (`VerificationResult.Trusted`). |
| **Subsequent connections — key changed** | A prominent warning dialog shows the old and new fingerprints. The transfer is blocked until the user explicitly accepts the new key or cancels. |
| **User rejects** | Connection is aborted; no fingerprint is stored. |

### Fingerprint Format

SHA-256 digest of the raw encoded public key, Base64-encoded without padding, prefixed with `SHA256:` — matching the OpenSSH fingerprint format.

---

## Resolved Design Questions

| Question | Resolution |
|----------|------------|
| Fingerprint/biometric before revealing credentials? | **Not implemented in v1.** Passwords are protected by Android Keystore at rest; biometric unlock deferred to a future release. |
| TOFU or strict host-key verification by default? | **TOFU.** First-connect fingerprint is shown to the user for manual verification before being persisted. |
| Maximum file size warning? | **Not implemented in v1.** Streaming transfers with no in-memory buffering make large files practical; a warning could be added in a future release. |
