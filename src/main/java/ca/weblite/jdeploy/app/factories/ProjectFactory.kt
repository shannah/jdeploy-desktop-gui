package ca.weblite.jdeploy.app.factories

import ca.weblite.jdeploy.app.records.Project
import org.json.JSONObject
import java.util.*

class ProjectFactory {
    fun createOne(projectPath: String?, packageJson: JSONObject): Project {
        val uuid = if (packageJson.has("uuid")
        ) UUID.fromString(packageJson.getString("uuid"))
        else UUID.randomUUID()
        return Project(
            packageJson.getString("name"),
            projectPath!!,
            uuid
        )
    }
}
