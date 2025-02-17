package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.NpmAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.NpmAccountEntity
import javax.inject.Singleton

@Singleton
class NpmAccountEntityFactory {
    fun createOne(npmAccount: NpmAccount): NpmAccountEntity {
        return NpmAccountEntity(
            accountName = npmAccount.accountName,
            username = npmAccount.username,
            password = npmAccount.password,
            id = npmAccount.uuid
        )
    }

    fun extractOne(npmAccount: NpmAccount): NpmAccountEntity {
        return npmAccount.entity as NpmAccountEntity
    }

    fun extractOrCreate(npmAccount: NpmAccount): NpmAccountEntity {
        return if (npmAccount.entity == null) {
            createOne(npmAccount)
        } else {
            extractOne(npmAccount)
        }
    }
}
