package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import ca.weblite.jdeploy.app.tests.ProgrammableClock
import ca.weblite.jdeploy.app.tests.TestDIContext
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TouchProjectTest: BaseIntegrationTest() {
    private var projectDirectory: File? = null
    private val clock = ProgrammableClock()
    @BeforeAll
    override fun setup() {
        super.setup()
        projectDirectory = createTempDir("project")
        createMinimalPackageJsonAt(projectDirectory!!.absolutePath)
    }

    @AfterAll
    public fun cleanup() {
        projectDirectory?.deleteRecursively()
    }

    @Test
    fun testLoadProject() {

        val projectService = DIContext.get(ProjectService::class.java)
        var project = projectService.loadProject(projectDirectory!!.absolutePath)
        clock.setTimeInMillis(1000000);
        project = projectService.touch(project)
        assertEquals(1000, project.lastOpened)

        clock.setTimeInMillis(2000000);
        project = projectService.touch(project)
        assertEquals(2000, project.lastOpened)
    }

    override fun createDIContext(): DIContext {
        val ctx = TestDIContext()
        ctx.setClock(clock)
        return ctx
    }

    private fun createMinimalPackageJsonAt(path: String) {
        val packageJson = File(path + File.separator + "package.json")
        packageJson.writeText(
            """
            {
                "name": "my-project",
                "version": "1.0.0",
                "description": "My Project",
                "main": "index.js",
                "scripts": {
                    "test": "echo \"Error: no test specified\" && exit 1"
                },
                "author": "John Doe",
                "license": "ISC",
                "jdeploy": {
              
                }
            }
            """.trimIndent()
        )
    }
}