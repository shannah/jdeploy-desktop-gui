package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.factories.ControllerFactory;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.services.Edt;
import ca.weblite.jdeploy.app.services.PreferencesService;
import ca.weblite.jdeploy.app.services.ProjectService;
import ca.weblite.jdeploy.app.services.ProjectValidator;
import ca.weblite.jdeploy.app.system.env.EnvironmentInterface;
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface;

import java.awt.*;

public class OpenProjectController implements Runnable{

    private final FileSystemUiInterface fileSystemUi;

    private final ProjectValidator projectValidator;

    private final ControllerFactory controllerFactory;

    private final ProjectService projectService;

    private final PreferencesService preferencesService;

    private final Edt edt;

    private final Window parentWindow;

    private final EnvironmentInterface environment;

    public OpenProjectController(Window parentWindow) {
        this.parentWindow = parentWindow;
        fileSystemUi = DIContext.get(FileSystemUiInterface.class);
        projectValidator = DIContext.get(ProjectValidator.class);
        controllerFactory = DIContext.get(ControllerFactory.class);
        edt = DIContext.get(Edt.class);
        projectService = DIContext.get(ProjectService.class);
        preferencesService = DIContext.get(PreferencesService.class);
        environment = DIContext.get(EnvironmentInterface.class);

    }

    @Override
    public void run() {
        String path = fileSystemUi.openDirectoryDialog(
                parentWindow,
                "Open Project",
                preferencesService.getRootPreferences().get(
                        "lastProjectPath",
                        environment.getUserHomeDirectory()
                ),
                null,
                (selectedPath) -> projectValidator
                        .isValidProject(
                                selectedPath,
                                ProjectValidator.ValidationLevel.HasPackageJson
                        )
        );
        if (path == null) {
            // No project was selected
            return;
        }

        if (!projectValidator.isValidProject(path, ProjectValidator.ValidationLevel.MeetsMinimumRequirements)) {
            // The selected project is not a valid project
            edt.invokeLater(
                    controllerFactory.createErrorController(new Exception("Invalid project"))
            );
            return;
        }

        Project project = null;
        try {
            project = projectService.loadProject(path);
        } catch (Exception e) {
            edt.invokeLater(
                    controllerFactory.createErrorController(e)
            );
            return;
        }

        edt.invokeLater(
                controllerFactory.createProjectController(project)
        );
    }
}
