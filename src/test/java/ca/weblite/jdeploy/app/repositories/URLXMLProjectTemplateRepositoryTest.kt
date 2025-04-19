package ca.weblite.jdeploy.app.repositories

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

class URLXMLProjectTemplateRepositoryTest {

    @Test
    fun `should load templates from resource URL correctly`() {
        val resourceUrl: URL = javaClass.getResource("/templates/test-templates.xml")
            ?: error("Test resource not found")

        val repository = URLXMLProjectTemplateRepository(resourceUrl)

        val templates = runBlocking {
            repository.findAll()
        }

        assertEquals(1, templates.templates.size)
        val template = templates.templates[0]

        assertEquals("test-template", template.name)
        assertEquals("Test Template", template.displayName)
        assertEquals(listOf("Testing", "Example"), template.categories)
        assertEquals("http://example.com/screenshot.png", template.screenshots.first().url)
        assertEquals("http://example.com/video.mp4", template.screencasts.first().url)
        assertEquals("Test Author", template.author)
    }
}
