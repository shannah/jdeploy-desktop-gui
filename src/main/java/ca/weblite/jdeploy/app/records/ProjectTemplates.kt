package ca.weblite.jdeploy.app.records

data class ProjectTemplates(
    val templates: List<Template>
) {
    fun getCategories(): List<String> {
        return templates.flatMap { it.categories }.distinct()
    }

    fun getUiToolkits(): List<String> {
        return templates.map { it.uiToolkit }.distinct()
    }

    fun getBuildTools(): List<String> {
        return templates.map { it.buildTool }.distinct()
    }

    fun getProgrammingLanguages(): List<String> {
        return templates.map { it.programmingLanguage }.distinct()
    }
}