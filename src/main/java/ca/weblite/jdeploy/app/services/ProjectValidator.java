package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.exceptions.InvalidProjectException;
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

        try {
            validate(path, level);
            return true;
        } catch (InvalidProjectException e) {
            return false;
        }
    }

    public void validate(String path, ValidationLevel level) throws InvalidProjectException {
        if (!fileSystem.isDirectory(path)) {
            throw new InvalidProjectException(path, "The path is not a directory");
        }

        if (level.ordinal() >= ValidationLevel.HasPackageJson.ordinal()) {
            if (!fileSystem.exists(path + File.separator + "package.json")) {
                throw new InvalidProjectException(path, "The project does not contain a package.json file");
            }
        }

        if (level.ordinal() >= ValidationLevel.MeetsMinimumRequirements.ordinal()) {
            try {
                JSONObject packageJson = packageJsonService.readOne(path + File.separator + "package.json");
                if (!packageJson.has("name")) {
                    throw new InvalidProjectException(path, "The package.json file has no name");
                }
                if (!packageJson.has("version")) {
                    throw new InvalidProjectException(path, "The package.json file has no version");
                }
                if (!packageJson.has("jdeploy")) {
                    throw new InvalidProjectException(path, "The package.json file has no jdeploy section");
                }
            } catch (Exception e) {
                throw new InvalidProjectException(path, "The package.json file is invalid. Reason: " + e.getMessage(), e);
            }
        }
    }
}
