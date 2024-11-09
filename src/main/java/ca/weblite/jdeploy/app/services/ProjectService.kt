package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.ProjectRepositoryInterface
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepositoryInterface
) {
    fun findOneById(id: UUID?): Project {
        return projectRepository.findOneById(id)
    }

    fun findRecent(): ProjectSet {
        return projectRepository.findRecent()
    }

    fun findOneByPath(path: String): Project {
        return projectRepository.findOnebyPath(path)
    }
}
