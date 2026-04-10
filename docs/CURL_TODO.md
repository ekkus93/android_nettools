# Android NetTools — Curl TODO

Detailed task and subtask list for implementing the libcurl-backed curl feature described in `docs/CURL_SPECS.md`.

---

## 1. Product and Documentation Setup

- [ ] Create and maintain curl-specific docs
  - [x] Add `docs/CURL_SPECS.md`
  - [x] Add `docs/CURL_TODO.md`
  - [ ] Update any top-level docs that list available tools once curl is implemented
- [x] Confirm v1 defaults in code and docs
  - [x] One active curl run at a time
  - [x] Logging off by default
  - [x] Saved command history off by default
  - [x] Single global workspace root
  - [x] Output caps: 4 MB stdout, 1 MB stderr

---

## 2. Native Build Infrastructure

- [x] Add Android native build support
  - [x] Enable NDK in the Android app module
  - [x] Add CMake-based native build configuration
  - [x] Define build inputs/outputs for native curl integration
- [x] Integrate libcurl
  - [x] Bring in libcurl sources or a controlled vendor/build strategy
  - [x] Build libcurl for `arm64-v8a` and `armeabi-v7a`
  - [x] Add `x86_64` for debug/development builds
  - [x] Configure OpenSSL as the TLS backend
  - [x] Enable HTTP/2 if stable in the chosen build pipeline
  - [x] Explicitly defer HTTP/3 for v1
- [ ] Control native packaging
  - [x] Ensure debug and release ABI filters match the approved matrix
  - [x] Keep APK/AAB packaging predictable
  - [ ] Verify R8/packaging does not strip required native assets
- [x] Expose native build metadata
  - [x] Surface embedded curl version
  - [x] Surface supported features/protocols from the compiled build

---

## 3. Native Execution Bridge

- [x] Create a bridge between Kotlin and embedded native curl execution
  - [x] Define a request model for invoking curl runs
  - [x] Define a response/event model for stdout, stderr, progress, exit, and failures
  - [x] Stream stdout incrementally back to Kotlin
  - [x] Stream stderr incrementally back to Kotlin
  - [x] Surface final exit code
  - [x] Surface timing/summary metadata when requested
- [x] Add cancellation support
  - [x] Define a cancellable native execution handle
  - [x] Map UI/service cancellation to native cancellation
  - [x] Ensure cancellation is safe during uploads/downloads
- [ ] Handle lifecycle and error boundaries
  - [ ] Protect against native crashes propagating as silent app failures
  - [ ] Convert native failures into explicit domain errors
  - [ ] Ensure the bridge cleans up native resources on completion, failure, and cancellation

---

## 4. Curl Command Parsing and Validation

- [x] Implement command preprocessing
  - [x] Accept full raw commands starting with `curl`
  - [x] Accept bare arguments and prepend `curl`
  - [x] Collapse multiline shell-style continuations
  - [x] Preserve quoted tokens correctly
- [x] Implement validation
  - [x] Detect unclosed quotes before execution
  - [x] Detect empty commands
  - [x] Detect unsupported or misspelled options
  - [x] Base unknown-option validation on the actual embedded curl build
  - [x] Produce inline user-facing validation messages
- [x] Define parser behavior for path-bearing arguments
  - [x] Identify file input flags
  - [x] Identify file output flags
  - [x] Identify upload/download patterns that need workspace translation
  - [x] Keep raw command fidelity while still enabling Android-aware file handling

---

## 5. Workspace Model

- [x] Design the workspace domain model
  - [x] Define a single global workspace root
  - [x] Persist the selected workspace root
  - [x] Define workspace-relative path rules for Unix-style paths
- [x] Implement workspace storage management
  - [x] Create app-side storage layout for workspace content
  - [x] Add import into workspace via Android picker
  - [x] Add export from workspace via Android picker
  - [x] Handle conflicts during import/export
  - [x] Handle missing or revoked picker permissions explicitly
- [x] Implement workspace path translation
  - [x] Map pasted Unix-style paths into workspace-managed paths
  - [x] Normalize paths consistently
  - [x] Prevent path traversal outside the workspace model
  - [x] Keep displayed paths understandable to the user

---

## 6. Workspace Browser

- [x] Add a dedicated workspace browser screen
  - [x] Add navigation route and home entry points as needed
  - [x] Show files and directories in a mobile-friendly list
  - [x] Support loading, empty, and error states
- [x] Implement workspace file operations
  - [x] Create directory
  - [x] Rename file
  - [x] Rename directory
  - [x] Move file
  - [x] Move directory
  - [x] Delete file
  - [x] Delete directory
- [x] Implement import/export actions
  - [x] Import selected file(s) into workspace
  - [x] Export selected file(s) out of workspace
  - [x] Surface clear errors for partial or failed copy operations

---

## 7. Curl File Semantics

- [x] Define the supported file-argument translation layer
  - [x] Handle local upload-style references
  - [x] Handle local download-style references
  - [x] Handle explicit output-file arguments
  - [x] Handle config-file arguments if present
  - [x] Handle common `@file` payload patterns
- [x] Implement translation from pasted command to executable local paths
  - [x] Rewrite workspace-rooted paths for native execution
  - [x] Preserve the visible user command or clearly show the effective path mapping
  - [x] Ensure translation is reversible enough for good error messages
- [x] Validate file existence/intent when possible
  - [x] Catch obviously missing local input files before run
  - [x] Catch obviously invalid destination patterns before run

---

## 8. Domain Layer for Curl Runs

- [x] Add curl domain models
  - [x] Curl command/request model
  - [x] Curl run status model
  - [x] Curl output chunk/result model
  - [x] Curl run summary model
  - [x] Curl validation error model
- [x] Add curl repository/service interfaces
  - [x] Execution repository
  - [x] Workspace repository
  - [x] Log/history repository
- [x] Add use cases
  - [x] Validate curl command
  - [x] Start curl run
  - [x] Observe active curl run
  - [x] Cancel active curl run
  - [x] Save output/logs
  - [x] Clear logs
  - [x] Import/export workspace files

---

## 9. Persistence Layer

- [x] Add Room storage for curl data
  - [x] Curl run metadata entity
  - [x] Curl stdout storage entity or file-backed index
  - [x] Curl stderr storage entity or file-backed index
  - [x] Settings entity/keys for logging-enabled and history-enabled flags
  - [x] Workspace configuration entity/keys
- [x] Implement retention rules
  - [x] Enforce 4 MB stdout cap per run
  - [x] Enforce 1 MB stderr cap per run
  - [x] Mark truncated output explicitly
  - [x] Preserve metadata when output is truncated
- [x] Implement log/history behavior
  - [x] Logging disabled by default
  - [x] Persist logs across runs when enabled
  - [x] Support manual clearing of logs
  - [x] Keep saved command history off by default
  - [x] Do not allow editing/exporting saved commands

---

## 10. Background Execution Service

- [x] Add a foreground service for curl runs or extend the existing background execution model safely
  - [x] Guarantee one active curl job at a time
  - [x] Start foreground execution promptly
  - [x] Reconnect UI to active runs after navigation/process recreation where applicable
- [x] Implement notifications
  - [x] Active run notification
  - [x] Completion notification
  - [x] Failure notification
  - [x] Cancellation notification where appropriate
- [x] Wire cancellation through the full stack
  - [x] UI cancel button
  - [x] Notification cancel action
  - [x] Service-to-native cancellation handoff

---

## 11. Cleanup and Partial-File Handling

- [ ] Define local cleanup behavior in code
  - [x] Delete failed/partial downloads
  - [x] Delete cancelled partial downloads
  - [ ] Ensure local temp/work files are not leaked
- [ ] Define remote cleanup behavior in code
  - [ ] Attempt best-effort cleanup of remote partial uploads
  - [ ] Surface a warning if remote cleanup fails
  - [ ] Ensure cancellation and failure paths both try cleanup
- [ ] Add cleanup observability
  - [ ] Record whether cleanup succeeded, failed, or was skipped
  - [x] Surface cleanup warnings in a user-friendly form

---

## 12. UI — Home and Navigation

- [x] Add curl entry points to navigation
  - [x] Add a Curl route constant
  - [x] Add a Workspace Browser route constant
  - [x] Add a Curl Logs/History route constant if separate
- [x] Update home screen
  - [x] Add a Curl card/tile
  - [x] Add a sensible icon and subtitle

---

## 13. UI — Curl Runner Screen

- [x] Create the main curl execution screen
  - [x] Multiline command input
  - [x] Run button
  - [x] Validation feedback area
  - [x] Access point to workspace browser
  - [x] Access point to logs/history/settings
- [x] Add UX behaviors
  - [x] Disable Run while a job is active
  - [x] Show current run status
  - [x] Preserve draft command across configuration changes
  - [x] Handle pasted multiline commands cleanly

---

## 14. UI — Curl Results Screen

- [x] Build a polished results screen
  - [x] Separate stdout and stderr sections
  - [x] Show active/completed/failed/cancelled state clearly
  - [x] Show optional exit code and timing metadata on demand
  - [x] Indicate truncation when output caps are hit
- [x] Add actions
  - [x] Cancel
  - [x] Copy stdout
  - [x] Copy stderr
  - [x] Save output
- [x] Improve readability
  - [x] Normalize output presentation for mobile
  - [x] Keep stdout/stderr meaningfully distinct
  - [x] Avoid losing significant line structure

---

## 15. UI — Logs / History Screen

- [x] Add a simple first-pass logs/history screen
  - [x] List prior runs with status and timestamps
  - [x] Show whether logs/output were retained
  - [x] Show truncated/non-truncated state
  - [x] Allow manual clearing
- [x] Keep organization intentionally simple for v1
  - [x] Do not overdesign filtering/sorting unless needed to make it usable
  - [x] Leave room for later refinement

---

## 16. UI — Settings and Preferences

- [ ] Add curl-related settings
  - [x] Enable/disable logging
  - [x] Enable/disable saved command history
  - [ ] Select/change workspace root
  - [x] Clear logs
- [x] Surface consequences of settings
  - [x] Warn that enabling logs can persist sensitive command content
  - [x] Explain workspace-root behavior clearly

---

## 17. Error Handling

- [ ] Map major failure classes to clear user messages
  - [x] Validation failure
  - [x] Unsupported option/build mismatch
  - [x] Missing workspace file
  - [x] Import/export failure
  - [x] Native runtime failure
  - [x] TLS/certificate failure surfaced by curl
  - [ ] Cancellation
  - [ ] Partial remote cleanup failure
- [ ] Ensure no silent failures
  - [x] Service cleanup paths
  - [ ] Native bridge teardown
  - [x] Workspace import/export operations

---

## 18. Dependency Injection and App Wiring

- [x] Add Hilt modules for curl components
  - [x] Native executor binding
  - [x] Workspace manager binding
  - [x] Curl repositories/use cases
  - [x] Settings/log persistence bindings
- [ ] Ensure view models remain properly separated from Android framework details where possible

---

## 19. Testing

- [ ] Unit tests
  - [x] Command preprocessing for `curl ...` vs bare args
  - [x] Multiline continuation parsing
  - [x] Unclosed-quote validation
  - [x] Unknown-option validation
  - [x] Workspace path normalization
  - [x] File-argument translation
  - [x] Output-cap truncation behavior
  - [x] Cleanup decision logic
- [ ] Integration tests
  - [ ] Native bridge happy-path execution
  - [ ] Cancellation during active run
  - [ ] Download cleanup on failure
  - [ ] Remote partial-upload cleanup attempt on cancellation/failure
  - [ ] Logging enabled vs disabled persistence behavior
  - [ ] Workspace import/export flows
- [ ] UI tests
  - [ ] Run a valid command end-to-end through the UI
  - [ ] Validation error display for malformed commands
  - [ ] Results screen shows stdout/stderr separately
  - [ ] Workspace browser CRUD flows
- [ ] Device/build validation
  - [ ] Verify release ABI packaging (`arm64-v8a`, `armeabi-v7a`)
  - [ ] Verify debug/dev `x86_64` packaging
  - [ ] Verify foreground/background behavior on a real device

---

## 20. Final Validation and Polish

- [ ] Re-run project validation after implementation
  - [x] Run `./gradlew lintDebug`
  - [x] Run `./gradlew test`
  - [ ] Run any existing Android tests relevant to touched UI/workspace flows
- [ ] Validate user-visible behavior
  - [ ] Workspace root selection
  - [ ] Import/export flows
  - [ ] Persistent logs behavior
  - [ ] Output truncation messaging
  - [ ] Remote cleanup warning behavior
- [ ] Update docs to reflect the final embedded curl capability set

---

## Recommended Implementation Order

1. Native build infrastructure
2. JNI execution bridge
3. Command parsing/validation
4. Workspace model and path translation
5. Domain/persistence layer
6. Background execution service
7. Curl runner UI
8. Results/logs UI
9. Workspace browser
10. Cleanup hardening
11. Final testing and polish
