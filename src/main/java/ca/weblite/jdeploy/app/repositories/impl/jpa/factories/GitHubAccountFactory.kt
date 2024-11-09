package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.GitHubAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.GitHubAccountEntity
import javax.inject.Singleton

@Singleton
class GitHubAccountFactory {
    fun createOne(entity: GitHubAccountEntity): GitHubAccount {
        return GitHubAccount(
            accountName = entity.accountName,
            username = entity.username,
            token = entity.token,
            uuid = entity.id
        )
    }
}
