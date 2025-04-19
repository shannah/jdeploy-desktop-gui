package ca.weblite.jdeploy.app.repositories
import ca.weblite.jdeploy.app.records.ProjectTemplates

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class XMLProjectTemplateRepositoryTest {

 @Test
 fun `should load templates from XML correctly`() {
  val xmlContent = """
            <projectTemplates xmlns="http://jdeploy.com/project-templates">
                <template>
                    <displayName>Test Template</displayName>
                    <name>test-template</name>
                    <uiToolkit>TestUI</uiToolkit>
                    <categories>
                        <category>Testing</category>
                        <category>Example</category>
                    </categories>
                    <screenshots>
                        <screenshot url="http://example.com/screenshot.png"/>
                    </screenshots>
                    <screencasts>
                        <screencast url="http://example.com/video.mp4"/>
                    </screencasts>
                    <iconUrl>http://example.com/icon.png</iconUrl>
                    <demoDownloadUrl>http://example.com/demo.zip</demoDownloadUrl>
                    <webAppUrl>http://example.com/demo</webAppUrl>
                    <author>Test Author</author>
                    <license>MIT</license>
                    <credits>Test credits</credits>
                    <description>Test description</description>
                    <buildTool>gradle</buildTool>
                    <programmingLanguage>Kotlin</programmingLanguage>
                </template>
            </projectTemplates>
        """.trimIndent()

  // Write XML to a temporary file
  val tempFile = File.createTempFile("project-templates", ".xml")
  tempFile.writeText(xmlContent)

  // Create the repository
  val repository = XMLProjectTemplateRepository(tempFile)

  // Run the test in a coroutine context
  val templates: ProjectTemplates = runBlocking {
   repository.findAll()
  }

  // Assert template data
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
