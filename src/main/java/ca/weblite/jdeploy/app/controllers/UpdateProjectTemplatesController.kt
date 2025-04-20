package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.services.ProjectTemplateCatalog
import ca.weblite.swinky.coroutines.SwingDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FlowLayout
import java.awt.Frame
import javax.swing.*

class UpdateProjectTemplatesController(
    private val templateCatalog: ProjectTemplateCatalog,
    private val parent: Frame // Or a reference to any other parent window
) {
    fun update() {
        // Create a dialog to show the progress
        val dialog = JDialog(parent, "Updating Templates", true).apply {
            layout = FlowLayout()
            add(JLabel("Updating template catalog. Please wait..."))
            val progressBar = JProgressBar()
            progressBar.isIndeterminate = true
            add(progressBar)
            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
            pack()
            setLocationRelativeTo(parent)
        }

        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                // Perform the long-running update in the background
                templateCatalog.update()
            }

            override fun done() {
                // Dispose of the dialog once the update completes
                dialog.dispose()
            }
        }

        // Start the background task
        worker.execute()

        // Show the dialog (this will block the EDT if modal = true)
        dialog.isVisible = true
    }

    suspend fun updateSuspending() {
        // Perform the long-running update in the background
        val dialog: JDialog
        withContext(SwingDispatcher) {
            dialog = JDialog(parent, "Updating Templates", true).apply {
                layout = FlowLayout()
                add(JLabel("Updating template catalog. Please wait..."))
                val progressBar = JProgressBar()
                progressBar.isIndeterminate = true
                add(progressBar)
                defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
                pack()
                setLocationRelativeTo(parent)
            }
        }

        withContext(Dispatchers.IO) {
            // Perform the long-running update in the background
            templateCatalog.update()
        }

        withContext(SwingDispatcher) {
            // Dispose of the dialog once the update completes
            dialog.dispose()
        }
    }
}
