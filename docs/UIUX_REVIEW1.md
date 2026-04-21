# UI/UX Review 1

**Reviewed by:** Claude Sonnet 4.6  
**Date:** 2026-04-21  
**Scope:** All Jetpack Compose screens — `HomeScreen`, `TransferScreen`, `SftpBrowserScreen`, `ProgressScreen`, `HistoryScreen`, `SavedConnectionsScreen`, and the theme layer (`Color.kt`, `Theme.kt`).

---

## ✅ What's Good

1. **Material Design 3 consistently applied** — `ListItem`, `Card`, `TopAppBar`, proper color tokens everywhere; nothing hardcoded.
2. **Dark/light theme** — well-structured with semantic color names; teal primary is a strong technical identity.
3. **Breadcrumb navigation** in SFTP browser handles deep paths elegantly with a `LazyRow`.
4. **Pull-to-refresh** in SFTP browser is good muscle memory for mobile users.
5. **Inline validation errors** on form fields across Transfer and SavedConnections screens — no Toast-only validation.
6. **ProgressScreen queue transparency** — all active jobs visible; primary job highlighted with `primaryContainer` color; resume offset shown with tertiary color label.
7. **Status badges** in History (color-coded: green/red/grey/teal) — scannable at a glance.
8. **Searchable history** with clear state for empty results.
9. **TOFU host-key dialog** — security-first UX, fingerprint visible before accepting.
10. **Consistent empty states** with context-sensitive hints ("Tap + to add one", "No transfers yet").

---

## ❌ What's Bad / Bugs

### High Impact

1. **Wrong context menu icon in SFTP browser.**
   `DriveFileRenameOutline` (a pencil) is used as the 3-dot overflow trigger for Rename + Delete.
   Users will read it as "edit/rename only" and never discover Delete.
   **Fix:** Replace with `Icons.Filled.MoreVert`.

2. **Cancel transfer does nothing in-app.**
   The "Cancel Transfer" button in the `ProgressScreen` dialog just calls `showCancelDialog = false`.
   The code comment says cancellation is handled via notification — but users inside the app have no
   in-app way to actually cancel. The dialog is a dead end and actively misleads users.
   **Fix:** Wire the confirm button to `ProgressViewModel` → `TransferForegroundService` cancellation.

3. **No file picker for private key path** in `SavedConnectionsScreen`.
   The field is a plain `OutlinedTextField` — users must manually type `/data/user/0/...` paths.
   On Android this is practically impossible without rooting or `adb`.
   **Fix:** Add an `ActivityResultContracts.OpenDocument` launcher beside the key path field.

4. **Profile edit dialog is too cramped.**
   Six stacked form fields inside an `AlertDialog` (`Name`, `Host`, `Port`, `Username`, `Auth`, `Password/Key`)
   is a bad mobile pattern. Dialogs are for confirmations, not complex forms.
   **Fix:** Replace with a `ModalBottomSheet` or a dedicated full-screen editor.

### Medium Impact

5. **HomeScreen has no active-transfer indicator.**
   If a transfer is running, the home screen shows nothing about it. Users have no way to know
   a background job is in progress without pulling down the notification shade.
   **Fix:** Show a "N active transfers" banner/row that navigates to `ProgressScreen`.

6. **TransferScreen form is very long with no grouping.**
   Host/auth fields and transfer-target fields are one unseparated wall of inputs.
   **Fix:** Add `Card` or section-header grouping ("Connection" / "Transfer Details") to reduce
   cognitive load, especially for first-time users.

7. **HistoryScreen: no status filter.**
   Text search is good, but users commonly want "show me all failed transfers."
   There is no chip/filter row for status filtering.
   **Fix:** Add a horizontal `FilterChip` row below the search bar (All / Success / Failed / Resumed).

8. **No "Re-run" action from history detail.**
   A common workflow is: see a failed transfer → retry it. The detail dialog has only "Close."
   **Fix:** Add a "Transfer again" button that navigates to `TransferScreen` with the fields pre-filled.

9. **Breadcrumbs hidden at depth 1.**
   The breadcrumb row only renders when `state.breadcrumbs.size > 1`, so at the home directory
   the breadcrumbs disappear entirely. Users lose orientation.
   **Fix:** Always render the breadcrumb row; just show the single root crumb when at root.

10. **No Material You / Dynamic Color.**
    Android 12+ users expect dynamic theming. Not supporting it makes the app feel less native.
    **Fix:** Apply `dynamicLightColorScheme`/`dynamicDarkColorScheme` with a `Build.VERSION.SDK_INT >= 31` guard, falling back to the current teal palette.

### Low Impact

11. **Dead code in `FileEntryRow`.**
    There is an empty `if` block with the comment `// Show context menu button` that does nothing
    and will confuse future readers.
    **Fix:** Remove it.

12. **`DetailRow` label is a fixed 100dp.**
    Will truncate for longer labels in other locales or if the label set grows.
    **Fix:** Use `IntrinsicSize.Min` or `wrapContentWidth()` for the label column.

13. **`SimpleDateFormat` created per recomposition in `HistoryEntryRow`.**
    `HistoryDetailDialog` correctly wraps `SimpleDateFormat` in `remember { }`, but `HistoryEntryRow`
    does not — it allocates a new instance on every recomposition.
    **Fix:** Wrap in `remember { SimpleDateFormat(...) }`.

14. **`Surface80`/`Surface40` are dead color constants.**
    Defined in `Color.kt` but never assigned to the `surface` slot in `Theme.kt`.
    The M3 `surface` defaults are used instead.
    **Fix:** Either assign them or remove them to avoid confusion.

---

## 🔁 Workflow Critique

| Flow | Issue |
|---|---|
| First-time upload | Form is daunting with no section grouping or guidance on "remote path" |
| Retry failed transfer | Dead end — no retry action from `HistoryScreen` |
| Cancel running job | Only works from the system notification; in-app button is a no-op |
| Add private-key auth | Requires manual path typing — essentially broken on most devices |
| Check if a transfer is running | Must open notification shade; no indicator on `HomeScreen` |
| Browse remote before setting path | Transfer → SFTP Browser → back — works but feels disconnected |

---

## 🎨 Visual Appeal

- The teal/indigo palette is cohesive and reads as technical/professional.
- Typography scale (`titleMedium`, `bodySmall`, `labelSmall`) is used correctly and consistently.
- `ListItem`, `Card`, `HorizontalDivider` — consistent Material3 building blocks; feels like a real production app.
- **HomeScreen visual hierarchy is flat.** All three nav cards have identical weight/styling.
  The primary action ("SCP Transfer") should be visually dominant — larger, `primaryContainer`
  background, or a `FilledButton` entry point.
- No branded illustration or app icon on `HomeScreen` — the launch experience looks like a skeleton.
  A simple vector illustration or the app icon in a hero area would immediately lift the feel.

---

## 🏆 Top 5 Recommendations (Priority Order)

| # | Issue | Effort | Impact |
|---|---|---|---|
| 1 | Fix the cancel button — wire it to actually cancel the job | Low | High |
| 2 | Replace context menu icon with `MoreVert` in SFTP browser | Trivial | High |
| 3 | Add file picker for private key path | Medium | High |
| 4 | Move profile edit out of `AlertDialog` into `ModalBottomSheet` or full screen | Medium | High |
| 5 | Add active-transfer status indicator on `HomeScreen` | Low | Medium |
