package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.cache.FileSystemCache
import ca.weblite.jdeploy.app.records.ProjectTemplates
import ca.weblite.jdeploy.services.ProjectTemplateCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProjectTemplateRepository @Inject constructor(
    private val fileSystemCache: FileSystemCache,
    private val projectTemplateCatalog: ProjectTemplateCatalog,
) : ProjectTemplateRepositoryInterface {

    private val url: URL = URI.create("https://raw.githubusercontent.com/shannah/jdeploy-project-templates/master/projects.xml").toURL()

    private val urlDelegate by lazy {
        URLXMLProjectTemplateRepository(
            url,
            fileSystemCache = fileSystemCache
        )
    }

    private val fileDelegate by lazy {
        XMLProjectTemplateRepository(
            projectTemplateCatalog.projectsIndexFile,
        )
    }

    override suspend fun findAll(): ProjectTemplates = withContext(Dispatchers.IO) {
        if (projectTemplateCatalog.projectsIndexFile.exists()) {
            fileDelegate.findAll()
        } else {
            urlDelegate.findAll()
        }
    }

    suspend fun updateCatalog() {
        withContext(Dispatchers.IO) {
            projectTemplateCatalog.update()
        }
    }

    suspend fun clearCache() {
        fileSystemCache.purge(url, "")
    }

    fun clearCacheBlocking() {
        runBlocking {
            clearCache()
        }
    }
}
