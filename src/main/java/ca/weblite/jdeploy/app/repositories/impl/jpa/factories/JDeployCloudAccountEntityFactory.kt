package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.JDeployCloudAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.JDeployCloudAccountEntity

class JDeployCloudAccountEntityFactory {
    fun createOne(account: JDeployCloudAccount): JDeployCloudAccountEntity {
        return JDeployCloudAccountEntity(
            accountName = account.accountName,
            serverUrl = account.serverUrl,
            token = account.token,
            id = account.uuid
        )
    }

    fun extractOne(account: JDeployCloudAccount): JDeployCloudAccountEntity {
        return account.entity as JDeployCloudAccountEntity
    }

    fun extractOrCreate(account: JDeployCloudAccount): JDeployCloudAccountEntity {
        return if (account.entity == null) {
            createOne(account)
        } else {
            extractOne(account)
        }
    }
}
