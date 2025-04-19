package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.app.records.Screencast
import ca.weblite.jdeploy.app.records.Screenshot
import ca.weblite.jdeploy.app.records.Template
import javax.inject.Singleton

@Singleton
class MockProjectTemplateRepository: ProjectTemplateRepositoryInterface {
    override suspend fun findAll(): ProjectTemplates {
        // Simulate fetching templates from a database or API
        val templates = listOf(
            Template(
                displayName = "Spring Boot Starter",
                name = "spring-boot-starter",
                uiToolkit = "Vaadin",
                categories = listOf("Web", "Microservice"),
                screenshots = listOf(
                    Screenshot(url = "http://example.com/screenshot1.png"),
                    Screenshot(url = "http://example.com/screenshot2.png")
                ),
                screencasts = listOf(
                    Screencast(url = "http://example.com/demo.mp4")
                ),
                iconUrl = "http://example.com/icon.png",
                demoDownloadUrl = "http://example.com/demo.zip",
                webAppUrl = "http://example.com/demo",
                author = "Jane Doe",
                license = "Apache-2.0",
                credits = "Contributors listed at http://example.com/credits",
                description = "A starter template for Spring Boot with Vaadin UI.",
                buildTool = "maven",
                programmingLanguage = "Java",
                sourceUrl = "http://example.com/source"
            ),
            Template(
                displayName = "Kotlin Desktop Starter",
                name = "kotlin-desktop-starter",
                uiToolkit = "Swing",
                categories = listOf("Desktop", "GUI"),
                screenshots = listOf(
                    Screenshot(url = "http://example.com/kotlin-desktop1.png")
                ),
                screencasts = emptyList(),
                iconUrl = "http://example.com/kotlin-icon.png",
                demoDownloadUrl = "http://example.com/kotlin-demo.zip",
                webAppUrl = "http://example.com/kotlin-demo",
                author = "John Smith",
                license = "MIT",
                credits = "Contributors listed at http://example.com/kotlin-credits",
                description = "A Kotlin-based desktop application starter template.",
                buildTool = "gradle",
                programmingLanguage = "Kotlin",
            )
        )
        return ProjectTemplates(templates)
    }
}