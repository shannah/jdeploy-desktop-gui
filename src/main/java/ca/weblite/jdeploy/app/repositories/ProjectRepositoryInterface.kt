package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.collections.ProjectSet
import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.Project
import java.util.*

interface ProjectRepositoryInterface {
    @Throws(NotFoundException::class)
    fun findOneById(id: UUID): Project

    fun findRecent(): ProjectSet

    @Throws(NotFoundException::class)
    fun findOnebyPath(path: String): Project

    fun saveOne(project: Project): Project
}
