# Android NetTools — Curl Feature Specifications

## Overview

**Android NetTools** will add a libcurl-backed curl runner so users can execute real curl commands from Android with a mobile-friendly UI, persistent optional logs, and an app-managed workspace for local file operations.

---

## Feature: Curl Runner

### Summary

Allow the user to paste and run raw curl commands using an embedded libcurl-based native runtime. The feature should stay close to real curl behavior while adapting local files, output, lifecycle, and storage to Android.

---

### Functional Requirements

#### 1. Command Input

- The user can paste a full raw curl command line.
- If the pasted text starts with `curl`, run it as provided.
- If the pasted text does not start with `curl`, treat it as curl arguments and prepend `curl`.
- Support multiline pasted commands with shell-style line continuations.
- Preserve quoted arguments and whitespace semantics consistent with the app's parser and execution engine.
- Pre-run validation must catch at least:
  - Unclosed quotes
  - Unknown or misspelled option names, based on the embedded curl build's supported options

#### 2. Execution Scope

- The feature must execute any command supported by the embedded libcurl/curl build.
- The embedded build should expose as broad a curl protocol surface as is practical for Android packaging.
- The app will run one curl job at a time.
- A running curl job must continue in the background if the user navigates away.
- The user must be able to cancel an in-progress curl job.

#### 3. Output and Results

- Show **stdout** and **stderr** separately.
- Normalize output presentation for mobile readability without changing the meaning of the output.
- The UI must be able to show, on demand:
  - Exit code
  - Timing metadata
  - Truncation indicators when output exceeds retention caps
- The user can:
  - Copy stdout
  - Copy stderr
  - Save output

#### 4. Logs and Saved Command History

- Detailed curl logs are **off by default**.
- When enabled, logs persist across runs until cleared by the user.
- The user can manually clear stored logs.
- Saved command history is **off by default**.
- If enabled later in the feature, saved commands are retained locally.
- Saved commands are not editable.
- Saved commands are not exportable.

#### 5. Workspace and Local Files

- Local file handling is rooted in a single global app-private workspace directory for v1.
- The workspace root is not user-selectable in v1; users move files in and out through Android pickers.
- Unix-style local paths in curl commands are interpreted relative to this workspace model.
- The user must be able to import files into the workspace via Android file pickers.
- The user must be able to export files from the workspace via Android file pickers.
- Common curl file operations must work through the workspace model, including uploads and downloads.
- Desktop-style local paths such as `/tmp/foo.json` are treated as workspace-rooted paths rather than direct Android filesystem paths.

#### 6. Workspace Browser

- The workspace file manager lives in a separate screen from the curl runner.
- The user must be able to:
  - Browse workspace files and directories
  - Move files and directories
  - Rename files and directories
  - Delete files
  - Create directories
  - Delete directories

#### 7. Cleanup Semantics

- Failed or partial downloads are deleted locally.
- Cancellation must attempt best-effort cleanup of local and remote partial files.
- Failed or cancelled uploads should not leave remote partial files when cleanup succeeds.
- If remote cleanup fails, surface a warning to the user that a remote partial file may remain.

#### 8. Background Execution

- Curl runs must use background-capable execution consistent with the app's existing transfer behavior.
- The app must surface progress/state while a run is active.
- Reopening the app during an active run must reconnect the UI to the current job state.

---

### Non-Functional Requirements

| Requirement | Detail |
|-------------|--------|
| **Compatibility** | Android 8.0 (API 26) and above. |
| **Execution model** | Use embedded native curl/libcurl rather than reimplementing curl behavior in Kotlin. |
| **Concurrency** | One active curl job at a time. |
| **Storage model** | Use a single app-private app-managed workspace root for local file semantics. |
| **Performance** | Stream output and file data; avoid unnecessary full-file in-memory buffering. |
| **Safety** | Logging is opt-in and disabled by default. |
| **Error handling** | Surface validation, execution, cancellation, cleanup, and packaging/runtime errors clearly. |

---

### Native Packaging Requirements

#### Release ABIs

- `arm64-v8a`
- `armeabi-v7a`

#### Debug / Development ABIs

- `arm64-v8a`
- `armeabi-v7a`
- `x86_64`

#### Native Defaults

- Use `libcurl` as the execution engine.
- Use **OpenSSL** as the TLS backend.
- Enable broad protocol support where practical.
- Enable HTTP/2 if the build remains stable.
- Defer HTTP/3 for v1.

---

### User Interface

#### Screens

1. **Home / Utility Launcher**
   - Add a Curl entry point.
2. **Curl Runner Screen**
   - Multiline command input
   - Run button
   - Validation feedback
   - Access to workspace tools
3. **Curl Results Screen**
   - Stdout panel
   - Stderr panel
   - Run status
   - Optional exit-code/timing metadata
   - Actions: Cancel, Copy stdout, Copy stderr, Save output
4. **Workspace Browser**
   - Browse and manage workspace files/directories
   - Import/export via Android pickers
5. **Curl Logs / History Screen**
   - Simple first-pass organization is acceptable
   - Persisted run records and stored output when logging is enabled

#### UX Notes

- Material Design 3 components.
- Support both light and dark themes.
- Validation should block clearly malformed input before execution.
- Start with a functional log-screen organization and refine later.

---

### Persistence and Retention

- Persist run metadata across runs when logging is enabled.
- Persist stored output across runs when logging is enabled.
- Keep as much output as practical while enforcing a cap.
- Default output-retention caps:
  - `stdout`: 4 MB per run
  - `stderr`: 1 MB per run
- If retained output exceeds the cap, mark the stored output as truncated.
- Keep run metadata even when output is truncated.

---

### Out of Scope (v1)

- Form-based request builder
- Editable/exportable saved commands
- HTTP/3 support

---

### Open Implementation Notes

1. Unknown-option validation should be derived from the embedded curl build rather than a hardcoded static flag list.
2. Workspace-path translation must be consistent across command parsing, execution, import/export, and UI display.
3. Output normalization should improve readability on mobile without hiding meaningful stderr/stdout distinctions.
