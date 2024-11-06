package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.collections.ProjectSettingsSet;
import ca.weblite.jdeploy.app.exceptions.InvalidProjectException;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.records.ProjectSettings;
import ca.weblite.jdeploy.app.repositories.ProjectSettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ProjectSettingsService {

    private final ProjectService projectService;


    private final ProjectSettingsRepository projectSettingsRepository;

    @Inject
    public ProjectSettingsService(
            ProjectService projectService,
            ProjectSettingsRepository projectSettingsRepository
    ) {
        this.projectService = projectService;
        this.projectSettingsRepository = projectSettingsRepository;
    }

    public ProjectSettingsSet findRecent() {
        return new ProjectSettingsSet();
    }

    public ProjectSettings findOne(String projectPath) throws IOException, InvalidProjectException {
        Project project = projectService.findOne(projectPath);
        return projectSettingsRepository.findOne(project);
    }
}
