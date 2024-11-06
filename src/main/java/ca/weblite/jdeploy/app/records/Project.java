package ca.weblite.jdeploy.app.records;

import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public record Project(
    String name,
    String path,

    UUID uuid
) {
    public String getPackageJsonPath() {
        return path + File.separator + "package.json";
    }
}
