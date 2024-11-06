package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.system.files.FileSystemInterface;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class ProjectValidator {

    private final FileSystemInterface fileSystem;

    private final PackageJsonService packageJsonService;

    @Inject
    public ProjectValidator(FileSystemInterface fileSystem, PackageJsonService packageJsonService) {
        this.fileSystem = fileSystem;
        this.packageJsonService = packageJsonService;
    }

    public enum ValidationLevel {
        DirectoryExists,
        HasPackageJson,
        MeetsMinimumRequirements,
    }
    public boolean isValidProject(String path, ValidationLevel level) {

        if (!fileSystem.isDirectory(path)) {
            return false;
        }

        if (level.ordinal() >= ValidationLevel.HasPackageJson.ordinal()) {
            if (!fileSystem.exists(path + File.separator + "package.json")) {
                return false;
            }
        }

        if (level.ordinal() >= ValidationLevel.MeetsMinimumRequirements.ordinal()) {
            try {
                JSONObject packageJson = packageJsonService.readOne(path + File.separator + "package.json");
                if (!packageJson.has("name")) {
                    return false;
                }
                if (!packageJson.has("version")) {
                    return false;
                }
                if (!packageJson.has("jdeploy")) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }
}
