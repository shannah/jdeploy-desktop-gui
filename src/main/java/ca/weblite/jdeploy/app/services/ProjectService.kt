package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.exceptions.InvalidProjectException
import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.factories.ProjectFactory
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.repositories.ProjectRepositoryInterface
import ca.weblite.jdeploy.app.system.files.FileSystemInterface
import org.apache.commons.io.IOUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectService @Inject constructor(
    private val projectRepository: ProjectRepositoryInterface,
    private val npmAccountService: NpmAccountService,
    private val gitHubAccountService: GitHubAccountService,
    private val projectFactory: ProjectFactory,
    private val fileSystem: FileSystemInterface,
) {
    fun findOneById(id: UUID): Project {
        return projectRepository.findOneById(id)
    }

    fun findRecent(): ProjectSet {
        return projectRepository.findRecent()
    }

    @Throws(NotFoundException::class, InvalidProjectException::class)
    fun loadProject(path: String): Project {
        addProjectAtPath(path)
        return projectRepository.findOnebyPath(path)
    }

    fun saveOne(project: Project): Project {
        project.npmAccount?.let {
            project.npmAccount= npmAccountService.saveOne(it)
        }
        project.gitHubAccount?.let {
            project.gitHubAccount = gitHubAccountService.saveOne(it)
        }
        if (!idFileExists(project.path)) {
            writeIdToFile(project)
        }
        return projectRepository.saveOne(project)
    }

    @Throws(InvalidProjectException::class)
    private fun addProjectAtPath(path: String) {
        val fileSystemProject = projectFactory.createOne(path)
        val dbProject = try {
            projectRepository.findOnebyPath(path)
        } catch (e: NotFoundException) {
            projectRepository.saveOne(fileSystemProject)
        }

        if (!idFileExists(path)) {
            writeIdToFile(fileSystemProject)
        }

        if (fileSystemProject.uuid != dbProject.uuid) {
            try {
                projectRepository.findOneById(fileSystemProject.uuid!!)
            } catch (e: NotFoundException) {
                projectRepository.deleteOne(dbProject)
                projectRepository.saveOne(fileSystemProject)
            }
        }
    }

    private fun idFileExists(path: String): Boolean {
        return fileSystem.exists("$path/.jdeploy/uuid")
    }

    private fun writeIdToFile(project: Project) {
        if (!fileSystem.exists("${project.path}/.jdeploy")) {
            fileSystem.mkdir("${project.path}/.jdeploy")
        }
        fileSystem.openOutputStream("${project.path}/.jdeploy/uuid").use {
            IOUtils.write(project.uuid.toString(), it, Charsets.UTF_8)
        }
    }
}
