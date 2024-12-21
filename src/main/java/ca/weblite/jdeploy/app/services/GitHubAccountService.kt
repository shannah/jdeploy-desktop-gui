package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.GitHubAccount
import ca.weblite.jdeploy.app.repositories.GitHubAccountRepositoryInterface
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubAccountService @Inject constructor(
    private val gitHubAccountRepository: GitHubAccountRepositoryInterface
) {
    @Throws(NotFoundException::class)
    fun findOneById(uuid: UUID): GitHubAccount {
        return gitHubAccountRepository.findOneById(uuid)
    }

    fun saveOne(gitHubAccount: GitHubAccount): GitHubAccount {
        return gitHubAccountRepository.saveOne(gitHubAccount)
    }
}
