package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoadProjectTest: BaseIntegrationTest() {
    private var projectDirectory: File? = null
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
        val project = projectService.loadProject(projectDirectory!!.absolutePath)

        assertEquals("my-project", project.name)
        assertEquals(project.path, projectDirectory!!.absolutePath)
        val projectFound = projectService.findOneById(project.uuid!!)
        assertEquals(project.name, projectFound.name)

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