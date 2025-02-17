package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.NpmAccount
import java.util.*

interface NpmAccountRepositoryInterface {
    fun findOneById(id: UUID): NpmAccount
    fun saveOne(npmAccount: NpmAccount): NpmAccount
}