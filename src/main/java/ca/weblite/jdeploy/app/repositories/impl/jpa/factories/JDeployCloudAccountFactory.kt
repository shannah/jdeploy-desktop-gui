package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.JDeployCloudAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.JDeployCloudAccountEntity
import javax.inject.Singleton

@Singleton
class JDeployCloudAccountFactory {
    fun createOne(entity: JDeployCloudAccountEntity): JDeployCloudAccount {
        return JDeployCloudAccount(
            accountName = entity.accountName,
            serverUrl = entity.serverUrl,
            token = entity.token,
            uuid = entity.id
        )
    }
}
