---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Project Persistence and Validation

## R · Requirements
- Each project the user opens is persisted in the SQLite database. Records carry:
  - Stable `UUID` (also written to a `.jdeploy/uuid` file in the project directory).
  - `name` (from `package.json`'s `name`), `path` (absolute filesystem path),
    `lastOpened` (epoch seconds), optional `npmAccount` and `gitHubAccount` FKs.
- Projects are returned in `findRecent()` ordered by `lastOpened` (ascending in
  current code — see [DRIFT] note below).
- A directory is considered a valid jDeploy project at `MeetsMinimumRequirements`
  level only when its `package.json` has the keys `name`, `version`, and `jdeploy`.
- Persisting a project also persists its embedded `npmAccount` and `gitHubAccount`
  if present.
- A project's UUID never changes once written: its identity is the UUID file on disk
  (or, if missing, a freshly generated UUID written to disk on next access).
- Deleting a project from the DB is supported via `deleteOne` (used internally by
  `ProjectService.addProjectAtPath` to reconcile UUID divergence; not exposed as a
  user gesture in the GUI today).
- Definition of Done as it stands today: covered by `JpaIntegrationTest.kt`,
  `ProjectServiceTest.kt`, `LoadProjectTest.kt`, `TouchProjectTest.kt`. Tests use H2
  in-memory via `TestAppConfig`/`TestEmfProvider`/`TestJDeployDesktopGuiModule`.

## E · Entities

- **`Project`** record
  (`src/main/java/ca/weblite/jdeploy/app/records/Project.kt:6-17`) — domain DTO with:
  - `name: String`, `path: String` (mutable), `uuid: UUID?`, `lastOpened: Long`
    (defaults to `now()/1000`), optional `npmAccount`, `gitHubAccount`.
  - `entity: Any?` (line 13) — opaque back-reference to the source `ProjectEntity`
    used by `ProjectEntityFactory.extractOne` to round-trip the same JPA-managed
    instance back into the persistence context. Records loaded from the DB carry
    a non-null `entity`; records built fresh from the filesystem do not.
  - Computed property `packageJsonPath: String` returning `path + File.separator
    + "package.json"` (lines 15-16).

- **`ProjectEntity`**
  (`src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/ProjectEntity.kt:8-54`)
  — table `projects`. Fields:
    - `id: UUID?` — PK, `updatable=false`, `nullable=false`, `columnDefinition="TEXT"` (lines 10-12).
    - `name: String` — non-null (line 14-15).
    - `path: String` — non-null (line 17-18).
    - `lastOpened: Long` — non-null, defaults to `System.currentTimeMillis()/1000`
      (line 20-21).
    - `npmAccount: NpmAccountEntity?` — `@ManyToOne` FK on column `npm_account_id`,
      nullable (line 23-25).
    - `gitHubAccount: GitHubAccountEntity?` — `@ManyToOne` FK on column
      `github_account_id`, nullable (line 27-29).

- **`ProjectSet`** (`src/main/java/ca/weblite/jdeploy/app/collections/ProjectSet.java:8-19`)
  — `Iterable<Project>` backed by a `LinkedHashSet`, so iteration order is the
  insertion order from the varargs constructor.

- **`ProjectValidator.ValidationLevel`**
  (`src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java:24-28`):
  ordinal-ordered enum.

- **Schema**:
  - `db/migration/V3__Create_projects.sql:3-9` creates `projects(id TEXT PK, name NOT NULL,
    path NOT NULL, npm_account_id, gitHub_account_id)`.
  - `db/migration/V4__Add_last_opened.sql:2` adds `last_opened INTEGER NOT NULL DEFAULT 0`.

## A · Approach
- The repository (`JpaProjectRepository`) is the only path to the DB. Higher-level
  consumers go through `ProjectService`, which orchestrates filesystem and DB
  consistency.
- A project's identity is the UUID file at `<path>/.jdeploy/uuid`. `ProjectFactory`
  reads it (or generates a new UUID) when constructing a `Project` from the
  filesystem, and `ProjectService.saveOne`/`addProjectAtPath` writes it back when it
  is missing.
- Reconciliation on UUID divergence (filesystem UUID ≠ DB UUID at the same path):
  prefer the filesystem record. Look up by FS UUID; if absent in DB, delete the
  conflicting DB row and save the FS project (`ProjectService.kt:70-77`). This
  handles the case where a project directory was copied/restored.
- `package.json` parsing goes through `PackageJsonService` (a thin wrapper over
  `org.json.JSONObject`).
- The `Project.entity` back-reference exists so that updates from the GUI (where the
  EM may close between reads and writes) can re-attach the same managed entity on
  merge instead of inserting a new row. `ProjectEntityFactory.extractOrCreate`
  preserves the existing entity if present and only `createOne`s a fresh entity for
  records that came from the filesystem (e.g. via `ProjectService.addProjectAtPath`).

## S · Structure

- `src/main/java/ca/weblite/jdeploy/app/records/Project.kt` — record carrying name,
  path, uuid, lastOpened, optional accounts, and the `entity` back-reference.
- `src/main/java/ca/weblite/jdeploy/app/services/ProjectService.kt`
- `src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java`
- `src/main/java/ca/weblite/jdeploy/app/services/PackageJsonService.java`
- `src/main/java/ca/weblite/jdeploy/app/factories/ProjectFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectRepositoryInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaProjectRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/ProjectEntity.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/ProjectFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/ProjectEntityFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/collections/ProjectSet.java`
- `src/main/resources/db/migration/V3__Create_projects.sql`
- `src/main/resources/db/migration/V4__Add_last_opened.sql`

## O · Operations

### 1. Project Entity — `ProjectEntity`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/entities/ProjectEntity.kt`

1. Responsibility: JPA mapping for `projects`.
2. Fields: see Entities section.
3. Constructors:
   - No-arg (Hibernate-only) — initialises `name`/`path` to empty strings (lines 32-35).
   - Full — `(id, name, path, npmAccount, gitHubAccount, lastOpened)` (lines 37-51).
4. Constraints:
   - `last_opened` defaults to `now()` for newly-constructed entities (line 21);
     callers that want to preserve the original value (e.g. `JpaProjectRepository.saveOne`)
     re-assign it before merging (line 48).

### 2. JPA Project Repository — `JpaProjectRepository`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/repositories/JpaProjectRepository.kt`

1. Responsibility: synchronous CRUD over `ProjectEntity`. Singleton (`@Singleton`).
2. Methods:
   - `findOneById(id: UUID): Project` (line 23-33)
     - Logic: `executeInTransaction { em.createQuery("FROM ProjectEntity ps WHERE ps.id
       = :id").setParameter("id", id).resultList.firstOrNull() }`. Throw
       `NotFoundException("Project not found")` if null. Map via
       `ProjectFactory.createOne(entity)`.
   - `findRecent(): ProjectSet` (line 35-42)
     - Logic: `FROM ProjectEntity ps ORDER BY ps.lastOpened`. Pass list through
       `ProjectFactory.createCollection(entities)`.
   - `saveOne(project: Project): Project` (line 43-61)
     - Logic: open a transaction directly on `databaseService.entityManager` (not
       through `executeInTransaction`); convert via
       `projectEntityFactory.extractOrCreate(project)`; copy `lastOpened` from the
       record (line 48); assert `entity.id != null` before merge (line 50); call
       `em.merge(entity)`; commit. Assert id stable (line 53). Map back to record;
       assert UUID stable (line 55). On any exception, roll back and rethrow.
   - `deleteOne(project: Project): Boolean` (line 63-75)
     - Logic: open transaction; call `projectEntityFactory.extractOne(project)`;
       `em.remove(entity)`; commit; return true. Roll back and rethrow on exception.
   - `findOnebyPath(path: String): Project` (line 77-87)
     - Logic: `FROM ProjectEntity ps WHERE ps.path = :path` first result; throw
       `NotFoundException` if absent. Map via factory.

### 3. Project Service — `ProjectService`
File: `src/main/java/ca/weblite/jdeploy/app/services/ProjectService.kt`

1. Responsibility: orchestrate filesystem + DB to look up and persist projects.
   Singleton.
2. Fields (constructor): `projectRepository`, `npmAccountService`, `gitHubAccountService`,
   `projectFactory`, `fileSystem`, `clock` (lines 17-23).
3. Methods:
   - `findOneById(id): Project` (line 25-27) — delegate.
   - `findRecent(): ProjectSet` (line 29-31) — delegate.
   - `touch(project): Project` (line 33-36)
     - Logic: set `project.lastOpened = clock.now().timeInMillis / 1000`; persist via
       `projectRepository.saveOne(project)`.
   - `loadProject(path): Project` (line 38-42, throws `NotFoundException`,
     `InvalidProjectException`)
     - Logic: call `addProjectAtPath(path)`; return `projectRepository.findOnebyPath(path)`.
   - `saveOne(project): Project` (line 44-55)
     - Logic: if `project.npmAccount` non-null, save via `npmAccountService.saveOne`
       and reassign; same for `gitHubAccount`. If `<path>/.jdeploy/uuid` doesn't
       exist, write it. Persist via `projectRepository.saveOne`.
   - `addProjectAtPath(path)` (private, line 57-78)
     - Logic: build FS-derived `Project` via `projectFactory.createOne(path)`. Look
       up by path; if `NotFoundException`, save the FS project. Ensure UUID file
       exists. If FS UUID and DB UUID differ, attempt `findOneById(fsUuid)` — if
       absent, delete the DB record and save the FS project (UUID divergence
       reconciliation).
   - `idFileExists(path): Boolean` (line 80-82) — check `<path>/.jdeploy/uuid`.
   - `writeIdToFile(project): Unit` (line 84-91) — ensure `.jdeploy` directory; write
     UUID string in UTF-8.

### 4. Project Validator — `ProjectValidator`
File: `src/main/java/ca/weblite/jdeploy/app/services/ProjectValidator.java`

1. Responsibility: classify a path as a valid jDeploy project at increasing levels of
   strictness. Singleton.
2. Constants:
   - `ValidationLevel { DirectoryExists, HasPackageJson, MeetsMinimumRequirements }`
     (lines 24-28).
3. Methods:
   - `isValidProject(String, ValidationLevel): boolean` (line 29-37) — wrap `validate`,
     translate `InvalidProjectException` to false.
   - `validate(String path, ValidationLevel level): void` throws
     `InvalidProjectException` (line 39-66)
     - Logic:
       1. `fileSystem.isDirectory(path)` else throw `"The path is not a directory"` (lines 40-42).
       2. If level ≥ `HasPackageJson`, `fileSystem.exists(path/package.json)` else throw
          `"The project does not contain a package.json file"` (lines 44-48).
       3. If level ≥ `MeetsMinimumRequirements`, parse via `packageJsonService.readOne`;
          require `name`, `version`, `jdeploy` keys; on parse failure throw
          `"The package.json file is invalid. Reason: ..."` carrying the cause
          (lines 50-65).

### 5. Project Factory (filesystem) — `ProjectFactory`
File: `src/main/java/ca/weblite/jdeploy/app/factories/ProjectFactory.kt`

1. Responsibility: build a `Project` record from a directory path, validating it and
   reading the on-disk UUID. Singleton.
2. Methods:
   - `createOne(projectPath): Project` (line 20-22, throws `InvalidProjectException`)
     - Delegate to `createOneFromProjectPath`.
   - `createOneFromProjectPath(projectPath)` (line 33-38)
     - Logic: validate at `MeetsMinimumRequirements`; read `package.json`; build
       record.
   - `createOneFromPackageJson(projectPath, packageJson)` (line 24-31)
     - Logic: read UUID file or `UUID.randomUUID()`; return
       `Project(packageJson["name"], projectPath, uuid)`.
   - `readUuidFromFileSystem(projectPath): UUID?` (line 40-47)
     - Logic: if `<path>/.jdeploy/uuid` doesn't exist, null; else parse contents.

### 6. Project Mapper Factories (JPA) — `ProjectFactory` (JPA) / `ProjectEntityFactory`
Files:
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/ProjectFactory.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/impl/jpa/factories/ProjectEntityFactory.kt`

1. Responsibility: translate between `ProjectEntity` and `Project` records, including
   nested account mapping. Both `@Singleton`.
2. Methods (`ProjectFactory`):
   - `createOne(entity: ProjectEntity): Project` (lines 15-25)
     - Logic: build a new `Project(name=entity.name, path=entity.path, uuid=entity.id,
       lastOpened=entity.lastOpened, npmAccount=npmAccountFactory.createOne(entity.npmAccount)?,
       gitHubAccount=gitHubAccountFactory.createOne(entity.gitHubAccount)?,
       entity=entity)`. The `entity = entity` argument (line 23) is the round-trip
       hook that lets `extractOne` later recover the JPA-managed instance.
   - `createCollection(entities: List<ProjectEntity>): ProjectSet` (lines 27-29)
     - Logic: `ProjectSet(*entities.map { createOne(it) }.toTypedArray())` —
       splats the mapped list into the varargs constructor; `LinkedHashSet`
       semantics preserve order.
3. Methods (`ProjectEntityFactory`):
   - `createOne(project: Project): ProjectEntity` (lines 13-26)
     - Logic: build a fresh `ProjectEntity(id=project.uuid, name, path,
       lastOpened, npmAccount=npmAccountFactory.extractOrCreate(it)?,
       gitHubAccount=gitHubAccountFactory.extractOrCreate(it)?)`. Does NOT preserve
       any existing entity reference — only used when the record has no `entity`
       set (e.g. came from the filesystem).
   - `extractOne(project: Project): ProjectEntity` (lines 28-30)
     - Logic: `project.entity as ProjectEntity` — straight cast. Throws
       `ClassCastException`/`NullPointerException` if `entity` is null. Callers
       must use `extractOrCreate` unless they know `entity` is set.
   - `extractOrCreate(project: Project): ProjectEntity` (lines 32-38)
     - Logic: if `project.entity == null`, return `createOne(project)`; else
       `extractOne(project)`.

### 7. Read package.json — `PackageJsonService`
File: `src/main/java/ca/weblite/jdeploy/app/services/PackageJsonService.java`

1. Responsibility: read a `package.json` file from disk and return it as a parsed
   `org.json.JSONObject`. Singleton.
2. Fields:
   - `fileSystem: FileSystemInterface` — injected (lines 17-22).
3. Methods:
   - `readOne(String path): JSONObject` throws `IOException` (lines 23-27)
     - Logic: open an `InputStream` via `fileSystem.openInputStream(path)`; read
       to UTF-8 string via `IOUtils.toString`; construct `new JSONObject(...)`. The
       try-with-resources block ensures the stream closes on both success and
       parse failure.
4. Constraints:
   - Caller is responsible for handling `IOException` (file missing) and the
     `org.json.JSONException` raised by `JSONObject(...)` on malformed JSON.
     `ProjectValidator.validate` wraps both into `InvalidProjectException`
     (`ProjectValidator.java:62-64`).

### 8. Project Repository Contract — `ProjectRepositoryInterface`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectRepositoryInterface.kt`

1. Responsibility: declare the persistence contract that `JpaProjectRepository`
   implements; pin which operations may throw `NotFoundException`.
2. Methods (lines 8-20):
   - `findOneById(id: UUID): Project` — `@Throws(NotFoundException)`.
   - `findRecent(): ProjectSet`.
   - `findOnebyPath(path: String): Project` — `@Throws(NotFoundException)`.
   - `saveOne(project: Project): Project`.
   - `deleteOne(project: Project): Boolean`.

### 9. Project Collection — `ProjectSet`
File: `src/main/java/ca/weblite/jdeploy/app/collections/ProjectSet.java`

1. Responsibility: typed `Iterable<Project>` with insertion-order iteration,
   accepting a varargs constructor.
2. Fields:
   - `projects: LinkedHashSet<Project>` (line 9).
3. Methods:
   - `ProjectSet(Project... projects)` (lines 11-13)
     - Logic: `Arrays.asList(projects)` then `addAll` into the `LinkedHashSet`.
       Duplicates (by `Project.equals`) are silently dropped — `Project` does not
       override `equals`, so identity equality applies. [INFERRED]
   - `iterator(): Iterator<Project>` (lines 15-18) — delegates to the set.

## N · Norms
- Database access goes through `DatabaseService.executeInTransaction { ... }`. Direct
  use of `entityManager.transaction` is the exception, not the rule (used in
  `JpaProjectRepository.saveOne`/`deleteOne` to allow finer-grained assert/rollback).
- Project paths are stored verbatim. No normalization or symlink resolution. If two
  paths point to the same project on disk via different strings, they get separate
  rows. [INFERRED]
- `lastOpened` is in epoch *seconds* (not millis), set via `clock.now().timeInMillis / 1000`
  (`ProjectService.kt:34`). Don't change the unit — the column is named `last_opened
  INTEGER` and divisions/comparisons assume seconds.
- `[DRIFT]` `JpaProjectRepository.findRecent()` orders by `lastOpened` ascending —
  most recent should usually be first (`ORDER BY ... DESC`). Verify behavior before
  fixing; the current sort may be relied on by `MainMenuViewController`'s rendering.
- The V3 schema declares the GitHub-account FK column as `gitHub_account_id`
  (mixed case, `V3__Create_projects.sql:8`) while `ProjectEntity` declares
  `@JoinColumn(name = "github_account_id")` (lowercase,
  `ProjectEntity.kt:28`). SQLite resolves unquoted identifiers case-insensitively
  so this works in practice on SQLite. **Porting hazard:** moving the schema to a
  case-sensitive engine (Postgres-with-quoted-identifiers, MySQL with
  `lower_case_table_names=0` on Linux) will break the join. If you migrate engines,
  fix the column name in a new Flyway migration *before* the move.

## S · Safeguards
- `JpaProjectRepository.saveOne` asserts `entity.id != null` before merge to catch
  cases where a record is constructed without an ID (line 50).
- The same method also asserts post-merge that `entity.id` and the round-tripped
  record's UUID didn't change (lines 53, 55) — guards against accidental ID
  regeneration.
- UUID file write is preceded by mkdir of `.jdeploy` to avoid `FileNotFoundException`
  (`ProjectService.kt:85-87`).
- UUID divergence reconciliation deletes the conflicting DB row only when
  `findOneById(fsUuid)` raises `NotFoundException`, so we never delete a row that
  belongs to a different project at a different path (`ProjectService.kt:70-77`).
- Validation messages always include the offending path (`InvalidProjectException`
  `path` field) so callers/`ErrorController` can show actionable errors.
- `ProjectEntityFactory.extractOne` is a straight cast of `project.entity as
  ProjectEntity` and will throw `ClassCastException`/`NullPointerException` if the
  record was built from the filesystem (no entity reference). Callers should always
  use `extractOrCreate` unless they have a record they know was loaded from the DB.
  `JpaProjectRepository.deleteOne` uses `extractOne` directly — only safe because
  the only call path is `ProjectService.addProjectAtPath` line 74, where the
  `dbProject` argument was just returned from `findOnebyPath` and therefore carries
  its `entity`.
