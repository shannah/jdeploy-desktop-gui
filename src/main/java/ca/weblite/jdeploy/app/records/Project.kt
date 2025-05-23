package ca.weblite.jdeploy.app.records

import java.io.File
import java.util.*

class Project(
    val name: String,
    var path: String,
    val uuid: UUID? = null,
    var lastOpened: Long = System.currentTimeMillis()/1000,
    var npmAccount: NpmAccount? = null,
    var gitHubAccount: GitHubAccount? = null,
    val entity: Any? = null,
) {
    val packageJsonPath: String
        get() = path + File.separator + "package.json"
}
