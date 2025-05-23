package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.cache.FileSystemCache
import ca.weblite.jdeploy.app.records.ProjectTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class URLXMLProjectTemplateRepository(
    private val url: URL,
    private val fileSystemCache: FileSystemCache? = null
) : ProjectTemplateRepositoryInterface {

    override suspend fun findAll(): ProjectTemplates = withContext(Dispatchers.IO) {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val builder = factory.newDocumentBuilder()
        val doc = if (fileSystemCache == null) {
                url.openStream().use { builder.parse(it) }
            } else {
                fileSystemCache.load(url, "").use { builder.parse(it) }
            }
        ProjectTemplateXMLParser.parse(doc)
    }
}
