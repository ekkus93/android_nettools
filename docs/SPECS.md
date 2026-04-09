# Android NetTools — Feature Specifications

## Overview

**Android NetTools** is a Swiss Army knife of network utilities for Android. It provides power-user networking tools in a clean, unified mobile interface.

---

## Feature: SCP (Secure Copy Protocol)

### Summary

Allow the user to securely transfer files and directories between their Android device and a remote host using the SCP protocol (SSH-based file transfer).

---

### Functional Requirements

#### 1. Transfer Directions

| Direction | Description |
|-----------|-------------|
| **Upload (Phone → Remote)** | Copy a file or directory from local Android storage to a remote host |
| **Download (Remote → Phone)** | Copy a file or directory from a remote host to local Android storage |

#### 2. Connection Parameters

The user must be able to specify:

- **Host** — hostname or IP address of the remote machine
- **Port** — SSH port (default: `22`)
- **Username** — remote account username
- **Authentication method** — one of:
  - Password (typed at transfer time or optionally saved)
  - Private key file (PEM/OpenSSH format, stored on device)
- **Remote path** — absolute or `~`-relative path on the remote host

#### 3. File Selection

- **Upload:** User picks a file or directory via the Android system file picker or an in-app file browser.
- **Download:** User types or browses the remote path; destination folder on device is selectable.
- Support single files and recursive directory transfers.

#### 4. Progress & Feedback

- Display a per-file and overall progress bar during transfer.
- Show transfer speed (KB/s or MB/s) and estimated time remaining.
- Display a clear success or failure notification when the transfer completes.
- Transfers must continue in the background (foreground service) if the user navigates away.

#### 5. Host Management (Saved Connections)

- Users can save named connection profiles (host, port, username, auth method).
- Saved profiles are listed for quick reuse.
- Passwords are stored in Android Keystore / EncryptedSharedPreferences; private key files are referenced by path.

#### 6. Transfer History

- Log of recent transfers: timestamp, direction, remote host, file/directory name, size, status (success/failure/cancelled).
- Retained locally; user can clear history.

#### 7. Cancellation

- The user can cancel an in-progress transfer at any time.
- Partially transferred files are cleaned up on cancellation.

---

### Non-Functional Requirements

| Requirement | Detail |
|-------------|--------|
| **Security** | All transfers encrypted via SSH. Private keys and passwords never logged. Strict host-key verification with option to trust-on-first-use (TOFU). |
| **Compatibility** | Android 8.0 (API 26) and above. |
| **Permissions** | `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (or `READ_MEDIA_*` on API 33+), `INTERNET`, `FOREGROUND_SERVICE`. |
| **Dependency** | Use an established Java/Kotlin SSH library (e.g., [JSch](http://www.jcraft.com/jsch/) or [SSHJ](https://github.com/hierynomus/sshj)) — do not implement SSH from scratch. |
| **Performance** | Streaming transfer; avoid loading entire files into memory. |
| **Error handling** | Surface descriptive error messages (auth failure, host unreachable, permission denied, disk full, etc.). |

---

### User Interface

#### Screens

1. **Home / Utility Launcher** — Grid or list of available network tools (SCP is the first entry).
2. **SCP Transfer Screen**
   - Connection fields (or saved-profile picker)
   - Direction toggle (Upload / Download)
   - Local path selector
   - Remote path field
   - **Transfer** button
3. **Transfer Progress Sheet** — Full-screen or bottom sheet showing live progress.
4. **Saved Connections Screen** — CRUD for connection profiles.
5. **Transfer History Screen** — List of past transfers.

#### UX Notes

- Material Design 3 components.
- Support both light and dark themes.
- Inline validation on connection fields before transfer starts (e.g., empty host, malformed path).

---

### Out of Scope (v1)

- Multi-hop / jump-host support
- SCP server mode (acting as the remote host)

---

### v1 Additions

#### SFTP Browser

- Interactive file browser for the remote host using SFTP (a natural complement to SCP over the same SSH connection).
- Allows the user to navigate the remote directory tree, preview file names/sizes/permissions, and select items to transfer.
- Support basic remote operations: rename, delete, create directory.
- Launches from the SCP Transfer Screen as an optional remote-path picker.

#### Resumable Transfers

- For downloads: if a transfer is interrupted (network drop, app killed), resume from the byte offset already received rather than restarting from zero.
- For uploads: resume from the last confirmed byte written on the remote side (requires checking remote file size via SFTP before restarting).
- UI indicates when a transfer is being resumed vs. started fresh.
- Partial/incomplete files on the remote are marked with a `.part` suffix until the transfer completes successfully.

---

### Open Questions

1. Should the app support fingerprint/biometric unlock before revealing saved credentials?
2. Should known-hosts verification be strict by default, or TOFU?
3. Is there a maximum file size limit we should warn users about?
