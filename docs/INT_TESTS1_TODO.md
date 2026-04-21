# INT_TESTS1_TODO.md — Integration Test TODO List

All tests use Apache MINA SSHD (already in test deps) as a real in-process SSH server.
New test files go in `app/src/test/kotlin/dev/nettools/android/data/ssh/`.
Follow the established pattern in `ScpClientIntegrationTest` for server setup/teardown.

---

## Phase 1 — SftpClient Integration Tests

**New file:** `app/src/test/kotlin/dev/nettools/android/data/ssh/SftpClientIntegrationTest.kt`

`SftpClient` only has mocked unit tests (SftpClientTest.kt). Every public method needs
a real MINA SSHD-backed integration test to verify the SSHJ ↔ MINA wire protocol works
correctly and that domain model mapping (RemoteFileEntry fields) is accurate.

Server setup: same as ScpClientIntegrationTest — MINA SSHD with SftpSubsystemFactory,
VirtualFileSystemFactory rooted at a @TempDir, port=0.

### Task 1.1 — listDirectory

- [x] 1.1.1 `listDirectory` on an empty directory returns an empty list
- [x] 1.1.2 `listDirectory` on a directory with files returns one RemoteFileEntry per file
- [x] 1.1.3 `listDirectory` entries have correct `name`, `path`, and `isDirectory=false` for files
- [x] 1.1.4 `listDirectory` entries have `isDirectory=true` for subdirectories
- [x] 1.1.5 `listDirectory` does NOT include `.` or `..` entries
- [x] 1.1.6 `listDirectory` entries have correct `sizeBytes` matching the actual file size
- [x] 1.1.7 `listDirectory` `path` field is the full absolute path (`/parent/child`)

### Task 1.2 — resolvePath

- [x] 1.2.1 `resolvePath("~")` returns the home directory (not the literal string `~`)
- [x] 1.2.2 `resolvePath("~/subdir")` expands correctly relative to home
- [x] 1.2.3 `resolvePath("/absolute/path")` returns the path unchanged
- [x] 1.2.4 `resolvePath(".")` returns the home directory

### Task 1.3 — mkdir

- [x] 1.3.1 `mkdir("/newdir")` creates a directory that then appears in `listDirectory`
- [x] 1.3.2 `mkdir` on an already-existing path throws an exception (server-side error)

### Task 1.4 — rename

- [x] 1.4.1 `rename("/old.txt", "/new.txt")` moves the file; old path no longer exists, new path exists
- [x] 1.4.2 After rename, the entry returned by `listDirectory` uses the new name
- [x] 1.4.3 `rename` on a directory renames the directory and all children

### Task 1.5 — delete

- [x] 1.5.1 `delete("/file.txt")` removes a file; `listDirectory` no longer includes it
- [x] 1.5.2 `delete("/dir")` removes a non-empty directory recursively (no error)
- [x] 1.5.3 `delete` on a non-existent path throws an exception

### Task 1.6 — stat

- [x] 1.6.1 `stat("/file.txt")` returns a RemoteFileEntry with correct size and `isDirectory=false`
- [x] 1.6.2 `stat("/dir")` returns `isDirectory=true`
- [x] 1.6.3 `stat` on a non-existent path returns null (not an exception)

### Task 1.7 — getFileSize

- [x] 1.7.1 `getFileSize("/file.txt")` returns the exact byte length of the file
- [x] 1.7.2 `getFileSize` on a non-existent path returns null

### Task 1.8 — resolveUploadDestination

- [x] 1.8.1 When remotePath is an existing directory, fileName is appended: `"/uploads" → "/uploads/file.txt"`
- [x] 1.8.2 When remotePath is a full file path (not an existing dir), it is returned as-is
- [x] 1.8.3 When remotePath ends with `/`, fileName is always appended

---

## Phase 2 — SshConnectionManager TOFU / Host-Key Integration Tests

**New file:** `app/src/test/kotlin/dev/nettools/android/data/ssh/SshConnectionManagerIntegrationTest.kt`

These tests exercise the TOFU security flow end-to-end through the real
`SshConnectionManager` + `KnownHostsManager` + an in-memory `KnownHostRepository` fake.
The MINA server uses a fixed key pair so that fingerprints are deterministic.

Server setup: MINA SSHD with `SimpleGeneratorHostKeyProvider`, a fixed key file written
to a @TempDir so the server's fingerprint is stable across test runs.

### Task 2.1 — peekHostKey

- [x] 2.1.1 `peekHostKey` returns a non-null fingerprint starting with `"SHA256:"` when server is reachable
- [x] 2.1.2 `peekHostKey` returns the same fingerprint on repeated calls to the same server
- [x] 2.1.3 `peekHostKey` does NOT leave an authenticated SSH session open (server has no authenticated sessions after the call)
- [x] 2.1.4 `peekHostKey` returns null when the host is unreachable (port not listening)

### Task 2.2 — TOFU: first connection

- [x] 2.2.1 `KnownHostsManager.checkAndVerify` returns `FirstConnect` when no fingerprint is stored yet
- [x] 2.2.2 After `acceptHost`, `checkAndVerify` returns `Trusted` for the same host:port
- [x] 2.2.3 The accepted fingerprint matches the one returned by `peekHostKey`

### Task 2.3 — connect: trusted host (password auth)

- [x] 2.3.1 `connect` succeeds after `acceptHost` has stored the server's fingerprint
- [x] 2.3.2 The returned SSHClient is authenticated (can open an SFTP session)
- [x] 2.3.3 `connect` throws `TransferError.AuthFailure` when password is wrong on a trusted host
- [x] 2.3.4 The SSHClient is closed by `connect` before the exception propagates (no resource leak)

### Task 2.4 — connect: key changed (MITM warning)

- [x] 2.4.1 When a second MINA server on a different port is pre-registered under the first server's host:port, `checkAndVerify` returns `KeyChanged` with the correct old and new fingerprints
- [x] 2.4.2 `buildHostKeyVerifier` rejects (returns false) when `checkAndVerify` returns `KeyChanged`, causing `connect` to throw

### Task 2.5 — connect: unknown host (no stored key)

- [x] 2.5.1 `connect` throws (does not silently succeed) when no fingerprint is stored and the host verifier returns false
- [x] 2.5.2 `peekHostKey` + `acceptHost` + `connect` is the correct TOFU sequence; verify it all succeeds end-to-end

### Task 2.6 — connect: unreachable host

- [x] 2.6.1 `connect` to a port with no listener throws `TransferError.HostUnreachable`
- [x] 2.6.2 `connect` to an unknown hostname throws `TransferError.HostUnreachable`

---

## Phase 3 — ScpClient Cancellation & Error Integration Tests

**Extend file:** `app/src/test/kotlin/dev/nettools/android/data/ssh/ScpClientIntegrationTest.kt`

The existing tests cover the happy path. These tests cover cancellation mid-stream
and server-side error propagation.

### Task 3.1 — Upload cancellation

- [x] 3.1.1 Cancelling the collector coroutine mid-upload does not leave a `.part` file on the server after a short delay (SFTP rename never happens; `.part` is abandoned or cleaned)
- [x] 3.1.2 Cancelling mid-upload throws `CancellationException` (does not swallow it silently)
- [x] 3.1.3 After cancellation, the server-side `.part` file's size is less than the full file size (confirms the cancel was mid-stream)

### Task 3.2 — Download cancellation

- [x] 3.2.1 Cancelling mid-download leaves a partial local file (not a complete one)
- [x] 3.2.2 Cancelling mid-download throws `CancellationException`
- [x] 3.2.3 After cancellation, the local partial file's size is less than the total file size

### Task 3.3 — Upload: remote path permission error

- [x] 3.3.1 Uploading to a path inside a read-only directory on the server emits a failure through the Flow (close with exception) — the Flow terminates with a non-CancellationException

### Task 3.4 — Large file transfer progress fidelity

- [x] 3.4.1 Upload of a 1 MB file emits multiple progress events (not just one at the end)
- [x] 3.4.2 The final progress event has `bytesTransferred == totalBytes`
- [x] 3.4.3 Download of a 1 MB file emits multiple progress events
- [x] 3.4.4 `speedBytesPerSec` is positive on all progress events

---

## Phase 4 — ErrorMapper End-to-End Integration Tests

**New file:** `app/src/test/kotlin/dev/nettools/android/data/ssh/ErrorMapperIntegrationTest.kt`

`ErrorMapperTest` verifies class-name heuristics in isolation. These tests verify that
real SSHJ exceptions thrown by a live MINA server are correctly mapped by
`SshConnectionManager.connect()`.

Server setup: MINA SSHD with strict password auth (only one valid credential pair).

### Task 4.1 — Authentication failures

- [x] 4.1.1 Wrong password → `connect` throws `TransferError.AuthFailure`
- [x] 4.1.2 Correct username, wrong key type (password auth but wrong credential) → `TransferError.AuthFailure`

### Task 4.2 — Network failures

- [x] 4.2.1 Connecting to a port with no listener (refused) → `TransferError.HostUnreachable`
- [x] 4.2.2 Connecting to a valid port that closes immediately (server.stop() before connect) → `TransferError.HostUnreachable`

### Task 4.3 — Permission denied

- [x] 4.3.1 Attempting SFTP `mkdir` in a directory where the VirtualFileSystem rejects writes → the exception propagates and can be mapped via `ErrorMapper.mapException` to `TransferError.PermissionDenied`

---

## Phase 5 — SftpClient + ScpClient Combined Flow Integration Test

**New file:** `app/src/test/kotlin/dev/nettools/android/data/ssh/TransferFlowIntegrationTest.kt`

End-to-end scenario: browse via SftpClient → identify file → upload/download via ScpClient.
This verifies that the two clients interoperate on the same SSHClient connection without
session conflicts.

### Task 5.1 — Upload then browse

- [x] 5.1.1 Upload a file via `ScpClient.upload`; immediately `SftpClient.listDirectory` on the same SSHClient shows the new file
- [x] 5.1.2 `SftpClient.stat` on the uploaded file returns the correct size

### Task 5.2 — Browse, rename, then download

- [x] 5.2.1 Create a file on the server; `SftpClient.listDirectory` shows it; `SftpClient.rename` renames it; `ScpClient.download` retrieves it under the new name with correct content

### Task 5.3 — Upload → resume download

- [x] 5.3.1 Upload a 100-byte file via ScpClient; locally create a 50-byte partial file; `ScpClient.downloadResumable` with offset=50 produces a file identical to the original

### Task 5.4 — mkdir → upload into new directory

- [x] 5.4.1 `SftpClient.mkdir("/newdir")`; `ScpClient.upload(sshClient, file, "/newdir/file.txt")` succeeds; `SftpClient.listDirectory("/newdir")` shows the file

---

## Implementation Notes

- **Server setup template** (copy from ScpClientIntegrationTest):
  ```kotlin
  server = SshServer.setUpDefaultServer().apply {
      port = 0
      keyPairProvider = SimpleGeneratorHostKeyProvider()
      passwordAuthenticator = PasswordAuthenticator { _, _, _ -> true }
      commandFactory = ScpCommandFactory.Builder().build()
      subsystemFactories = listOf(SftpSubsystemFactory.Builder().build())
      fileSystemFactory = VirtualFileSystemFactory(serverRoot)
      start()
  }
  ```
- For Phase 2 **strict auth**, use `PasswordAuthenticator { user, pass, _ -> user == "user" && pass == "secret" }` and reject all others.
- For Phase 2 **KeyChanged**, start two servers with different host key files. Accept server A's fingerprint, then try to connect to server B using server A's host:port entry.
- For **Phase 3 cancellation tests**, use `Job().cancel()` on the collection scope or `take(1)` on the Flow to trigger mid-stream cancellation of a large file.
- For **Phase 4 permission denied**, override `VirtualFileSystemFactory` with a custom filesystem that throws `AccessDeniedException` on mkdir.
- **Run lint and tests separately** to avoid the AGP lint/kapt stub bug:
  ```
  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew --no-daemon --console=plain lintDebug
  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew --no-daemon --console=plain test
  ```
- **Commit after each Phase** once lint and tests pass.
