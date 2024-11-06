package ca.weblite.jdeploy.app.factories;

import ca.weblite.jdeploy.app.controllers.ErrorController;
import ca.weblite.jdeploy.app.controllers.ProjectController;
import ca.weblite.jdeploy.app.records.ProjectSettings;

import javax.inject.Singleton;

@Singleton
public class ControllerFactory {

    public ErrorController createErrorController(Throwable exception) {
        return new ErrorController(exception);
    }

    public ProjectController createProjectController(ProjectSettings projectSettings) {
        return new ProjectController(projectSettings);
    }
}
