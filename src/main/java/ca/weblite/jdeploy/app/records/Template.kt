package ca.weblite.jdeploy.app.records

data class Template(
    val displayName: String,
    val name: String,
    val uiToolkit: String,
    val categories: List<String>,
    val screenshots: List<Screenshot>,
    val screencasts: List<Screencast>,
    val iconUrl: String? = null,
    val demoDownloadUrl: String? = null,
    val webAppUrl: String? = null,
    val author: String,
    val license: String,
    val credits: String,
    val description: String,
    val buildTool: String,
    val programmingLanguage: String,
    val tileImageUrl: String = DEFAULT_TILE_IMAGE_URL,
    val sourceUrl: String? = null,
) {
    companion object {
        val DEFAULT_TILE_IMAGE_URL: String by lazy {
            this::class.java.getResource("/ca/weblite/jdeploy/app/assets/project_template_tile_512.png")?.toExternalForm()
                ?: throw IllegalStateException("Default tile image resource not found")
        }
    }
}