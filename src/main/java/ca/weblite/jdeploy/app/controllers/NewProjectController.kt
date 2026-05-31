package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.accounts.AccountInterface
import ca.weblite.jdeploy.app.accounts.AccountType
import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.exceptions.GitHubAuthException
import ca.weblite.jdeploy.app.exceptions.ValidationFailedException
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.forms.NewProjectForm
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface
import ca.weblite.jdeploy.builders.ProjectGeneratorRequestBuilder
import ca.weblite.jdeploy.services.GitHubUsernameService
import ca.weblite.jdeploy.services.GithubTokenService
import ca.weblite.jdeploy.services.ProjectGenerator
import ca.weblite.jdeploy.services.ProjectTemplateCatalog
import java.awt.FlowLayout
import java.awt.Frame
import java.io.File
import java.io.IOException
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
    private val gitHubUsernameService: GitHubUsernameService = DIContext.get(GitHubUsernameService::class.java),
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
            if (account != null && account.getAccessToken() != null) {
                githubTokenService.setToken(account.getAccessToken());
            }
            account
        }
    }

    private fun requiresGithubLogin(): Boolean {
        return isGitHubRepositoryRequested()
    }

    /**
     * Whether project creation will create/push a GitHub repository and therefore
     * needs valid GitHub credentials. This must mirror the condition in
     * [createProject] that sets the github repository on the request.
     */
    private fun isGitHubRepositoryRequested(): Boolean {
        return dialog.gitHubReleasesRadioButton.isSelected
                && dialog.createGithubRepositoryUrlCheckBox.isSelected
                && dialog.githubRepositoryUrl.text.isNotEmpty()
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
                // Verify the GitHub credentials up front so we fail fast with a
                // clear message instead of part way through generation with a 401.
                validateGitHubTokenIfNeeded()
                // Perform the long-running project creation task
                val projectDirectory = createProject()
                saveDefaultValues()
                preferences.flush()
                return projectDirectory
            }

            override fun done() {
                // Dispose of the progress dialog before handling the result so any
                // follow-up dialogs (e.g. re-authentication) aren't blocked by it.
                progressDialog.dispose()
                try {
                    // Attempt to retrieve the result to check for exceptions
                    openProject(get())
                } catch (e: Exception) {
                    if (isGitHubAuthError(e)) {
                        promptReauthenticationAndRetry(e)
                    } else {
                        e.printStackTrace()
                        controllerFactory.createErrorController(e).run()
                    }
                }
            }
        }

        // Start the background task
        worker.execute()

        // Show the progress dialog (this will block the EDT if modal = true)
        progressDialog.isVisible = true
    }

    /**
     * Validates that we have a working GitHub token before generating a project
     * that needs one. Throws [GitHubAuthException] if no token is set or GitHub
     * rejects it, so the create flow can prompt the user to (re-)authenticate.
     */
    @Throws(GitHubAuthException::class)
    private fun validateGitHubTokenIfNeeded() {
        if (!isGitHubRepositoryRequested()) {
            return
        }
        val token = githubTokenService.token
        if (token == null || token.isBlank()) {
            throw GitHubAuthException("No GitHub account selected. Please choose or add a GitHub account.")
        }
        try {
            gitHubUsernameService.gitHubUsername
        } catch (e: IOException) {
            throw GitHubAuthException(
                "GitHub authentication failed. Your token may be invalid or expired.",
                e
            )
        }
    }

    /**
     * Detects whether the given throwable (or any of its causes) represents a
     * GitHub authentication failure, including the raw 401 errors thrown by the
     * jdeploy CLI before it had a dedicated auth exception type.
     */
    private fun isGitHubAuthError(throwable: Throwable?): Boolean {
        var current = throwable
        while (current != null) {
            if (current is GitHubAuthException) {
                return true
            }
            val message = current.message?.lowercase() ?: ""
            if (message.contains("bad credentials")
                || message.contains("response code: 401")
                || message.contains("response code:401")
                || message.contains("http 401")
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    /**
     * Clears the rejected token, prompts the user to pick or add a GitHub account
     * with a valid token, then retries project creation. If the user cancels, the
     * original error is surfaced.
     */
    private fun promptReauthenticationAndRetry(originalError: Throwable) {
        // Clear the bad token so it isn't silently reused. setToken(null) would
        // throw (Properties reject null values) so clear with an empty string.
        githubTokenService.setToken("")
        JOptionPane.showMessageDialog(
            dialog,
            "GitHub authentication failed (the credentials were rejected).\n" +
                    "Please select or add a GitHub account with a valid personal access token.",
            "GitHub Authentication Required",
            JOptionPane.WARNING_MESSAGE
        )
        AccountChooserController(dialog, AccountType.GITHUB).show().thenAccept { account ->
            SwingUtilities.invokeLater {
                if (account != null && account.getAccessToken() != null) {
                    githubTokenService.setToken(account.getAccessToken())
                    // Remove the partially-created project from the failed attempt so
                    // the retry doesn't trip the "directory already exists" check.
                    cleanupPartialProject()
                    handleCreateProject()
                } else {
                    controllerFactory.createErrorController(originalError).run()
                }
            }
        }
    }

    /**
     * Deletes the project directory (and its `-releases` sibling) left behind by a
     * failed creation attempt, so the user can retry without a manual cleanup.
     */
    private fun cleanupPartialProject() {
        try {
            val projectDir = getProjectDirectory()
            if (projectDir.exists()) {
                projectDir.deleteRecursively()
            }
            val releasesDir = File(projectDir.parentFile, projectDir.name + "-releases")
            if (releasesDir.exists()) {
                releasesDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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