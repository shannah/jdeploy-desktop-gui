package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.forms.TemplateChooserPanel.Model
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.ktswing.style.Stylesheet
import java.awt.Frame
import javax.swing.*

class NewProjectForm(
    private val parentFrame: Frame,
    private val templateChooserModel: TemplateChooserPanel.Model
): JFrame("Create New Project"), NewProjectFormInterface {
    override val displayName:JTextField
    override val groupId:JTextField
    override val artifactId:JTextField
    override val projectLocation:JTextField
    override val projectTemplate:JComboBox<String>
    override val selectProjectLocationButton:JButton
    override val refreshTemplatesButton:JButton
    override val npmProjectName:JTextField
    override val githubRepositoryUrl:JTextField
    override val githubReleasesRepositoryUrl:JTextField
    override val createGithubReleasesRepositoryCheckBox:JCheckBox
    override val createGithubRepositoryUrlCheckBox:JCheckBox
    override val npmRadioButton:JRadioButton
    override val gitHubReleasesRadioButton:JRadioButton
    override val createProjectButton:JButton

    init{
        val stylesheet = Stylesheet() {
            textField {
                columns = 20
            }
        }

        val projectWizard = NewProjectWizard(templateChooserModel)
        contentPane = projectWizard
        displayName = projectWizard.displayName
        groupId = projectWizard.groupId
        artifactId = projectWizard.artifactId
        projectLocation = projectWizard.projectLocation
        projectTemplate = projectWizard.projectTemplate
        selectProjectLocationButton = projectWizard.selectProjectLocationButton
        refreshTemplatesButton = projectWizard.refreshTemplatesButton
        npmProjectName = projectWizard.npmProjectName
        githubRepositoryUrl = projectWizard.githubRepositoryUrl
        githubReleasesRepositoryUrl = projectWizard.githubReleasesRepositoryUrl
        createGithubReleasesRepositoryCheckBox = projectWizard.createGithubReleasesRepositoryCheckBox
        createGithubRepositoryUrlCheckBox = projectWizard.createGithubRepositoryUrlCheckBox
        npmRadioButton = projectWizard.npmRadioButton
        gitHubReleasesRadioButton = projectWizard.gitHubReleasesRadioButton
        createProjectButton = projectWizard.createProjectButton

        stylesheet.apply(this)
        pack()
        setLocationRelativeTo(parentFrame)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val model = object : Model {
                override suspend fun getProjectTemplates(): ProjectTemplates {
                    return DIContext.get(MockProjectTemplateRepository::class.java).findAll()
                }
            }
            NewProjectForm(JFrame(), model).apply {
                isVisible = true
            }
        }
    }

}