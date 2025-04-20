package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.app.records.Template
import ca.weblite.swinky.button
import ca.weblite.swinky.extensions.at
import ca.weblite.swinky.panel
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class NewProjectWizard(
    templateChooserModel: TemplateChooserPanel.Model,
    var tileDelegate: TemplateTileDelegate? = null
): JPanel(), NewProjectFormInterface {
    // Constants
    companion object {
        const val SELECT_PROJECT_TEMPLATE_STEP = 0
        const val PROJECT_DETAILS_STEP = 1
    }

    private lateinit var templateChooserPanel: TemplateChooserPanel
    private lateinit var nextButton: JButton
    private lateinit var backButton: JButton

    override val displayName: JTextField
    override val groupId: JTextField
    override val artifactId: JTextField
    override val projectLocation: JTextField
    override val projectTemplate: JComboBox<String>
    override val selectProjectLocationButton: JButton
    override val refreshTemplatesButton: JButton
    override val npmProjectName: JTextField
    override val githubRepositoryUrl: JTextField
    override val githubReleasesRepositoryUrl: JTextField
    override val createGithubReleasesRepositoryCheckBox: JCheckBox
    override val createGithubRepositoryUrlCheckBox: JCheckBox
    override val npmRadioButton: JRadioButton
    override val gitHubReleasesRadioButton: JRadioButton
    lateinit override var createProjectButton: JButton

    private val cardLayout = CardLayout()
    lateinit private var cardPanel: JPanel
    private var currentStep = 0;
    private val steps = listOf(
        "Select Project Template",
        "Project Details",
    )
    init {
        // Initialize the panel here
        layout = BorderLayout()
        preferredSize = Dimension(1024, 600)
        val projectPanel = NewProjectPanel()
        displayName = projectPanel.displayName
        groupId = projectPanel.groupId
        artifactId = projectPanel.artifactId
        projectLocation = projectPanel.projectLocation
        projectTemplate = projectPanel.projectTemplate
        selectProjectLocationButton = projectPanel.selectProjectLocationButton
        refreshTemplatesButton = projectPanel.refreshTemplatesButton
        npmProjectName = projectPanel.npmProjectName
        githubRepositoryUrl = projectPanel.githubRepositoryUrl
        githubReleasesRepositoryUrl = projectPanel.githubReleasesRepositoryUrl
        createGithubReleasesRepositoryCheckBox = projectPanel.createGithubReleasesRepositoryCheckBox
        createGithubRepositoryUrlCheckBox = projectPanel.createGithubRepositoryUrlCheckBox
        npmRadioButton = projectPanel.npmRadioButton
        gitHubReleasesRadioButton = projectPanel.gitHubReleasesRadioButton
        projectPanel.createProjectButton.isVisible = false

        panel {
            layout = cardLayout
            cardPanel = this

            templateChooserPanel = TemplateChooserPanel(templateChooserModel).apply {
                tileDelegate = object : TemplateTileDelegate {
                    override fun openTemplateSources(template: Template) {
                        this@NewProjectWizard.tileDelegate?.openTemplateSources(template)
                    }

                    override fun openTemplateDemoDownloadPage(template: Template) {
                        // Handle opening demo download page
                        this@NewProjectWizard.tileDelegate?.openTemplateDemoDownloadPage(template)
                    }

                    override fun openWebAppUrl(template: Template) {
                        this@NewProjectWizard.tileDelegate?.openWebAppUrl(template)
                    }
                }
            }
            add(templateChooserPanel, steps[0])
            add(projectPanel, steps[1])

        } at BorderLayout.CENTER

        panel {
            layout = FlowLayout(FlowLayout.RIGHT)
            button {
                text = "Back"
                backButton = this
                addActionListener {
                    if (currentStep > 0) {
                        currentStep--
                        updateStep()
                    }
                }
            }
            button {
                text = "Next"
                nextButton = this
                addActionListener {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                        updateStep()
                    }
                }
            }
            button {
                text = "Finish"
                createProjectButton = this
            }
        } at BorderLayout.SOUTH
        updateStep()
    }

    private fun updateStep() {
        if (currentStep == PROJECT_DETAILS_STEP) {
            if (templateChooserPanel.selectedTemplateTile !== null) {
                System.out.println("Selected template: ${templateChooserPanel.selectedTemplateTile!!.model.name}")
                projectTemplate.addItem(templateChooserPanel.selectedTemplateTile!!.model.name)
                projectTemplate.selectedItem = templateChooserPanel.selectedTemplateTile?.model?.name
            }
        }
        backButton.isEnabled = currentStep > 0
        nextButton.isEnabled = currentStep < steps.size - 1
        // Update the current step in the wizard
        cardLayout.show(cardPanel, steps[currentStep])
    }
}