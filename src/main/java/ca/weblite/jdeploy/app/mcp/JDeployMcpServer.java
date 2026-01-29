package ca.weblite.jdeploy.app.mcp;

import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.services.ProjectInitializer;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    /**
     * Run the MCP server using stdio transport.
     */
    public static void run() {
        // Initialize DI context to get ProjectInitializer with all dependencies
        new JDeployDesktopGuiModule().install();
        projectInitializer = DIContext.get(ProjectInitializer.class);

        JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new ObjectMapper());
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(jsonMapper);

        McpSyncServer server = McpServer.sync(transportProvider)
            .serverInfo(SERVER_NAME, SERVER_VERSION)
            .capabilities(ServerCapabilities.builder()
                .tools(true)
                .build())
            .build();

        // Register the setup_jdeploy tool
        server.addTool(createSetupTool());

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

    private static String loadSetupInstructions() {
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
