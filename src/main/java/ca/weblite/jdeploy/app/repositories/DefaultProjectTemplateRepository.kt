package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.cache.FileSystemCache
import ca.weblite.jdeploy.app.records.ProjectTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultProjectTemplateRepository @Inject constructor(
    private val fileSystemCache: FileSystemCache
) : ProjectTemplateRepositoryInterface {

    private val url: URL = URI.create("https://raw.githubusercontent.com/shannah/jdeploy-project-templates/master/projects.xml").toURL()

    private val delegate by lazy {
        URLXMLProjectTemplateRepository(
            url,
            fileSystemCache = fileSystemCache
        )
    }

    override suspend fun findAll(): ProjectTemplates = withContext(Dispatchers.IO) {
        delegate.findAll()
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
