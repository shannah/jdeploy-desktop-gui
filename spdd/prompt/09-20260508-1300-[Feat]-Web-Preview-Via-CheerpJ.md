---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Web Preview via CheerpJ

## R · Requirements
- A user with an open project (a `ProjectEditorContext` from `JDeployProjectEditor`)
  can generate a CheerpJ-based web build of their app and preview it locally without
  leaving the application.
- Generation runs on a background thread (`SwingWorker`) while a modal indeterminate
  progress dialog "Generating Web App..." is displayed.
- After generation succeeds:
  - A non-modal `WebPreviewForm` opens, with the title `"<Project Name> - Web Preview"`,
    `alwaysOnTop=true`, and three buttons:
    - **Open in Browser** — opens `http://localhost:<server.listeningPort>` via
      `ProjectEditorContext.browse(URI)`.
    - **Open Web App Directory** — opens the destination directory via
      `desktopInterop.openDirectory(result.dest)`.
    - **Stop** — stops the embedded server and disposes the form.
  - Closing the form via the window's close button also stops the server.
- Generation invokes `CheerpjService` with args `["--serve", "--preview"]` and the
  project's `package.json` path.
- Definition of Done as it stands today: manual verification — no automated tests for
  this flow. [INFERRED]

## E · Entities
- **`CheerpjService`** (jDeploy CLI; outside this repo). `execute()` returns a
  `CheerpjService.Result` carrying `server: NanoHTTPD`-like (with `listeningPort`,
  `stop()`) and `dest: File` (the generated web-app directory).
- **`ProjectEditorContext`** (`ca.weblite.jdeploy.app.records.ProjectEditorContext`)
  — exposes `projectSettings()` (with `name`, `packageJsonPath`),
  `browse(URI)`, and `desktopInterop.openDirectory(File)`.
- **`StaticFileServer`** — used internally by `CheerpjService`. [INFERRED — imported
  but not directly used here.]

## A · Approach
- The controller is a `Runnable` so it can be `EventQueue.invokeLater`'d from menu/button
  handlers. If invoked off the EDT, it self-redirects (`run()` lines 19-23).
- The progress dialog is non-modal (`false`) but `isAlwaysOnTop = true`, so it does not
  freeze the app yet still floats over the editor.
- On success the preview form is also `alwaysOnTop = true` to stay visible above the
  editor while the user clicks through Browser / Directory / Stop.
- Server lifecycle is owned by the preview form: both window-close and the **Stop**
  button call `server.stop()`. The user must explicitly close the form to release
  the port.

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/controllers/WebPreviewController.kt` — the controller.
- `src/main/java/ca/weblite/jdeploy/app/forms/WebPreviewForm.kt` — preview frame
  with three action buttons.
- `ca.weblite.jdeploy.services.CheerpjService` — external (jDeploy CLI).

## O · Operations

### 1. Run Web Preview — `WebPreviewController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/WebPreviewController.kt`

1. Responsibility: generate the CheerpJ web build off the EDT and present a preview
   window controlling the embedded HTTP server.
2. Fields (constructor):
   - `parentFrame: JFrame` (line 17).
   - `context: ProjectEditorContext` (line 17).
3. Methods:
   - `run(): Unit` (line 19-25)
     - Logic: if not on EDT, `EventQueue.invokeLater(this)` and return; else call
       `generateWebAppWorker()`.
   - `generateWebApp(): CheerpjService.Result` (private, line 27-34)
     - Logic: build `CheerpjService(File(context.projectSettings().packageJsonPath),
       null)` with args `["--serve", "--preview"]`; return its `execute()`.
   - `generateWebAppWorker(): Unit` (private, line 36-87)
     - Logic:
       1. Build a non-modal `JDialog(parentFrame, "Generating Web App...", false)`
          with an indeterminate `JProgressBar` and a label
          `"Generating web app using CheerpJ Please wait..."`; pack and center; set
          `isAlwaysOnTop = true`; show it (lines 37-48).
       2. Launch a `SwingWorker<CheerpjService.Result, Void?>` whose `doInBackground`
          calls `generateWebApp()` (lines 49-53).
       3. In `done()`:
          - Dispose the progress dialog (line 55).
          - `try { val result = get(); ... }` to retrieve the result (line 57).
          - Build a `WebPreviewForm`; set its title to
            `"<projectSettings.name> - Web Preview"`; locate relative to parent;
            set `isAlwaysOnTop = true` (lines 59-62).
          - Add a `WindowAdapter` whose `windowClosing` calls `server.stop()`
            (lines 63-67).
          - Set `nowServingLabel.text` to
            `"Now serving your web app at http://localhost:<port>"` (line 69).
          - Wire **Open in Browser** to `context.browse(URI("http://localhost:<port>"))`
            (lines 70-72).
          - Wire **Open Web App Directory** to
            `context.desktopInterop.openDirectory(result.dest)` (lines 73-75).
          - Wire **Stop** to `server.stop(); previewForm.dispose()` (lines 76-79).
          - Pack and show (lines 80-81).
          - `catch (e: Exception)`: capture in private `error: Exception?` field
            (line 82-84). [DRIFT — error is captured but never surfaced to the user.]

### 2. Web Preview Form — `WebPreviewForm`
File: `src/main/java/ca/weblite/jdeploy/app/forms/WebPreviewForm.kt`

1. Responsibility: a `JFrame` with a label and three buttons, exposed as fields the
   controller wires after construction.
2. Fields (lateinit, public-get/private-set):
   - `nowServingLabel: JLabel` (line 15) — set initial text overwritten by controller.
   - `openOpenInBrowserButton: JButton` (line 13).
   - `openWebAppDirectory: JButton` (line 16).
   - `stopButton: JButton` (line 14).
3. Methods (init block, lines 17-44):
   - Set `defaultCloseOperation = DISPOSE_ON_CLOSE`; size 800x600; build a
     `BorderPane` with a centred label panel and a south `FlowLayout` panel hosting
     the three buttons.

## N · Norms
- All UI work is on the EDT; the only non-EDT code path is the `SwingWorker.doInBackground`
  invocation of `CheerpjService.execute()`.
- The preview window is `alwaysOnTop` to keep it above the project editor while
  users test the web build. Don't change this without thinking about the editor's
  z-order behavior.
- The CheerpJ CLI args `--serve --preview` must stay paired (`WebPreviewController.kt:32`):
  `--serve` starts the embedded server, `--preview` configures preview-mode output.

## S · Safeguards
- `run()` self-corrects to the EDT if invoked off-thread (`WebPreviewController.kt:20-23`).
- Both **Stop** and `windowClosing` stop the server, so the user has two ways to
  release the port (`WebPreviewController.kt:63-67, 76-79`).
- The progress dialog is disposed inside `done()` regardless of success or failure
  (line 55) to avoid a stuck "generating..." dialog.
- `[DRIFT]` Generation failures are captured into a local `error: Exception?` but not
  shown to the user (`WebPreviewController.kt:82-84`). When this canvas is regenerated,
  consider whether to route the error through `ControllerFactory.createErrorController`
  for parity with other features.
