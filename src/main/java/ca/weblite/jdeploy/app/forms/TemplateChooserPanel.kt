package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.records.Template
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.ktswing.*
import ca.weblite.ktswing.coroutines.SwingDispatcher
import ca.weblite.ktswing.extensions.at
import ca.weblite.ktswing.extensions.onMouseClicked
import ca.weblite.ktswing.style.Stylesheet
import ca.weblite.ktswing.swingx.searchField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JPanel

class TemplateChooserPanel(model: Model, var tileDelegate: TemplateTileDelegate? = null): JPanel() {

    var selectedTemplateTile : TemplateTile? = null
    private set

    private lateinit var languageFilter: JComboBox<String>
    private lateinit var uiToolkitFilter: JComboBox<String>
    private lateinit var buildToolFilter: JComboBox<String>
    private lateinit var templateList: TemplateList
    private var templateListLoaded: Boolean = false
    private lateinit var projectTemplates: ProjectTemplates

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
                                languageFilter = this
                                // Add items to the combo box
                                addItem("All")
                                addItem("Java")
                                addItem("Kotlin")
                                addItem("JRuby")

                                addActionListener{
                                    updateTemplateList()
                                }
                            }
                        }

                        panel {
                            label {
                                text = "UI Toolkit"
                            }
                            comboBox<String> {
                                uiToolkitFilter = this
                                // Add items to the combo box
                                addItem("All")
                                addItem("Swing")
                                addItem("JavaFX")
                                addItem("Codename One")
                                addItem("FXGL")

                                addActionListener{
                                    updateTemplateList()
                                }
                            }
                        }

                        panel {
                            label {
                                text = "Build Tool"
                            }
                            comboBox<String> {
                                buildToolFilter = this
                                // Add items to the combo box
                                addItem("All")
                                addItem("Maven")
                                addItem("Gradle")
                                addItem("Ant")

                                addActionListener{
                                    updateTemplateList()
                                }
                            }
                        }
                    } at BorderLayout.CENTER

                } at BorderLayout.NORTH

                scrollPane {
                    name = "templateListWrapper"
                    horizontalScrollBarPolicy = javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                    border = BorderFactory.createEmptyBorder()
                    verticalScrollBar.unitIncrement = 32;
                    val scope = CoroutineScope(SwingDispatcher)
                    scope.launch {
                        System.out.println("Loading templates...")
                        projectTemplates = model.getProjectTemplates()
                        System.out.println("Loaded templates: ${projectTemplates.templates.size}")
                        updateFilters()
                        templateList(projectTemplates) {
                            tileDelegate = object: TemplateTileDelegate {
                                override fun openTemplateSources(template: Template) {
                                    this@TemplateChooserPanel.tileDelegate?.openTemplateSources(template)
                                }

                                override fun openTemplateDemoDownloadPage(template: Template) {
                                   this@TemplateChooserPanel.tileDelegate?.openTemplateDemoDownloadPage(template)
                                }

                                override fun openWebAppUrl(template: Template) {
                                    this@TemplateChooserPanel.tileDelegate?.openWebAppUrl(template)
                                }
                            }
                            templateList = this
                            templateListLoaded = true
                            updateTemplateList()
                        }

                        Stylesheet() {
                            val stylesheet = this
                            register(TemplateTile::class.java) {
                                onMouseClicked("selectTile") {
                                    selectedTemplateTile = this
                                    System.out.println("Mouse clicked");
                                    stylesheet.revalidate(this@TemplateChooserPanel)
                                }

                                if (selectedTemplateTile == this) {
                                    // Set border to something that looks selected
                                    border = BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(java.awt.Color(0x0078D7), 2),
                                        BorderFactory.createEmptyBorder(4, 4, 4, 4)
                                    )
                                } else {
                                    border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
                                }
                            }
                        }.apply(this@TemplateChooserPanel)
                    }
                } at BorderLayout.CENTER
            }
        } at BorderLayout.CENTER
    }

    private fun updateFilters() {

        val uiToolkit = uiToolkitFilter.selectedItem as String
        val buildTool = buildToolFilter.selectedItem as String
        val language = languageFilter.selectedItem as String

        uiToolkitFilter.removeAllItems()
        buildToolFilter.removeAllItems()
        languageFilter.removeAllItems()
        uiToolkitFilter.addItem("All")
        buildToolFilter.addItem("All")
        languageFilter.addItem("All")

        projectTemplates.getUiToolkits().forEach { uiToolkitFilter.addItem(it)}
        projectTemplates.getBuildTools().forEach { buildToolFilter.addItem(it) }
        projectTemplates.getProgrammingLanguages().forEach { languageFilter.addItem(it) }

        uiToolkitFilter.selectedItem = uiToolkit
        buildToolFilter.selectedItem = buildTool
        languageFilter.selectedItem = language
    }

    private fun updateTemplateList() {
        if (!templateListLoaded) {
            return
        }
        val uiToolkit = uiToolkitFilter.selectedItem
        val buildTool = buildToolFilter.selectedItem
        val language = languageFilter.selectedItem

        templateList.filter { template ->
            (uiToolkit == "All" || template.uiToolkit == uiToolkit) &&
            (buildTool == "All" || template.buildTool == buildTool) &&
            (language == "All" || template.programmingLanguage == language)
        }
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
                    contentPane.add(TemplateChooserPanel(model))
                    setSize(800, 600)
                    isVisible = true
                }
            }
        }
    }
}