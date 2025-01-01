package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.accounts.AccountInterface
import ca.weblite.jdeploy.app.accounts.AccountType
import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.exceptions.ValidationFailedException
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.forms.NewProjectForm
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface
import ca.weblite.jdeploy.builders.ProjectGeneratorRequestBuilder
import ca.weblite.jdeploy.services.ProjectGenerator
import ca.weblite.jdeploy.services.ProjectTemplateCatalog
import java.awt.FlowLayout
import java.awt.Frame
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.prefs.Preferences
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class NewProjectController(
    private val fileSystemUi: FileSystemUiInterface,
    private val projectGenerator: ProjectGenerator,
    private val templateCatalog: ProjectTemplateCatalog,
    private val controllerFactory: ControllerFactory
) {
    private lateinit var dialog: NewProjectForm

    constructor(owner: Frame): this(
        fileSystemUi = DIContext.get(FileSystemUiInterface::class.java),
        projectGenerator = DIContext.get(ProjectGenerator::class.java),
        templateCatalog = DIContext.get(ProjectTemplateCatalog::class.java),
        controllerFactory = DIContext.get(ControllerFactory::class.java),
    ) {
        dialog = NewProjectForm(owner)
        dialog.apply {
            arrayOf(artifactId, groupId, displayName, projectLocation).forEach {
                it.document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent?) {
                        update()
                    }

                    override fun removeUpdate(e: DocumentEvent?) {
                        update()
                    }

                    override fun changedUpdate(e: DocumentEvent?) {
                        update()
                    }
                })
            }

            selectProjectLocationButton.addActionListener{
                val selectedPath = fileSystemUi.openDirectoryDialog(
                    owner,
                    "Select project location",
                    System.getProperty("user.home", "."),
                    null,
                    null,
                )

                if (selectedPath != null) {
                    setDefaultValue("projectLocation", selectedPath)
                    projectLocation.text = selectedPath
                    preferences.flush()
                }

            }

            npmRadioButton.addActionListener{
                update()
            }

            gitHubReleasesRadioButton.addActionListener{
                update()
            }

            if (!templateCatalog.isCatalogInitialized) {
                updateTemplateCatalog(owner)
            } else {
                updateTemplateOptions()
            }

            refreshTemplatesButton.addActionListener {
                updateTemplateCatalog(owner)
            }

            createProjectButton.addActionListener{
                handleCreateProject()
            }

            artifactId.addActionListener {
                setDefaultValue("artifactId", artifactId.text)
                update()
            }

            groupId.addActionListener {
                setDefaultValue("groupId", groupId.text)
                update()
            }

            displayName.addActionListener {
                setDefaultValue("displayName", displayName.text)
                update()
            }

            projectTemplate.addActionListener {
                update()
            }

            projectLocation.addActionListener {
                setDefaultValue("projectLocation", projectLocation.text)
                update()
            }
            setDefaultValues()
            update()

        }
    }

    fun show() {
        dialog.pack()
        dialog.isVisible = true
    }

    private fun setDefaultValues() {
        dialog.apply {
            groupId.text = getDefaultValue("groupId")
            projectLocation.text = getDefaultValue("projectLocation")
            if (projectLocation.text.isEmpty()) {
                projectLocation.text = System.getProperty("user.home", ".")
            }
            for (i in 0 until projectTemplate.itemCount) {
                if (projectTemplate.getItemAt(i) == getDefaultValue("projectTemplate")) {
                    projectTemplate.selectedIndex = i
                    break
                }
            }
        }
    }

    private fun update() {
        dialog.apply {
            setGitHubSettingsEnabled(gitHubReleasesRadioButton.isSelected)
            setNpmSettingsEnabled(npmRadioButton.isSelected)
            createProjectButton.isEnabled = !groupId.text.isEmpty()
                    && !projectLocation.text.isEmpty()
                    && !artifactId.text.isEmpty()
                    && !displayName.text.isEmpty()
        }
    }

    private fun setNpmSettingsEnabled(enabled: Boolean) {
        dialog.npmProjectName.isEnabled = enabled
    }

    private fun setGitHubSettingsEnabled(enabled: Boolean) {
        dialog.apply {
            githubRepositoryUrl.isEnabled = enabled
            githubReleasesRepositoryUrl.isEnabled = enabled
            createGithubReleasesRepositoryCheckBox.isEnabled = enabled
            createGithubRepositoryUrlCheckBox.isEnabled = enabled
        }
    }

    private fun selectGitHubAccount(): CompletableFuture<AccountInterface?> {

        val future = CompletableFuture<AccountInterface?>()
        if (!requiresGithubLogin()) {
            future.complete(null)
            return future
        }
        return AccountChooserController(dialog, AccountType.GITHUB).show().thenApply { account ->
            if (account == null) {
                dialog.gitHubReleasesRadioButton.isSelected = false
            }
            account
        }
    }

    private fun requiresGithubLogin(): Boolean {
        return dialog.gitHubReleasesRadioButton.isSelected
                && (
                dialog.createGithubReleasesRepositoryCheckBox.isSelected
                        || dialog.createGithubRepositoryUrlCheckBox.isSelected
                )
    }

    private fun handleCreateProject() {
        // Create a progress dialog
        val progressDialog = JDialog(dialog, "Creating Project", true).apply {
            layout = FlowLayout()
            add(JLabel("Creating project. Please wait..."))
            val progressBar = JProgressBar().apply {
                isIndeterminate = true
            }
            add(progressBar)
            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            pack()
            setLocationRelativeTo(dialog)
        }

        // Define the SwingWorker
        val worker = object : SwingWorker<File, File>() {
            override fun doInBackground(): File {
                // Perform the long-running project creation task
                val projectDirectory = createProject()
                saveDefaultValues()
                preferences.flush()
                return projectDirectory
            }

            override fun done() {
                try {
                    // Attempt to retrieve the result to check for exceptions
                    openProject(get())
                } catch (e: Exception) {
                    e.printStackTrace()
                    controllerFactory.createErrorController(e).run()
                } finally {
                    // Dispose of the progress dialog
                    progressDialog.dispose()
                }
            }
        }

        // Start the background task
        worker.execute()

        // Show the progress dialog (this will block the EDT if modal = true)
        progressDialog.isVisible = true
    }

    @Throws(ValidationFailedException::class)
    private fun createProject(): File {
        validate()
        val params = ProjectGeneratorRequestBuilder().apply {
            appTitle = dialog.displayName.text
            projectName = dialog.artifactId.text
            if (dialog.npmRadioButton.isSelected && dialog.npmProjectName.text.isNotEmpty()) {
                projectName = dialog.npmProjectName.text
            }
            parentDirectory = File(dialog.projectLocation.text)
            groupId = dialog.groupId.text
            artifactId = dialog.artifactId.text
            templateName = dialog.projectTemplate.selectedItem?.toString()
        }

        return projectGenerator.generate(params.build())

    }

    private fun openProject(projectDirectory: File) {
        val openProjectController = OpenProjectController(
            parentWindow = dialog,
            fromPath = projectDirectory.absolutePath,
            closeParentWindowOnSuccess = true
        )
        openProjectController.run()
    }

    private fun updateTemplateCatalog(owner: Frame) {
        val updateController = UpdateProjectTemplatesController(templateCatalog, owner)
        updateController.update()
        updateTemplateOptions()
    }

    private fun updateTemplateOptions() {
        dialog.apply {
            val selectedItem = projectTemplate.selectedItem
            projectTemplate.removeAllItems()
            templateCatalog.projectTemplates.forEach {
                projectTemplate.addItem(it.name)
            }
            if (selectedItem != null) {
                for (i in 0 until projectTemplate.itemCount) {
                    if (projectTemplate.getItemAt(i) == selectedItem) {
                        projectTemplate.selectedIndex = i
                        break
                    }
                }
            }
        }
    }

    private fun getDefaultValue(key: String): String {
        return preferences.get(key, "")
    }

    private fun setDefaultValue(key: String, value: String) {
        preferences.put(key, value)
    }

    private val preferences: Preferences by lazy {
        Preferences.userNodeForPackage(NewProjectController::class.java)
    }

    private fun getProjectDirectory(): File {
        return File(File(dialog.projectLocation.text), dialog.artifactId.text)
    }

    @Throws(ValidationFailedException::class)
    private fun validate() {
        dialog.apply {
            if (groupId.text.isEmpty()) {
                throw ValidationFailedException("Group ID is required")
            }

            if (artifactId.text.isEmpty()) {
                throw ValidationFailedException("Artifact ID is required")
            }

            if (displayName.text.isEmpty()) {
                throw ValidationFailedException("Display Name is required")
            }

            if (projectLocation.text.isEmpty()) {
                throw ValidationFailedException("Project Location is required")
            }

            if (projectTemplate.selectedItem == null) {
                throw ValidationFailedException("Project Template is required")
            }

            if (getProjectDirectory().exists()) {
                throw ValidationFailedException("Project directory already exists")
            }

            if (!File(projectLocation.text).isDirectory()) {
                throw ValidationFailedException("Project location does not exist")
            }

            if (npmRadioButton.isSelected && npmProjectName.text.isEmpty()) {
                throw ValidationFailedException("NPM Project Name is required")
            }

            if (gitHubReleasesRadioButton.isSelected) {
                throw ValidationFailedException("GitHub is not supported yet via this form")
            }
        }
    }

    private fun saveDefaultValues() {
        dialog.apply {
            setDefaultValue("groupId", groupId.text)
            setDefaultValue("artifactId", artifactId.text)
            setDefaultValue("displayName", displayName.text)
            setDefaultValue("projectLocation", projectLocation.text)
            if (projectTemplate.selectedItem != null) {
                setDefaultValue("projectTemplate", projectTemplate.selectedItem.toString())
            }
        }
    }
}