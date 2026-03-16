package ca.weblite.jdeploy.app.records

import java.util.*

@JvmRecord
data class JDeployCloudAccount(
    val accountName: String,
    val serverUrl: String = "https://cloud.jdeploy.com",
    val token: String? = null,
    val uuid: UUID? = null,
    val entity: Any? = null,
)
