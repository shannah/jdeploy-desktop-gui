package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectServiceTest: BaseIntegrationTest() {
    @Test
    fun testCrud() {
        val projectService = DIContext.get(ProjectService::class.java)
        val project = Project(
            name = "My Project",
            path = "/path/to/project",
        )
        val projectSaved = projectService.saveOne(project)
        assertEquals(project.name, projectSaved.name)
        assertEquals(project.path, projectSaved.path)
        val projectFound = projectService.findOneById(projectSaved.uuid!!)
        assertEquals(project.name, projectFound.name)

    }
}