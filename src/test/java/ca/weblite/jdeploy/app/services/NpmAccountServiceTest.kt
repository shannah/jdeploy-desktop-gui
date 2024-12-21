package ca.weblite.jdeploy.app.services

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.records.NpmAccount
import ca.weblite.jdeploy.app.tests.BaseIntegrationTest
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NpmAccountServiceTest: BaseIntegrationTest() {

        @Test
        fun testCrud() {
            val service = DIContext.get(NpmAccountService::class.java)
            val account = NpmAccount(
                accountName = "My NPM Account",
                username = "npmuser",
            )
            val accountSaved = service.saveOne(account)
            assertEquals(account.accountName, accountSaved.accountName)
            assertEquals(account.username, accountSaved.username)
            val accountFound = service.findOneById(accountSaved.uuid!!)
            assertEquals(account.accountName, accountFound.accountName)
        }
}