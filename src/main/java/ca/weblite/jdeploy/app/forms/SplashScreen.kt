package ca.weblite.jdeploy.app.forms

import ca.weblite.swinky.BorderPane
import ca.weblite.swinky.label
import ca.weblite.swinky.panel
import ca.weblite.swinky.progressBar
import ca.weblite.swinky.swingx.imagePanel
import org.jdesktop.swingx.JXImagePanel
import org.jdesktop.swingx.VerticalLayout
import javax.swing.BorderFactory
import javax.swing.JFrame

class SplashScreen: JFrame() {
    init {

        contentPane = BorderPane().apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(java.awt.Color(0,0,0,50), 1),
                BorderFactory.createEmptyBorder(20,20,20,20)
            )

            north= panel{
                layout = VerticalLayout()
                label{
                    horizontalAlignment = javax.swing.SwingConstants.CENTER;
                    text = "jDeploy"
                    font = font.deriveFont(40f)
                    border = BorderFactory.createEmptyBorder(10,10,10,10)
                }
                label{
                    horizontalAlignment = javax.swing.SwingConstants.CENTER;
                    text = "Deploy your jar as a desktop application"
                    font = font.deriveFont(16f)
                }
            }

            center = imagePanel(javaClass.getResource("/ca/weblite/jdeploy/app/assets/jdeploy-home-hero.png")!!){
                border = BorderFactory.createEmptyBorder(20,20,20,20)
                style = JXImagePanel.Style.SCALED_KEEP_ASPECT_RATIO
            }

            south = panel{
                layout=VerticalLayout()

                progressBar{
                    isIndeterminate = true
                    border=BorderFactory.createEmptyBorder(10,10,10,10)
                }

                label{
                    val currentYear = java.time.LocalDate.now().year
                    text = "Copyright (c) 2021-${currentYear} Web Lite Solutions Corp.  All rights reserved."
                    font = font.deriveFont(10f)
                }
            }

        }

        isUndecorated = true
        setSize(400, 300)
        setLocationRelativeTo(null)
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }

    public fun showSplash() {
        isVisible = true
        bringToFront(this)

        // Periodically check to see if any other JFrames are visible.
        // close if it finds any
        Thread {
            while (isVisible) {
                Thread.sleep(1000)
                if (JFrame.getFrames().any { it.isVisible && it != this }) {
                    dispose()
                }
            }
        }.start()
    }

    private fun bringToFront(frame: JFrame) {
        // Make sure the window is in normal state
        frame.extendedState = JFrame.NORMAL

        // Temporarily set always on top to try to grab focus
        frame.isAlwaysOnTop = true

        // Bring to front
        frame.toFront()

        // Request focus for the frame
        frame.requestFocus()
    }
}