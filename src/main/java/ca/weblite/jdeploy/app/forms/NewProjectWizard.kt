package ca.weblite.jdeploy.app.forms

import ca.weblite.ktswing.button
import ca.weblite.ktswing.extensions.at
import ca.weblite.ktswing.panel
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class NewProjectWizard(templateChooserModel: TemplateChooserPanel.Model): JPanel(), NewProjectFormInterface {
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
    override val createProjectButton: JButton

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
        preferredSize = Dimension(800, 600)
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
        createProjectButton = projectPanel.createProjectButton

        panel {
            layout = cardLayout
            cardPanel = this

            val templateChooserPanel = TemplateChooserPanel(templateChooserModel)
            add(templateChooserPanel, steps[0])
            add(projectPanel, steps[1])

        } at BorderLayout.CENTER

        panel {
            layout = FlowLayout(FlowLayout.RIGHT)
            button {
                text = "Back"
                addActionListener {
                    if (currentStep > 0) {
                        currentStep--
                        updateStep()
                    }
                }
            }
            button {
                text = "Next"
                addActionListener {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                        updateStep()
                    }
                }
            }
            button {
                text = "Finish"
                addActionListener {
                    // Handle finish action
                    println("Finish button clicked")
                }
            }
        } at BorderLayout.SOUTH

    }

    private fun updateStep() {
        // Update the current step in the wizard
        cardLayout.show(cardPanel, steps[currentStep])
    }
}