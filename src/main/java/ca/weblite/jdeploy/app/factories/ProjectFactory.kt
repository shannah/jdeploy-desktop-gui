package ca.weblite.jdeploy.app.factories

import ca.weblite.jdeploy.app.exceptions.InvalidProjectException
import ca.weblite.jdeploy.app.records.Project
import ca.weblite.jdeploy.app.services.PackageJsonService
import ca.weblite.jdeploy.app.services.ProjectValidator
import ca.weblite.jdeploy.app.system.files.FileSystemInterface
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectFactory  @Inject constructor(
    private val packageJsonService: PackageJsonService,
    private val validator: ProjectValidator,
    private val fileSystem: FileSystemInterface,
){
    @Throws(InvalidProjectException::class)
    fun createOne(projectPath: String): Project {
        return createOneFromProjectPath(projectPath)
    }

    private fun createOneFromPackageJson(projectPath: String, packageJson: JSONObject): Project {
        val uuid = readUuidFromFileSystem(projectPath) ?: UUID.randomUUID()
        return Project(
            packageJson.getString("name"),
            projectPath,
            uuid
        )
    }

    @Throws(InvalidProjectException::class)
    private fun createOneFromProjectPath(projectPath: String): Project {
        if (!validator.isValidProject(projectPath, ProjectValidator.ValidationLevel.MeetsMinimumRequirements)) {
            throw InvalidProjectException("Invalid project at path: $projectPath")
        }
        val packageJson = packageJsonService.readOne("${projectPath}/package.json")
        return createOneFromPackageJson(projectPath, packageJson)
    }

    private fun readUuidFromFileSystem(projectPath: String): UUID? {
        val uuidFile = "$projectPath/.jdeploy/uuid"
        if (!fileSystem.exists(uuidFile)) {
            return null
        }
        val uuidString = fileSystem.openInputStream(uuidFile).readAllBytes().toString(Charsets.UTF_8)
        return UUID.fromString(uuidString)
    }
}
