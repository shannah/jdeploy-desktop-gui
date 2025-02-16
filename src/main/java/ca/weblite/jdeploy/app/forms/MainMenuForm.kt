package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.app.records.Project
import ca.weblite.ktswing.*
import ca.weblite.ktswing.extensions.classList
import ca.weblite.ktswing.extensions.onMouseEntered
import ca.weblite.ktswing.extensions.onMouseExited
import ca.weblite.ktswing.style.Stylesheet
import ca.weblite.ktswing.swingx.searchField
import org.jdesktop.swingx.JXSearchField
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.swing.FontIcon
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel

val stylesheet = Stylesheet() {
    panel("#center"){} chain button {
        alignmentX = 0.5f
        border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
        background = Color(0,0,0,0)
        cursor = java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        onMouseEntered(this@Stylesheet) {
            classList.add("hover")
            this@Stylesheet.revalidate(this)
        }

        onMouseExited(this@Stylesheet) {
            classList.remove("hover")
            this@Stylesheet.revalidate(this)
        }
    }

    button(".hover") {
        background = Color(0,0,0,10)
    }
}

class MainMenuForm(): JPanel() {
    private var openButton: JButton? = null
    private var recentProjects: JList<Project>? = null
    private var openRecentButton: JButton? = null
    private var importProject: JButton? = null
    private var createProjectButton: JButton? = null
    private var heroGraphicWrapper: JPanel? = null
    private var searchField: JXSearchField? = null

    fun getSearchField(): JXSearchField {
        return searchField!!
    }
    fun getOpenButton(): JButton {
        return openButton!!
    }

    fun getRecentProjects(): JList<Project> {
        return recentProjects!!
    }

    fun getOpenRecentButton(): JButton {
        return openRecentButton!!
    }

    fun getImportProject(): JButton {
        return importProject!!
    }

    fun getCreateProjectButton(): JButton {
        return createProjectButton!!
    }

    fun getHeroGraphicWrapper(): JPanel {
        return heroGraphicWrapper!!
    }

    init {
        splitPane {
            leftComponent = borderPane{
                border = BorderFactory.createEmptyBorder(4,4,4,4)
                north = searchField{
                    searchField = this
                }
                center = scrollPane {
                    recentProjects = JList<Project>(arrayOf())
                    setViewportView(recentProjects)
                }
                south = button {
                    text = "Open recent"
                    toolTipText = "Open the selected recent project"
                    openRecentButton = this
                }

            }
            rightComponent = borderPane {
                north = label {
                    text = "jDeploy Main Menu"
                }

                center = panel {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    name = "center"
                    panel {
                        heroGraphicWrapper = this
                    }
                    button {
                        openButton = this
                        text = "Open Project..."
                        icon = FontIcon.of(Material.FOLDER_OPEN, 24, Color.BLACK)
                        toolTipText = "Open an existing Java project that is already configured to use jDeploy"
                    }
                    button {
                        text = "Configure existing project for jDeploy..."
                        importProject = this
                        icon = FontIcon.of(Material.FILE_DOWNLOAD, 24, Color.BLACK)
                        toolTipText = "Set up an existing Java project to use jDeploy"
                    }
                    button {
                        text = "Create new project..."
                        createProjectButton = this
                        toolTipText = "Create a new Java project that is configured to use jDeploy"
                        icon = FontIcon.of(Material.CREATE, 24, Color.BLACK)
                    }
                }
            }

        }
        stylesheet.apply(this)
    }
}
