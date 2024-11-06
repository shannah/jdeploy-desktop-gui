package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.factories.ControllerFactory;
import ca.weblite.jdeploy.app.records.ProjectSettings;
import ca.weblite.jdeploy.app.services.Edt;
import ca.weblite.jdeploy.app.services.PreferencesService;
import ca.weblite.jdeploy.app.services.ProjectSettingsService;
import ca.weblite.jdeploy.app.services.ProjectValidator;
import ca.weblite.jdeploy.app.system.env.EnvironmentInterface;
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class OpenProjectController implements Runnable{

    private final FileSystemUiInterface fileSystemUi;

    private final ProjectValidator projectValidator;

    private final ControllerFactory controllerFactory;

    private final ProjectSettingsService projectSettingsService;

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
        projectSettingsService = DIContext.get(ProjectSettingsService.class);
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

        ProjectSettings projectSettings = null;
        try {
            projectSettings = projectSettingsService.findOne(path);
        } catch (Exception e) {
            edt.invokeLater(
                    controllerFactory.createErrorController(e)
            );
            return;
        }

        edt.invokeLater(
                controllerFactory.createProjectController(projectSettings)
        );
    }
}
