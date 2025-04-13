package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.ktswing.*
import ca.weblite.ktswing.coroutines.SwingDispatcher
import ca.weblite.ktswing.extensions.at
import ca.weblite.ktswing.swingx.searchField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JFrame
import javax.swing.JPanel

class TemplateChooserForm(model: Model): JPanel() {

    interface Model {
        suspend fun getProjectTemplates(): ProjectTemplates
    }

    init {
        layout = BorderLayout()
        // Initialize the form components here
        // For example, you can add buttons, labels, etc.
        splitPane {
            orientation = javax.swing.JSplitPane.HORIZONTAL_SPLIT
            leftComponent = panel {}
            rightComponent = panel {
                // Add components to the right side of the split pane
                layout = BorderLayout()

                panel {
                    layout = BorderLayout()
                    // Add components to the north section
                    panel {
                        layout = BorderLayout()

                        panel {
                            searchField {
                                // Set default size to show about 30 characters
                                preferredSize = java.awt.Dimension(300, 30)
                            }
                        } at BorderLayout.EAST

                    } at BorderLayout.NORTH

                    panel {
                        layout = FlowLayout(FlowLayout.LEFT)

                        panel {
                            label {
                                text = "Language"
                            }
                            comboBox<String> {
                                // Add items to the combo box
                                addItem("All")
                                addItem("Java")
                                addItem("Kotlin")
                                addItem("JRuby")
                            }
                        }

                        panel {
                            label {
                                text = "UI Toolkit"
                            }
                            comboBox<String> {
                                // Add items to the combo box
                                addItem("All")
                                addItem("Swing")
                                addItem("JavaFX")
                                addItem("Codename One")
                                addItem("FXGL")
                            }
                        }

                        panel {
                            label {
                                text = "Build Tool"
                            }
                            comboBox<String> {
                                // Add items to the combo box
                                addItem("All")
                                addItem("Maven")
                                addItem("Gradle")
                                addItem("Ant")
                            }
                        }
                    } at BorderLayout.CENTER

                } at BorderLayout.NORTH

                panel {
                    name = "templateListWrapper"
                    layout = BorderLayout()
                    val scope = CoroutineScope(SwingDispatcher)
                    scope.launch {
                        templateList(model.getProjectTemplates()) {

                        } at BorderLayout.CENTER
                    }
                } at BorderLayout.CENTER
            }
        } at BorderLayout.CENTER
    }

    // Test main method with frame containing this form
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            javax.swing.SwingUtilities.invokeLater {
                JFrame("Template Chooser Form").apply{
                    defaultCloseOperation = javax.swing.JFrame.EXIT_ON_CLOSE
                    val model = object : Model {
                        override suspend fun getProjectTemplates(): ProjectTemplates {
                            return DIContext.get(MockProjectTemplateRepository::class.java).findAll()
                        }
                    }
                    contentPane.add(TemplateChooserForm(model))
                    setSize(800, 600)
                    isVisible = true
                }
            }
        }
    }
}