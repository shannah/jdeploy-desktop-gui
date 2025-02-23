package ca.weblite.jdeploy.app.forms

import ca.weblite.ktswing.BorderPane
import ca.weblite.ktswing.button
import ca.weblite.ktswing.label
import ca.weblite.ktswing.panel
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

class WebPreviewForm: JFrame("Web Preview") {
    lateinit var openOpenInBrowserButton: JButton private set
    lateinit var stopButton: JButton private set
    lateinit var nowServingLabel: JLabel private set
    lateinit var openWebAppDirectory: JButton private set
    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        setSize(800, 600)

        contentPane = BorderPane().apply {
            center = panel {
                label {
                    text = "Now serving your web app at http://localhost:8080"
                    nowServingLabel = this
                }
            }
            south = panel {
                layout = FlowLayout(FlowLayout.CENTER)
                button {
                    text = "Open in Browser"
                    openOpenInBrowserButton = this
                }
                button {
                    text = "Open Web App Directory"
                    openWebAppDirectory = this
                }
                button {
                    text = "Stop"
                    stopButton = this
                }
            }
        }
    }
}