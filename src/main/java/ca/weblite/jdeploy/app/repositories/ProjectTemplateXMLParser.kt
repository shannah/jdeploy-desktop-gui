package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.*
import org.w3c.dom.Document
import org.w3c.dom.Element

internal object ProjectTemplateXMLParser {
    private const val NS = "http://jdeploy.com/project-templates"

    fun parse(document: Document): ProjectTemplates {
        val templates = document.getElementsByTagNameNS(NS, "template")
        val list = mutableListOf<Template>()

        for (i in 0 until templates.length) {
            val templateNode = templates.item(i) as Element
            var tileImageUrl = templateNode.getText(NS, "tileImageUrl")
            if (tileImageUrl.isEmpty()) {
                tileImageUrl = Template.DEFAULT_TILE_IMAGE_URL
            }
            list.add(
                Template(
                    displayName = templateNode.getText(NS, "displayName"),
                    name = templateNode.getText(NS, "name"),
                    uiToolkit = templateNode.getText(NS, "uiToolkit"),
                    categories = templateNode.getElements(NS, "category"),
                    screenshots = templateNode.getChildElements(NS, "screenshots", "screenshot")
                        .map { Screenshot(url = it.getAttribute("url")) },
                    screencasts = templateNode.getChildElements(NS, "screencasts", "screencast")
                        .map { Screencast(url = it.getAttribute("url")) },
                    tileImageUrl = tileImageUrl,
                    iconUrl = templateNode.getText(NS, "iconUrl"),
                    demoDownloadUrl = templateNode.getText(NS, "demoDownloadUrl").takeIf { it.isNotEmpty() },
                    webAppUrl = templateNode.getText(NS, "webAppUrl").takeIf { it.isNotEmpty() },
                    author = templateNode.getText(NS, "author"),
                    license = templateNode.getText(NS, "license"),
                    credits = templateNode.getText(NS, "credits"),
                    description = templateNode.getText(NS, "description"),
                    buildTool = templateNode.getText(NS, "buildTool"),
                    programmingLanguage = templateNode.getText(NS, "programmingLanguage"),
                    sourceUrl = templateNode.getText(NS, "sourceUrl").takeIf { it.isNotEmpty() }
                )
            )
        }

        return ProjectTemplates(list)
    }

    // Helper extensions
    private fun Element.getText(namespace: String, tag: String): String =
        getElementsByTagNameNS(namespace, tag).item(0)?.textContent?.trim() ?: ""

    private fun Element.getElements(namespace: String, tag: String): List<String> =
        getElementsByTagNameNS(namespace, tag).let { nodeList ->
            List(nodeList.length) { i -> nodeList.item(i).textContent.trim() }
        }

    private fun Element.getChildElements(namespace: String, parentTag: String, childTag: String): List<Element> {
        val parent = getElementsByTagNameNS(namespace, parentTag).item(0) as? Element ?: return emptyList()
        return (0 until parent.childNodes.length)
            .mapNotNull { parent.childNodes.item(it) }
            .filterIsInstance<Element>()
            .filter { it.localName == childTag && it.namespaceURI == namespace }
    }
}
