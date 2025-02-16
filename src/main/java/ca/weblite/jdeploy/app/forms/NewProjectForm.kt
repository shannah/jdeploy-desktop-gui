package ca.weblite.jdeploy.app.forms

import ca.weblite.ktswing.*
import ca.weblite.ktswing.jgoodies.Form
import ca.weblite.ktswing.style.Stylesheet
import java.awt.FlowLayout
import java.awt.Frame
import javax.swing.*

class NewProjectForm(private val parentFrame: Frame): JFrame("Create New Project") {
    private lateinit var _displayName:JTextField
    val displayName: JTextField
        get() = _displayName

    private lateinit var _groupId:JTextField
    val groupId: JTextField
        get() = _groupId
    private lateinit var _artifactId:JTextField
    val artifactId: JTextField
        get() = _artifactId
    private lateinit var _projectLocation:JTextField
    val projectLocation: JTextField
        get() = _projectLocation
    private lateinit var _projectTemplate:JComboBox<String>
    val projectTemplate: JComboBox<String>
        get() = _projectTemplate
    private lateinit var _selectProjectLocationButton:JButton
    val selectProjectLocationButton: JButton
        get() = _selectProjectLocationButton
    private lateinit var _refreshTemplatesButton:JButton
    val refreshTemplatesButton: JButton
        get() = _refreshTemplatesButton
    private lateinit var _npmProjectName:JTextField
    val npmProjectName: JTextField
        get() = _npmProjectName
    private lateinit var _githubRepositoryUrl:JTextField
    val githubRepositoryUrl: JTextField
        get() = _githubRepositoryUrl
    private lateinit var _githubReleasesRepositoryUrl:JTextField
    val githubReleasesRepositoryUrl: JTextField
        get() = _githubReleasesRepositoryUrl
    private lateinit var _createGithubReleasesRepositoryCheckBox:JCheckBox
    val createGithubReleasesRepositoryCheckBox: JCheckBox
        get() = _createGithubReleasesRepositoryCheckBox
    private lateinit var _createGithubRepositoryUrlCheckBox:JCheckBox
    val createGithubRepositoryUrlCheckBox: JCheckBox
        get() = _createGithubRepositoryUrlCheckBox
    private lateinit var _npmRadioButton:JRadioButton
    val npmRadioButton: JRadioButton
        get() = _npmRadioButton
    private lateinit var _gitHubReleasesRadioButton:JRadioButton
    val gitHubReleasesRadioButton: JRadioButton
        get() = _gitHubReleasesRadioButton
    private lateinit var _createProjectButton:JButton
    val createProjectButton: JButton
        get() = _createProjectButton

    init{
        val stylesheet = Stylesheet() {
            textField {
                columns = 20
            }
        }

        contentPane = Form(cols="2*(default, \$lcgap), default").apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            row {
                label{
                    text = "Application Display Name"
                } at x(1)

                textField{
                    _displayName = this
                } at x(3)
            }
            row {
                label{
                    text = "Group ID"
                } at x(1)

                textField{
                    _groupId = this
                } at x(3)

            }

            row {
                label{
                    text = "Artifact ID"
                } at x(1)

                textField{
                    _artifactId = this
                } at x(3)
            }

            row {
                label{
                    text = "Project Location"
                } at x(1)

                textField{
                    _projectLocation = this
                } at x(3)

                button{
                    text = "Select ..."
                    _selectProjectLocationButton = this
                } at x(5)
            }

            row {
                label{
                    text = "Project Template"
                } at x(1)

                comboBox<String>{
                    _projectTemplate = this
                } at x(3)

                button{
                    text = "Refresh"
                    _refreshTemplatesButton = this
                } at x(5)
            }

            row {
                separator("Publish settings"){} at xw(1, 5)
            }

            row {
                label {
                    text = "Publish to"
                } at x(1)

                panel {
                    layout=FlowLayout(FlowLayout.LEFT)
                    val buttonGroup = ButtonGroup()
                    radioButton{
                        text = "GitHub"
                        buttonGroup.add(this)
                        _gitHubReleasesRadioButton = this
                    }
                    radioButton {
                        text = "npm"
                        buttonGroup.add(this)
                        _npmRadioButton = this
                    }
                } at xw(3, 3)
            }

            row {
                separator("npm Settings"){} at xw(1, 5)
            }

            row {
                label {
                    text = "Project Name"
                } at x(1)

                textField {
                    _npmProjectName = this
                } at x(3)
            }

            row {
                separator("GitHub Settings"){} at xw(1, 5)
            }

            row {
                label {
                    text = "Repository URL"
                } at x(1)

                textField {
                    _githubRepositoryUrl = this
                } at x(3)

                checkBox {
                    text = "Create Repository"
                    _createGithubRepositoryUrlCheckBox = this
                } at x(5)
            }

            row {
                label {
                    text = "Releases Repository URL"
                } at x(1)

                textField {
                    _githubReleasesRepositoryUrl = this
                } at x(3)

                checkBox {
                    text = "Create Repository"
                    _createGithubReleasesRepositoryCheckBox = this
                } at x(5)
            }

            row {
                panel {
                    layout = FlowLayout(FlowLayout.RIGHT)
                    button {
                        text = "Create Project"
                        _createProjectButton = this
                    }
                } at xw(1, 5)
            }

        }
        stylesheet.apply(this)
        pack()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            NewProjectForm(JFrame()).apply {
                isVisible = true
            }
        }
    }

}