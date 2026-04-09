# Android NetTools — Code Review TODO 1

Detailed follow-up task list derived from the first code review. This list focuses on the substantive issues found in transfer execution, SFTP browser wiring, local file handling, and shared progress state.

Work these items in order. Later sections depend on earlier sections.

---

## 1. Restore the real transfer execution path

- [ ] Replace placeholder service behavior in `TransferForegroundService`
  - [ ] Remove dummy `TransferProgress` creation from `processJob()`
  - [ ] Remove the comment-only placeholder that claims transfer logic is wired elsewhere
  - [ ] Make the service consume pending work from `TransferProgressHolder`
  - [ ] Decide whether the service should process one queued job at a time or drain the full queue
- [ ] Inject the real transfer dependencies into `TransferForegroundService`
  - [ ] Inject `TransferProgressHolder`
  - [ ] Inject `SshConnectionManager`
  - [ ] Inject `ScpClient`
  - [ ] Inject `SftpClient` if needed for resume/stat/path handling
  - [ ] Inject `KnownHostsManager`
  - [ ] Inject `TransferHistoryRepository`
  - [ ] Inject any helper needed to open local content streams if SAF support is added
- [ ] Start the foreground service correctly
  - [ ] Ensure `startForeground()` is called immediately after the service starts processing work
  - [ ] Confirm the service cannot be launched into an idle state where Android kills it for not entering foreground in time
  - [ ] Make `onStartCommand()` trigger processing instead of only handling cancel actions
- [ ] Execute the actual SCP transfer inside the service
  - [ ] Dequeue `PendingTransferParams`
  - [ ] Open the SSH session with `SshConnectionManager`
  - [ ] Branch on `TransferDirection`
  - [ ] Upload via `ScpClient` for upload jobs
  - [ ] Download via `ScpClient` for download jobs
  - [ ] Collect `Flow<TransferProgress>` updates and push them into the shared progress holder
  - [ ] Close SSH/SFTP resources with `use {}` or equivalent cleanup
- [ ] Implement robust job lifecycle handling
  - [ ] Mark jobs `QUEUED` before execution
  - [ ] Mark jobs `IN_PROGRESS` when work begins
  - [ ] Mark jobs `COMPLETED` only after the transfer actually finishes
  - [ ] Mark jobs `FAILED` on real errors
  - [ ] Mark jobs `CANCELLED` on user cancellation
  - [ ] Remove finished jobs from active progress state
  - [ ] Stop the service when no work remains
- [ ] Record transfer outcomes to history
  - [ ] Insert successful transfers into `TransferHistoryRepository`
  - [ ] Insert failed transfers with useful error detail
  - [ ] Insert cancelled transfers with cancelled status
- [ ] Wire notification behavior to real state
  - [ ] Show indeterminate notification before total size is known
  - [ ] Switch to determinate progress when total bytes are known
  - [ ] Show success notification on completion
  - [ ] Show failure notification on error
  - [ ] Keep cancel action working during active transfers
- [ ] Handle transfer errors end-to-end
  - [ ] Map low-level exceptions with `ErrorMapper`
  - [ ] Surface unknown-host-key and changed-host-key failures in a way the UI can react to
  - [ ] Avoid leaking credentials or key paths in logs or user-visible errors
- [ ] Validate the service flow
  - [ ] Start an upload and confirm bytes actually move
  - [ ] Start a download and confirm bytes actually move
  - [ ] Confirm progress updates appear on the Progress screen
  - [ ] Confirm the service stops itself after the queue is empty

---

## 2. Make `TransferProgressHolder` the single source of truth

- [ ] Remove duplicate transfer state from `TransferForegroundService`
  - [ ] Delete the service-local `_transferProgress` flow if the holder will own progress
  - [ ] Delete the service-local `_activeJobs` flow if the holder will own active jobs
  - [ ] Remove any unused binder/state exposure that no screen actually consumes
- [ ] Make all progress and job mutations atomic in `TransferProgressHolder`
  - [ ] Replace `_activeJobs.value = ...` with `_activeJobs.update { ... }`
  - [ ] Replace `_progress.value = ...` with `_progress.update { ... }`
  - [ ] Preserve current semantics for enqueue, status updates, and removal
- [ ] Review queue behavior for concurrency correctness
  - [ ] Confirm `ConcurrentLinkedQueue` is sufficient for the chosen execution model
  - [ ] Ensure a job cannot be dequeued twice
  - [ ] Ensure cancellation cannot leave stale entries in active state
- [ ] Align the UI with shared state
  - [ ] Keep `ProgressViewModel` observing only `TransferProgressHolder`
  - [ ] Ensure the service writes every state transition into the holder
  - [ ] Ensure the Transfer screen never assumes a job started successfully before the service acknowledges it
- [ ] Validate race-safety
  - [ ] Queue multiple transfers quickly and confirm no state is lost
  - [ ] Cancel a job while another is starting and confirm active/progress maps remain consistent

---

## 3. Wire the SFTP browser to real connection data

- [ ] Decide how the browser receives connection parameters
  - [ ] Prefer sharing the current transfer form state rather than duplicating connection state
  - [ ] Choose between `SavedStateHandle`, nav arguments, or a shared ViewModel-backed handoff
- [ ] Pass the required connection details when opening `SftpBrowserScreen`
  - [ ] Host
  - [ ] Port
  - [ ] Username
  - [ ] Auth type
  - [ ] Password if password auth is selected
  - [ ] Key path if key auth is selected
  - [ ] Optional initial remote path
- [ ] Connect the browser on screen entry
  - [ ] Call `SftpBrowserViewModel.connect(...)` from `SftpBrowserScreen`
  - [ ] Guard against repeated reconnects on recomposition
  - [ ] Show a clear error state if required connection inputs are missing
- [ ] Ensure path selection round-trip works
  - [ ] Return the selected remote path to `TransferScreen`
  - [ ] Preserve the chosen path in the transfer form
  - [ ] Confirm both file and directory selection behave as intended
- [ ] Revisit browser navigation details
  - [ ] Verify breadcrumb generation for home/root/absolute paths
  - [ ] Confirm home navigation resolves correctly on real servers
  - [ ] Confirm refresh and create-directory actions only run after connection succeeds
- [ ] Validate the browser flow
  - [ ] Open browser from a populated transfer form and confirm connection succeeds
  - [ ] Browse into directories and back out
  - [ ] Pick a path and confirm it appears in the transfer form
  - [ ] Confirm rename/delete/mkdir still work after the new wiring

---

## 4. Fix local-path handling so picker-selected files can actually transfer

- [ ] Decide on the supported local file model
  - [ ] Either support Storage Access Framework URIs end-to-end
  - [ ] Or restrict the UI to real filesystem paths only
  - [ ] Prefer SAF support because `OpenDocument` and `OpenDocumentTree` already return URIs
- [ ] If SAF is kept, refactor transfer code to support URI-backed I/O
  - [ ] Add a local file abstraction instead of assuming `java.io.File`
  - [ ] Support reading upload sources from `ContentResolver.openInputStream()`
  - [ ] Support writing downloads to `ContentResolver`/`DocumentFile`
  - [ ] Preserve streaming behavior; do not buffer full files in memory
  - [ ] Preserve cancellation behavior during stream copy
  - [ ] Preserve resume logic where feasible for SAF-backed destinations
  - [ ] Document any limitations if resume is unavailable for some URI types
- [ ] If filesystem paths are required instead, fix the UI accordingly
  - [ ] Stop storing `content://...` strings in `localPath`
  - [ ] Use a picker strategy that yields a real path, if valid on supported Android versions
  - [ ] Reject unsupported URI inputs with inline validation
- [ ] Update `TransferJob` semantics if needed
  - [ ] Clarify whether `localPath` is always a file path, always a URI string, or a polymorphic local source/destination reference
  - [ ] Rename fields or add types if the current `String localPath` model is too ambiguous
- [ ] Update progress/history display logic
  - [ ] Ensure filenames still render correctly for URI-backed jobs
  - [ ] Avoid showing opaque `content://...` values in primary UI when a display name is available
- [ ] Validate local-path behavior
  - [ ] Upload a file selected from SAF
  - [ ] Download into a directory selected from SAF
  - [ ] Verify failures are user-friendly when URI permissions are missing or revoked

---

## 5. Follow-up cleanup and regression coverage

- [ ] Add regression tests for the reviewed issues
  - [ ] Unit test `TransferProgressHolder` atomic state updates
  - [ ] Unit test service queue consumption / job lifecycle transitions
  - [ ] Unit test browser connection handoff logic
  - [ ] Unit test local-path parsing/abstraction logic
- [ ] Add integration coverage for end-to-end transfer execution
  - [ ] Upload flow through the service
  - [ ] Download flow through the service
  - [ ] Unknown-host-key flow through the service + UI
  - [ ] Cancellation while a transfer is active
- [ ] Re-run project validation after fixes
  - [ ] `./gradlew assembleDebug`
  - [ ] `./gradlew lintDebug`
  - [ ] `./gradlew test`
  - [ ] `./gradlew assembleRelease`

---

## Recommended implementation order

1. Restore real service execution.
2. Make `TransferProgressHolder` the only live state owner.
3. Wire the SFTP browser connection handoff.
4. Fix SAF/local-path handling.
5. Add regression coverage and re-run validation.
