package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.factories.ControllerFactory
import ca.weblite.jdeploy.app.forms.MainMenuForm
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.services.Edt
import ca.weblite.jdeploy.app.services.ProjectService
import ca.weblite.jdeploy.app.swing.ResponsiveImagePanel
import ca.weblite.jdeploy.app.views.mainMenu.ProjectListCellRenderer
import ca.weblite.swinky.coroutines.SwingDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent

class MainMenuViewController : JFrameViewController() {
    private val projectService: ProjectService = DIContext.get(ProjectService::class.java)
    private val controllerFactory: ControllerFactory = DIContext.get(ControllerFactory::class.java)
    override fun initUI(): JComponent {
        val edt = DIContext.get(Edt::class.java)

        val mainMenu = MainMenuForm()

        mainMenu.getOpenButton().addActionListener { e: ActionEvent? ->
            edt.invokeLater(
                OpenProjectController(
                    frame
                )
            )
        }

        mainMenu.getRecentProjects().model = buildRecentProjectsModel()
        mainMenu.getRecentProjects().cellRenderer = ProjectListCellRenderer()

        val openRecentAction = openRecentAction(mainMenu)

        // openRecentAction should only be enabled when there is a project in the recent projects list selected
        mainMenu.getRecentProjects().selectionModel.addListSelectionListener { e: ListSelectionEvent? ->
            openRecentAction.isEnabled = mainMenu.getRecentProjects().selectedValue != null
        }
        mainMenu.getOpenRecentButton().action = openRecentAction

        mainMenu.getRecentProjects().addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    openRecentAction.actionPerformed(null)
                }
            }
        })
        mainMenu.getSearchField().document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                filterProjects()
            }

            override fun removeUpdate(e: DocumentEvent) {
                filterProjects()
            }

            override fun changedUpdate(e: DocumentEvent) {
                filterProjects()
            }

            fun filterProjects() {
                val query = mainMenu.getSearchField().text
                val model = mainMenu.getRecentProjects().model as DefaultListModel<Project>
                model.clear()
                for (project in projectService.findRecent()) {
                    if (!query.isEmpty() && !project.name.lowercase(Locale.getDefault())
                            .contains(query.lowercase(Locale.getDefault()))
                    ) {
                        continue
                    }
                    model.addElement(project)
                }
            }
        })

        mainMenu.getImportProject().addActionListener { e: ActionEvent? ->
            ImportProjectViewController(
                frame
            ).run()
        }

        mainMenu.getCreateProjectButton().addActionListener { e: ActionEvent? ->
            val newProjectController = NewProjectController(frame)
            // create coroutine scope
            val scope = CoroutineScope(SwingDispatcher)
            scope.launch {
                // show the new project controller
                newProjectController.show()
            }
        }

        mainMenu.getHeroGraphicWrapper().add(
            ResponsiveImagePanel(
                "/ca/weblite/jdeploy/app/assets/jdeploy-home-hero.png"
            ),
            BorderLayout.CENTER
        )

        return mainMenu
        //return new TestForm().getMhPanel();
    }

    override fun onBeforeShow() {
        frame.title = "jDeploy"
    }

    private fun buildRecentProjectsModel(): ListModel<Project> {
        val model = DefaultListModel<Project>()
        for (project in projectService.findRecent()) {
            model.addElement(project)
        }

        return model
    }

    private fun openRecentAction(mainMenu: MainMenuForm): Action {
        return object : AbstractAction() {
            init {
                putValue(NAME, "Open")
                putValue(SHORT_DESCRIPTION, "Open the selected recent project")
                isEnabled = false
            }

            override fun actionPerformed(e: ActionEvent) {
                val project = mainMenu.getRecentProjects().selectedValue
                if (project != null) {
                    OpenProjectController(frame, project.path).run()
                }
            }
        }
    }
}
