package ca.weblite.jdeploy.app.factories;

import ca.weblite.jdeploy.app.records.Project;
import org.json.JSONObject;

import java.util.UUID;

public class ProjectFactory {
    public Project createOne(String projectPath, JSONObject packageJson) {
        UUID uuid = packageJson.has("uuid")
                ? UUID.fromString(packageJson.getString("uuid"))
                : UUID.randomUUID();
        return new Project(
                packageJson.getString("name"),
                projectPath,
                uuid
        );
    }
}
