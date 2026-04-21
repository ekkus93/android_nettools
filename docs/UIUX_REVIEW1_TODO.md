# UI/UX Review 1 — TODO List

Based on `docs/UIUX_REVIEW1.md`.  
Issues are grouped by phase (priority). Each phase should be linted and tested before moving on.

Legend: `[ ]` = pending · `[x]` = done

---

## Phase 1 — Critical Bug Fixes (High Impact)

### Task 1.1 — Fix in-app cancel transfer (ProgressScreen)

**File:** `app/src/main/kotlin/dev/nettools/android/ui/progress/ProgressScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/progress/ProgressViewModel.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/service/TransferForegroundService.kt`

- [x] 1.1.1 Add a `cancelJob(jobId: String)` function to `ProgressViewModel` that sends a cancel
      intent to `TransferForegroundService` (use a `CANCEL_JOB` action with the job ID as an extra).
- [x] 1.1.2 In `TransferForegroundService`, handle the `CANCEL_JOB` intent action: look up the
      coroutine job by ID in the active-job map and call `.cancel()` on it; update the transfer
      status to `CANCELLED` in the Room database.
- [x] 1.1.3 In `ProgressScreen`, replace the no-op `showCancelDialog = false` in the confirm button
      with a call to `viewModel.cancelJob(job.id)`, then dismiss the dialog.
- [x] 1.1.4 After cancellation, navigate the user back to `HomeScreen` (or pop back stack) if the
      cancelled job was the primary (`job.id == jobId`) and there are no remaining active jobs.
- [x] 1.1.5 Update the cancel dialog body text to remove the misleading "Partial files will be left
      on the remote" note (or make it accurate after verifying actual cleanup behavior).
- [x] 1.1.6 Add a unit test in `ProgressViewModelTest` verifying that `cancelJob()` emits the
      correct intent and updates job status to `CANCELLED`.

---

### Task 1.2 — Replace context menu icon in SFTP browser

**File:** `app/src/main/kotlin/dev/nettools/android/ui/sftp/SftpBrowserScreen.kt`

- [x] 1.2.1 Replace the `Icons.Filled.DriveFileRenameOutline` import with `Icons.Filled.MoreVert`
      in `FileEntryRow`.
- [x] 1.2.2 Update the `IconButton` that triggers the dropdown menu to use `Icons.Filled.MoreVert`
      with `contentDescription = "More options for ${entry.name}"`.
- [x] 1.2.3 Verify the rename menu item still shows `DriveFileRenameOutline` as its *leading icon*
      (only the trigger button icon changes, not the menu item icon).
- [x] 1.2.4 Remove the empty dead-code `if` block in `FileEntryRow` that contains only the comment
      `// Show context menu button` (related cleanup, same file).

---

### Task 1.3 — Add file picker for private key path

**File:** `app/src/main/kotlin/dev/nettools/android/ui/connections/SavedConnectionsScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/connections/SavedConnectionsViewModel.kt`

- [x] 1.3.1 Add an `ActivityResultContracts.OpenDocument` launcher in `SavedConnectionsScreen`
      (or its parent `Activity`) that filters for all file types (`"*/*"`).
- [x] 1.3.2 Add a `pickKeyFile()` event / callback in `SavedConnectionsViewModel` to expose the
      launcher trigger to the composable without passing `Context` into the ViewModel.
- [x] 1.3.3 In `ProfileEditDialog` (or its replacement), add a row next to the "Key file path"
      `OutlinedTextField` consisting of a small `IconButton` with `Icons.Filled.FolderOpen`
      (`contentDescription = "Browse for key file"`).
- [x] 1.3.4 On picker result, call `viewModel.onKeyPathChange(uri.path ?: "")` to populate the
      field. Store the URI string, not just the last path segment, so the service can open it.
- [x] 1.3.5 Persist the key file as a `content://` URI string in the profile; update
      `SshConnectionManager` to open private keys via `ContentResolver.openInputStream()` when
      the path starts with `content://`.
- [x] 1.3.6 Take a persistable URI permission (`FLAG_GRANT_READ_URI_PERMISSION`) so the URI
      remains accessible after the picker is closed.
- [x] 1.3.7 Add a unit test verifying that a `content://` key path is opened via `ContentResolver`
      and not treated as a filesystem path.

---

### Task 1.4 — Replace profile edit AlertDialog with ModalBottomSheet

**File:** `app/src/main/kotlin/dev/nettools/android/ui/connections/SavedConnectionsScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/connections/SavedConnectionsViewModel.kt`

- [x] 1.4.1 Import `androidx.compose.material3.ModalBottomSheet` and
      `androidx.compose.material3.rememberModalBottomSheetState`.
- [x] 1.4.2 Replace the `AlertDialog { ProfileEditDialog(...) }` call with a `ModalBottomSheet`
      that is shown when `editState != null`.
- [x] 1.4.3 Inside the sheet, use a `Column` with `verticalScroll(rememberScrollState())` so all
      fields are accessible on small screens without the sheet content being clipped.
- [x] 1.4.4 Add a drag handle at the top of the sheet (use `SheetDefaults.DragHandle()`).
- [x] 1.4.5 Place "Save" and "Cancel" as a `Row` of `Button`/`OutlinedButton` at the bottom of the
      sheet (not as dialog action buttons), with Save using the primary filled style.
- [x] 1.4.6 Ensure the sheet is dismissed on back-gesture and on successful save.
- [x] 1.4.7 Delete the old `ProfileEditDialog` composable function and all related `AlertDialog`
      scaffolding.
- [x] 1.4.8 Verify that keyboard behavior is correct: the sheet should resize when the soft keyboard
      opens (`WindowCompat.setDecorFitsSystemWindows(window, false)` may be needed at Activity level).

---

## Phase 2 — Medium Impact UX Improvements

### Task 2.1 — Add active-transfer indicator to HomeScreen

**File:** `app/src/main/kotlin/dev/nettools/android/ui/HomeScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/HomeViewModel.kt` *(create if not exists)*

- [x] 2.1.1 Create (or extend) `HomeViewModel` with a `activeTransferCount: StateFlow<Int>` that
      observes `TransferRepository.getActiveJobs()` and emits the count.
- [x] 2.1.2 In `HomeScreen`, collect `activeTransferCount` from the ViewModel.
- [x] 2.1.3 When `activeTransferCount > 0`, render a `Card` or `Surface` banner at the top of the
      screen (above the nav cards) with text "N transfer(s) in progress" and a chevron icon, tinted
      with `primaryContainer` background.
- [x] 2.1.4 Make the banner clickable — it should navigate to `Routes.PROGRESS` with a
      placeholder `jobId` (use the first active job's ID, or a sentinel value).
- [x] 2.1.5 When `activeTransferCount == 0`, the banner should not be rendered (no empty space).
- [x] 2.1.6 Add a unit test for `HomeViewModel` verifying the count emits correctly when the
      repository returns 0, 1, and N active jobs.

---

### Task 2.2 — Add section grouping to TransferScreen form

**File:** `app/src/main/kotlin/dev/nettools/android/ui/transfer/TransferScreen.kt`

- [x] 2.2.1 Wrap the connection-related fields (Host, Port, Username, Auth method, Password/Key)
      in a `Card` with a section label "Connection" rendered as a `Text` with
      `MaterialTheme.typography.labelLarge` at the top of the card.
- [x] 2.2.2 Wrap the transfer-target fields (Direction toggle, Local path, Remote path, Protocol)
      in a second `Card` with a section label "Transfer".
- [x] 2.2.3 Wrap the optional/save fields (Save profile checkbox, Profile name) in a third `Card`
      with label "Options".
- [x] 2.2.4 Add 8dp vertical spacing between each card section.
- [x] 2.2.5 Add placeholder helper text under the "Remote path" field (e.g.,
      `"e.g. /home/user/uploads"`) to guide first-time users.
- [x] 2.2.6 Ensure the full form remains scrollable inside a `Column` with
      `verticalScroll(rememberScrollState())`.

---

### Task 2.3 — Add status filter chips to HistoryScreen

**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryViewModel.kt`

- [x] 2.3.1 Add a `statusFilter: StateFlow<HistoryStatus?>` to `HistoryViewModel` (null = "All").
- [x] 2.3.2 Add `onStatusFilterChange(status: HistoryStatus?)` in `HistoryViewModel` and apply the
      filter in the existing `history` derived flow (combine with the text search query).
- [x] 2.3.3 In `HistoryScreen`, add a `LazyRow` of `FilterChip` components below the search
      `OutlinedTextField`: chips labeled **All**, **Success**, **Failed**, **Resumed**, **Cancelled**.
- [x] 2.3.4 The active chip should use `selected = true` styling (M3 `FilterChip` handles this
      automatically when `selected` is set).
- [x] 2.3.5 Add 8dp horizontal padding and 4dp vertical padding to the chip row.
- [x] 2.3.6 Add a unit test verifying that combining text search + status filter returns the correct
      subset of history entries.

---

### Task 2.4 — Add "Transfer again" from history detail

**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryDetailDialog.kt` *(extract or inline)*  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/transfer/TransferScreen.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/transfer/TransferViewModel.kt`

- [x] 2.4.1 Add a `NavController` parameter to `HistoryDetailDialog` (or pass a callback).
- [x] 2.4.2 Add a "Transfer again" `TextButton` as a second confirm button in the dialog (alongside
      "Close"), using the primary color.
- [x] 2.4.3 On click, navigate to `Routes.TRANSFER` and pass the history entry's `host`, `remoteDir`,
      `fileName`, and `direction` as navigation arguments (or via `SavedStateHandle`).
- [x] 2.4.4 In `TransferViewModel`, check `SavedStateHandle` on init for pre-fill arguments and
      populate `_uiState` accordingly.
- [x] 2.4.5 In `TransferScreen`, ensure the pre-filled form fields are visible immediately when the
      screen is opened from history (no extra user interaction needed to see them).
- [x] 2.4.6 Update `Routes.kt` to include optional query parameters for pre-fill (`?host=...` etc.)
      or define a separate `Routes.TRANSFER_PREFILL` route if query params are unwieldy.

---

### Task 2.5 — Always show breadcrumbs in SFTP browser

**File:** `app/src/main/kotlin/dev/nettools/android/ui/sftp/SftpBrowserScreen.kt`

- [x] 2.5.1 Remove the `if (state.breadcrumbs.size > 1)` guard around the `BreadcrumbRow` call.
- [x] 2.5.2 Ensure `BreadcrumbRow` renders correctly when `breadcrumbs` has exactly one element
      (no trailing separator should be shown for the single crumb — verify existing logic).
- [x] 2.5.3 Keep the `HorizontalDivider` below the breadcrumb row visible at all times.
- [x] 2.5.4 Update `SftpBrowserViewModelTest` to assert that the root breadcrumb is always present
      in `uiState.breadcrumbs` immediately after connecting.

---

### Task 2.6 — Add Material You / Dynamic Color support

**File:** `app/src/main/kotlin/dev/nettools/android/ui/theme/Theme.kt`

- [x] 2.6.1 Add `import android.os.Build` and
      `import androidx.compose.material3.dynamicDarkColorScheme` /
      `import androidx.compose.material3.dynamicLightColorScheme` to `Theme.kt`.
- [x] 2.6.2 Add a `dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`
      parameter to `NetToolsTheme`.
- [x] 2.6.3 In `NetToolsTheme`, derive `colorScheme` using version-guarded when expression.
- [x] 2.6.4 Pass `LocalContext.current` inside the composable (already available via Compose).
- [x] 2.6.5 Verify the static teal palette is still used as a fallback on API < 31.
- [x] 2.6.6 Update the `NetToolsTheme` KDoc to document the `dynamicColor` parameter.

---

## Phase 3 — Low Impact / Code Cleanup

### Task 3.1 — Fix DetailRow fixed 100dp label width

**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryScreen.kt`

- [x] 3.1.1 Replace the `Modifier.width(100.dp)` on the label `Text` in `DetailRow` with no fixed
      width (natural wrap content).
- [x] 3.1.2 Add `Modifier.weight(1f)` to the value `Text` so it still fills remaining width.
- [x] 3.1.3 Verify the dialog looks correct with both short labels ("Size") and long ones
      ("Remote path") in both light and dark themes.

---

### Task 3.2 — Fix SimpleDateFormat per-recomposition allocation in HistoryEntryRow

**File:** `app/src/main/kotlin/dev/nettools/android/ui/history/HistoryScreen.kt`

- [x] 3.2.1 Both `HistoryEntryRow` and `HistoryDetailDialog` already wrap `SimpleDateFormat`
      in `remember { }` — no changes needed.

---

### Task 3.3 — Remove dead Surface80/Surface40 color constants

**File:** `app/src/main/kotlin/dev/nettools/android/ui/theme/Color.kt`  
**File:** `app/src/main/kotlin/dev/nettools/android/ui/theme/Theme.kt`

- [x] 3.3.1 Assigned `Surface80` to `LightColors.surface` and `Surface40` to `DarkColors.surface`.
- [x] 3.3.2 Lint passes with no unused-constant warnings.

---

## Phase 4 — Visual Polish

### Task 4.1 — Elevate primary action on HomeScreen

**File:** `app/src/main/kotlin/dev/nettools/android/ui/HomeScreen.kt`

- [x] 4.1.1 "SCP Transfer" NavCard uses `primaryContainer` background.
- [x] 4.1.2 Icon size for "SCP Transfer" card is 48dp.
- [x] 4.1.3 Title uses `MaterialTheme.typography.titleLarge`.
- [x] 4.1.4 Other cards remain `surfaceVariant` at 36dp / `titleMedium`.
- [x] 4.1.5 Verified elevated card with NavCard color/style params.

---

### Task 4.2 — Add hero branding to HomeScreen

**File:** `app/src/main/kotlin/dev/nettools/android/ui/HomeScreen.kt`  
**File:** `app/src/main/res/drawable/ic_nettools_hero.xml`

- [x] 4.2.1 Created `ic_nettools_hero.xml` vector drawable (transfer arrows motif, 96×96dp).
- [x] 4.2.2 Added `Image(painterResource(R.drawable.ic_nettools_hero))` centered at 80dp.
- [x] 4.2.3 `contentDescription = null` (decorative).
- [x] 4.2.4 Added tagline "SSH file transfers, simplified" below the hero image.
- [x] 4.2.5 Fits comfortably in a standard Column layout.

---

## Verification Checklist (after all phases)

- [x] `./gradlew lintDebug` passes with zero errors.
- [x] `./gradlew test` — all unit tests pass.
- [ ] Manually verify cancel flow: start a transfer, open ProgressScreen, cancel it, confirm status
      becomes Cancelled and the service stops the transfer.
- [ ] Manually verify key file picker: create a new profile with Private Key auth, tap the folder
      icon, pick a file, confirm the path populates and the field is not editable by typing.
- [ ] Manually verify Material You: run on an API 31+ emulator with a colored wallpaper; confirm
      the color scheme adapts.
- [ ] Manually verify breadcrumbs at root: open SFTP browser, confirm breadcrumb row is visible
      immediately at the home directory.
- [x] Committed and pushed to `master` after all phases.
