package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.exceptions.InvalidProjectException
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.services.Edt
import ca.weblite.jdeploy.app.services.PreferencesService
import ca.weblite.jdeploy.app.services.ProjectService
import ca.weblite.jdeploy.app.services.ProjectValidator
import ca.weblite.jdeploy.app.system.env.EnvironmentInterface
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface
import java.awt.Window

class OpenProjectController @JvmOverloads constructor(
    private val parentWindow: Window,
    private val fromPath: String? = null,
    private val closeParentWindowOnSuccess: Boolean = false
) : Runnable {
    private val fileSystemUi: FileSystemUiInterface = DIContext.get(FileSystemUiInterface::class.java)

    private val projectValidator: ProjectValidator = DIContext.get(ProjectValidator::class.java)

    private val controllerFactory: ControllerFactory = DIContext.get(ControllerFactory::class.java)

    private val projectService: ProjectService = DIContext.get(ProjectService::class.java)

    private val preferencesService: PreferencesService = DIContext.get(PreferencesService::class.java)

    private val edt: Edt = DIContext.get(Edt::class.java)

    private val environment: EnvironmentInterface = DIContext.get(EnvironmentInterface::class.java)

    override fun run() {
        val path = fromPath
            ?: fileSystemUi.openDirectoryDialog(
                parentWindow,
                "Open Project",
                preferencesService.rootPreferences["lastProjectPath", environment.userHomeDirectory],
                null
            ) { selectedPath: String? ->
                projectValidator
                    .isValidProject(
                        selectedPath,
                        ProjectValidator.ValidationLevel.HasPackageJson
                    )
            }
        if (path == null) {
            // No project was selected
            return
        }

        try {
            projectValidator.validate(path, ProjectValidator.ValidationLevel.MeetsMinimumRequirements)
        } catch (e: InvalidProjectException) {
            edt.invokeLater(
                controllerFactory.createErrorController(e)
            )
            return
        }

        var project: Project? = null
        try {
            project = projectService.touch(projectService.loadProject(path))
        } catch (e: Exception) {
            edt.invokeLater(
                controllerFactory.createErrorController(e)
            )
            return
        }

        edt.invokeLater(
            controllerFactory.createProjectController(project)
        )
        edt.invokeLater {
            if (closeParentWindowOnSuccess) {
                parentWindow.dispose()
            }
        }
    }
}
