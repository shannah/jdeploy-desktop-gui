package ca.weblite.jdeploy.app.repositories.impl.jpa.repositories

import ca.weblite.jdeploy.app.collections.ProjectSet
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
    override fun findOneById(id: UUID): Project {
        val entity = databaseService.executeInTransaction { em: EntityManager ->
            em.createQuery("SELECT ps FROM ProjectEntity ps WHERE ps.id = :project", ProjectEntity::class.java)
                .setParameter("project", id)
                .singleResult
        }

        return projectFactory.createOne(entity)
    }

    override fun findRecent(): ProjectSet {
        val entities = databaseService.executeInTransaction { em: EntityManager ->
            em.createQuery(
                """
                    SELECT ps FROM ProjectSettings ps
                    ORDER BY ps.lastOpened DESC
                    
                    """.trimIndent(), ProjectEntity::class.java
            )
                .resultList
        }

        return projectFactory.createCollection(entities)
    }
    override fun saveOne(project: Project): Project {
        val entityManager = databaseService.entityManager
        val entity = projectEntityFactory.extractOrCreate(project)
        entityManager.persist(entity)

        return projectFactory.createOne(entity)
    }

    override fun findOnebyPath(path: String): Project {
        val entity = databaseService.executeInTransaction { em: EntityManager ->
            em.createQuery("SELECT ps FROM ProjectEntity ps WHERE ps.path = :path", ProjectEntity::class.java)
                .setParameter("path", path)
                .singleResult
        }

        return projectFactory.createOne(entity)
    }

}
