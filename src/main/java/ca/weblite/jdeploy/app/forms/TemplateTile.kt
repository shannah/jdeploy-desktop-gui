package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.Template
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.jdeploy.app.swing.components.tagLabel
import ca.weblite.ktswing.button
import ca.weblite.ktswing.coroutines.SwingDispatcher
import ca.weblite.ktswing.extensions.at
import ca.weblite.ktswing.extensions.classList
import ca.weblite.ktswing.label
import ca.weblite.ktswing.panel
import ca.weblite.ktswing.swingx.imagePanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jdesktop.swingx.JXImagePanel
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.swing.FontIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.net.URI
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class TemplateTile(model: Template): JPanel() {
    lateinit var createButton: JButton
        private set

    init {
        layout = BorderLayout()
        toolTipText = model.description
        panel {
            label {
                text = model.displayName
                classList.add("template-name")
            }
        } at BorderLayout.NORTH
        imagePanel(imageUrl = URI(model.tileImageUrl).toURL()) {
            style = JXImagePanel.Style.SCALED_KEEP_ASPECT_RATIO
            preferredSize = java.awt.Dimension(256, 256)
        } at  BorderLayout.CENTER
        panel {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            panel {
                classList.add("tags")
                tagLabel {
                    text = model.programmingLanguage
                    classList.add("language-tag")
                    background = Color(0xD0E8FF)
                    foreground = Color(0x004080)
                }

                tagLabel {
                    text = model.uiToolkit
                    classList.add("ui-toolkit-tag")
                    background = Color(0xDFF5DD)
                    foreground = Color(0x1B5E20)
                }

                tagLabel {
                    text = model.buildTool
                    classList.add("build-tool-tag")
                    background = Color(0xFFEBC8)
                    foreground = Color(0x8B5A00)
                }
            }

            panel {
                classList.add("authorPanel")
                label {
                    text = "By ${model.author}"
                    classList.add("author")
                }
            }

            panel {
                classList.add("buttonsPanel")
                layout = FlowLayout(FlowLayout.RIGHT)
                button {
                    text = "Create"
                    icon = FontIcon.of(Material.ADD)
                    toolTipText = "Create a new project from this template"

                    createButton = this
                }
            }


        } at BorderLayout.SOUTH
    }

    // create a test main method that will open in JFrame
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val scope = CoroutineScope(SwingDispatcher)
            scope.launch {
                val frame = javax.swing.JFrame("Template Tile Test")
                frame.defaultCloseOperation = javax.swing.JFrame.EXIT_ON_CLOSE
                frame.setSize(400, 300)
                val templateTileRepository = DIContext.get(MockProjectTemplateRepository::class.java)
                frame.contentPane = TemplateTile(templateTileRepository.findAll().templates.get(0))
                frame.isVisible = true
            }
        }
    }

}