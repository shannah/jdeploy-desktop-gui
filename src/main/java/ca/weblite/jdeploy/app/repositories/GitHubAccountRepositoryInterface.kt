package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.GitHubAccount
import java.util.*

interface GitHubAccountRepositoryInterface {
    @Throws(NotFoundException::class)
    fun findOneById(id: UUID): GitHubAccount
    fun findOneByIdOrNull(id: UUID): GitHubAccount?
    fun saveOne(gitHubAccount: GitHubAccount): GitHubAccount
}