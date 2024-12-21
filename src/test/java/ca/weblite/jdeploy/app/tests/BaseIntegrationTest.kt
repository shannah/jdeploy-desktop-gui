package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open abstract class BaseIntegrationTest {
    protected lateinit var emf: EntityManagerFactory

    @BeforeAll
    open fun setup() {

        DIContext.setInstance(TestDIContext());

        val jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        Flyway.configure()
            .dataSource(jdbcUrl, "", "")
            .load()
            .migrate()

        // Create an EntityManagerFactory for our test
        emf = DIContext.get(EmfProviderInterface::class.java).getEntityManagerFactory()
    }

    @AfterAll
    fun teardown() {
        emf.close()
    }
}
