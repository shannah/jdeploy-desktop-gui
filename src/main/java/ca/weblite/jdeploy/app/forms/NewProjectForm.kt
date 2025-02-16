package ca.weblite.jdeploy.app.forms

import ca.weblite.ktswing.*
import ca.weblite.ktswing.jgoodies.Form
import ca.weblite.ktswing.style.Stylesheet
import java.awt.FlowLayout
import java.awt.Frame
import javax.swing.*

class NewProjectForm(private val parentFrame: Frame): JFrame("Create New Project") {
    lateinit var displayName:JTextField private set
    lateinit var groupId:JTextField private set
    lateinit var artifactId:JTextField private set
    lateinit var projectLocation:JTextField private set
    lateinit var projectTemplate:JComboBox<String> private set
    lateinit var selectProjectLocationButton:JButton private set
    lateinit var refreshTemplatesButton:JButton private set
    lateinit var npmProjectName:JTextField private set
    lateinit var githubRepositoryUrl:JTextField private set
    lateinit var githubReleasesRepositoryUrl:JTextField private set
    lateinit var createGithubReleasesRepositoryCheckBox:JCheckBox private set
    lateinit var createGithubRepositoryUrlCheckBox:JCheckBox private set
    lateinit var npmRadioButton:JRadioButton private set
    lateinit var gitHubReleasesRadioButton:JRadioButton private set
    lateinit var createProjectButton:JButton private set

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
                    displayName = this
                } at x(3)
            }
            row {
                label{
                    text = "Group ID"
                } at x(1)

                textField{
                    groupId = this
                } at x(3)

            }

            row {
                label{
                    text = "Artifact ID"
                } at x(1)

                textField{
                    artifactId = this
                } at x(3)
            }

            row {
                label{
                    text = "Project Location"
                } at x(1)

                textField{
                    projectLocation = this
                } at x(3)

                button{
                    text = "Select ..."
                    selectProjectLocationButton = this
                } at x(5)
            }

            row {
                label{
                    text = "Project Template"
                } at x(1)

                comboBox<String>{
                    projectTemplate = this
                } at x(3)

                button{
                    text = "Refresh"
                    refreshTemplatesButton = this
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
                        gitHubReleasesRadioButton = this
                    }
                    radioButton {
                        text = "npm"
                        buttonGroup.add(this)
                        npmRadioButton = this
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
                    npmProjectName = this
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
                    githubRepositoryUrl = this
                } at x(3)

                checkBox {
                    text = "Create Repository"
                    createGithubRepositoryUrlCheckBox = this
                } at x(5)
            }

            row {
                label {
                    text = "Releases Repository URL"
                } at x(1)

                textField {
                    githubReleasesRepositoryUrl = this
                } at x(3)

                checkBox {
                    text = "Create Repository"
                    createGithubReleasesRepositoryCheckBox = this
                } at x(5)
            }

            row {
                panel {
                    layout = FlowLayout(FlowLayout.RIGHT)
                    button {
                        text = "Create Project"
                        createProjectButton = this
                    }
                } at xw(1, 5)
            }

        }
        stylesheet.apply(this)
        pack()
        setLocationRelativeTo(parentFrame)
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