package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.exceptions.NotFoundException
import ca.weblite.jdeploy.app.records.NpmAccount
import ca.weblite.jdeploy.app.repositories.NpmAccountRepositoryInterface
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NpmAccountService @Inject constructor (
    private val npmAccountRepository: NpmAccountRepositoryInterface
) {
    @Throws(NotFoundException::class)
    fun findOneById(uuid: UUID): NpmAccount {
        return npmAccountRepository.findOneById(uuid)
    }

    fun saveOne(npmAccount: NpmAccount): NpmAccount {
        return npmAccountRepository.saveOne(npmAccount)
    }
}
