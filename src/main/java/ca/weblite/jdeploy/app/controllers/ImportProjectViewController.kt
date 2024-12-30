package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.forms.ImportProjectFormJ
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface
import ca.weblite.jdeploy.services.ProjectInitializer
import java.awt.EventQueue
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.SwingWorker

class ImportProjectViewController(parentFrame: JFrame): JFrameViewController(parentFrame) {
    private val fileSystemUi: FileSystemUiInterface = DIContext.get(FileSystemUiInterface::class.java);
    private val projectInitializer: ProjectInitializer = DIContext.get(ProjectInitializer::class.java);
    private val controllerFactory: ControllerFactory = DIContext.get(ControllerFactory::class.java);
    override fun initUI(): JComponent {
        val form = ImportProjectFormJ()
        form.browseProjectDirectory.addActionListener {
            val projectDir = fileSystemUi.openDirectoryDialog(
                frame,
                "Select Project Directory",
                null,
                null,
                null
            )
            form.projectDirectory.text = projectDir
        };

        form.cancelButton.addActionListener {
            frame.dispose()
        }

        form.importButton.addActionListener {
            handleImportProject(form.projectDirectory.text, form.generateGitHubWorkflow.isSelected)
        }

        return form
    }

    private fun handleImportProject(projectDirectory: String, generateGitHubWorkflow: Boolean) {
        object : SwingWorker<ProjectInitializer.Response?, Void?>() {

            // We capture an exception if one occurs during doInBackground()
            private var error: Exception? = null

            override fun doInBackground(): ProjectInitializer.Response? {
                return try {
                    // Perform blocking work off the UI thread
                    projectInitializer.decorate(
                        ProjectInitializer.Request(
                            projectDirectory,
                            null,
                            false,  // dryRun
                            generateGitHubWorkflow,   // generateGithubWorkflow
                            null
                        )
                    )
                } catch (ex: Exception) {
                    error = ex
                    null
                }
            }

            override fun done() {
                // Back on the Event Dispatch Thread. Safely update UI or invoke controllers.

                if (error != null) {
                    // If an error occurred, run the error controller on the EDT
                    EventQueue.invokeLater(controllerFactory.createErrorController(error))
                } else {
                    // On success, open project
                    EventQueue.invokeLater(OpenProjectController(frame, projectDirectory, closeParentWindowOnSuccess = true))
                }
            }
        }.execute()  // Start the background task
    }
}