package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.ProjectTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XMLProjectTemplateRepository(
    private val xmlFile: File
) : ProjectTemplateRepositoryInterface {

    override suspend fun findAll(): ProjectTemplates = withContext(Dispatchers.IO) {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(xmlFile)
        ProjectTemplateXMLParser.parse(doc)
    }
}
