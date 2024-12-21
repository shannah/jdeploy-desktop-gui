package ca.weblite.jdeploy.app.records

import java.util.*

@JvmRecord
data class GitHubAccount(
    val accountName: String,
    val username: String,
    val token: String? = null,
    val uuid: UUID? = null,
    val entity: Any? = null,
)
