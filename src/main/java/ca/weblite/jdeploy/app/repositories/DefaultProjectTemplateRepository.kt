package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.ProjectTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.inject.Singleton

@Singleton
class DefaultProjectTemplateRepository : ProjectTemplateRepositoryInterface {

    private val delegate = URLXMLProjectTemplateRepository(
        URI.create("https://raw.githubusercontent.com/shannah/jdeploy-project-templates/master/projects.xml").toURL()
    )

    override suspend fun findAll(): ProjectTemplates = withContext(Dispatchers.IO) {
        delegate.findAll()
    }
}
