# Unit Tests 1 — TODO List

Based on the test coverage gap analysis.  
All new tests go in `app/src/test/kotlin/dev/nettools/android/`.  
Framework: **JUnit 5 + MockK** (matching the existing test suite).  
Use `UnconfinedTestDispatcher` + `Dispatchers.setMain` for all ViewModel tests.

Legend: `[ ]` = pending · `[x]` = done

---

## Phase 1 — SavedConnectionsViewModel (High Priority)

**New file:** `ui/connections/SavedConnectionsViewModelTest.kt`

### Task 1.1 — openEditor: new profile

- [x] 1.1.1 `openEditor(null)` sets `editState` to a blank `ProfileEditState` with `isNew = true`,
      default port `"22"`, and `AuthType.PASSWORD`.
- [x] 1.1.2 `openEditor(null)` does NOT call `credentialStore.getPassword()` for new profiles.

### Task 1.2 — openEditor: existing profile (no saved password)

- [x] 1.2.1 `openEditor(profile)` with `profile.savePassword = false` pre-populates all fields:
      `name`, `host`, `port.toString()`, `username`, `authType`, `keyPath`.
- [x] 1.2.2 `openEditor(profile)` with `profile.savePassword = false` sets `password = ""`
      without calling `credentialStore.getPassword()`.
- [x] 1.2.3 `openEditor(profile)` sets `isNew = false` on the resulting `editState`.

### Task 1.3 — openEditor: existing profile with saved password

- [x] 1.3.1 `openEditor(profile)` with `profile.savePassword = true` calls
      `credentialStore.getPassword(profile.id)` and populates `password` in `editState`.
- [x] 1.3.2 If `credentialStore.getPassword()` returns `null`, `editState.password` is `""`.

### Task 1.4 — dismissEditor

- [x] 1.4.1 `dismissEditor()` sets `editState` to `null`.

### Task 1.5 — field change helpers clear validation errors

- [x] 1.5.1 `onNameChange("x")` updates `editState.name` and clears `nameError`.
- [x] 1.5.2 `onHostChange("x")` updates `editState.host` and clears `hostError`.
- [x] 1.5.3 `onPortChange("x")` updates `editState.port` and clears `portError`.
- [x] 1.5.4 `onUsernameChange("x")` updates `editState.username` and clears `usernameError`.

### Task 1.6 — saveProfile: validation errors

- [x] 1.6.1 `saveProfile()` with blank `name` sets `editState.nameError` to a non-null message
      and does NOT call `profileRepository.save()`.
- [x] 1.6.2 `saveProfile()` with blank `host` sets `editState.hostError`.
- [x] 1.6.3 `saveProfile()` with `port = "0"` sets `editState.portError`
      ("Port must be 1–65535").
- [x] 1.6.4 `saveProfile()` with `port = "65536"` sets `editState.portError`.
- [x] 1.6.5 `saveProfile()` with `port = "abc"` (non-numeric) sets `editState.portError`.
- [x] 1.6.6 `saveProfile()` with blank `username` sets `editState.usernameError`.
- [x] 1.6.7 Multiple validation failures are all set simultaneously (all four errors can
      be present at once — fill no fields and save).

### Task 1.7 — saveProfile: happy path (new profile)

- [x] 1.7.1 `saveProfile()` with all valid fields calls `profileRepository.save()` with a
      newly generated UUID (not the blank default ID) and correct field values.
- [x] 1.7.2 After a successful save, `editState` is set to `null`.
- [x] 1.7.3 When `savePassword = true` and `password` is non-blank, calls
      `credentialStore.savePassword(newId, password)`.
- [x] 1.7.4 When `savePassword = false`, calls `credentialStore.deletePassword(newId)`
      (to clear any previously stored password for the same ID).

### Task 1.8 — saveProfile: happy path (edit existing)

- [x] 1.8.1 `saveProfile()` for an existing profile uses the profile's existing `id`,
      not a new UUID.
- [x] 1.8.2 When `savePassword = true` and `password` is blank, does NOT call
      `credentialStore.savePassword()` (blank password is not stored).

### Task 1.9 — delete flow

- [x] 1.9.1 `requestDelete(id)` sets `deleteConfirmId` to the given ID.
- [x] 1.9.2 `dismissDelete()` sets `deleteConfirmId` to `null`.
- [x] 1.9.3 `confirmDelete(id)` calls `profileRepository.delete(id)`.
- [x] 1.9.4 `confirmDelete(id)` calls `credentialStore.deletePassword(id)`.
- [x] 1.9.5 After `confirmDelete`, `deleteConfirmId` is set to `null`.

---

## Phase 2 — WorkspacePathResolver (High Priority, Zero Effort)

**New file:** `data/workspace/WorkspacePathResolverTest.kt`

### Task 2.1 — empty / blank input

- [x] 2.1.1 `normalize("")` returns `"/"`.
- [x] 2.1.2 `normalize("   ")` (whitespace only) returns `"/"`.

### Task 2.2 — root

- [x] 2.2.1 `normalize("/")` returns `"/"`.
- [x] 2.2.2 `normalize("//")` returns `"/"`.
- [x] 2.2.3 `normalize("///a")` collapses consecutive slashes → `"/a"`.

### Task 2.3 — simple paths

- [x] 2.3.1 `normalize("/foo")` returns `"/foo"`.
- [x] 2.3.2 `normalize("/foo/bar")` returns `"/foo/bar"`.
- [x] 2.3.3 `normalize("/foo/bar/baz")` returns `"/foo/bar/baz"`.

### Task 2.4 — dot segments

- [x] 2.4.1 `normalize("/./foo")` removes `.` segment → `"/foo"`.
- [x] 2.4.2 `normalize("/foo/./bar")` removes middle `.` → `"/foo/bar"`.
- [x] 2.4.3 `normalize("/foo/.")` trailing dot → `"/foo"`.

### Task 2.5 — double-dot segments

- [x] 2.5.1 `normalize("/a/b/../c")` pops `b` → `"/a/c"`.
- [x] 2.5.2 `normalize("/a/../b/../c")` → `"/c"`.
- [x] 2.5.3 `normalize("/a/../../b")` double-dot beyond root stays at root → `"/b"`.
- [x] 2.5.4 `normalize("/../..")` → `"/"` (cannot escape root).
- [x] 2.5.5 `normalize("/a/b/c/../../d")` → `"/a/d"`.

### Task 2.6 — leading/trailing whitespace

- [x] 2.6.1 `normalize("  /foo/bar  ")` trims and normalizes → `"/foo/bar"`.

### Task 2.7 — paths without leading slash

- [x] 2.7.1 `normalize("foo/bar")` treats as relative and returns `"/foo/bar"`.

---

## Phase 3 — SftpBrowserViewModel behavior (Medium Priority)

**New file:** `ui/sftp/SftpBrowserViewModelTest.kt`  
Mock: `SshConnectionManager`, `SftpClient`, `KnownHostsManager`, `TransferProgressHolder`.

### Task 3.1 — setSortOrder

- [x] 3.1.1 `setSortOrder(SortOrder.SIZE)` re-sorts `uiState.entries` by size ascending
      without making any new SFTP calls (mock `sftpClient` should not be called).
- [x] 3.1.2 `setSortOrder(order)` updates `uiState.sortOrder` to the new value.
- [x] 3.1.3 Directories remain before files regardless of which sort order is applied.

### Task 3.2 — navigate: success

- [x] 3.2.1 `navigate("/some/path")` calls `sftpClient.listDirectory(client, "/some/path")`.
- [x] 3.2.2 On success, `uiState.currentPath` is updated to `"/some/path"`.
- [x] 3.2.3 On success, `uiState.entries` reflects the returned list (sorted by current order).
- [x] 3.2.4 On success, `uiState.breadcrumbs` matches `buildBreadcrumbs("/some/path")`.
- [x] 3.2.5 `uiState.isLoading` is `false` after the call completes.

### Task 3.3 — navigate: failure

- [x] 3.3.1 When `sftpClient.listDirectory()` throws, `uiState.errorMessage` is set to a
      non-null, non-blank string.
- [x] 3.3.2 `uiState.isLoading` is `false` after a failed navigate call.
- [x] 3.3.3 `onErrorDismissed()` clears `uiState.errorMessage` to `null`.

### Task 3.4 — rename dialog state machine

- [x] 3.4.1 `requestRename(entry)` sets `uiState.renameTarget = entry` and
      `uiState.renameNewName = entry.name`.
- [x] 3.4.2 `onRenameNameChange("new_name")` updates `uiState.renameNewName`.
- [x] 3.4.3 `dismissRename()` sets `uiState.renameTarget = null` and
      `uiState.renameNewName = ""`.
- [x] 3.4.4 `confirmRename()` calls `sftpClient.rename()` with the correct old and new paths
      (new path = parent + "/" + renameNewName).
- [x] 3.4.5 `confirmRename()` with blank `renameNewName` does NOT call `sftpClient.rename()`.
- [x] 3.4.6 After a successful rename, `renameTarget` is cleared to `null`.

### Task 3.5 — delete dialog state machine

- [x] 3.5.1 `requestDelete(entry)` sets `uiState.deleteTarget = entry`.
- [x] 3.5.2 `dismissDelete()` sets `uiState.deleteTarget = null`.
- [x] 3.5.3 `confirmDelete()` calls `sftpClient.delete()` with the target path.
- [x] 3.5.4 After a successful delete, `deleteTarget` is cleared to `null`.
- [x] 3.5.5 `confirmDelete()` when `deleteTarget == null` does NOT call `sftpClient.delete()`.

### Task 3.6 — new directory dialog state machine

- [x] 3.6.1 `requestNewDir()` sets `uiState.showNewDirDialog = true` and
      `uiState.newDirName = ""`.
- [x] 3.6.2 `onNewDirNameChange("uploads")` updates `uiState.newDirName`.
- [x] 3.6.3 `dismissNewDir()` sets `uiState.showNewDirDialog = false` and
      `uiState.newDirName = ""`.
- [x] 3.6.4 `confirmNewDir()` calls `sftpClient.mkdir()` with
      `"${currentPath}/${newDirName}"` as the path.
- [x] 3.6.5 `confirmNewDir()` with blank `newDirName` does NOT call `sftpClient.mkdir()`.
- [x] 3.6.6 After a successful mkdir, `showNewDirDialog` is `false` and `newDirName` is `""`.

### Task 3.7 — selectEntry

- [x] 3.7.1 `selectEntry(entry)` sets `uiState.selectedPath = entry.path`.

---

## Phase 4 — TransferViewModel: validation + prefill (Medium Priority)

**Extend existing file:** `ui/transfer/TransferViewModelTest.kt`

### Task 4.1 — startTransfer: validation errors

- [x] 4.1.1 Calling `startTransfer()` with blank `host` sets `uiState.hostError` and does NOT
      start the foreground service.
- [x] 4.1.2 Calling `startTransfer()` with `port = "0"` sets `uiState.portError`.
- [x] 4.1.3 Calling `startTransfer()` with `port = "65536"` sets `uiState.portError`.
- [x] 4.1.4 Calling `startTransfer()` with non-numeric port sets `uiState.portError`.
- [x] 4.1.5 Calling `startTransfer()` with blank `username` sets `uiState.usernameError`.
- [x] 4.1.6 Calling `startTransfer()` with blank `localPath` sets `uiState.localPathError`.
- [x] 4.1.7 Calling `startTransfer()` with blank `remotePath` sets `uiState.remotePathError`.
- [x] 4.1.8 All validation errors are set simultaneously when all fields are blank
      (no short-circuit after first failure).

### Task 4.2 — onAuthTypeChange

- [x] 4.2.1 `onAuthTypeChange(AuthType.PRIVATE_KEY)` updates `uiState.authType`.
- [x] 4.2.2 `onAuthTypeChange(AuthType.PASSWORD)` updates `uiState.authType`.

### Task 4.3 — SavedStateHandle prefill (new — added by UIUX work)

- [x] 4.3.1 When `SavedStateHandle` contains `"host" = "myserver.com"`, `uiState.host` is
      pre-populated to `"myserver.com"` on ViewModel init.
- [x] 4.3.2 When `SavedStateHandle` contains `"remoteDir" = "/uploads"` and
      `"fileName" = "report.pdf"`, `uiState.remotePath` is pre-populated to
      `"/uploads/report.pdf"`.
- [x] 4.3.3 When `SavedStateHandle` contains `"direction" = "DOWNLOAD"`, `uiState.direction`
      is pre-populated to `TransferDirection.DOWNLOAD`.
- [x] 4.3.4 When `SavedStateHandle` contains `"direction" = "UPLOAD"`, `uiState.direction`
      is pre-populated to `TransferDirection.UPLOAD`.
- [x] 4.3.5 When `SavedStateHandle` is empty (normal navigation), none of the prefill fields
      are set and the form starts blank (regression guard).
- [x] 4.3.6 When `SavedStateHandle` contains only `"host"` (missing remoteDir / fileName),
      `uiState.remotePath` remains `""` (no partial prefill).

### Task 4.4 — onProfileSelected

- [x] 4.4.1 `onProfileSelected(profileId)` populates `host`, `port`, `username`, `authType`
      from the profile with the matching ID in `savedProfiles`.
- [x] 4.4.2 When `profile.savePassword = true`, `onProfileSelected` loads the stored password
      via `credentialStore.getPassword(profileId)` and sets `uiState.password`.
- [x] 4.4.3 When the profile ID does not exist in `savedProfiles`, no fields are changed.

---

## Phase 5 — SaveCurlOutputUseCase (Medium Priority)

**New file:** `domain/usecase/curl/SaveCurlOutputUseCaseTest.kt`  
Mock: `WorkspaceRepository`.

### Task 5.1 — path generation

- [x] 5.1.1 `invoke(runId, output)` returns a path matching the pattern
      `"/curl-output-$runId.txt"`.
- [x] 5.1.2 Different `runId` values produce different paths (no collision).

### Task 5.2 — content format

- [x] 5.2.1 The text written to `workspaceRepository.writeTextFile()` contains the string
      `"=== stdout ==="`.
- [x] 5.2.2 The written text contains `"=== stderr ==="`.
- [x] 5.2.3 The written text contains `output.stdoutText` verbatim.
- [x] 5.2.4 The written text contains `output.stderrText` verbatim.
- [x] 5.2.5 Stdout section appears before the stderr section in the written text.

### Task 5.3 — delegation

- [x] 5.3.1 `invoke()` calls `workspaceRepository.writeTextFile()` exactly once with the
      correct path and the formatted content.

---

## Phase 6 — CurlLogsViewModel (Medium Priority)

**New file:** `ui/curl/CurlLogsViewModelTest.kt`  
Mock: `CurlRunRepository`, `ClearCurlLogsUseCase`.

### Task 6.1 — runs emission

- [x] 6.1.1 `runs` emits an empty list when the repository emits an empty list.
- [x] 6.1.2 `runs` emits records sorted newest-first by `summary.startedAt`
      when the repository emits an unsorted list.
- [x] 6.1.3 When the repository emits a new list (simulated update), `runs` re-emits
      the updated and re-sorted list.

### Task 6.2 — clearAll

- [x] 6.2.1 `clearAll()` calls `clearCurlLogs()` (the use case).
- [x] 6.2.2 `clearAll()` is called from `viewModelScope` — calling it is safe without
      additional `advanceUntilIdle()` (verify the use case is invoked via `coVerify`).

---

## Phase 7 — CancelActiveCurlRunUseCase (Low Priority)

**New file:** `domain/usecase/curl/CancelActiveCurlRunUseCaseTest.kt`  
Mock: `CurlRunHolder`, `Context`.

### Task 7.1 — holder interaction

- [x] 7.1.1 `invoke(runId)` calls `holder.requestCancel(runId)` with the exact `runId`.
- [x] 7.1.2 `invoke(runId)` calls `context.startForegroundService()` (or the compat wrapper).

### Task 7.2 — intent construction

- [x] 7.2.1 The started service intent has `action = CurlForegroundService.ACTION_CANCEL`.
- [x] 7.2.2 The intent extra `CurlForegroundService.EXTRA_RUN_ID` equals the given `runId`.

---

## Phase 8 — DispatchPendingCurlRunUseCase (Low Priority)

**New file:** `domain/usecase/curl/DispatchPendingCurlRunUseCaseTest.kt`  
Mock: `CurlRunHolder`, `Context`.

### Task 8.1 — holder interaction

- [x] 8.1.1 `invoke(pendingRun)` calls `curlRunHolder.setPendingRun(pendingRun)` with the
      exact `pendingRun` object.

### Task 8.2 — service start

- [x] 8.2.1 `invoke(pendingRun)` calls `context.startForegroundService()` after setting
      the pending run (verify ordering with a `verifyOrder { }` MockK block).

---

## Phase 9 — Repository Integration Tests (Nice-to-Have)

Use `androidx.room:room-testing` with `Room.inMemoryDatabaseBuilder` for these.

**New file:** `data/repository/ConnectionProfileRepositoryImplTest.kt`

### Task 9.1 — ConnectionProfileRepositoryImpl

- [x] 9.1.1 `getAll()` emits an empty list on a fresh database.
- [x] 9.1.2 `save(profile)` followed by `getAll().first()` returns a list containing the profile.
- [x] 9.1.3 `save(profile)` twice with the same ID (upsert) replaces the first record.
- [x] 9.1.4 `getById(id)` returns the profile when it exists.
- [x] 9.1.5 `getById(id)` returns `null` when the profile does not exist.
- [x] 9.1.6 `delete(id)` removes the profile; subsequent `getById(id)` returns `null`.
- [x] 9.1.7 `getAll()` re-emits (Flow updates) after a `save()` or `delete()` call.

**New file:** `data/repository/KnownHostRepositoryImplTest.kt`

### Task 9.2 — KnownHostRepositoryImpl

- [x] 9.2.1 `getByHost(host, port)` returns `null` when no entry is stored.
- [x] 9.2.2 `save(host, port, fingerprint)` followed by `getByHost(host, port)` returns the
      stored fingerprint.
- [x] 9.2.3 `save()` twice with the same `host:port` key (upsert) replaces the fingerprint.
- [x] 9.2.4 `delete(host, port)` removes the entry; subsequent `getByHost` returns `null`.
- [x] 9.2.5 Different `port` values for the same `host` are stored as separate entries.

---

## Verification Checklist (after all phases)

- [x] `./gradlew --no-daemon --console=plain lintDebug` passes with zero errors and warnings.
- [x] `./gradlew --no-daemon --console=plain test` — all tests pass (existing + new).
- [x] No test imports `android.*` classes directly without mocking
      (pure JVM tests must not depend on the Android framework).
- [x] All new test classes use `@BeforeEach` / `@AfterEach` to set/reset `Dispatchers.Main`.
- [x] Commit and push to `master` after all phases and verification pass.
