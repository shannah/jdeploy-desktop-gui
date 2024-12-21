package ca.weblite.jdeploy.app.repositories.impl.jpa.factories

import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.impl.jpa.entities.ProjectEntity
import javax.inject.Singleton

@Singleton
class ProjectEntityFactory(
    private val npmAccountFactory: NpmAccountEntityFactory,
    private val gitHubAccountFactory: GitHubAccountEntityFactory
) {
    fun createOne(project: Project): ProjectEntity {
        return ProjectEntity(
            name = project.name,
            path = project.path,
            id = project.uuid,
            npmAccount = project.npmAccount?.let {
                npmAccountFactory.extractOrCreate(it)
            },
            gitHubAccount = project.gitHubAccount?.let {
                gitHubAccountFactory.extractOrCreate(it)
            }
        )
    }

    fun extractOne(project: Project): ProjectEntity {
        return project.entity as ProjectEntity
    }

    fun extractOrCreate(project: Project): ProjectEntity {
        return if (project.entity == null) {
            createOne(project)
        } else {
            extractOne(project)
        }
    }
}
