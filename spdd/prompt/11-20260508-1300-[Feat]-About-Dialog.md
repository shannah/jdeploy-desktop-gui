---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: About Dialog

## R · Requirements
- The user can open an About dialog two ways:
  1. From the application's `Help → About jDeploy` menu item
     (`MainMenuViewController.kt:144-149`).
  2. On macOS, via the system "About" menu item, wired through `Desktop.setAboutHandler`
     during boot (`JdeployDesktopGui.java:69-104`).
- The dialog is application-modal, non-resizable, with a 64×64-scaled jDeploy icon,
  a `"jDeploy"` 24-pt sans-serif title, a single line of version-or-commit metadata,
  an OK button (default), and Esc-to-close.
- Version text logic: read system properties `jdeploy.app.version` (default
  `"Unknown"`) and `jdeploy.commitHash` (default `"Unknown"`). If the version starts
  with `"0.0.0"`, show `"Commit: <hash>"`; else show `"Version: <version>"`.
- The dialog icon (window iconImage) is the same `/ca/weblite/jdeploy/app/assets/icon.png`
  used elsewhere; if the icon resource fails to load the body falls back to a text
  "jDeploy" label in 32-pt bold.
- Definition of Done as it stands today: covered by `AboutDialogTest.java`,
  `AboutDialogVersionTest.java`, `AboutDialogVisualTest.java`.

## E · Entities
- None — the dialog is a self-contained `JDialog` subclass with no external state.

## A · Approach
- Two-step layout: `initializeComponents()` configures close behavior and dialog icon;
  `setupLayout()` builds the BorderLayout content; `setupBehavior()` adds Esc binding
  and default button. Construction sequence in the constructor: init → layout →
  behavior → pack → centre on parent.
- Icon load is wrapped in try/catch in *both* `initializeComponents` (window icon)
  and `setupLayout` (visible icon panel) so a missing/bad resource never blocks the
  dialog.
- Version is read from system properties, not from build metadata files: build
  configuration must inject `-Djdeploy.app.version=...` and `-Djdeploy.commitHash=...`
  for non-default values to appear. [INFERRED — based on `getVersionText`.]

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/forms/AboutDialog.java` — the dialog.
- `src/main/java/ca/weblite/jdeploy/app/controllers/MainMenuViewController.kt:138-155` —
  Help menu wiring.
- `src/main/java/com/github/shannah/jdeploydesktopgui/JdeployDesktopGui.java:69-104` —
  macOS Desktop About handler.
- `src/main/resources/ca/weblite/jdeploy/app/assets/icon.png` — icon resource.

## O · Operations

### 1. About Dialog — `AboutDialog`
File: `src/main/java/ca/weblite/jdeploy/app/forms/AboutDialog.java`

1. Responsibility: present application metadata in a modal dialog.
2. Constants:
   - `DEFAULT_VERSION = "Unknown"`, `DEFAULT_COMMIT = "Unknown"` (lines 9-10).
3. Methods:
   - Constructor `AboutDialog(Window parent)` (line 12-19)
     - Logic: `super(parent, "About jDeploy", APPLICATION_MODAL)`; call
       `initializeComponents()`, `setupLayout()`, `setupBehavior()`; `pack()`;
       `setLocationRelativeTo(parent)`.
   - `initializeComponents(): void` (private, line 21-30)
     - Logic: set close op `DISPOSE_ON_CLOSE`; try to set window icon from
       `/ca/weblite/jdeploy/app/assets/icon.png`. Swallows any exception.
   - `setupLayout(): void` (private, line 32-94)
     - Logic: BorderLayout(10,10); content panel with 20/20/10/20 empty border;
       top panel with icon at NORTH and 24-pt title at CENTER; if icon resource
       fails, replace icon with 32-pt bold "jDeploy" text label (lines 51-56);
       infoPanel with 14-pt sans-serif `versionLabel` from `getVersionText()`;
       button panel with OK button (line 84) wired to `dispose()`.
   - `setupBehavior(): void` (private, line 96-110)
     - Logic: minimum size 300×200; non-resizable; register Esc on the root pane
       to dispose; set default button to the OK button found by indexing the button
       panel.
   - `getVersionText(): String` (private, line 112-122)
     - Logic: read `jdeploy.app.version` and `jdeploy.commitHash`; if version starts
       with `"0.0.0"`, return `"Commit: <commitHash>"`; else `"Version: <version>"`.

### 2. Help Menu Wiring — `MainMenuViewController.setupMenuBar`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/MainMenuViewController.kt`

1. Responsibility: install the menu bar on `onBeforeShow` and add `Help → About jDeploy`.
2. Method:
   - `setupMenuBar(): Unit` (line 138-155)
     - Logic: build `JMenuBar` with a `Help` menu containing `About jDeploy`. Action
       creates `AboutDialog(frame)` and shows it.

### 3. Desktop About Handler — `JdeployDesktopGui.setupDesktopAboutHandler`
File: `src/main/java/com/github/shannah/jdeploydesktopgui/JdeployDesktopGui.java`

1. Responsibility: register a system About handler so macOS app-menu invocations open
   the same custom `AboutDialog`.
2. Method:
   - `setupDesktopAboutHandler(): void` (private, line 69-105)
     - Logic: gate on `Desktop.isDesktopSupported()` and
       `desktop.isSupported(Desktop.Action.APP_ABOUT)`. Set handler that finds
       active `Frame` (or first visible) and calls
       `new AboutDialog(activeFrame).setVisible(true)` on the EDT.

## N · Norms
- Use `APPLICATION_MODAL` modality so the user must dismiss the dialog before
  resuming work (consistent with macOS native About behavior).
- Don't add additional content to this dialog without considering the Desktop
  About-handler entry point — both menu and system invocation must produce the same
  result.

## S · Safeguards
- All resource loads (`icon.png`) are wrapped in try/catch with a text fallback so
  the dialog still renders if the icon is missing or corrupted
  (`AboutDialog.java:25-29, 44-56`).
- Esc-to-close is registered on the root pane, ensuring keyboard dismissal even when
  the OK button isn't focused (`AboutDialog.java:102-106`).
- `getVersionText` falls back to `"Unknown"` defaults for both properties so missing
  build-time injection never produces empty UI strings.
- Desktop About handler registration is gated behind two `isSupported` checks to
  avoid `UnsupportedOperationException` on Linux/Windows (`JdeployDesktopGui.java:71-75`).
