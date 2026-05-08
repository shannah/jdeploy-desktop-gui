---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Import Existing Project

## R · Requirements
- The user opens an "Import Project" window from the main menu and points it at an
  existing Java project directory (typically Maven or Gradle, not yet jDeploy-aware).
- The window has:
  - A directory text field with a `Browse...` button (opens a directory dialog).
  - A `Generate GitHub Workflow` checkbox.
  - `Cancel` and `Import` buttons.
- On `Import`:
  1. Run `ProjectInitializer.decorate(Request(projectDirectory, npmToken=null,
     dryRun=false, generateGithubWorkflow=<checkbox>, cheerpjSplash=null))` off the EDT
     in a `SwingWorker`.
  2. On success, open the now-jDeploy-decorated project via
     `OpenProjectController(frame, projectDirectory, closeParentWindowOnSuccess=true)`.
  3. On failure, route the exception to a fresh `ErrorController` on the EDT.
- On `Cancel`, dispose the import frame.
- Window title is `"Import Project"`.
- Definition of Done as it stands today: manual verification — no automated test for
  the controller. [INFERRED]

## E · Entities
- `ProjectInitializer.Request` (jDeploy CLI; outside this repo) — the import request.
  Fields used here: `projectDirectory`, `npmToken`, `dryRun`, `generateGithubWorkflow`,
  `cheerpjSplash`.
- `ProjectInitializer.Response` — carries flags such as `generatedGithubWorkflow`,
  `githubWorkflowExists` (referenced from `JDeployMcpServer.java:189-197`).

## A · Approach
- The controller extends the same `JFrameViewController` used by the main menu, which
  hides the parent frame while the import window is open and re-shows it on close.
- All filesystem decoration logic lives in `ProjectInitializer.decorate(...)`; this
  controller is just a UI shell that produces a request and routes the response.
- Error path is decoupled: the `SwingWorker` captures any exception and the `done()`
  method dispatches an `ErrorController` on the EDT, never re-throwing on the worker
  thread.

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/controllers/ImportProjectViewController.kt` — controller.
- `src/main/java/ca/weblite/jdeploy/app/forms/ImportProjectFormKts.kt` — KtSwing DSL form
  used by the controller.
- `src/main/java/ca/weblite/jdeploy/app/forms/ImportProjectForm.java` +
  `ImportProjectForm.form` — IntelliJ form designer scaffold (legacy variant). [INFERRED]
- `src/main/java/ca/weblite/jdeploy/app/factories/ControllerFactory.java` — produces
  `ErrorController` and `ProjectController` instances.
- `src/main/java/ca/weblite/jdeploy/app/controllers/OpenProjectController.kt` — used to
  open the imported project on success.

## O · Operations

### 1. Show Import Frame — `ImportProjectViewController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/ImportProjectViewController.kt`

1. Responsibility: build the import UI, wire `Browse`, `Cancel`, `Import` actions, and
   route `ProjectInitializer.decorate` results.
2. Fields:
   - `fileSystemUi: FileSystemUiInterface` (line 14).
   - `projectInitializer: ProjectInitializer` (line 15).
   - `controllerFactory: ControllerFactory` (line 16).
3. Methods:
   - `onBeforeShow(): Unit` (line 18-21)
     - Logic: call `super.onBeforeShow()`; set `frame.title = "Import Project"`.
   - `initUI(): JComponent` (line 22-44)
     - Logic:
       1. Build `ImportProjectFormKts()` (line 23).
       2. Wire `browseProjectDirectory` button to
          `fileSystemUi.openDirectoryDialog(frame, "Select Project Directory", null,
          null, null)`; assign result text to `form.projectDirectory.text`
          (lines 24-33).
       3. `cancelButton` disposes the frame (lines 35-37).
       4. `importButton` calls `handleImportProject(form.projectDirectory.text,
          form.generateGitHubWorkflow.isSelected)` (lines 39-41).
       5. Return `form`.
   - `handleImportProject(projectDirectory: String, generateGitHubWorkflow: Boolean): Unit`
     (line 46-82)
     - Logic: launch a `SwingWorker<ProjectInitializer.Response?, Void?>`. In
       `doInBackground`, call `projectInitializer.decorate(new Request(projectDirectory,
       null, false, generateGitHubWorkflow, null))` (lines 55-63). Capture any
       exception into a private `error: Exception?` field and return null.
       In `done()` on the EDT:
         - If `error != null`, run `controllerFactory.createErrorController(error)`
           via `EventQueue.invokeLater` (line 75).
         - Else open the project via `OpenProjectController(frame, projectDirectory,
           closeParentWindowOnSuccess = true)` (line 78).

## N · Norms
- The file dialog is provided by `FileSystemUiInterface`, which on macOS uses the
  native `FileDialog` and on other platforms uses `JFileChooser` (see
  `JdeployGuiModule.java:42-56`).
- The window's lifecycle is delegated entirely to the `JFrameViewController` base
  (auto re-show parent on close, icon, packing, etc.).
- After import, control transfers to `OpenProjectController` with
  `closeParentWindowOnSuccess=true` so the import window does not linger behind the
  editor.

## S · Safeguards
- The worker's catch block holds the original exception object so the `ErrorController`
  can render its message without losing the stack trace
  (`ImportProjectViewController.kt:50, 64-67, 73-75`).
- `cancelButton` simply disposes — there's no confirmation dialog, but since import is
  the destructive operation (and not yet started), cancellation has no side effects.
- `npmToken=null` and `dryRun=false` are hard-coded in this UI flow — token-based
  imports happen via the MCP `setup_jdeploy` tool (see MCP canvas), not here.
