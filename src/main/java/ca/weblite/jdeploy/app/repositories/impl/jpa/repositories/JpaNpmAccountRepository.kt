package ca.weblite.jdeploy.app.repositories.impl.jpa.repositories

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.NpmAccount
import ca.weblite.jdeploy.app.repositories.NpmAccountRepositoryInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.NpmAccountEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.NpmAccountEntityFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.NpmAccountFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JpaNpmAccountRepository @Inject constructor(
    private val databaseService: DatabaseService,
    private val npmAccountFactory: NpmAccountFactory,
    private val npmAccountEntityFactory: NpmAccountEntityFactory
): NpmAccountRepositoryInterface {
    override fun findOneById(id: UUID): NpmAccount {
        val entity = databaseService.executeInTransaction { em ->
            em.createQuery("SELECT na FROM NpmAccountEntity na WHERE na.id = :id", NpmAccountEntity::class.java)
                .setParameter("id", id)
                .singleResult
        }
        if (entity == null) {
            throw NotFoundException("NpmAccount with id $id not found")
        }

        return npmAccountFactory.createOne(entity)
    }

    override fun saveOne(npmAccount: NpmAccount): NpmAccount {
        val entityManager = databaseService.entityManager
        val entity = npmAccountEntityFactory.extractOrCreate(npmAccount)
        entityManager.persist(entity)

        return npmAccountFactory.createOne(entity)
    }

}
