package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.repositories.MockProjectTemplateRepository
import ca.weblite.ktswing.coroutines.SwingDispatcher
import ca.weblite.ktswing.extensions.createComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Container
import javax.swing.JPanel

class TemplateList(model: ProjectTemplates): JPanel() {
    init {
        val templateTiles = model.templates.map { TemplateTile(it) }
        templateTiles.forEach { add(it) }
    }

    // Add a test main method to run this class independently
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Create a sample ProjectTemplates object
            val templateRepository = DIContext.get(MockProjectTemplateRepository::class.java)
            val scope = CoroutineScope(SwingDispatcher)
            scope.launch {
                // Create an instance of TemplateList with the sample data
                val templateList = TemplateList(templateRepository.findAll())
                // Display the template list in a JFrame or any other container
                javax.swing.JFrame("Template List").apply {
                    defaultCloseOperation = javax.swing.WindowConstants.EXIT_ON_CLOSE
                    contentPane.add(templateList)
                    pack()
                    isVisible = true
                }
            }

        }
    }
}

fun Container.templateList(model: ProjectTemplates, init: TemplateList.() -> Unit = {}): TemplateList =
    createComponent(factory = { TemplateList(model) }, init = init)