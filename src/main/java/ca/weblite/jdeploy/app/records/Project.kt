package ca.weblite.jdeploy.app.records

import java.io.File
import java.util.*

data class Project(
    val name: String,
    val path: String,
    val uuid: UUID? = null,
    val npmAccount: NpmAccount? = null,
    val gitHubAccount: GitHubAccount? = null,
) {
    val packageJsonPath: String
        get() = path + File.separator + "package.json"
}
