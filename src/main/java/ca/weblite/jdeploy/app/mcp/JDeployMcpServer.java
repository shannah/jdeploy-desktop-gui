package ca.weblite.jdeploy.app.mcp;

import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.app.records.ProjectTemplates;
import ca.weblite.jdeploy.app.records.Template;
import ca.weblite.jdeploy.app.repositories.ProjectTemplateRepositoryInterface;
import ca.weblite.jdeploy.builders.ProjectGeneratorRequestBuilder;
import ca.weblite.jdeploy.services.ProjectGenerator;
import ca.weblite.jdeploy.services.ProjectInitializer;
import ca.weblite.jdeploy.services.ProjectTemplateCatalog;
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

    private static final String TOOL_NAME = "setup_jdeploy";
    private static final String TOOL_DESCRIPTION =
        "Initialize a Java project for jDeploy deployment. " +
        "This creates the necessary package.json configuration and optionally generates GitHub workflow files. " +
        "After running this tool, follow the returned instructions to complete the setup.";

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

        // Register tools
        server.addTool(createSetupTool());
        server.addTool(createListTemplatesTool());
        server.addTool(createNewProjectTool());

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

            // Load and return the setup instructions
            String instructions = loadSetupInstructions();

            String resultMessage = String.format(
                "Successfully initialized jDeploy project at: %s\n\n" +
                "Generated files:\n" +
                "- package.json (jDeploy configuration)\n" +
                "%s\n" +
                "## Next Steps\n\n%s",
                projectDirectory,
                generateGithubWorkflow ? "- .github/workflows/ (CI/CD workflow)\n" : "",
                instructions
            );

            return CallToolResult.builder()
                .content(List.of(new TextContent(resultMessage)))
                .isError(false)
                .build();

        } catch (Exception e) {
            return CallToolResult.builder()
                .content(List.of(new TextContent("Failed to initialize project: " + e.getMessage())))
                .isError(true)
                .build();
        }
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
                    "languages, UI toolkits, and categories. Use this before new_project to see available options.",
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
                "Create a new jDeploy project from a template. " +
                    "Use list_templates first to see available templates.",
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

        File projectDir = projectGenerator.generate(builder.build());

        return CallToolResult.builder()
            .content(List.of(new TextContent(
                "Project created successfully at: " + projectDir.getAbsolutePath()
            )))
            .isError(false)
            .build();
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
}
