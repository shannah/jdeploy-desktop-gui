package ca.weblite.jdeploy.app.repositories.impl.jpa.repositories

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.ProjectRepositoryInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.ProjectEntity
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.ProjectEntityFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.factories.ProjectFactory
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService
import jakarta.persistence.EntityManager
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JpaProjectRepository @Inject constructor(
    private val databaseService: DatabaseService,
    private val projectFactory: ProjectFactory,
    private val projectEntityFactory: ProjectEntityFactory,
) : ProjectRepositoryInterface {

    @Throws(NotFoundException::class)
    override fun findOneById(id: UUID): Project {
        val entity = databaseService.executeInTransaction {
            it.createQuery("FROM ProjectEntity ps WHERE ps.id = :id", ProjectEntity::class.java)
                .setParameter("id", id)
                .resultList
                .firstOrNull()
        } ?: throw NotFoundException("Project not found")

        return projectFactory.createOne(entity)
    }

    override fun findRecent(): ProjectSet {
        val entities = databaseService.executeInTransaction {
            it.createQuery("FROM ProjectEntity ps ORDER BY ps.lastOpened", ProjectEntity::class.java)
                .resultList
        }

        return projectFactory.createCollection(entities)
    }
    override fun saveOne(project: Project): Project {
        val entityManager = databaseService.entityManager
        entityManager.transaction.begin()
        try {
            val entity = projectEntityFactory.extractOrCreate(project)
            entity.lastOpened = project.lastOpened
            val idBefore = entity.id
            assert(idBefore != null, { "ID of project should not be null" })
            entityManager.merge(entity)
            entityManager.transaction.commit()
            assert(entity.id == idBefore, { "ID of saved project should not change" })
            val savedProject = projectFactory.createOne(entity)
            assert(savedProject.uuid == project.uuid, { "UUID of saved project should match unsaved project" })
            return savedProject
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        }
    }

    override fun deleteOne(project: Project): Boolean {
        val entityManager = databaseService.entityManager
        entityManager.transaction.begin()
        try {
            val entity = projectEntityFactory.extractOne(project)
            entityManager.remove(entity)
            entityManager.transaction.commit()
            return true
        } catch (e: Exception) {
            entityManager.transaction.rollback()
            throw e
        }
    }

    @Throws(NotFoundException::class)
    override fun findOnebyPath(path: String): Project {
        val entity = databaseService.executeInTransaction {
            it.createQuery("FROM ProjectEntity ps WHERE ps.path = :path", ProjectEntity::class.java)
                .setParameter("path", path)
                .resultList
                .firstOrNull()
        } ?: throw NotFoundException("Project not found")

        return projectFactory.createOne(entity)
    }


}
