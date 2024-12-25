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
class ProjectServiceTest: BaseIntegrationTest() {
    private var projectDirectory: File? = null
    @BeforeAll
    override fun setup() {
        super.setup()
        projectDirectory = createTempDir("project")
    }

    @AfterAll
    public fun cleanup() {
        projectDirectory?.deleteRecursively()
    }

    @Test
    fun testCrud() {
        val projectService = DIContext.get(ProjectService::class.java)
        val project = Project(
            name = "My Project",
            path = projectDirectory!!.absolutePath,
        )
        val projectSaved = projectService.saveOne(project)
        assertEquals(project.name, projectSaved.name)
        assertEquals(project.path, projectSaved.path)
        val projectFound = projectService.findOneById(projectSaved.uuid!!)
        assertEquals(project.name, projectFound.name)

    }
}