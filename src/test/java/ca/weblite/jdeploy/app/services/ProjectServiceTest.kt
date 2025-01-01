package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class ProjectServiceTest: BaseIntegrationTest() {
    private var projectDirectory: File? = null
    @BeforeAll
    fun createProjectDirectory() {
        projectDirectory = createTempDir("project")
    }

    @AfterAll
    fun cleanup() {
        projectDirectory?.deleteRecursively()
    }

    @Test
    fun testCrud() {
        val projectService = DIContext.get(ProjectService::class.java)
        val project = Project(
            uuid = UUID.randomUUID(),
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