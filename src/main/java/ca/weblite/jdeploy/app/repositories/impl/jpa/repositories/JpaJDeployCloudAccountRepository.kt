package ca.weblite.jdeploy.app.repositories.impl.jpa.repositories

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.JDeployCloudAccount
import ca.weblite.jdeploy.app.repositories.JDeployCloudAccountRepositoryInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.JDeployCloudAccountEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.JDeployCloudAccountEntityFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.JDeployCloudAccountFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService
import java.util.*
import javax.inject.Inject

class JpaJDeployCloudAccountRepository @Inject constructor(
    private val databaseService: DatabaseService,
    private val accountFactory: JDeployCloudAccountFactory,
    private val accountEntityFactory: JDeployCloudAccountEntityFactory
) : JDeployCloudAccountRepositoryInterface {

    override fun findOneById(id: UUID): JDeployCloudAccount {
        val result = databaseService.executeInTransaction { em ->
            em.createQuery(
                "SELECT a FROM JDeployCloudAccountEntity a WHERE a.id = :id",
                JDeployCloudAccountEntity::class.java
            )
                .setParameter("id", id)
                .singleResult
        }
        if (result == null) {
            throw NotFoundException("JDeployCloudAccount with id $id not found")
        }
        return accountFactory.createOne(result)
    }

    override fun findOneByIdOrNull(id: UUID): JDeployCloudAccount? {
        return try {
            findOneById(id)
        } catch (e: NotFoundException) {
            null
        }
    }

    override fun saveOne(account: JDeployCloudAccount): JDeployCloudAccount {
        val entityManager = databaseService.entityManager
        val entity = accountEntityFactory.extractOrCreate(account)
        entityManager.persist(entity)
        return accountFactory.createOne(entity)
    }
}
