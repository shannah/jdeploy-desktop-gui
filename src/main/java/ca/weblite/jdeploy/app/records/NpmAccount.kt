package ca.weblite.jdeploy.app.records

import java.util.*

data class NpmAccount(
    val uuid: UUID? = null,
    val accountName: String,
    val username: String? = null,
    val password: String? = null,
    val entity: Any? = null,
)
