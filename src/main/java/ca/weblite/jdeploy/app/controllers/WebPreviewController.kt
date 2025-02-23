package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.forms.WebPreviewForm
import ca.weblite.jdeploy.app.records.ProjectEditorContext
import ca.weblite.jdeploy.gui.tabs.CheerpJSettings
import ca.weblite.jdeploy.http.StaticFileServer
import ca.weblite.jdeploy.services.CheerpjService
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.EventQueue
import java.io.File
import java.net.URI
import java.net.URL
import javax.swing.*

class WebPreviewController(private val parentFrame: JFrame, private val context: ProjectEditorContext) : Runnable {

    override fun run() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this)
            return
        }
        generateWebAppWorker()
    }

    private fun generateWebApp():  CheerpjService.Result {
        val cheerpjService = CheerpjService(File(context.projectSettings().packageJsonPath), null)
        cheerpjService.setArgs(listOf("serve").toTypedArray())
        return cheerpjService.execute()
    }

    private fun generateWebAppWorker() {
        val progressDialog = JDialog(parentFrame, "Generating Web App...", false).apply {
            layout = BorderLayout()
            val progressBar = JProgressBar()
            progressBar.isIndeterminate = true
            add(progressBar, BorderLayout.CENTER)
            add(JLabel("Generating web app using CheerpJ Please wait..."), BorderLayout.NORTH)
            pack()
            setLocationRelativeTo(parentFrame)
            isAlwaysOnTop = true
        }

        progressDialog.isVisible = true
        object : SwingWorker<CheerpjService.Result, Void?>() {
            private var error: Exception? = null
            override fun doInBackground(): CheerpjService.Result {
                return generateWebApp()
            }
            override fun done() {
                progressDialog.dispose()
                try {
                    val result = get()
                    val server = result.server
                    val previewForm = WebPreviewForm()
                    previewForm.title = context.projectSettings().name + " - Web Preview"
                    previewForm.setLocationRelativeTo(parentFrame)
                    previewForm.isAlwaysOnTop = true
                    previewForm.addWindowListener(object : java.awt.event.WindowAdapter() {
                        override fun windowClosing(e: java.awt.event.WindowEvent) {
                            server.stop()
                        }
                    })

                    previewForm.nowServingLabel.text = "Now serving your web app at http://localhost:" + server.listeningPort
                    previewForm.openOpenInBrowserButton.addActionListener {
                        context.browse(URI("http://localhost:" + server.listeningPort));
                    }
                    previewForm.openWebAppDirectory.addActionListener {
                        context.desktopInterop.openDirectory(result.dest)
                    }
                    previewForm.stopButton.addActionListener {
                        server.stop();
                        previewForm.dispose();
                    }
                    previewForm.pack()
                    previewForm.isVisible = true
                } catch (e: Exception) {
                    error = e
                }
            }
        }.execute()
    }
}