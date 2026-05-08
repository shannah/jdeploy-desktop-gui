---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Create New Project From Template

## R · Requirements
- The user opens a wizard from the main menu's **Create Project** button. The wizard
  collects:
  - `groupId` (required)
  - `artifactId` (required)
  - `displayName` (required)
  - `projectLocation` (parent directory; required, must be a directory; created project
    folder must not already exist)
  - `projectTemplate` (required; from the template catalog)
  - npm radio + `npmProjectName` (required iff npm selected)
  - GitHub-Releases radio + `githubRepositoryUrl` and optional
    `githubReleasesRepositoryUrl` (required iff GitHub releases selected)
  - "Create GitHub repo" / "Create releases repo" checkboxes (drive whether GitHub login
    is required and whether `isPrivateRepository` is set)
- Defaults for all fields are remembered in `Preferences.userNodeForPackage(NewProjectController)`.
- Template catalog refresh:
  - On show, if a refresh hasn't run in the last hour, trigger
    `UpdateProjectTemplatesController.updateSuspending()` first (1-hour throttle via
    static `lastProjectTemplateUpdate`).
  - A **Refresh** button next to the template combobox forces a sync update.
- If GitHub login is required (GitHub Releases selected AND any "create repo" checkbox
  checked), launch the `AccountChooserController(GITHUB)` and copy the chosen access
  token to `GithubTokenService.setToken(...)`.
- GitHub repository fields must match `owner/repository`; full URLs and extra slashes
  raise `ValidationFailedException` with field-specific guidance. Owner/repo characters
  are restricted to `[a-zA-Z0-9._-]+`.
- On **Create**, run `ProjectGenerator.generate(...)` off the EDT in a `SwingWorker`
  while a modal indeterminate progress dialog blocks the wizard. On success, save
  defaults and open the new project via `OpenProjectController(fromPath, closeParent=true)`.
- Tile delegate methods open the template's demo download URL, source URL, or web-app
  URL via `Desktop.browse(...)` if present.
- Definition of Done as it stands today: form validates and the project is created;
  no automated test for the wizard. [INFERRED]

## E · Entities
- `Template` (`ca.weblite.jdeploy.app.records.Template`) — name, displayName, demoDownloadUrl,
  sourceUrl, webAppUrl, etc. (see `04` template catalog canvas).
- `ProjectGeneratorRequestBuilder` (jDeploy CLI; outside repo) — fluent builder for
  `appTitle`, `projectName`, `parentDirectory`, `groupId`, `artifactId`, `templateName`,
  `githubRepository`, `isPrivateRepository`.
- `ValidationFailedException`
  (`src/main/java/ca/weblite/jdeploy/app/exceptions/ValidationFailedException.kt`).

## A · Approach
- The wizard is a JDialog (`NewProjectForm`) owned by the main menu frame. Lifecycle is
  controlled by a Kotlin coroutine launched on `SwingDispatcher` so `show()` can suspend
  on the template-catalog refresh before showing the dialog.
- Template-catalog refresh has two paths: blocking (via
  `UpdateProjectTemplatesController.update()`, used for the explicit Refresh button when
  the catalog isn't initialized at construction time) and suspending (used at show-time
  with a one-hour throttle).
- Account selection is launched as a `CompletableFuture<AccountInterface?>`; the
  `Create` button is wired through `selectGitHubAccount().thenRun { handleCreateProject() }`
  so account selection sequences before generation.
- Defaults persistence uses Java Preferences (`Preferences.userNodeForPackage(NewProjectController)`).
  Each form field's `actionPerformed` updates the default; `saveDefaultValues()` writes
  the final state on success.

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt` — wizard controller.
- `src/main/java/ca/weblite/jdeploy/app/forms/NewProjectForm.kt` — wizard dialog (KtSwing).
- `src/main/java/ca/weblite/jdeploy/app/forms/NewProjectFormInterface.kt` — form contract.
- `src/main/java/ca/weblite/jdeploy/app/forms/NewProjectPanel.kt` — panel composing fields. [INFERRED]
- `src/main/java/ca/weblite/jdeploy/app/forms/NewProjectWizard.kt` — wizard chrome. [INFERRED]
- `src/main/java/ca/weblite/jdeploy/app/forms/TemplateChooserPanel.kt` — model + tile picker.
- `src/main/java/ca/weblite/jdeploy/app/forms/TemplateList.kt`,
  `TemplateTile.kt`, `TemplateTileDelegate.kt` — tile rendering.
- `src/main/java/ca/weblite/jdeploy/app/controllers/UpdateProjectTemplatesController.kt` —
  template-catalog refresh with progress dialog.
- `src/main/java/ca/weblite/jdeploy/app/controllers/AccountChooserController.java` —
  GitHub account picker (see `Publishing-Accounts` canvas).

## O · Operations

### 1. Construct Wizard — `NewProjectController` constructor
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Responsibility: build the dialog, wire field listeners, populate template combobox,
   restore defaults, and wire the Create button.
2. Fields:
   - `dialog: NewProjectForm` — the dialog (line 42).
   - `owner: Frame` — parent frame (line 43).
   - `lastProjectTemplateUpdate: Long` (companion, line 46) — last time the catalog was
     auto-refreshed.
   - `preferences` (lazy, lines 350-352): `Preferences.userNodeForPackage(NewProjectController)`.
   - Injected: `fileSystemUi`, `projectGenerator`, `templateCatalog`, `controllerFactory`,
     `githubTokenService`, `projectTemplateRepository`.
3. Method:
   - `constructor(owner: Frame)` (line 49-163)
     - Logic:
       1. Build a `TemplateChooserPanel.Model` whose `getProjectTemplates()` calls
          `projectTemplateRepository.findAll()` (lines 56-60).
       2. Construct `NewProjectForm(owner, templateChooserModel)` and assign a
          `TemplateTileDelegate` whose three callbacks open the template's demo
          download / source / web-app URLs (lines 61-72).
       3. Set the dialog icon (line 75).
       4. Add a `DocumentListener` to `artifactId`, `groupId`, `displayName`,
          `projectLocation` that calls `update()` on every change (lines 76-90).
       5. `selectProjectLocationButton` opens `fileSystemUi.openDirectoryDialog(...)`,
          stores the result as default, and updates the field (lines 92-107).
       6. npm/gitHub radio buttons re-enable dependent fields and pre-fill `npmProjectName`
          from `artifactId` if empty (lines 109-118).
       7. If template catalog is not initialized, call `updateTemplateCatalog(dialog)`;
          otherwise `updateTemplateOptions()` (lines 120-124).
       8. `refreshTemplatesButton` triggers a blocking `updateTemplateCatalog(dialog)`
          (lines 126-128).
       9. `createProjectButton` chains `selectGitHubAccount().thenRun { handleCreateProject() }`
          (lines 130-134).
       10. Field action listeners persist their values as defaults via `setDefaultValue`
           (lines 136-158).
       11. Restore defaults (`setDefaultValues()`) and call `update()` (lines 159-160).
   - `show(): Unit` (suspend, line 165-171)
     - Logic: call `updateTemplateCatalogSuspending(owner)`; on `SwingDispatcher`, pack
       and show the dialog.

### 2. Validate Form — `NewProjectController.validate`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Responsibility: pre-flight validation before submitting to `ProjectGenerator`.
2. Method:
   - `validate(): Unit` throws `ValidationFailedException` (lines 358-404)
     - Logic (in order):
       1. Reject empty `groupId`, `artifactId`, `displayName`, `projectLocation`,
          `projectTemplate` (lines 361-379).
       2. Reject if `getProjectDirectory()` already exists (lines 381-382).
       3. Reject if `projectLocation` is not a directory (lines 385-387).
       4. If npm selected, require non-empty `npmProjectName` (lines 389-391).
       5. If GitHub Releases selected, require `githubRepositoryUrl` non-empty and
          format-valid (lines 393-401). If `githubReleasesRepositoryUrl` non-empty,
          format-validate it as well.
   - `validateGitHubRepositoryFormat(repositoryUrl, fieldName): Unit`
     (lines 406-483)
     - Logic: trim; reject `http(s)://` prefix; require exactly one `/`; both parts
       non-empty; both parts match `^[a-zA-Z0-9._-]+$`. Each rejection raises
       `ValidationFailedException` with a field-specific message.

### 3. Submit Project Creation — `NewProjectController.handleCreateProject` / `createProject`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Responsibility: run `ProjectGenerator.generate(...)` off-EDT with a modal progress
   dialog and open the resulting project on success.
2. Methods:
   - `handleCreateProject(): Unit` (line 236-279)
     - Logic: build a modal `JDialog("Creating Project")` with an indeterminate
       progress bar and `DO_NOTHING_ON_CLOSE` (lines 238-248); `SwingWorker` calls
       `createProject()`, persists defaults, flushes preferences (lines 252-258);
       on `done()`, on success call `openProject(get())`; on failure run
       `controllerFactory.createErrorController(e)`; always dispose the dialog
       (lines 260-271).
   - `createProject(): File` (throws `ValidationFailedException`, lines 282-307)
     - Logic: call `validate()`; assemble `ProjectGeneratorRequestBuilder` from the
       form (npm name overrides project name when npm is selected; GitHub repo and
       `isPrivateRepository` set when applicable); call `projectGenerator.generate(...)`.
   - `openProject(File): Unit` (line 309-316)
     - Logic: create `OpenProjectController(parentWindow=dialog, fromPath=projectDir,
       closeParentWindowOnSuccess=true)` and call `run()`.

### 4. Update Template Catalog — `NewProjectController.updateTemplateCatalog* / updateTemplateOptions`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Methods:
   - `updateTemplateCatalog(owner: Frame)` (line 318-322)
     - Logic: blocking `UpdateProjectTemplatesController(templateCatalog, owner).update()`,
       then `updateTemplateOptions()`.
   - `updateTemplateOptions()` (line 324-340)
     - Logic: clear and repopulate `projectTemplate` combobox from
       `templateCatalog.projectTemplates`; restore prior selection if still present.
   - `updateTemplateCatalogSuspending(owner: Frame): Unit` (suspend, line 542-551)
     - Logic: throttle — do nothing if `lastProjectTemplateUpdate >= now - 1h`; else
       update `lastProjectTemplateUpdate = now` and call
       `UpdateProjectTemplatesController.updateSuspending()`, then `updateTemplateOptions()`.

### 5. Select GitHub Account — `NewProjectController.selectGitHubAccount` / `requiresGithubLogin`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Methods:
   - `requiresGithubLogin(): Boolean` (line 228-234)
     - Logic: true iff GitHub Releases radio selected AND
       (`createGithubReleasesRepositoryCheckBox.isSelected ||
       createGithubRepositoryUrlCheckBox.isSelected`).
   - `selectGitHubAccount(): CompletableFuture<AccountInterface?>` (line 213-226)
     - Logic: if `!requiresGithubLogin()`, complete immediately with null. Otherwise
       call `AccountChooserController(dialog, AccountType.GITHUB).show()` and, on the
       returned account, call `githubTokenService.setToken(account.accessToken)` if
       both are non-null. Return the account.

### 6. Open Demo / Source / Web URLs — `NewProjectController` tile delegates
File: `src/main/java/ca/weblite/jdeploy/app/controllers/NewProjectController.kt`

1. Methods:
   - `openTemplateDemoDownloadPage(template)` (line 497-510): null-safe URL → `Desktop.browse`.
   - `openTemplateSources(template)` (line 512-525): null/empty-safe URL → `Desktop.browse`.
   - `openWebDemo(template)` (line 527-540): null/empty-safe URL → `Desktop.browse`.
   - All three swallow exceptions with `e.printStackTrace()`.

## N · Norms
- Long-running work runs in `SwingWorker` or coroutine `withContext(Dispatchers.IO)` to
  keep the EDT free.
- Wizard defaults persist into `Preferences.userNodeForPackage(NewProjectController)` —
  this is intentionally separate from the application's main `PreferencesService` because
  it's wizard-local UX state, not application state.
- All user-visible validation errors use `ValidationFailedException` with a clear,
  copy-paste-friendly format example. Don't replace these messages with generic ones.

## S · Safeguards
- Pre-flight `validate()` runs before any directory is created or network call is made
  (`createProject:283`).
- The Create button is disabled until all four required identifiers are non-empty
  (`update():193-197`).
- Template-catalog refresh-on-show is throttled to once per hour to avoid repeated
  network fetches (`updateTemplateCatalogSuspending:543-547`).
- GitHub URL validator rejects full URLs with a specific message rather than silently
  succeeding (`validateGitHubRepositoryFormat:411-417`) — past mistakes here cause
  GitHub Action publishing to fail at release time.
- Existing project directory is rejected up-front (`validate:381-383`) to avoid
  partial overwrites.
