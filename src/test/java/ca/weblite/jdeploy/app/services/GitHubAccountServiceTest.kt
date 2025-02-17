package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.records.GitHubAccount
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitHubAccountServiceTest: BaseIntegrationTest() {

    @Test
    fun testCrud() {
        val service = DIContext.get(GitHubAccountService::class.java)
        val account = GitHubAccount(
            accountName = "My GitHub Account",
            username = "myusername",
        )
        val accountSaved = service.saveOne(account)
        assertEquals(account.accountName, accountSaved.accountName)
        assertEquals(account.username, accountSaved.username)
        val accountFound = service.findOneById(accountSaved.uuid!!)
        assertEquals(account.accountName, accountFound.accountName)
    }
}