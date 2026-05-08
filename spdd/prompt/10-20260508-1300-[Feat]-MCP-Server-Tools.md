---
bootstrap: true
generated_at: 2026-05-08T13:00:00-07:00
---

# REASONS Canvas: MCP Server Tools

## R · Requirements
- When the application is launched with `jdeploy.mode != "gui"` and `--mcp` argument
  is present, it starts an MCP (Model Context Protocol) stdio server that exposes
  jDeploy as a set of tools to AI assistants.
- The server identifies itself as `serverInfo("jdeploy", "1.0.0")` and advertises
  `tools=true` capability.
- Tool registration is gated on the boolean preference `mcpToolsEnabled` (default
  `"true"`); if disabled, the server starts but exposes no tools.
- Four tools, intended to compose into the workflow
  `list_templates → new_project → build → publish_release`:
  1. **`setup_jdeploy`** — configure an existing Java project for jDeploy. Accepts
     `projectDirectory` (defaults to CWD) and `generateGithubWorkflow` (default
     false). Returns the setup-instructions Markdown plus a status message indicating
     what was generated.
  2. **`list_templates`** — return the project-template catalog as JSON
     (name, displayName, description, buildTool, programmingLanguage, uiToolkit,
     categories).
  3. **`new_project`** — create a new project from a template. Required:
     `parentDirectory`, `projectName`, `appTitle`, `templateName`, `groupId`,
     `artifactId`. Optional: `packageName`, `mainClassName`. Returns next-steps
     instructions including `gh` commands and (if MCP project detected)
     auto-registration notes.
  4. **`publish_release`** — generate (but do not execute) the shell commands to
     publish an existing project as a GitHub Release. Required: `projectDirectory`.
     Optional: `version` (defaults to `"v" + package.version`), `title`, `notes`.
- Setup instructions are loaded from
  `https://github.com/shannah/jdeploy-claude/raw/refs/heads/main/CLAUDE.md` with
  fallback to bundled `/mcp/setup-instructions.md`, then to a hard-coded default.
- An MCP project is detected by the presence of `jdeploy.ai.mcp` in `package.json`.
  When detected, both `new_project` and `publish_release` responses include a
  "MCP Server Auto-Registration" section listing supported AI tools.
- Definition of Done as it stands today: manual verification by registering the
  server with an MCP-aware client. No automated tests for the MCP tools. [INFERRED]

## E · Entities
- **`McpServer` / `McpSyncServer`** (from `io.modelcontextprotocol.server`) — the SDK
  server abstraction used in synchronous mode over stdio.
- **`McpServerFeatures.SyncToolSpecification`** — a `Tool` descriptor + handler.
- **`McpSchema.Tool`**, **`JsonSchema`**, **`CallToolResult`**, **`TextContent`** — MCP
  schema records used to declare and respond from tools.
- **`ProjectInitializer.Request` / `.Response`** (jDeploy CLI) —
  `setup_jdeploy`'s payload (see Import canvas).
- **`ProjectGenerator` / `ProjectGeneratorRequestBuilder`** — used for `new_project`.
- **`ProjectTemplateCatalog`** — used by `list_templates` and `new_project` to ensure
  the catalog is initialized before reads.

## A · Approach
- The MCP server runs on a single JVM and shares the DI container with the GUI: a
  fresh `JDeployDesktopGuiModule().install()` call at the start of `run()`
  initializes the same dependency graph (`JDeployMcpServer.java:69-74`).
- Transport is stdio via `StdioServerTransportProvider` with a Jackson JSON mapper.
- Tools are registered in a single batch (`addTool` on lines 91-94) only if the
  `mcpToolsEnabled` preference is `"true"`. Toggling at runtime requires restarting
  the server.
- Setup instructions are *runtime-dynamic*: the live URL on GitHub takes priority
  over bundled content so we can ship instruction changes without an app release.
  This is a deliberate design choice — keep the fallback chain
  (URL → resource → hard-coded) intact.
- All tool handlers wrap their logic in try/catch; on failure they return
  `CallToolResult.builder().isError(true).content("Error ...")` rather than throwing
  out of the server thread.
- Server keeps the process alive with `Thread.currentThread().join()` so the JVM
  stays up until the transport closes (line 99-101).

## S · Structure
- `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java` — entire server.
- `src/main/resources/mcp/setup-instructions.md` — fallback instructions content.
- `src/main/java/com/github/shannah/jdeploydesktopgui/JdeployDesktopGui.java:38-44` —
  CLI dispatch into `JDeployMcpServer.run()`.
- `src/main/java/ca/weblite/jdeploy/app/controllers/MainMenuViewController.kt:117-127` —
  the GUI checkbox that toggles `mcpToolsEnabled`.

## O · Operations

### 1. Run the Server — `JDeployMcpServer.run`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: bootstrap DI, build the MCP server, conditionally register tools,
   and block until shutdown.
2. Constants:
   - `SERVER_NAME = "jdeploy"`, `SERVER_VERSION = "1.0.0"`,
     `MCP_TOOLS_ENABLED_KEY = "mcpToolsEnabled"` (lines 47-49).
3. Static fields:
   - `projectInitializer`, `projectGenerator`, `templateCatalog`, `templateRepository`
     (lines 60-63) — resolved from DI in `run()`.
4. Methods:
   - `run(): void` (line 68-106)
     - Logic: install DI module; resolve `ProjectInitializer`, `ProjectGenerator`,
       `ProjectTemplateCatalog`, `ProjectTemplateRepositoryInterface`; build
       Jackson-backed JSON mapper and stdio transport; build sync server with
       `tools(true)` capability; if `PreferencesInterface.get("mcpToolsEnabled",
       "true") == "true"`, register the four tools; block on
       `Thread.currentThread().join()`; close server on interrupt.

### 2. Setup Tool — `createSetupTool` / `handleSetupTool`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: configure an existing Java project for jDeploy by calling
   `ProjectInitializer.decorate`; always return the latest setup instructions.
2. Schema:
   - `projectDirectory: string` (default CWD), `generateGithubWorkflow: boolean`
     (default false). Both optional. Additional properties: false (lines 110-128).
3. Methods:
   - `createSetupTool(): SyncToolSpecification` (line 108-151) — defines tool name
     `"setup_jdeploy"` and the description string promoting the
     `setup_jdeploy → build → publish_release` workflow.
   - `handleSetupTool(arguments): CallToolResult` (line 153-216)
     - Logic:
       1. Read `projectDirectory` (default `System.getProperty("user.dir")`) and
          `generateGithubWorkflow` (boolean coercion).
       2. Always pre-load setup instructions via `loadSetupInstructions()` (line 167).
       3. Try `projectInitializer.decorate(...)`; on success append a list of
          generated files (`package.json`, optional `.github/workflows/jdeploy.yml`)
          (lines 172-197).
       4. On exception, append a "Note: Project at <path> already has a jDeploy
          configuration" message — assumes failure means already-configured
          (lines 199-207).
       5. Append the instructions and return `isError=false`.

### 3. Setup Instructions Loader — `loadSetupInstructions`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: provide the latest setup instructions Markdown.
2. Constants:
   - `REMOTE_INSTRUCTIONS_URL =
     "https://github.com/shannah/jdeploy-claude/raw/refs/heads/main/CLAUDE.md"` (line 218-219).
3. Methods:
   - `loadSetupInstructions(): String` (line 221-239)
     - Logic: try `downloadRemoteInstructions()` first; else read
       `/mcp/setup-instructions.md` resource; else return `getDefaultInstructions()`.
   - `downloadRemoteInstructions(): String` (line 241-261)
     - Logic: open `HttpURLConnection` with 5s connect/read timeouts; follow redirects;
       return body if HTTP 200; on any exception return null.
   - `getDefaultInstructions(): String` (line 525-535) — five-step text block.

### 4. List Templates Tool — `createListTemplatesTool` / `handleListTemplates`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: return all templates from the catalog as JSON.
2. Schema:
   - Empty object, `additionalProperties=false` (line 266-268).
3. Methods:
   - `createListTemplatesTool(): SyncToolSpecification` (line 265-290) — name
     `"list_templates"`; description references the
     `list_templates → new_project → build → publish_release` workflow.
   - `handleListTemplates(): CallToolResult` (line 292-323)
     - Logic: if `templateCatalog.isCatalogInitialized()` is false, call
       `templateCatalog.update()`; run blocking coroutine call to
       `templateRepository.findAll(continuation)` via
       `kotlinx.coroutines.BuildersKt.runBlocking`; serialize each `Template` to a
       map of `name`, `displayName`, `description`, `buildTool`, `programmingLanguage`,
       `uiToolkit`, `categories`; pretty-print as JSON; return as text content.

### 5. New Project Tool — `createNewProjectTool` / `handleNewProject` / `detectMcpProject`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: generate a new project via `ProjectGenerator` and return next-step
   instructions.
2. Schema (lines 328-369):
   - Required: `parentDirectory`, `projectName`, `appTitle`, `templateName`,
     `groupId`, `artifactId`.
   - Optional: `packageName`, `mainClassName`.
3. Methods:
   - `createNewProjectTool(): SyncToolSpecification` (line 327-394) — name `"new_project"`.
   - `handleNewProject(arguments): CallToolResult` (line 396-499)
     - Logic:
       1. Read all required and optional args (lines 397-405).
       2. Initialize template catalog if needed.
       3. Build `ProjectGeneratorRequestBuilder`, set fields, optionally set
          `packageName`/`mainClassName`; call `projectGenerator.generate(builder.build())`
          to get `projectDir` (lines 411-426).
       4. Detect Maven (`pom.xml`) vs Gradle, presence of wrapper, derive a
          `buildCommand` (lines 429-435).
       5. Detect MCP project (`detectMcpProject(projectDir)`).
       6. Build a Markdown next-steps block: build command, `gh auth refresh -h
          github.com -s workflow`, `git init/add/commit`, `gh repo create`,
          `gh release create v1.0.0`. Include a private-repository note about
          `JDEPLOY_RELEASES_TOKEN`. Append "How Users Install This App" section.
          If `isMcpProject`, append the auto-registration tool list (Claude Desktop,
          Claude Code, VS Code, Cursor, Windsurf, Gemini CLI, Codex CLI, OpenCode,
          etc.) (lines 440-493).
   - `detectMcpProject(File): boolean` (private, line 505-523)
     - Logic: read `package.json`; return true iff `jdeploy.ai.mcp` exists.

### 6. Publish Release Tool — `createPublishReleaseTool` / `handlePublishRelease`
File: `src/main/java/ca/weblite/jdeploy/app/mcp/JDeployMcpServer.java`

1. Responsibility: inspect a project and return a step-by-step Markdown command list
   to publish a GitHub Release. Does NOT execute commands.
2. Schema:
   - Required: `projectDirectory`. Optional: `version`, `title`, `notes` (lines 540-565).
3. Methods:
   - `createPublishReleaseTool(): SyncToolSpecification` (line 539-591) — name
     `"publish_release"`.
   - `handlePublishRelease(arguments): CallToolResult` (line 593-796)
     - Logic:
       1. Validate `package.json` exists (lines 598-606).
       2. Validate `jdeploy` section exists (lines 612-620).
       3. Compute `version` (`v` prefix forced); `title` (defaults to version);
          `notes` (defaults to `"Release <version>"`) (lines 622-639).
       4. Resolve `jarPath`, `jarExists`, `repository` (from `package.json`'s
          `repository` or `jdeploy.github.repository`, with `https://github.com/`
          stripped) (lines 642-657).
       5. Detect Maven vs Gradle; derive `buildCommand` from `jdeploy.buildCommand`
          (string or array) or default by build tool (lines 659-683).
       6. Check `.git` directory; check MCP project (lines 686-687).
       7. Build a Markdown response: `cd` block, then numbered steps:
          - **Build the project** (always, with warning if jar path absent).
          - **Initialize git repository** OR **Commit any pending changes**
            (depending on `.git` presence).
          - **Create GitHub repository and push** OR **Push to remote**.
          - **Create GitHub Release** (`gh release create ...`).
          - "What Happens Next" section listing the GH Action steps and links to
            `actions/` and `releases/tag/<version>` if repo known.
          - "How Users Install This App" + MCP auto-registration block when
            applicable (lines 689-790).
       8. Return `isError=false`.

## N · Norms
- The MCP server installs a *new* DI module per process via
  `new JDeployDesktopGuiModule().install()` (line 70). Don't share state across the
  GUI and MCP processes — each is independent.
- All tool descriptions explicitly call out the
  `list_templates → new_project → build → publish_release` workflow so client LLMs
  can sequence calls correctly. Keep the description text in sync across tools when
  changing the workflow.
- The remote instructions URL is intentionally a `raw.githubusercontent.com`-style
  permalink to `main`. Don't embed a commit hash there — instructions should track
  `main` so updates ship without an app release.
- `publish_release` is *intentionally* a planner, not an executor. It returns
  commands, never runs them. Keep it that way: the MCP client (the LLM) is the
  agent that runs commands.

## S · Safeguards
- Tool registration is preference-gated; an admin-style toggle is available in the
  GUI (`MainMenuViewController.kt:117-127`).
- Each tool handler wraps its body in try/catch and returns
  `CallToolResult.isError(true)` on exception so the server thread never escapes
  with an unhandled error (e.g. lines 142-149, 282-288, 384-392, 580-590).
- HTTP fetch of remote instructions has 5-second connect and read timeouts and is
  fail-soft to bundled content (`downloadRemoteInstructions:241-261`).
- `setup_jdeploy` treats `ProjectInitializer.decorate` failure as "already
  configured" (lines 199-207). This is a deliberate, lenient interpretation — do
  not change to a hard error without verifying that the underlying initializer
  always throws on conflict, not on transient I/O errors.
- `publish_release` validates the project state before producing commands; if
  `package.json` or the `jdeploy` section is missing, it returns a focused error
  message pointing at `setup_jdeploy` (lines 598-620).
- Build command extraction handles both string and array `buildCommand` values
  (lines 668-678) — preserves shell-style joining without invoking a shell.
