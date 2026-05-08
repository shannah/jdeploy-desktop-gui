---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Main Menu and Recent Projects

## R · Requirements
- The main menu is the first window the user sees after the splash. It exposes:
  - **Open** an existing jDeploy project from disk.
  - **Open Recent** — single-click selects, double-click opens; auto-disabled when no
    selection.
  - **Recent projects list** — populated from the database (project recency) and
    filterable via a live search field.
  - **Import Project** — launch the importer.
  - **Create Project** — launch the new-project wizard (in a coroutine).
  - **MCP Tools** checkbox — toggle MCP tool registration on the next CLI launch
    (persisted under preference key `mcpToolsEnabled`, default `"true"`).
  - **Help → About jDeploy** menu item — show the About dialog.
- Hero graphic image is added centered in the hero panel.
- Definition of Done as it stands today: window opens with title `"jDeploy"`, search
  filters by case-insensitive substring of project name, recent list refreshes from
  `ProjectService.findRecent()`. No automated end-to-end menu tests; visual smoke
  via running the app. [INFERRED]

## E · Entities
- `Project` record — the recent-list elements (`ca.weblite.jdeploy.app.records.Project`).
  Identified by UUID; carries `name`, `path`, `lastOpened`, optional accounts.
- `ProjectListCellRenderer`
  (`src/main/java/ca/weblite/jdeploy/app/views/mainMenu/ProjectListCellRenderer.java`) —
  renders `Project` rows in the JList.

## A · Approach
- Frame ownership is handled by the abstract `JFrameViewController` base:
  `MainMenuViewController` overrides `initUI()` and `onBeforeShow()`, and runs by
  calling `JFrameViewController.show()` (which is invoked indirectly via `Runnable.run()`).
- Recent-projects filter uses `DocumentListener` on the search field and rebuilds the
  list model from `ProjectService.findRecent()` per keystroke, applying a case-insensitive
  `String.contains` filter on the project name.
- The MCP-tools toggle persists synchronously into `PreferencesService.rootPreferences`,
  flushed via `PreferencesInterface.commit()` (failures swallowed silently — line 124-126).
- Each menu action delegates to a feature-specific controller (`OpenProjectController`,
  `ImportProjectViewController`, `NewProjectController`).

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/controllers/MainMenuViewController.kt` — the controller.
- `src/main/java/ca/weblite/jdeploy/app/controllers/JFrameViewController.java` — abstract base.
- `src/main/java/ca/weblite/jdeploy/app/forms/MainMenuForm.kt` — KtSwing DSL form.
- `src/main/java/ca/weblite/jdeploy/app/views/mainMenu/ProjectListCellRenderer.java` — list cell renderer.
- `src/main/java/ca/weblite/jdeploy/app/swing/ResponsiveImagePanel.java` — hero image panel.

## O · Operations

### 1. Build Frame Lifecycle Base — `JFrameViewController` (abstract)
File: `src/main/java/ca/weblite/jdeploy/app/controllers/JFrameViewController.java`

1. Responsibility: provide a reusable `Runnable` view controller that builds a
   `JFrame`, packs it, sets the icon, fires `onBeforeShow()`, and shows it. If a
   parent frame is supplied, hides it on launch and re-shows it when the child window
   closes (back-button behavior).
2. Fields:
   - `rootComponent: JComponent` — the root component returned by `initUI()`.
   - `parentFrame: JFrame` — optional parent (null for top-level main menu).
3. Methods:
   - `initUI(): JComponent` — abstract; subclass builds the UI tree.
   - `show(): void` — assemble the frame.
     - Logic: call `initUI()`; create new `JFrame`; set close op (`EXIT_ON_CLOSE` if no
       parent, `DISPOSE_ON_CLOSE` otherwise); when child closes, re-show parent
       (lines 27-32); set content pane, pack, center, set icon from
       `/ca/weblite/jdeploy/app/assets/icon.png` (lines 35-42); call `onBeforeShow()`
       (line 43); show frame (line 44).
   - `onBeforeShow(): void` — empty hook (line 47-48).
   - `run(): void` — calls `show()` so the controller can be passed as a `Runnable`.
   - `getFrame(): JFrame` — returns `rootComponent.getTopLevelAncestor()` cast to JFrame.

### 2. Show Main Menu — `MainMenuViewController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/MainMenuViewController.kt`

1. Responsibility: build the main-menu UI, wire button actions, manage the recent-projects
   list and search filter, and toggle MCP tools.
2. Fields:
   - `projectService: ProjectService` (line 27) — recent project lookup + touch.
   - `controllerFactory: ControllerFactory` (line 28) — creates `ProjectController` and
     `ErrorController` instances. (Imported but used only indirectly via downstream calls.)
   - `preferencesService: PreferencesService` (line 29) — provides `rootPreferences`.
3. Companion:
   - `MCP_TOOLS_ENABLED_KEY = "mcpToolsEnabled"` (line 32).
4. Methods:
   - `initUI(): JComponent` (line 35-131)
     - Logic:
       1. Resolve `Edt` from DI (line 36).
       2. Construct `MainMenuForm()` (line 38).
       3. Open button → `edt.invokeLater(OpenProjectController(frame))` (lines 40-46).
       4. Set the recent-projects list model from `buildRecentProjectsModel()` and the
          cell renderer to `ProjectListCellRenderer` (lines 48-49).
       5. Build `openRecentAction(mainMenu)` and bind it to the **Open Recent** button;
          enable/disable based on selection (lines 51-57).
       6. Double-click on the list invokes `openRecentAction.actionPerformed(null)`
          (lines 59-65).
       7. Add `DocumentListener` to the search field; on each change, clear the list
          model and re-add projects whose lowercase name contains the query (lines 66-92).
       8. Import button → `ImportProjectViewController(frame).run()` (lines 94-98).
       9. Create-project button → launch a coroutine on `SwingDispatcher` that calls
          `NewProjectController(frame).show()` (lines 100-108).
       10. Add `ResponsiveImagePanel("/ca/weblite/jdeploy/app/assets/jdeploy-home-hero.png")`
           into the hero wrapper (lines 110-115).
       11. MCP-tools checkbox: read `rootPrefs.get("mcpToolsEnabled", "true")`,
           sync UI (line 119-120). On toggle, persist via `set` + `commit()`,
           swallowing exceptions (lines 121-127).
       12. Return the form (line 129).
   - `onBeforeShow()` (line 133-136): set frame title to `"jDeploy"`, install menu bar.
   - `setupMenuBar(): void` (lines 138-155): build a `Help` menu containing
     `About jDeploy` that opens `AboutDialog(frame)`.
   - `buildRecentProjectsModel(): ListModel<Project>` (lines 157-164): create
     `DefaultListModel<Project>`, fill via `projectService.findRecent()`.
   - `openRecentAction(mainMenu): Action` (lines 166-181): produce an `AbstractAction`
     that, on activate, takes the selected `Project`, and runs
     `OpenProjectController(frame, project.path).run()`. Action starts disabled.

### 3. Render Recent-Projects List — `ProjectListCellRenderer`
File: `src/main/java/ca/weblite/jdeploy/app/views/mainMenu/ProjectListCellRenderer.java`

1. Responsibility: render a `Project` row in the recent-projects JList. [INFERRED — file
   exists but not read in this bootstrap; assumed to extend `DefaultListCellRenderer`.]

### 4. Compose Form — `MainMenuForm`
File: `src/main/java/ca/weblite/jdeploy/app/forms/MainMenuForm.kt`

1. Responsibility: KtSwing DSL form exposing the buttons, search field, recent list,
   hero wrapper, and MCP-tools checkbox referenced by the controller. [INFERRED]
2. Methods exposed (from controller usage in `MainMenuViewController.kt`):
   - `getOpenButton()`, `getOpenRecentButton()`, `getRecentProjects(): JList<Project>`,
     `getSearchField(): JTextField`, `getImportProject()`, `getCreateProjectButton()`,
     `getHeroGraphicWrapper(): JPanel`, `getMcpToolsCheckBox(): JCheckBox`.

## N · Norms
- All Swing handlers run on the EDT — long-running follow-ups (e.g. `NewProjectController.show()`)
  switch off the EDT explicitly via coroutines/`SwingWorker`.
- Preference flushes are best-effort: `commit()` failures are silently swallowed for the
  MCP toggle (lines 124-126). Don't add user-visible error reporting here without
  understanding why this was chosen.

## S · Safeguards
- The Open-Recent action is auto-disabled until a list item is selected
  (`MainMenuViewController.kt:54-56, 171`).
- Double-click only fires when `e.clickCount == 2` (line 61).
- Filter loop short-circuits empty queries (line 84) so an empty search shows all recents.
- The MCP-tools preference defaults to `"true"` if missing, ensuring tools register on
  first run (line 119).
