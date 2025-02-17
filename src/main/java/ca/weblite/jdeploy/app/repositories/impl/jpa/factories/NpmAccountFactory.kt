package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.NpmAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.NpmAccountEntity
import javax.inject.Singleton

@Singleton
class NpmAccountFactory {
    fun createOne(entity: NpmAccountEntity): NpmAccount {
        return NpmAccount(
            uuid = entity.id,
            accountName = entity.accountName,
            username = entity.username,
            password = entity.password
        )
    }
}
