package ca.weblite.jdeploy.app.repositories;

import ca.weblite.jdeploy.app.exceptions.InvalidProjectException;
import ca.weblite.jdeploy.app.factories.ProjectFactory;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.services.PackageJsonService;
import ca.weblite.jdeploy.app.services.ProjectValidator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class ProjectRepository {

    private final ProjectValidator projectValidator;

    private final PackageJsonService packageJsonService;

    private final ProjectFactory projectFactory;

    @Inject
    public ProjectRepository(
            ProjectValidator projectValidator,
            PackageJsonService packageJsonService,
            ProjectFactory projectFactory
    ) {
        this.projectValidator = projectValidator;
        this.packageJsonService = packageJsonService;
        this.projectFactory = projectFactory;
    }

    public Project findOne(String projectPath) throws IOException, InvalidProjectException {
        if (!projectValidator.isValidProject(projectPath, ProjectValidator.ValidationLevel.MeetsMinimumRequirements)) {
            throw new InvalidProjectException(projectPath);
        }

        return projectFactory.createOne(
                projectPath,
                packageJsonService.readOne(projectPath + File.separator + "package.json")
        );
    }
}
