package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.ProjectRepositoryInterface
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepositoryInterface,
    private val npmAccountService: NpmAccountService,
    private val gitHubAccountService: GitHubAccountService,
) {
    fun findOneById(id: UUID): Project {
        return projectRepository.findOneById(id)
    }

    fun findRecent(): ProjectSet {
        return projectRepository.findRecent()
    }

    fun findOneByPath(path: String): Project {
        return projectRepository.findOnebyPath(path)
    }

    fun saveOne(project: Project): Project {
        project.npmAccount?.let {
            project.npmAccount= npmAccountService.saveOne(it)
        }
        project.gitHubAccount?.let {
            project.gitHubAccount = gitHubAccountService.saveOne(it)
        }
        return projectRepository.saveOne(project)
    }
}
