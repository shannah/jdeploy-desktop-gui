package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.exceptions.InvalidProjectException;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.repositories.ProjectRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ProjectService {

    private ProjectRepository projectRepository;

    @Inject
    public ProjectService(
            ProjectRepository projectRepository
    ) {
        this.projectRepository = projectRepository;
    }
    public Project findOne(String projectPath) throws IOException, InvalidProjectException {
        return projectRepository.findOne(projectPath);
    }
}
