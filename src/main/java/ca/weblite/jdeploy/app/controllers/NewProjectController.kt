package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.accounts.AccountInterface
import ca.weblite.jdeploy.app.accounts.AccountType
import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.exceptions.ValidationFailedException
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.forms.NewProjectForm
import ca.weblite.jdeploy.app.forms.NewProjectFormInterface
import ca.weblite.jdeploy.app.forms.TemplateChooserPanel.Model
import ca.weblite.jdeploy.app.forms.TemplateTileDelegate
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.records.Template
import ca.weblite.jdeploy.app.repositories.DefaultProjectTemplateRepository
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.jdeploy.app.repositories.ProjectTemplateRepositoryInterface
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface
import ca.weblite.jdeploy.builders.ProjectGeneratorRequestBuilder
import ca.weblite.jdeploy.services.GithubTokenService
import ca.weblite.jdeploy.services.ProjectGenerator
import ca.weblite.jdeploy.services.ProjectTemplateCatalog
import ca.weblite.ktswing.coroutines.SwingDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val controllerFactory: ControllerFactory,
    private val githubTokenService: GithubTokenService = DIContext.get(GithubTokenService::class.java),
    private val projectTemplateRepository: ProjectTemplateRepositoryInterface = DIContext.get(ProjectTemplateRepositoryInterface::class.java)
) {
    private lateinit var dialog: NewProjectForm
    private lateinit var owner: Frame

    companion object {
        var lastProjectTemplateUpdate = 0L
    }

    constructor(owner: Frame): this(
        fileSystemUi = DIContext.get(FileSystemUiInterface::class.java),
        projectGenerator = DIContext.get(ProjectGenerator::class.java),
        templateCatalog = DIContext.get(ProjectTemplateCatalog::class.java),
        controllerFactory = DIContext.get(ControllerFactory::class.java),
    ) {
        this.owner = owner
        val templateChooserModel = object : Model {
            override suspend fun getProjectTemplates(): ProjectTemplates {
                return projectTemplateRepository.findAll()
            }
        }
        dialog = NewProjectForm(owner, templateChooserModel = templateChooserModel).apply {
            tileDelegate = object : TemplateTileDelegate {
                override fun openTemplateDemoDownloadPage(template: Template) =
                    this@NewProjectController.openTemplateDemoDownloadPage(template)

                override fun openTemplateSources(template: Template) =
                    this@NewProjectController.openTemplateSources(template)

                override fun openWebAppUrl(template: Template) =
                    this@NewProjectController.openWebDemo(template)
            }
        }

        dialog.apply {
            iconImage = javaClass.getResource("/ca/weblite/jdeploy/app/assets/icon.png")?.let { ImageIcon(it).image }
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
                if (artifactId.text.isNotEmpty() && npmProjectName.text.isEmpty()) {
                    npmProjectName.text = artifactId.text
                }
                update()
            }

            gitHubReleasesRadioButton.addActionListener{
                update()
            }

            if (!templateCatalog.isCatalogInitialized) {
                updateTemplateCatalog(dialog)
            } else {
                updateTemplateOptions()
            }

            refreshTemplatesButton.addActionListener {
                updateTemplateCatalog(dialog)
            }

            createProjectButton.addActionListener{
                selectGitHubAccount().thenRun {
                    handleCreateProject()
                }
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

    suspend  fun show() {
        updateTemplateCatalogSuspending(owner)
        withContext(SwingDispatcher) {
            dialog.pack()
            dialog.isVisible = true
        }
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
            if (account != null && account.getAccessToken() != null) {
                githubTokenService.setToken(account.getAccessToken());
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
            if (
                dialog.gitHubReleasesRadioButton.isSelected
                && dialog.createGithubRepositoryUrlCheckBox.isSelected
                && dialog.githubRepositoryUrl.text.isNotEmpty()
                ) {
                githubRepository = dialog.githubRepositoryUrl.text
                isPrivateRepository = dialog.githubReleasesRepositoryUrl.text.isEmpty()
            }

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
                if (githubRepositoryUrl.text.isEmpty()) {
                    throw ValidationFailedException("GitHub Repository URL is required")
                }
                validateGitHubRepositoryFormat(githubRepositoryUrl.text, "GitHub Repository URL")

                if (githubReleasesRepositoryUrl.text.isNotEmpty()) {
                    validateGitHubRepositoryFormat(githubReleasesRepositoryUrl.text, "Releases Repository URL")
                }
            }
        }
    }

    @Throws(ValidationFailedException::class)
    private fun validateGitHubRepositoryFormat(repositoryUrl: String, fieldName: String) {
        val trimmed = repositoryUrl.trim()

        // Check if it's a full URL (starts with http:// or https://)
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)) {
            throw ValidationFailedException(
                "$fieldName must be in format 'owner/repository', not a full URL.\n" +
                "Example: octocat/Hello-World"
            )
        }

        // Check if it contains exactly one '/'
        val slashCount = trimmed.count { it == '/' }
        if (slashCount == 0) {
            throw ValidationFailedException(
                "$fieldName must include both owner and repository name.\n" +
                "Format: owner/repository\n" +
                "Example: octocat/Hello-World"
            )
        }

        if (slashCount > 1) {
            throw ValidationFailedException(
                "$fieldName contains too many '/' characters.\n" +
                "Format: owner/repository\n" +
                "Example: octocat/Hello-World"
            )
        }

        // Split and validate owner and repository parts
        val parts = trimmed.split('/')
        if (parts.size != 2) {
            throw ValidationFailedException(
                "$fieldName format is invalid.\n" +
                "Format: owner/repository\n" +
                "Example: octocat/Hello-World"
            )
        }

        val owner = parts[0].trim()
        val repo = parts[1].trim()

        if (owner.isEmpty()) {
            throw ValidationFailedException(
                "$fieldName is missing the owner name.\n" +
                "Format: owner/repository\n" +
                "Example: octocat/Hello-World"
            )
        }

        if (repo.isEmpty()) {
            throw ValidationFailedException(
                "$fieldName is missing the repository name.\n" +
                "Format: owner/repository\n" +
                "Example: octocat/Hello-World"
            )
        }

        // Validate characters in owner and repository names
        // GitHub allows alphanumeric, hyphens, underscores, and dots
        val validPattern = Regex("^[a-zA-Z0-9._-]+$")

        if (!owner.matches(validPattern)) {
            throw ValidationFailedException(
                "$fieldName owner '$owner' contains invalid characters.\n" +
                "Owner can only contain letters, numbers, hyphens, underscores, and dots."
            )
        }

        if (!repo.matches(validPattern)) {
            throw ValidationFailedException(
                "$fieldName repository '$repo' contains invalid characters.\n" +
                "Repository can only contain letters, numbers, hyphens, underscores, and dots."
            )
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

    private fun openTemplateDemoDownloadPage(template: Template) {
        // Open the demo download page for the selected template
        val url = template.demoDownloadUrl
        if (url == null) {
            return;
        }

        try {
            val uri = java.net.URI(url)
            java.awt.Desktop.getDesktop().browse(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openTemplateSources(template: Template) {
        // Open the sources for the selected template
        val url = template.sourceUrl
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            val uri = java.net.URI(url)
            java.awt.Desktop.getDesktop().browse(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openWebDemo(template: Template) {
        // Open the sources for the selected template
        val url = template.webAppUrl
        if (url.isNullOrEmpty()) {
            return;
        }

        try {
            val uri = java.net.URI(url)
            java.awt.Desktop.getDesktop().browse(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend private fun updateTemplateCatalogSuspending(owner: Frame) {
        if (lastProjectTemplateUpdate < System.currentTimeMillis() - 1000 * 60 * 60) {
            lastProjectTemplateUpdate = System.currentTimeMillis()
        } else {
            return
        }
        val updateController = UpdateProjectTemplatesController(templateCatalog, owner)
        updateController.updateSuspending()
        updateTemplateOptions()
    }

}