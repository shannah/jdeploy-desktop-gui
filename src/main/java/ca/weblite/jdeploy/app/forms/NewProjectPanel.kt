package ca.weblite.jdeploy.app.forms

import ca.weblite.ktswing.*
import ca.weblite.ktswing.extensions.at
import ca.weblite.ktswing.jgoodies.Form
import ca.weblite.ktswing.jgoodies.form
import ca.weblite.ktswing.style.Stylesheet
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import javax.swing.*

class NewProjectPanel(): JPanel(), NewProjectFormInterface {
    override lateinit var displayName:JTextField private set
    override lateinit var groupId:JTextField private set
    override lateinit var artifactId:JTextField private set
    override lateinit var projectLocation:JTextField private set
    override lateinit var projectTemplate:JComboBox<String> private set
    override lateinit var selectProjectLocationButton:JButton private set
    override lateinit var refreshTemplatesButton:JButton private set
    override lateinit var npmProjectName:JTextField private set
    override lateinit var githubRepositoryUrl:JTextField private set
    override lateinit var githubReleasesRepositoryUrl:JTextField private set
    override lateinit var createGithubReleasesRepositoryCheckBox:JCheckBox private set
    override lateinit var createGithubRepositoryUrlCheckBox:JCheckBox private set
    override lateinit var npmRadioButton:JRadioButton private set
    override lateinit var gitHubReleasesRadioButton:JRadioButton private set
    override lateinit var createProjectButton:JButton private set

    init{
        val stylesheet = Stylesheet() {
            textField {
                columns = 20
            }
        }
        layout = BorderLayout()
        preferredSize = Dimension(1024, 600)
        form(cols="default, \$lcgap, default:grow, \$lcgap, default") {
            border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
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
                    toolTipText = "Enter repository in format: owner/repository (e.g., octocat/Hello-World)"
                    // Set placeholder-like text using a custom property
                    putClientProperty("JTextField.placeholderText", "owner/repository")
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
                    toolTipText = "Enter repository in format: owner/repository (e.g., octocat/Hello-World-Releases)"
                    // Set placeholder-like text using a custom property
                    putClientProperty("JTextField.placeholderText", "owner/repository")
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

        } at BorderLayout.CENTER

        stylesheet.apply(this)
    }
}