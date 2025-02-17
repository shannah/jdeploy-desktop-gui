package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.GitHubAccount
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.GitHubAccountEntity

class GitHubAccountEntityFactory {
    fun createOne(gitHubAccount: GitHubAccount): GitHubAccountEntity {
        return GitHubAccountEntity(
            accountName = gitHubAccount.accountName,
            username = gitHubAccount.username,
            token = gitHubAccount.token,
            id = gitHubAccount.uuid
        )
    }

    fun extractOne(gitHubAccount: GitHubAccount): GitHubAccountEntity {
        return gitHubAccount.entity as GitHubAccountEntity
    }

    fun extractOrCreate(gitHubAccount: GitHubAccount): GitHubAccountEntity {
        return if (gitHubAccount.entity == null) {
            createOne(gitHubAccount)
        } else {
            extractOne(gitHubAccount)
        }
    }
}