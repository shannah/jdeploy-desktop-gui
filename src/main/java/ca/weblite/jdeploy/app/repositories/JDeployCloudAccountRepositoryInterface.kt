package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.JDeployCloudAccount
import java.util.*

interface JDeployCloudAccountRepositoryInterface {
    @Throws(NotFoundException::class)
    fun findOneById(id: UUID): JDeployCloudAccount
    fun findOneByIdOrNull(id: UUID): JDeployCloudAccount?
    fun saveOne(account: JDeployCloudAccount): JDeployCloudAccount
}
