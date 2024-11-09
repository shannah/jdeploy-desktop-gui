package ca.weblite.jdeploy.app.records

import java.util.*

@JvmRecord
data class NpmAccount(
    val uuid: UUID? = null,
    val accountName: String,
    val username: String? = null,

    val password: String? = null,
)
