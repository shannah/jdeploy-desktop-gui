---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Application Bootstrap and Splash

## R · Requirements
- The application launches via `JdeployDesktopGui.main(String[])` and selects between two
  modes from the system property `jdeploy.mode` (default `gui`):
  - `gui`: shows splash screen, initializes DI, ensures app data directory, runs Flyway
    migrations, clears the project-template URL cache, and opens the main menu.
  - any other value: runs CLI mode — if `--mcp` is in args, starts the MCP stdio server;
    otherwise delegates to the bundled jDeploy CLI (`JDeploy.main`).
- On non-macOS platforms, `FlatLightLaf` is installed as the Swing Look & Feel before any UI.
- A custom About dialog is wired into the macOS Desktop "About" menu via
  `Desktop.setAboutHandler` when supported.
- The application data directory must exist before DB migrations run; created if missing.
- DB migrations are idempotent: the `migrate()` call is safe to invoke on every startup.
- Definition of Done as it stands today: the application starts cleanly on Mac/Win/Linux,
  shows the splash, lands on the main menu, and the SQLite database is migrated to the
  latest Flyway version. No automated boot test exists. [INFERRED]

## E · Entities
- **`JdeployAppConfigInterface`** (`src/main/java/ca/weblite/jdeploy/app/config/JdeployAppConfigInterface.kt:5-8` — [INFERRED]
  inferred from references): exposes `getJdbcUrl()`, `getAppId()`, `getAppDataPath()`.
  The app data path is platform-aware — Windows `%APPDATA%`, macOS
  `~/Library/Application Support/<appId>`, Linux `$XDG_CONFIG_HOME/<appId>`.
- **`EmfProviderInterface`** / **`EmfProvider`** (singleton; lazily constructs Hibernate
  `EntityManagerFactory` for persistence-unit `jdeploy-gui` using JDBC URL from config).
- **`ClockInterface`** (`src/main/java/ca/weblite/jdeploy/app/system/env/ClockInterface.kt`)
  with `SystemClock` impl returning `Calendar.getInstance()`.

## A · Approach
- Single `main` entry point splits early on `jdeploy.mode` so the same shaded artifact
  serves three roles (GUI app, MCP stdio server, jDeploy CLI bridge).
- DI uses the [Feather](https://github.com/zsoltherpai/feather) framework via a custom
  `DIContext` facade. Three modules compose: `JdeployJpaModule` (persistence),
  `JdeployGuiModule` (UI/system adapters), and `JDeployDesktopGuiModule` (top-level
  cross-cutting binds). All initialized via `JDeployDesktopGuiModule.install()`.
- The splash screen is shown on the EDT before DI install so users see immediate feedback
  while the slower DB migration / cache clear runs on the calling thread.
- Persistence uses Hibernate JPA over SQLite (community dialect). DDL is fully owned by
  Flyway (`hibernate.hbm2ddl.auto=none`) — see `src/main/resources/META-INF/persistence.xml`.

## S · Structure
- `src/main/java/com/github/shannah/jdeploydesktopgui/JdeployDesktopGui.java` — main entry.
- `src/main/java/ca/weblite/jdeploy/app/di/JDeployDesktopGuiModule.java` — top-level DI module.
- `src/main/java/ca/weblite/jdeploy/app/di/JdeployGuiModule.java` — UI / platform / accounts binds.
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/di/JdeployJpaModule.java` — JPA repo binds.
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/di/EmfProvider.kt` — Hibernate EMF lazy provider.
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/services/DatabaseService.java` — Flyway migrate + EM accessor.
- `src/main/java/ca/weblite/jdeploy/app/forms/SplashScreen.kt` — splash window.
- `src/main/java/ca/weblite/jdeploy/app/config/JdeployAppConfig.kt` — platform-aware app data path / JDBC URL.
- `src/main/resources/META-INF/persistence.xml` — JPA + Hibernate + SQLite config.

## O · Operations

### 1. Run Entry Point — `JdeployDesktopGui`
File: `src/main/java/com/github/shannah/jdeploydesktopgui/JdeployDesktopGui.java`

1. Responsibility: dispatch to GUI, MCP server, or jDeploy CLI based on `jdeploy.mode`
   system property and arguments.
2. Methods:
   - `main(String[] args)`: void
     - Logic: read `System.getProperty("jdeploy.mode", "gui")` (line 29). If `"gui"`,
       call `runGuiMode(args)` (line 32); else call `runCliMode(args)` (line 34).
   - `runCliMode(String[] args)`: void
     - Logic: if args contain `--mcp`, call `JDeployMcpServer.run()` (line 40); else
       call `JDeploy.main(args)` (line 42) — delegates to bundled jDeploy CLI.
   - `runGuiMode(String[] args)`: void
     - Logic: on non-macOS, install `FlatLightLaf` (line 49); register macOS Desktop
       About handler (line 56); show splash on EDT (line 58–60); install DI module
       (line 61); ensure app data dir (line 63); run `DatabaseService.migrate()`
       (line 64); clear template URL cache via `DefaultProjectTemplateRepository.clearCacheBlocking()`
       (line 65); launch `MainMenuViewController` on EDT (line 66).
   - `setupDesktopAboutHandler()`: void
     - Logic: if `Desktop.isDesktopSupported()` and `APP_ABOUT` action is supported,
       set handler that finds the active/first-visible `Frame` and shows
       `AboutDialog` (lines 71–104).
   - `createApplicationFilesDirectory()`: void
     - Logic: read `JdeployAppConfigInterface.getAppDataPath()`; if not a directory,
       call `FileSystemInterface.mkdir(absolutePath)` (lines 108–117). Wraps `IOException`
       in `RuntimeException`.
3. Constraints:
   - Splash must be shown on the EDT.
   - DI must be installed before any `DIContext.get(...)` resolves; both `DatabaseService`
     and `DefaultProjectTemplateRepository` are resolved through it.

### 2. Install DI Container — `JDeployDesktopGuiModule`
File: `src/main/java/ca/weblite/jdeploy/app/di/JDeployDesktopGuiModule.java`

1. Responsibility: top-level DI module; coordinates the JPA and GUI sub-modules and
   provides cross-cutting singletons (`EmfProviderInterface`, `JdeployAppConfigInterface`,
   `ClockInterface`, `ProjectTemplateRepositoryInterface`).
2. Methods:
   - `install()`: void
     - Logic: call `DIContext.initialize(new JdeployJpaModule(), new JdeployGuiModule(), this)`
       (lines 17–23).
   - `@Provides getEmfProvider()`: returns `DIContext.get(EmfProvider.class)` (line 26-28).
   - `@Provides getConfig()`: returns `DIContext.get(JdeployAppConfig.class)` as
     `JdeployAppConfigInterface` (line 30-33).
   - `@Provides getClock()`: returns `SystemClock` as `ClockInterface` (line 35-38).
   - `@Provides getProjectTemplateRepository(DefaultProjectTemplateRepository impl)`:
     returns `DefaultProjectTemplateRepository` as `ProjectTemplateRepositoryInterface`
     (line 40-43).

### 3. Install GUI/System Bindings — `JdeployGuiModule`
File: `src/main/java/ca/weblite/jdeploy/app/di/JdeployGuiModule.java`

1. Responsibility: bind UI/system/account interfaces to default implementations.
   The `FileSystemUiInterface` is selected per platform (`MacFileSystemUi` on macOS,
   `JavaSEFileSystemUi` on Linux/Windows/other).
2. Methods:
   - `@Provides getEnvironment(DefaultEnvironment)`: `EnvironmentInterface` (line 22-25).
   - `@Provides getPreferences()`: `Preferences.userNodeForPackage(JDeployDesktopGuiModule.class)`
     (line 28-30).
   - `@Provides getPreferences(DefaultPreferences)`: `PreferencesInterface` (line 32-35).
   - `@Provides getFileSystem(JavaSEFileSystem)`: `FileSystemInterface` (line 37-40).
   - `@Provides getFileSystemUiInterface(EnvironmentInterface)`: `FileSystemUiInterface`
     — `MacFileSystemUi` on macOS, otherwise `JavaSEFileSystemUi` (lines 42-56).
   - `@Provides accountServiceInterface(PreferencesAccountService)`:
     `AccountServiceInterface` (line 58-61).
   - `@Provides passwordServiceInterface(JavaKeyringPasswordService)`:
     `PasswordServiceInterface` (line 62-65).

### 4. Install JPA Bindings — `JdeployJpaModule`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/di/JdeployJpaModule.java`

1. Responsibility: bind repository interfaces to JPA implementations.
2. Methods:
   - `@Provides` for `GitHubAccountRepositoryInterface`, `NpmAccountRepositoryInterface`,
     `ProjectRepositoryInterface` returning their `Jpa*` implementations (lines 13–28).

### 5. Provide EntityManagerFactory — `EmfProvider`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/di/EmfProvider.kt`

1. Responsibility: lazily construct one `EntityManagerFactory` for the
   `"jdeploy-gui"` persistence unit, using a JDBC URL from `JdeployAppConfigInterface`.
2. Methods:
   - `getEntityManagerFactory(): EntityManagerFactory`
     - Logic: lazy-init via `Persistence.createEntityManagerFactory("jdeploy-gui", configMap)`
       where `configMap` overrides `jakarta.persistence.jdbc.url` with `config.getJdbcUrl()`.

### 6. Migrate Database — `DatabaseService`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/services/DatabaseService.java`

1. Responsibility: provide a single shared `EntityManager`, transaction wrapper, close
   hook, and Flyway migration runner.
2. Fields:
   - `entityManager: EntityManager` — lazily created (line 21, 39-43).
   - `emf: EntityManagerFactory` — built from `EmfProviderInterface` in constructor (line 34).
   - `config: JdeployAppConfigInterface` — for app data path + JDBC URL (line 25, 35).
   - `fileSystem: FileSystemInterface` — for ensuring the data directory exists (line 27, 36).
3. Methods:
   - `getEntityManager(): EntityManager` — lazy init (line 39-44).
   - `<T> executeInTransaction(Function<EntityManager, T>): T` — begin/commit, rollback
     on `RuntimeException` and rethrow (line 46-61).
   - `close(): void` — close EM and EMF if non-null (line 63-70).
   - `migrate(): void`
     - Logic: ensure app data directory exists via `FileSystemInterface.mkdir` if absent
       (lines 73–77); read `config.getJdbcUrl()` (line 81); run `Flyway.configure()
       .dataSource(jdbcUrl, "", "").load().migrate()` (lines 82–85).
4. Constraints:
   - Flyway runs against the SQLite file at the URL from config; migrations from
     `src/main/resources/db/migration/V*.sql` apply in order V1 → V4.
   - `IOException` on directory creation is wrapped in `RuntimeException` (line 78–80).

### 7. Splash Screen — `SplashScreen`
File: `src/main/java/ca/weblite/jdeploy/app/forms/SplashScreen.kt`

1. Responsibility: undecorated window shown during boot. Disposed when main menu opens. [INFERRED]
2. Methods:
   - `showSplash(): Unit` — display the splash window. Called from EDT during boot
     (`JdeployDesktopGui.java:59`).

## N · Norms
- Swing UI work (frame creation, splash, dialogs) runs on the EDT via `EventQueue.invokeLater`
  or `SwingUtilities.invokeLater` — see `JdeployDesktopGui.java:58, 66`.
- `FlatLightLaf` is the default Look & Feel except on macOS, which keeps native LAF
  (`JdeployDesktopGui.java:47-53`).
- Hibernate auto-DDL is disabled in `persistence.xml`; schema changes go through
  Flyway migrations under `src/main/resources/db/migration/`.
- The persistence-unit name `"jdeploy-gui"` is hard-coded (referenced from `EmfProvider`).
  Don't rename it without updating both `persistence.xml` and the EMF provider.

## S · Safeguards
- Mode dispatch is fail-closed: an unknown `jdeploy.mode` value drops to CLI rather
  than crashing GUI initialization (`JdeployDesktopGui.java:31-35`).
- App data directory creation is checked before invocation; an `IOException` aborts
  startup loudly via `RuntimeException` (`JdeployDesktopGui.java:115-117`).
- Look-and-feel installation is wrapped in try/catch; a failure prints a stack trace but
  does not abort GUI startup (`JdeployDesktopGui.java:48-52`).
- Desktop API About-handler registration is gated on `Desktop.isDesktopSupported()` and
  `Desktop.Action.APP_ABOUT` to avoid `UnsupportedOperationException` on Linux/Windows
  (`JdeployDesktopGui.java:71-75`).
- DB transactions roll back on `RuntimeException` to avoid corrupt state (`DatabaseService.java:55-58`).
