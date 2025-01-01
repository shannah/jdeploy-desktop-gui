package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    protected lateinit var emf: EntityManagerFactory

    @BeforeAll
    open fun setup() {

        createDIContext().install()
        DIContext.get(DatabaseService::class.java).migrate();
        // Create an EntityManagerFactory for our test
        emf = DIContext.get(EmfProviderInterface::class.java).getEntityManagerFactory()
    }

    @AfterAll
    fun teardown() {
        emf?.close()
    }

    open protected fun createDIContext(): JDeployDesktopGuiModule {
        return TestJDeployDesktopGuiModule()
    }
}
