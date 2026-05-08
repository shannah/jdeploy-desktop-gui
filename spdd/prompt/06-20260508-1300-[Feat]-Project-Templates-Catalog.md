---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: Project Templates Catalog

## R · Requirements
- The application sources project templates from
  `https://raw.githubusercontent.com/shannah/jdeploy-project-templates/master/projects.xml`
  (`DefaultProjectTemplateRepository.kt:20`).
- Templates have `displayName`, `name`, `uiToolkit`, `categories`, `screenshots`,
  `screencasts`, `tileImageUrl` (with default fallback), `iconUrl`, optional
  `demoDownloadUrl`, optional `webAppUrl`, `author`, `license`, `credits`,
  `description`, `buildTool`, `programmingLanguage`, optional `sourceUrl`.
- Resolution order:
  1. If the on-disk catalog index file (managed by jDeploy CLI's
     `ProjectTemplateCatalog`) exists, parse it (`XMLProjectTemplateRepository`).
  2. Otherwise fetch the URL through `FileSystemCache` and parse
     (`URLXMLProjectTemplateRepository`).
- The cache stays in a platform-specific directory:
  - macOS: `~/Library/Caches/jdeploy`
  - Windows: `%LOCALAPPDATA%/jdeploy/cache`
  - Linux: `$XDG_CACHE_HOME/jdeploy` or `~/.cache/jdeploy`
  Cache file names are MD5 hashes of `url + variant` (`FileSystemCache.kt:69-78`).
- On application startup the URL cache is purged
  (`JdeployDesktopGui.java:65 → DefaultProjectTemplateRepository.clearCacheBlocking()`).
- The MCP server's `list_templates` tool exposes the same catalog over MCP
  (`JDeployMcpServer.java:265-323`).
- The new-project wizard refreshes the catalog at most once per hour on show
  (`NewProjectController.kt:542-551`); the user can also force a refresh from a
  Refresh button in the wizard.
- Definition of Done as it stands today: covered by
  `XMLProjectTemplateRepositoryTest.kt` and `URLXMLProjectTemplateRepositoryTest.kt`
  (parse + load).

## E · Entities
- **`Template`** (`ca.weblite.jdeploy.app.records.Template`) — the parsed template; a
  default tile image URL constant (`Template.DEFAULT_TILE_IMAGE_URL`,
  `ProjectTemplateXMLParser.kt:18`) is substituted when XML omits one.
- **`ProjectTemplates`** (`ca.weblite.jdeploy.app.records.ProjectTemplates`) — wraps a
  `List<Template>` (`ProjectTemplateXMLParser.kt:45`).
- **`Screenshot`**, **`Screencast`** — value objects with a single `url` (parser
  lines 26-29).
- The XML namespace `http://jdeploy.com/project-templates`
  (`ProjectTemplateXMLParser.kt:8`) is required on every recognized element.

## A · Approach
- Two `ProjectTemplateRepositoryInterface` implementations differ only in their input
  source (`File` vs `URL`); both share `ProjectTemplateXMLParser` for namespace-aware
  XML traversal.
- `DefaultProjectTemplateRepository` is the DI-bound interface impl and lazily delegates
  to the file-or-URL variant based on whether the on-disk index exists.
- `FileSystemCache` is a generic URL fetch-and-cache used both here and by `ImageLoader`.
  The cache key is an MD5 of `url + variant` so different image sizes coexist.
- The on-disk catalog index file is owned by `ProjectTemplateCatalog` (jDeploy CLI;
  outside this repo) — `update()` populates it, `isCatalogInitialized` reflects whether
  it exists.

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectTemplateRepositoryInterface.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/DefaultProjectTemplateRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/URLXMLProjectTemplateRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/XMLProjectTemplateRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/MockProjectTemplateRepository.kt`
- `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectTemplateXMLParser.kt`
- `src/main/java/ca/weblite/jdeploy/app/cache/FileSystemCache.kt`
- `src/main/java/ca/weblite/jdeploy/app/controllers/UpdateProjectTemplatesController.kt`
- `src/main/java/ca/weblite/jdeploy/app/images/ImageLoader.kt` — uses cache for tile/icon images.
- `src/main/resources/ca/weblite/jdeploy/app/assets/mock-project-templates.xml` — mock fixture for tests.

## O · Operations

### 1. Repository Interface — `ProjectTemplateRepositoryInterface`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectTemplateRepositoryInterface.kt`

1. Responsibility: single-method async repository contract.
2. Methods:
   - `suspend fun findAll(): ProjectTemplates`.

### 2. Routing Repository — `DefaultProjectTemplateRepository`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/DefaultProjectTemplateRepository.kt`

1. Responsibility: Singleton (`@Singleton`); choose between local-file and URL-based
   delegate; expose cache management.
2. Fields:
   - `url: URL` — the master template list URL (line 20).
   - `urlDelegate: URLXMLProjectTemplateRepository` (lazy, lines 22-27) — uses
     `FileSystemCache` keyed by `url`.
   - `fileDelegate: XMLProjectTemplateRepository` (lazy, lines 29-33) — uses
     `projectTemplateCatalog.projectsIndexFile`.
3. Methods:
   - `findAll(): ProjectTemplates` (suspend, line 35-41) — `withContext(Dispatchers.IO)`;
     if `projectsIndexFile.exists()` use `fileDelegate`, else `urlDelegate`.
   - `updateCatalog(): Unit` (suspend, line 43-47) — `withContext(Dispatchers.IO)`,
     call `projectTemplateCatalog.update()`.
   - `clearCache(): Unit` (suspend, line 49-51) — `fileSystemCache.purge(url, "")`.
   - `clearCacheBlocking(): Unit` (line 53-57) — `runBlocking { clearCache() }`.

### 3. URL Repository — `URLXMLProjectTemplateRepository`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/URLXMLProjectTemplateRepository.kt`

1. Responsibility: fetch the XML over HTTP (optionally through `FileSystemCache`) and
   parse it.
2. Methods:
   - `findAll(): ProjectTemplates` (suspend, line 15-24)
     - Logic: build a namespace-aware `DocumentBuilder`; if `fileSystemCache` is
       null, parse `url.openStream()` directly; otherwise parse
       `fileSystemCache.load(url, "")`. Delegate to `ProjectTemplateXMLParser.parse`.

### 4. Local File Repository — `XMLProjectTemplateRepository`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/XMLProjectTemplateRepository.kt`

1. Responsibility: parse a pre-fetched XML file from disk.
2. Methods:
   - `findAll(): ProjectTemplates` (suspend, line 13-18)
     - Logic: namespace-aware `DocumentBuilder` parses `xmlFile`; delegate to parser.

### 5. XML Parser — `ProjectTemplateXMLParser`
File: `src/main/java/ca/weblite/jdeploy/app/repositories/ProjectTemplateXMLParser.kt`

1. Responsibility: turn a parsed `Document` into `ProjectTemplates`. Uses the
   `http://jdeploy.com/project-templates` namespace exclusively.
2. Methods:
   - `parse(document: Document): ProjectTemplates` (line 10-46)
     - Logic: enumerate `<template>` elements via `getElementsByTagNameNS(NS,
       "template")`; per template build a `Template` with `tileImageUrl` defaulted to
       `Template.DEFAULT_TILE_IMAGE_URL` when blank; `demoDownloadUrl`, `webAppUrl`,
       and `sourceUrl` are nullable when blank; `categories` is a `List<String>` from
       repeated `<category>` elements; `screenshots`/`screencasts` come from nested
       `<screenshots><screenshot url=...>` / `<screencasts><screencast url=...>`
       structures.
   - `Element.getText`, `Element.getElements`, `Element.getChildElements` — private
     namespace-aware helpers (lines 49-63).

### 6. URL Cache — `FileSystemCache`
File: `src/main/java/ca/weblite/jdeploy/app/cache/FileSystemCache.kt`

1. Responsibility: download-once / read-many cache for arbitrary URLs, sharing a single
   per-platform cache directory.
2. Methods:
   - `load(url: URL, variant: String): InputStream` (suspend, line 22-44)
     - Logic: compute cache file via `getCacheFile(url, variant)`; if it exists, return
       its `inputStream()`; otherwise on `Dispatchers.IO` fetch `url.openStream()`,
       create parent dirs, copy to file; throw if file still missing afterwards;
       return its `inputStream()`.
   - `purge(url: URL, variant: String): Unit` (suspend, line 13-20)
     - Logic: if cache file exists, delete it on `Dispatchers.IO`.
   - `getCacheFile(url, variant): File` (line 46-50) — `File(getCacheDir(), getCacheKey(url, variant))`.
   - `getCacheDir(appName = "jdeploy"): File` (line 52-67) — platform dispatch on
     `os.name` lowercase: macOS `Library/Caches`, Windows `%LOCALAPPDATA%`, else
     `$XDG_CACHE_HOME`. Creates the directory if missing.
   - `getCacheKey(url, variant): String` (line 69-78) — MD5 of `url-with-`:`/`-replaced
     concatenated with `variant`, formatted as hex string.

### 7. Refresh Catalog with Progress — `UpdateProjectTemplatesController`
File: `src/main/java/ca/weblite/jdeploy/app/controllers/UpdateProjectTemplatesController.kt`

1. Responsibility: show a modal "Updating Templates" dialog while
   `ProjectTemplateCatalog.update()` runs off the EDT.
2. Methods:
   - `update(): Unit` (line 15-45)
     - Logic: build a modal indeterminate `JDialog`; launch a `SwingWorker` that calls
       `templateCatalog.update()`; in `done()` dispose the dialog. Show the dialog
       (modal blocks the EDT until `dispose`).
   - `updateSuspending(): Unit` (suspend, line 47-72)
     - Logic: on `SwingDispatcher` build the dialog; on `Dispatchers.IO` call
       `templateCatalog.update()`; on `SwingDispatcher` dispose the dialog.

## N · Norms
- All XML parsing is namespace-aware (`isNamespaceAware = true`,
  `URLXMLProjectTemplateRepository.kt:16`, `XMLProjectTemplateRepository.kt:14`). The
  parser ignores any element outside the `http://jdeploy.com/project-templates`
  namespace.
- Network-bound work always runs on `Dispatchers.IO`, never on the EDT.
- The cache directory is created lazily on first call to `getCacheDir`
  (`FileSystemCache.kt:62-64`); never assume it exists ahead of time.

## S · Safeguards
- Empty `tileImageUrl` is replaced by `Template.DEFAULT_TILE_IMAGE_URL` so the wizard
  always has an image to render (`ProjectTemplateXMLParser.kt:16-19`).
- `FileSystemCache.load` throws if the file is missing after the copy, surfacing
  network failures as exceptions rather than silently returning a 0-byte stream
  (`FileSystemCache.kt:39-41`).
- Cache key is MD5-hashed so `:` and `/` characters from the URL never reach the
  filesystem unencoded (`FileSystemCache.kt:71-77`).
- The refresh-throttle in the wizard (1 hour, `NewProjectController.kt:543-547`)
  prevents flooding GitHub with template-list requests on rapid wizard open/close.
