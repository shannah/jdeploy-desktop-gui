package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.ProjectEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectFactory @Inject constructor(
    private val npmAccountFactory: NpmAccountFactory,
    private val gitHubAccountFactory: GitHubAccountFactory

) {
    fun createOne(entity: ProjectEntity): Project {
        return Project(
            name = entity.name,
            path = entity.path,
            uuid = entity.id,
            lastOpened = entity.lastOpened,
            npmAccount = entity.npmAccount?.let { npmAccountFactory.createOne(it)},
            gitHubAccount = entity.gitHubAccount?.let { gitHubAccountFactory.createOne(it)},
            entity = entity
        )
    }

    fun createCollection(entities: List<ProjectEntity>): ProjectSet {
        return ProjectSet(*entities.map { createOne(it) }.toTypedArray())
    }
}
