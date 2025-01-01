package ca.weblite.jdeploy.app.factories;

import ca.weblite.jdeploy.app.controllers.ErrorController;
import ca.weblite.jdeploy.app.controllers.ProjectController;
import ca.weblite.jdeploy.app.records.Project;

import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class ControllerFactory {

    public ErrorController createErrorController(Throwable exception) {
        return new ErrorController(exception);
    }

    public ProjectController createProjectController(Project project) {
        return new ProjectController(project);
    }
}
