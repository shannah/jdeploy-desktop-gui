package ca.weblite.jdeploy.app.repositories.impl.jpa.repositories

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.GitHubAccount

import ca.weblite.jdeploy.app.repositories.GitHubAccountRepositoryInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.GitHubAccountEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.GitHubAccountEntityFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.GitHubAccountFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService
import java.util.*
import javax.inject.Inject

class JpaGitHubAccountRepository @Inject constructor(
    private val databaseService: DatabaseService,
    private val githubAccountFactory: GitHubAccountFactory,
    private val gitHubAccountEntityFactory: GitHubAccountEntityFactory
): GitHubAccountRepositoryInterface {
    override fun findOneById(id: UUID): GitHubAccount {
        val result = databaseService.executeInTransaction { em ->
            em.createQuery("SELECT ga FROM GitHubAccountEntity ga WHERE ga.id = :id", GitHubAccountEntity::class.java)
                .setParameter("id", id)
                .singleResult}
        if (result == null) {
            throw NotFoundException("GitHubAccount with id $id not found")
        }

        return githubAccountFactory.createOne(result)
    }

    override fun findOneByIdOrNull(id: UUID): GitHubAccount? {
        return try {
            findOneById(id)
        } catch (e: NotFoundException) {
            null
        }
    }

    override fun saveOne(gitHubAccount: GitHubAccount): GitHubAccount {
        val entityManager = databaseService.entityManager
        val entity = gitHubAccountEntityFactory.extractOrCreate(gitHubAccount)
        entityManager.persist(entity)

        return githubAccountFactory.createOne(entity)
    }
}