package ca.weblite.jdeploy.app.forms

import ca.weblite.swinky.*
import ca.weblite.swinky.jgoodies.form
import ca.weblite.swinky.style.Stylesheet
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class ImportProjectFormKts: JPanel() {

    lateinit var projectDirectory: JTextField private set
    lateinit var browseProjectDirectory: JButton private set
    lateinit var generateGitHubWorkflow: JCheckBox private set
    lateinit var cancelButton: JButton private set
    lateinit var importButton: JButton private set

    init {
        layout = BorderLayout()
        val styleSheet = Stylesheet(){
            textField {
                columns = 20
            }
        }
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        val content =  BorderPane().apply{
            layout = BorderLayout()
            center = form(cols="2*(default, \$lcgap), default"){
                row {
                    label {
                        text = "Project directory"
                    } at x(1)

                    textField {
                        projectDirectory = this
                    } at x(3)

                    button {
                        text = "Select..."
                        browseProjectDirectory = this
                    } at x(5)
                }
                row {
                    checkBox{
                        text = "Generate GitHub Workflow"
                        generateGitHubWorkflow = this
                    } at x(3)
                }
            }

            south = panel{
                layout = FlowLayout(FlowLayout.RIGHT)
                button{
                    text = "Cancel"
                    cancelButton = this
                }
                button{
                    text = "Import"
                    importButton = this
                }
            }
        }
        add(BorderLayout.CENTER, content)
        styleSheet.apply(this)
    }
}