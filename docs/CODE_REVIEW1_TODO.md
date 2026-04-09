# Code Review TODO

Detailed follow-up task list based on the latest code review. This list focuses on correctness, transfer reliability, lifecycle safety, and behavior mismatches with `docs/SPECS.md`.

---

## 1. Fix transfer queue execution semantics

- [x] Make queued transfers execute one at a time
  - [x] Refactor `TransferForegroundService.processAllQueued()` so it does not launch one coroutine per dequeued job
  - [x] Replace the current drain-the-queue behavior with a worker loop that starts the next job only after the previous job finishes
  - [x] Ensure only one active transfer is started unless configurable concurrency is intentionally introduced
  - [x] Keep cancellation working for the currently running job
- [x] Prevent duplicate or overlapping queue processing
  - [x] Verify repeated `onStartCommand()` calls cannot start a second queue-processing loop
  - [x] Ensure `restorePersistedJobs()` plus `processAllQueued()` cannot re-enqueue/rerun jobs already in progress
- [x] Add regression tests
  - [x] Add a service/holder test proving two queued jobs do not run concurrently
  - [x] Add a test proving the next job starts only after the prior one reaches a terminal state

## 2. Fix resumed download progress reporting

- [x] Emit progress updates during `downloadResumable()`
  - [x] Refactor `ScpClient.downloadResumable()` so resumed SFTP downloads report byte progress continuously
  - [x] Ensure progress reflects resumed offsets correctly rather than restarting from zero visually
  - [x] Verify notifications and progress UI update during resumed downloads
- [x] Add test coverage
  - [x] Add a unit/integration test asserting resumed downloads emit at least one progress event
  - [x] Add a test asserting final transferred byte count matches the remote file size after resume

## 3. Fix SFTP delete behavior for directories

- [x] Support deleting both files and directories from the SFTP browser
  - [x] Update `SftpClient.delete()` to inspect the remote path type before deleting
  - [x] Use file deletion for files and directory deletion for directories
  - [x] Decide and implement whether non-empty directories should be deleted recursively or rejected with a user-facing error
- [x] Align UI behavior with backend behavior
  - [x] Ensure the SFTP browser only offers operations that are actually supported
  - [x] Show a clear error if recursive directory delete is intentionally unsupported
- [x] Add regression tests
  - [x] Add a test for deleting a file
  - [x] Add a test for deleting an empty directory
  - [x] Add a test for deleting a non-empty directory based on the intended product behavior

## 4. Fix breadcrumb generation and navigation

- [x] Correct breadcrumb generation for absolute remote paths
  - [x] Update `buildBreadcrumbs()` so `/var/www` produces valid absolute breadcrumb targets
  - [x] Preserve correct behavior for `~` and home-relative navigation
  - [x] Avoid dropping the first real path segment for absolute paths
- [x] Validate breadcrumb UX
  - [x] Verify tapping each crumb navigates to the expected directory
  - [x] Verify home/root breadcrumbs are labeled clearly
- [x] Add regression tests
  - [x] Add pure unit tests for `~`
  - [x] Add pure unit tests for `/`
  - [x] Add pure unit tests for nested absolute paths
  - [x] Add pure unit tests for nested home-relative paths

## 5. Fix false-success path for SAF downloads

- [x] Fail the transfer if SAF destination creation fails
  - [x] Replace the silent `createDocument(... ) ?: return` path in `copySafDownload()` with an explicit failure
  - [x] Surface a user-friendly error when SAF permissions are missing, revoked, or destination creation fails
  - [x] Ensure the transfer is marked `FAILED`, not `COMPLETED`, when the final copy cannot be written
- [x] Harden SAF output writing
  - [x] Fail if `openOutputStream()` returns null
  - [x] Ensure partial temp files are cleaned up when SAF finalization fails
- [x] Add regression tests
  - [x] Add a test proving SAF document creation failure produces a failed transfer state
  - [x] Add a test proving null SAF output streams do not produce success history entries

## 6. Implement real resume support for SAF downloads

- [x] Preserve partial download state across retries for SAF targets
  - [x] Stop tying resumable temp files to transient `jobId` values
  - [x] Use a stable temp-file naming strategy derived from destination + remote path
  - [x] Keep partial temp files when a resumable retry is possible
  - [x] Clean up temp files only after successful finalization or explicit restart/discard
- [x] Make resume detection work for SAF destinations
  - [x] Detect existing partial cache files before starting a new SAF download
  - [x] Feed the existing byte count into `downloadResumable()`
  - [x] Indicate in UI/history/notification when a SAF transfer is being resumed
- [x] Handle stale or invalid partial files
  - [x] Detect mismatched remote size/content where possible
  - [x] Decide whether to restart automatically or prompt the user
- [x] Add regression tests
  - [x] Add a test proving an interrupted SAF download resumes from prior cached bytes
  - [x] Add a test proving successful completion cleans up the stable temp file

## 7. Replace non-streaming SAF upload staging

- [x] Remove full-file cache copies for SAF uploads
  - [x] Replace `resolveUploadFile()` temp staging with a streaming source backed by `ContentResolver`
  - [x] Ensure uploads from `content://` URIs do not require duplicating the full file in app cache
  - [x] Preserve cancellation behavior and cleanup guarantees
- [x] Verify compatibility with SSHJ
  - [x] Introduce the correct SSHJ source abstraction for streaming SAF content
  - [x] Ensure file name and file size metadata remain available for progress reporting
- [x] Add regression tests
  - [x] Add a test covering upload from a SAF-backed source
  - [x] Add a test proving large SAF uploads no longer depend on a temp file copy

## 8. Improve transfer finalization and error surfacing

- [x] Remove silent failure paths in critical transfer-finalization code
  - [x] Replace ignored `runCatching`/empty `catch` blocks with explicit logging or failure propagation where appropriate
  - [x] Review history recording, SSH cleanup, and SFTP browser session teardown for swallowed failures
- [x] Review cancellation history accuracy
  - [x] Confirm cancelled transfers record sensible byte counts and status details
  - [x] Decide whether cancelled entries should show last known transferred bytes or zero bytes
- [x] Add regression tests
  - [x] Add tests for cancellation history entries
  - [x] Add tests for failure notifications and history rows when finalization fails late

## 9. Review service startup robustness

- [x] Make foreground startup resilient with large restored queues
  - [x] Ensure the service enters foreground immediately, before any potentially slow restore/queue work
  - [x] Verify startup remains safe even with many persisted jobs
- [x] Add regression coverage
  - [x] Add a test or design-level guard proving queue restore cannot delay foreground startup past Android limits

## 10. Final validation after fixes

- [x] Re-run full verification after implementing the above
  - [x] Run `./gradlew lintDebug`
  - [x] Run `./gradlew testDebugUnitTest`
  - [x] Run any additional existing project test tasks needed for touched code paths
- [x] Update docs if behavior changes
  - [x] Update `docs/TODO.md` only if new follow-up work is added there intentionally
  - [x] Update any user-facing behavior notes affected by SAF resume/delete semantics

---

## Recommended implementation order

1. Queue serialization
2. SAF false-success fix
3. Resumed-download progress
4. Directory delete support
5. Breadcrumb fix
6. SAF resume persistence
7. Streaming SAF uploads
8. Cleanup/error-handling hardening
