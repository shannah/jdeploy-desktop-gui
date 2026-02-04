package ca.weblite.jdeploy.app.mcp;

import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.app.records.ProjectTemplates;
import ca.weblite.jdeploy.app.records.Template;
import ca.weblite.jdeploy.app.repositories.ProjectTemplateRepositoryInterface;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;
import ca.weblite.jdeploy.builders.ProjectGeneratorRequestBuilder;
import ca.weblite.jdeploy.services.ProjectGenerator;
import ca.weblite.jdeploy.services.ProjectInitializer;
import ca.weblite.jdeploy.services.ProjectTemplateCatalog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) server for jDeploy.
 * Provides LLM integration for setting up jDeploy projects.
 */
public class JDeployMcpServer {

    private static final String SERVER_NAME = "jdeploy";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String MCP_TOOLS_ENABLED_KEY = "mcpToolsEnabled";

    private static final String TOOL_NAME = "setup_jdeploy";
    private static final String TOOL_DESCRIPTION =
        "Configure an existing Java project for jDeploy deployment. " +
        "Use this for projects NOT created via new_project. " +
        "This creates the necessary package.json configuration and optionally generates GitHub workflow files for CI/CD. " +
        "If the project is already configured, it returns setup instructions without modifying existing files. " +
        "After running this tool, follow the returned instructions to complete setup, then use publish_release to publish. " +
        "Workflow: setup_jdeploy -> build -> publish_release.";

    private static ProjectInitializer projectInitializer;
    private static ProjectGenerator projectGenerator;
    private static ProjectTemplateCatalog templateCatalog;
    private static ProjectTemplateRepositoryInterface templateRepository;

    /**
     * Run the MCP server using stdio transport.
     */
    public static void run() {
        // Initialize DI context to get ProjectInitializer with all dependencies
        new JDeployDesktopGuiModule().install();
        projectInitializer = DIContext.get(ProjectInitializer.class);
        projectGenerator = DIContext.get(ProjectGenerator.class);
        templateCatalog = DIContext.get(ProjectTemplateCatalog.class);
        templateRepository = DIContext.get(ProjectTemplateRepositoryInterface.class);

        JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new ObjectMapper());
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);

        McpSyncServer server = McpServer.sync(transportProvider)
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(ServerCapabilities.builder()
                .tools(true)
                .build())
            .build();

        // Register tools only if MCP tools are enabled in preferences
        PreferencesInterface preferences = DIContext.get(PreferencesInterface.class);
        boolean mcpToolsEnabled = "true".equals(preferences.get(MCP_TOOLS_ENABLED_KEY, "true"));

        if (mcpToolsEnabled) {
            server.addTool(createSetupTool());
            server.addTool(createListTemplatesTool());
            server.addTool(createNewProjectTool());
            server.addTool(createPublishReleaseTool());
        }

        // Keep the server running
        // The transport provider handles the stdio communication
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            server.close();
        }
    }

    private static McpServerFeatures.SyncToolSpecification createSetupTool() {
        // Create the JSON Schema for the tool input
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectDirectory", Map.of(
            "type", "string",
            "description", "Path to the project directory to initialize. Defaults to current working directory if not specified."
        ));
        properties.put("generateGithubWorkflow", Map.of(
            "type", "boolean",
            "description", "Whether to generate GitHub Actions workflow files for CI/CD. Defaults to false.",
            "default", false
        ));

        JsonSchema inputSchema = new JsonSchema(
            "object",      // type
            properties,    // properties
            List.of(),     // required - empty list since both are optional
            false,         // additionalProperties
            null,          // description
            null           // $ref
        );

        return new McpServerFeatures.SyncToolSpecification(
            new Tool(
                TOOL_NAME,
                TOOL_DESCRIPTION,
                null,          // title (nullable)
                inputSchema,   // inputSchema
                null,          // outputSchema (nullable)
                null,          // annotations (nullable)
                null           // extensions (nullable)
            ),
            (exchange, arguments) -> {
                try {
                    return handleSetupTool(arguments);
                } catch (Exception e) {
                    return CallToolResult.builder()
                        .content(List.of(new TextContent("Error initializing project: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            }
        );
    }

    private static CallToolResult handleSetupTool(Map<String, Object> arguments) {
        // Get project directory, defaulting to current working directory
        String projectDirectory = (String) arguments.getOrDefault(
            "projectDirectory",
            System.getProperty("user.dir")
        );

        // Get generateGithubWorkflow flag, defaulting to false
        Object workflowParam = arguments.getOrDefault("generateGithubWorkflow", false);
        Boolean generateGithubWorkflow = workflowParam instanceof Boolean
            ? (Boolean) workflowParam
            : Boolean.parseBoolean(String.valueOf(workflowParam));

        // Load setup instructions upfront -- we return these regardless of initialization outcome
        String instructions = loadSetupInstructions();

        StringBuilder resultMessage = new StringBuilder();

        try {
            ProjectInitializer.Response response = projectInitializer.decorate(
                new ProjectInitializer.Request(
                    projectDirectory,
                    null,    // npmToken
                    false,   // dryRun
                    generateGithubWorkflow,
                    null     // cheerpjSplash
                )
            );

            resultMessage.append(String.format(
                "Successfully initialized jDeploy project at: %s\n\n" +
                "Generated/updated files:\n" +
                "- package.json (jDeploy configuration)\n",
                projectDirectory
            ));

            if (response.generatedGithubWorkflow) {
                resultMessage.append("- .github/workflows/jdeploy.yml (CI/CD workflow)\n");
            } else if (response.githubWorkflowExists) {
                resultMessage.append("- .github/workflows/jdeploy.yml (already exists)\n");
            } else if (generateGithubWorkflow) {
                resultMessage.append("- Note: GitHub workflow was requested but could not be generated " +
                    "(this can happen when updating an existing package.json). " +
                    "See the GitHub Workflows section in the instructions below to create it manually.\n");
            }

        } catch (Exception e) {
            resultMessage.append(String.format(
                "Note: Project at %s already has a jDeploy configuration. " +
                "No files were modified.\n\n" +
                "If you need to adjust the configuration, you can edit package.json directly. " +
                "The setup instructions below describe the expected format.\n",
                projectDirectory
            ));
        }

        resultMessage.append("\n## Setup Instructions\n\n");
        resultMessage.append(instructions);

        return CallToolResult.builder()
            .content(List.of(new TextContent(resultMessage.toString())))
            .isError(false)
            .build();
    }

    private static final String REMOTE_INSTRUCTIONS_URL =
        "https://github.com/shannah/jdeploy-claude/raw/refs/heads/main/CLAUDE.md";

    private static String loadSetupInstructions() {
        // First, try to download the latest instructions from GitHub
        String remote = downloadRemoteInstructions();
        if (remote != null) {
            return remote;
        }

        // Fall back to bundled resource file
        try (InputStream is = JDeployMcpServer.class.getResourceAsStream("/mcp/setup-instructions.md")) {
            if (is == null) {
                return getDefaultInstructions();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            return getDefaultInstructions();
        }
    }

    private static String downloadRemoteInstructions() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(REMOTE_INSTRUCTIONS_URL)
                .toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (Exception e) {
            // Ignore - will fall back to bundled instructions
        }
        return null;
    }

    // ---- list_templates tool ----

    private static McpServerFeatures.SyncToolSpecification createListTemplatesTool() {
        JsonSchema inputSchema = new JsonSchema(
            "object", Map.of(), List.of(), false, null, null
        );

        return new McpServerFeatures.SyncToolSpecification(
            new Tool(
                "list_templates",
                "List available jDeploy project templates with their names, descriptions, build tools, " +
                    "languages, UI toolkits, and categories. Call this before new_project to see available options. " +
                    "Templates include pre-configured GitHub workflows for CI/CD publishing. " +
                    "Workflow: list_templates -> new_project -> build -> publish_release.",
                null, inputSchema, null, null, null
            ),
            (exchange, arguments) -> {
                try {
                    return handleListTemplates();
                } catch (Exception e) {
                    return CallToolResult.builder()
                        .content(List.of(new TextContent("Error listing templates: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            }
        );
    }

    private static CallToolResult handleListTemplates() throws Exception {
        if (!templateCatalog.isCatalogInitialized()) {
            templateCatalog.update();
        }

        ProjectTemplates projectTemplates = kotlinx.coroutines.BuildersKt.runBlocking(
            kotlin.coroutines.EmptyCoroutineContext.INSTANCE,
            (scope, continuation) -> templateRepository.findAll(continuation)
        );

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> templateList = new ArrayList<>();

        for (Template template : projectTemplates.getTemplates()) {
            Map<String, Object> t = new HashMap<>();
            t.put("name", template.getName());
            t.put("displayName", template.getDisplayName());
            t.put("description", template.getDescription());
            t.put("buildTool", template.getBuildTool());
            t.put("programmingLanguage", template.getProgrammingLanguage());
            t.put("uiToolkit", template.getUiToolkit());
            t.put("categories", template.getCategories());
            templateList.add(t);
        }

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateList);

        return CallToolResult.builder()
            .content(List.of(new TextContent(json)))
            .isError(false)
            .build();
    }

    // ---- new_project tool ----

    private static McpServerFeatures.SyncToolSpecification createNewProjectTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("parentDirectory", Map.of(
            "type", "string",
            "description", "Absolute path to the parent directory where the project folder will be created."
        ));
        properties.put("projectName", Map.of(
            "type", "string",
            "description", "Name of the project (used as directory name)."
        ));
        properties.put("appTitle", Map.of(
            "type", "string",
            "description", "Display title of the application."
        ));
        properties.put("templateName", Map.of(
            "type", "string",
            "description", "Name of the project template to use. Use list_templates to see available options."
        ));
        properties.put("groupId", Map.of(
            "type", "string",
            "description", "Maven group ID (e.g. com.example)."
        ));
        properties.put("artifactId", Map.of(
            "type", "string",
            "description", "Maven artifact ID."
        ));
        properties.put("githubRepository", Map.of(
            "type", "string",
            "description", "Optional GitHub repository in owner/repo format for CI/CD integration."
        ));
        properties.put("privateRepository", Map.of(
            "type", "boolean",
            "description", "Whether the GitHub repository is private. Defaults to false.",
            "default", false
        ));
        properties.put("packageName", Map.of(
            "type", "string",
            "description", "Java package name. Optional, derived from groupId and artifactId if not set."
        ));
        properties.put("mainClassName", Map.of(
            "type", "string",
            "description", "Fully qualified main class name. Optional."
        ));

        JsonSchema inputSchema = new JsonSchema(
            "object",
            properties,
            List.of("parentDirectory", "projectName", "appTitle", "templateName", "groupId", "artifactId"),
            false,
            null,
            null
        );

        return new McpServerFeatures.SyncToolSpecification(
            new Tool(
                "new_project",
                "Create a new jDeploy project from a template with pre-configured build system and " +
                    "GitHub Actions workflow for CI/CD. Use list_templates first to see available templates. " +
                    "After creation, build the project, then use publish_release to publish. " +
                    "The jDeploy workflow builds native installers for Windows, macOS, and Linux. " +
                    "For MCP server projects, the installer automatically registers the server with " +
                    "detected AI tools (Claude Desktop, Claude Code, VS Code, Cursor, Windsurf, etc.). " +
                    "Workflow: list_templates -> new_project -> build -> publish_release.",
                null, inputSchema, null, null, null
            ),
            (exchange, arguments) -> {
                try {
                    return handleNewProject(arguments);
                } catch (Exception e) {
                    return CallToolResult.builder()
                        .content(List.of(new TextContent("Error creating project: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            }
        );
    }

    private static CallToolResult handleNewProject(Map<String, Object> arguments) throws Exception {
        String parentDirectory = (String) arguments.get("parentDirectory");
        String projectName = (String) arguments.get("projectName");
        String appTitle = (String) arguments.get("appTitle");
        String templateName = (String) arguments.get("templateName");
        String groupId = (String) arguments.get("groupId");
        String artifactId = (String) arguments.get("artifactId");

        String githubRepository = (String) arguments.getOrDefault("githubRepository", null);
        Object privateRepoParam = arguments.getOrDefault("privateRepository", false);
        boolean privateRepository = privateRepoParam instanceof Boolean
            ? (Boolean) privateRepoParam
            : Boolean.parseBoolean(String.valueOf(privateRepoParam));
        String packageName = (String) arguments.getOrDefault("packageName", null);
        String mainClassName = (String) arguments.getOrDefault("mainClassName", null);

        if (!templateCatalog.isCatalogInitialized()) {
            templateCatalog.update();
        }

        ProjectGeneratorRequestBuilder builder = new ProjectGeneratorRequestBuilder();
        builder.setParentDirectory(new File(parentDirectory));
        builder.setProjectName(projectName);
        builder.setAppTitle(appTitle);
        builder.setTemplateName(templateName);
        builder.setGroupId(groupId);
        builder.setArtifactId(artifactId);

        if (githubRepository != null) {
            builder.setGithubRepository(githubRepository);
            builder.setPrivateRepository(privateRepository);
        }
        if (packageName != null) {
            builder.setPackageName(packageName);
        }
        if (mainClassName != null) {
            builder.setMainClassName(mainClassName);
        }

        // Attempt project generation. If GitHub operations fail (e.g. auth issues),
        // the project files may still have been created on disk. Handle gracefully.
        File projectDir;
        boolean githubOperationFailed = false;
        String githubErrorDetail = null;

        try {
            projectDir = projectGenerator.generate(builder.build());
        } catch (Exception e) {
            // Check if project files were created on disk despite the error
            File candidateDir = new File(parentDirectory, projectName);
            if (candidateDir.exists() && new File(candidateDir, "package.json").exists()) {
                projectDir = candidateDir;
                githubOperationFailed = true;
                githubErrorDetail = e.getMessage();
            } else {
                throw e; // Project wasn't created at all — re-throw
            }
        }

        // Detect build tool from generated project
        boolean isMaven = new File(projectDir, "pom.xml").exists();
        boolean hasWrapper = isMaven
                ? new File(projectDir, "mvnw").exists()
                : new File(projectDir, "gradlew").exists();
        String buildCommand = isMaven
                ? (hasWrapper ? "./mvnw package" : "mvn clean package")
                : "./gradlew build";

        // Detect if this is an MCP server project
        boolean isMcpProject = detectMcpProject(projectDir);

        StringBuilder message = new StringBuilder();

        if (githubOperationFailed) {
            message.append("Project files created successfully at: ").append(projectDir.getAbsolutePath()).append("\n\n");
            message.append("> **Note:** GitHub repository creation was skipped (").append(githubErrorDetail).append("). ");
            message.append("This is expected when running via an AI agent. ");
            message.append("Use the `gh` CLI commands below to create the repository and publish.\n\n");
        } else {
            message.append("Project created successfully at: ").append(projectDir.getAbsolutePath()).append("\n\n");
        }

        message.append("## Next Steps\n\n");
        message.append("1. **Build the project** to verify it compiles:\n");
        message.append("   ```\n");
        message.append("   cd ").append(projectDir.getAbsolutePath()).append("\n");
        message.append("   ").append(buildCommand).append("\n");
        message.append("   ```\n\n");

        message.append("2. **Verify the JAR** was created and matches the path in `package.json`.\n\n");

        message.append("3. **Publish via GitHub** using the `gh` CLI:\n\n");
        message.append("   **Prerequisite:** Ensure your `gh` CLI has the `workflow` scope (needed to push GitHub Actions workflow files):\n");
        message.append("   ```\n");
        message.append("   gh auth refresh -h github.com -s workflow\n");
        message.append("   ```\n\n");
        message.append("   Then run:\n");
        message.append("   ```\n");
        message.append("   cd ").append(projectDir.getAbsolutePath()).append("\n");
        message.append("   git init\n");
        message.append("   git add .\n");
        message.append("   git commit -m \"Initial commit\"\n");

        if (githubRepository != null && !githubRepository.isEmpty()) {
            message.append("   gh repo create ").append(githubRepository);
            message.append(privateRepository ? " --private" : " --public");
            message.append(" --source . --push\n");
        } else {
            message.append("   gh repo create <owner/repo-name> --public --source . --push\n");
        }

        message.append("   gh release create v1.0.0 --title \"v1.0.0\" --notes \"Initial release\"\n");
        message.append("   ```\n\n");

        message.append("   > **Important:** Use `gh release create` to create the release — do NOT push tags manually ");
        message.append("with `git tag`/`git push --tags`, as this triggers the GitHub Action before a release exists, ");
        message.append("resulting in a draft release that jDeploy cannot update with download links.\n\n");

        if (githubRepository != null && !githubRepository.isEmpty()) {
            message.append("   Monitor the workflow: https://github.com/").append(githubRepository).append("/actions\n\n");
        }

        // Proposal 3b: Include end-user installation instructions
        message.append("## How Users Install This App\n\n");
        message.append("After the GitHub Action completes, native installers (Windows .exe, macOS .dmg, Linux packages) ");
        message.append("will be available on the GitHub release page.\n\n");

        if (isMcpProject) {
            message.append("### MCP Server Auto-Registration\n\n");
            message.append("Because this is an MCP server project, the jDeploy installer automatically detects AI tools ");
            message.append("on the user's system and offers to register the MCP server with them during installation.\n\n");
            message.append("**Supported AI tools:** Claude Desktop, Claude Code, VS Code (Copilot), Cursor, Windsurf, ");
            message.append("Gemini CLI, Codex CLI, OpenCode, and others.\n\n");
            message.append("Users simply download the installer from the release page and run it — no manual MCP ");
            message.append("configuration needed.\n\n");
        }

        if (githubRepository != null && !githubRepository.isEmpty()) {
            message.append("**Download page:** https://github.com/").append(githubRepository).append("/releases/latest\n");
        } else {
            message.append("**Download page:** `https://github.com/<owner>/<repo>/releases/latest`\n");
        }

        return CallToolResult.builder()
            .content(List.of(new TextContent(message.toString())))
            .isError(false)
            .build();
    }

    /**
     * Detect if a project is an MCP server by checking package.json for ai.mcp config
     * or checking source files for MCP annotations/imports.
     */
    private static boolean detectMcpProject(File projectDir) {
        // Check package.json for ai.mcp configuration
        File packageJson = new File(projectDir, "package.json");
        if (packageJson.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(packageJson);
                JsonNode jdeploy = root.get("jdeploy");
                if (jdeploy != null && jdeploy.has("ai")) {
                    JsonNode ai = jdeploy.get("ai");
                    if (ai != null && ai.has("mcp")) {
                        return true;
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private static String getDefaultInstructions() {
        return """
            Please complete the following steps to finish jDeploy setup:

            1. **Verify Build Configuration**: Ensure your project builds an executable JAR with dependencies.
            2. **Update package.json**: Verify the `jar` path points to your built JAR file.
            3. **Check JavaFX**: If using JavaFX, set `"javafx": true` in the jdeploy section.
            4. **Add Icon**: Place an `icon.png` (256x256 or larger) in the project root.
            5. **Test Build**: Run `npx jdeploy build` to verify the configuration.
            """;
    }

    // ---- publish_release tool ----

    private static McpServerFeatures.SyncToolSpecification createPublishReleaseTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("projectDirectory", Map.of(
            "type", "string",
            "description", "Absolute path to the jDeploy project directory containing package.json."
        ));
        properties.put("version", Map.of(
            "type", "string",
            "description", "Version tag for the release (e.g. v1.0.0). Defaults to 'v' + version from package.json."
        ));
        properties.put("title", Map.of(
            "type", "string",
            "description", "Release title. Defaults to the version tag."
        ));
        properties.put("notes", Map.of(
            "type", "string",
            "description", "Release notes text. Defaults to 'Release <version>'."
        ));

        JsonSchema inputSchema = new JsonSchema(
            "object",
            properties,
            List.of("projectDirectory"),
            false,
            null,
            null
        );

        return new McpServerFeatures.SyncToolSpecification(
            new Tool(
                "publish_release",
                "Generate the shell commands needed to publish a jDeploy project as a GitHub Release. " +
                    "This tool inspects the project state (git status, build artifacts, package.json) and " +
                    "returns the exact sequence of commands to run. It does NOT execute the commands itself — " +
                    "the AI agent should run them. The jDeploy GitHub Action will then build native installers " +
                    "for Windows, macOS, and Linux and attach them to the release. " +
                    "For MCP server projects, the installer automatically registers the server with detected " +
                    "AI tools (Claude Desktop, Claude Code, VS Code, Cursor, etc.). " +
                    "Use this after new_project or setup_jdeploy.",
                null, inputSchema, null, null, null
            ),
            (exchange, arguments) -> {
                try {
                    return handlePublishRelease(arguments);
                } catch (Exception e) {
                    return CallToolResult.builder()
                        .content(List.of(new TextContent("Error preparing publish commands: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            }
        );
    }

    private static CallToolResult handlePublishRelease(Map<String, Object> arguments) throws Exception {
        String projectDirectory = (String) arguments.get("projectDirectory");
        File projectDir = new File(projectDirectory);

        // Validate project directory
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            return CallToolResult.builder()
                .content(List.of(new TextContent(
                    "Error: No package.json found at " + projectDir.getAbsolutePath() + ".\n" +
                    "Run setup_jdeploy or new_project first to configure the project.")))
                .isError(true)
                .build();
        }

        // Parse package.json
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(packageJsonFile);
        JsonNode jdeployNode = root.get("jdeploy");

        if (jdeployNode == null) {
            return CallToolResult.builder()
                .content(List.of(new TextContent(
                    "Error: package.json does not contain a 'jdeploy' configuration section.\n" +
                    "Run setup_jdeploy to configure the project.")))
                .isError(true)
                .build();
        }

        // Read version from arguments or package.json
        String packageVersion = root.has("version") ? root.get("version").asText() : "1.0.0";
        String version = (String) arguments.getOrDefault("version", null);
        if (version == null || version.isEmpty()) {
            version = "v" + packageVersion;
        }
        if (!version.startsWith("v")) {
            version = "v" + version;
        }

        String title = (String) arguments.getOrDefault("title", null);
        if (title == null || title.isEmpty()) {
            title = version;
        }
        String notes = (String) arguments.getOrDefault("notes", null);
        if (notes == null || notes.isEmpty()) {
            notes = "Release " + version;
        }

        // Read JAR path and check if it exists
        String jarPath = jdeployNode.has("jar") ? jdeployNode.get("jar").asText() : null;
        boolean jarExists = jarPath != null && new File(projectDir, jarPath).exists();

        // Read repository from package.json
        String repository = root.has("repository") ? root.get("repository").asText() : null;
        // Also check jdeploy.github.repository
        if (repository == null || repository.isEmpty()) {
            JsonNode githubNode = jdeployNode.get("github");
            if (githubNode != null && githubNode.has("repository")) {
                repository = githubNode.get("repository").asText();
            }
        }
        // Strip URL prefix if present
        if (repository != null && repository.startsWith("https://github.com/")) {
            repository = repository.replace("https://github.com/", "");
        }

        // Detect build tool
        boolean isMaven = new File(projectDir, "pom.xml").exists();
        boolean hasWrapper = isMaven
                ? new File(projectDir, "mvnw").exists()
                : new File(projectDir, "gradlew").exists();

        // Detect build command from package.json or defaults
        String buildCommand;
        if (jdeployNode.has("buildCommand")) {
            JsonNode buildCmd = jdeployNode.get("buildCommand");
            if (buildCmd.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode arg : buildCmd) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(arg.asText());
                }
                buildCommand = sb.toString();
            } else {
                buildCommand = buildCmd.asText();
            }
        } else if (isMaven) {
            buildCommand = hasWrapper ? "./mvnw package" : "mvn clean package";
        } else {
            buildCommand = hasWrapper ? "./gradlew build" : "gradle build";
        }

        // Check git state
        boolean hasGitRepo = new File(projectDir, ".git").exists();
        boolean isMcpProject = detectMcpProject(projectDir);

        // Build the response with commands
        StringBuilder message = new StringBuilder();
        message.append("## Publish Release ").append(version).append("\n\n");
        message.append("Run the following commands in order from the project directory:\n");
        message.append("```\n");
        message.append("cd ").append(projectDir.getAbsolutePath()).append("\n");
        message.append("```\n\n");

        int step = 1;

        // Step: Build (always include, even if JAR exists, to ensure latest)
        message.append("### Step ").append(step++).append(": Build the project\n");
        message.append("```\n");
        message.append(buildCommand).append("\n");
        message.append("```\n");
        if (!jarExists && jarPath != null) {
            message.append("> **Warning:** The JAR at `").append(jarPath).append("` does not exist yet. ");
            message.append("This build step is required before publishing.\n");
        }
        message.append("\n");

        // Step: Git init (if needed)
        if (!hasGitRepo) {
            message.append("### Step ").append(step++).append(": Initialize git repository\n");
            message.append("```\n");
            message.append("git init\n");
            message.append("git add -A\n");
            message.append("git commit -m \"Initial commit\"\n");
            message.append("```\n\n");
        } else {
            // Commit any pending changes
            message.append("### Step ").append(step++).append(": Commit any pending changes\n");
            message.append("```\n");
            message.append("git add -A\n");
            message.append("git commit -m \"Release ").append(version).append("\" --allow-empty\n");
            message.append("```\n\n");
        }

        // Step: Create GitHub repo (if no remote detected)
        if (!hasGitRepo) {
            message.append("### Step ").append(step++).append(": Create GitHub repository and push\n");
            message.append("Ensure the `gh` CLI has the `workflow` scope:\n");
            message.append("```\n");
            message.append("gh auth refresh -h github.com -s workflow\n");
            message.append("```\n");
            message.append("Then create the repo:\n");
            message.append("```\n");
            if (repository != null && !repository.isEmpty()) {
                message.append("gh repo create ").append(repository).append(" --public --source . --push\n");
            } else {
                message.append("gh repo create <owner/repo-name> --public --source . --push\n");
            }
            message.append("```\n\n");
        } else {
            message.append("### Step ").append(step++).append(": Push to remote\n");
            message.append("```\n");
            message.append("git push\n");
            message.append("```\n\n");
        }

        // Step: Create release
        message.append("### Step ").append(step++).append(": Create GitHub Release\n");
        message.append("```\n");
        message.append("gh release create ").append(version);
        message.append(" --title \"").append(title).append("\"");
        message.append(" --notes \"").append(notes.replace("\"", "\\\"")).append("\"");
        message.append("\n");
        message.append("```\n\n");

        message.append("> **Important:** Use `gh release create` — do NOT push tags manually with ");
        message.append("`git tag`/`git push --tags`. The `gh release create` command creates a proper ");
        message.append("GitHub Release, which triggers the jDeploy GitHub Action. Pushing a tag alone ");
        message.append("results in a draft release that jDeploy cannot attach download links to.\n\n");

        // What happens next
        message.append("## What Happens Next\n\n");
        message.append("The jDeploy GitHub Action will:\n");
        message.append("1. Build the project\n");
        message.append("2. Create native installers for Windows (.exe), macOS (.dmg), and Linux (.deb)\n");
        message.append("3. Attach the installers to the GitHub Release as downloadable assets\n\n");

        if (repository != null && !repository.isEmpty()) {
            message.append("**Monitor the workflow:** https://github.com/").append(repository).append("/actions\n\n");
            message.append("**Release page:** https://github.com/").append(repository).append("/releases/tag/").append(version).append("\n\n");
        }

        // Installation info for end users
        message.append("## How Users Install This App\n\n");
        message.append("Users download the installer for their platform from the GitHub release page and run it.\n\n");

        if (isMcpProject) {
            message.append("### MCP Server Auto-Registration\n\n");
            message.append("Because this is an MCP server project, the jDeploy installer automatically detects AI tools ");
            message.append("on the user's system and offers to register the MCP server with them during installation.\n\n");
            message.append("**Supported AI tools:** Claude Desktop, Claude Code, VS Code (Copilot), Cursor, Windsurf, ");
            message.append("Gemini CLI, Codex CLI, OpenCode, and others.\n\n");
            message.append("Users simply download the installer and run it — no manual MCP configuration needed.\n\n");
        }

        if (repository != null && !repository.isEmpty()) {
            message.append("**Download page:** https://github.com/").append(repository).append("/releases/latest\n");
        }

        return CallToolResult.builder()
            .content(List.of(new TextContent(message.toString())))
            .isError(false)
            .build();
    }
}
