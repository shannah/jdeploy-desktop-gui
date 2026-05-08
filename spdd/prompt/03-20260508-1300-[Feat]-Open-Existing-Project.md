---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Open Existing Project

## R · Requirements
- The user can pick an existing jDeploy project directory via a native directory
  dialog and have it opened in the project editor (`JDeployProjectEditor`).
- An optional `fromPath` constructor argument bypasses the dialog (used when opening
  from the recent-projects list and after Create / Import).
- The dialog seeds the start directory with the `lastProjectPath` preference, falling
  back to the user's home directory.
- A directory shows up as selectable in the dialog only if it passes
  `ValidationLevel.HasPackageJson`. After selection, the project must additionally
  pass `ValidationLevel.MeetsMinimumRequirements`; otherwise an error is shown via
  `ErrorController`.
- Successfully opening a project:
  1. Touches its `lastOpened` timestamp (`ProjectService.touch`).
  2. Hands off to `ProjectController` which constructs a `JDeployProjectEditor` and
     shows it.
  3. If `closeParentWindowOnSuccess` is true, disposes the parent window.
- Definition of Done: existing tests in `LoadProjectTest.kt`, `ProjectServiceTest.kt`,
  and `TouchProjectTest.kt` cover the underlying service layer; no automated UI test
  for the controller (manual verification). [INFERRED]

## E · Entities
- `Project` record (`ca.weblite.jdeploy.app.records.Project`) — domain object with
  `name`, `path`, `uuid`, `lastOpened`.
- `ProjectValidator.ValidationLevel`
  (`src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java:24-28`):
  ordinal-ordered enum `DirectoryExists < HasPackageJson < MeetsMinimumRequirements`.
- `InvalidProjectException`
  (`src/main/java/ca/weblite/jdeploy/app/exceptions/InvalidProjectException.java`) —
  carries `projectPath` and `reason`.

## A · Approach
- The controller is a `Runnable` so it can be invoked via `Edt.invokeLater(...)` from
  the main menu and other callers.
- Validation is split: `HasPackageJson` is used as the dialog's filter so users can only
  *enter* valid-looking directories; `MeetsMinimumRequirements` (which parses
  `package.json` for `name`/`version`/`jdeploy`) runs after the directory is picked,
  surfacing precise error reasons via `ErrorController`.
- Project loading is idempotent in the database: `ProjectService.loadProject` ensures
  the project is in the DB and the `.jdeploy/uuid` filesystem marker exists, and
  reconciles divergent UUIDs.

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/controllers/OpenProjectController.kt` — the controller.
- `src/main/java/ca/weblite/jdeploy/app/controllers/ProjectController.java` — opens editor.
- `src/main/java/ca/weblite/jdeploy/app/services/ProjectService.kt` — DB sync + `touch`.
- `src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java` — validation levels.
- `src/main/java/ca/weblite/jdeploy/app/factories/ControllerFactory.java` — creates
  downstream controllers.
- `src/main/java/ca/weblite/jdeploy/app/factories/ProjectEditorFactory.java` — builds
  `JDeployProjectEditor` from a `Project` record.
- `src/main/java/ca/weblite/jdeploy/app/system/files/FileSystemUiInterface.java` —
  directory dialog API used here.

## O · Operations

### 1. Open a Project — `OpenProjectController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/OpenProjectController.kt`

1. Responsibility: pick a project directory (or accept one), validate it, register
   recency, and open the project editor.
2. Fields (constructor):
   - `parentWindow: Window` (line 16) — owner for the dialog and downstream errors.
   - `fromPath: String?` (line 17, `@JvmOverloads` default null) — pre-supplied path.
   - `closeParentWindowOnSuccess: Boolean` (line 18, default false).
3. Methods:
   - `run(): Unit` (line 34-80)
     - Logic:
       1. If `fromPath` is null, open a directory dialog via
          `FileSystemUiInterface.openDirectoryDialog(parentWindow, "Open Project",
          rootPreferences["lastProjectPath", environment.userHomeDirectory], null,
          predicate)`. The predicate calls `ProjectValidator.isValidProject(path,
          HasPackageJson)` (lines 35-47).
       2. If still null after the dialog (user cancelled), return (lines 48-51).
       3. Validate at `MeetsMinimumRequirements` level (line 54). On `InvalidProjectException`,
          dispatch `ControllerFactory.createErrorController(e)` to the EDT and return
          (lines 55-60).
       4. Call `projectService.touch(projectService.loadProject(path))` to register
          recency and ensure the project is in the DB (line 64). On any exception,
          dispatch an error controller and return (lines 63-70).
       5. Hand off to `ControllerFactory.createProjectController(project)` on the EDT
          (lines 72-74).
       6. If `closeParentWindowOnSuccess`, dispose `parentWindow` on the EDT
          (lines 75-79).

### 2. Open Project Editor — `ProjectController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/ProjectController.java`

1. Responsibility: instantiate and show `JDeployProjectEditor` for a `Project`. Handles
   IOException by routing to an `ErrorController`.
2. Methods:
   - `run(): void` (line 27-34)
     - Logic: try `projectEditorFactory.createOne(project).show()` (line 29-30); catch
       `IOException`, then `edt.invokeLater(new ErrorController(e))` (line 32).

### 3. Validate a Project Directory — `ProjectValidator`
File: `src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java`

1. Responsibility: assess whether a path is a valid jDeploy project at the requested level.
2. Constants:
   - `ValidationLevel { DirectoryExists, HasPackageJson, MeetsMinimumRequirements }`
     (lines 24-28) — ordered by ordinal.
3. Methods:
   - `isValidProject(String, ValidationLevel): boolean` (lines 29-37) —
     wraps `validate` and returns false on `InvalidProjectException`.
   - `validate(String path, ValidationLevel level): void` (lines 39-66)
     - Logic:
       1. If `!fileSystem.isDirectory(path)`, throw `InvalidProjectException(path,
          "The path is not a directory")` (line 40-42).
       2. If level ≥ `HasPackageJson`, require `path/package.json` to exist
          (lines 44-48).
       3. If level ≥ `MeetsMinimumRequirements`, parse `package.json` via
          `PackageJsonService.readOne(path)`; require `name`, `version`, `jdeploy`
          fields. Wrap any parsing exception in `InvalidProjectException` carrying
          the underlying message (lines 50-65).

### 4. Sync Filesystem ↔ Database — `ProjectService.loadProject` / `touch`
File: `src/main/java/ca/weblite/jdeploy/app/services/ProjectService.kt`

1. Responsibility: ensure the project at `path` exists in the DB with a UUID matching
   the `.jdeploy/uuid` file; update `lastOpened` on touch.
2. Methods:
   - `loadProject(path: String): Project` (line 39-42)
     - Logic: call `addProjectAtPath(path)` then `projectRepository.findOnebyPath(path)`.
   - `touch(project: Project): Project` (line 33-36)
     - Logic: set `project.lastOpened = clock.now().timeInMillis / 1000`; persist via
       `projectRepository.saveOne(project)`.
   - `addProjectAtPath(path: String): Unit` (private, line 57-78)
     - Logic: build a filesystem-derived `Project` via `ProjectFactory.createOne(path)`
       (line 59); look up by path in the DB or save the FS project if missing
       (lines 60-64); ensure `.jdeploy/uuid` exists, writing it if not (lines 66-68);
       if FS UUID and DB UUID differ, attempt `findOneById(fsUuid)`, and on
       `NotFoundException` delete the DB record and save the FS project to reconcile
       (lines 70-77).

### 5. Build a Project from the Filesystem — `ProjectFactory`
File: `src/main/java/ca/weblite/jdeploy/app/factories/ProjectFactory.kt`

1. Responsibility: validate the project directory, parse `package.json`, and produce
   a `Project` record carrying the UUID from `.jdeploy/uuid` (or a freshly generated one).
2. Methods:
   - `createOne(projectPath: String): Project` (line 20-22)
     - Logic: delegate to `createOneFromProjectPath(projectPath)`.
   - `createOneFromProjectPath(projectPath: String): Project` (line 33-38)
     - Logic: validate at `MeetsMinimumRequirements`; read `package.json`; call
       `createOneFromPackageJson`.
   - `createOneFromPackageJson(projectPath, packageJson): Project` (line 24-31)
     - Logic: read UUID from `.jdeploy/uuid` or generate; build `Project(name=
       packageJson.name, path=projectPath, uuid=uuid)`.
   - `readUuidFromFileSystem(projectPath: String): UUID?` (line 40-47)
     - Logic: if file missing, return null; else read text and `UUID.fromString`.

### 6. Build the Project Editor — `ProjectEditorFactory`
File: `src/main/java/ca/weblite/jdeploy/app/factories/ProjectEditorFactory.java`

1. Responsibility: read `package.json` and construct a `JDeployProjectEditor` wrapped in
   a `ProjectEditorContext`. [INFERRED — not read in this bootstrap.]

## N · Norms
- All non-trivial work runs through `Edt.invokeLater(...)` to ensure controller
  instantiation completes on the EDT (`OpenProjectController.kt:55-58, 66-69, 72-74`).
- Errors during open never throw out of the controller — they are routed to
  `ErrorController` and the controller returns silently after dispatching.

## S · Safeguards
- Two validation passes: cheap (`HasPackageJson`) inside the dialog filter, expensive
  (`MeetsMinimumRequirements`) after selection. The expensive pass is what surfaces
  "missing name/version/jdeploy" errors with the actual reason (`ProjectValidator.java:50-65`).
- User cancel (null path from dialog) is treated as a no-op — no error, no DB write
  (`OpenProjectController.kt:48-51`).
- UUID divergence between filesystem and DB is reconciled deterministically: the DB
  record is replaced with the filesystem record (`ProjectService.kt:70-77`). This
  matters when a project directory is copied/restored to a different machine.
