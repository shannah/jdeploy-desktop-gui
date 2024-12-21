package ca.weblite.jdeploy.app.tests

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
    fun setup() {
        val jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        Flyway.configure()
            .dataSource(jdbcUrl, "", "")
            .load()
            .migrate()
        // We create a map of properties to override the default persistence.xml settings
        val config = mapOf(
            "jakarta.persistence.jdbc.url" to "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "jakarta.persistence.jdbc.driver" to "org.h2.Driver",
            // Use H2 Dialect
            "hibernate.dialect" to "org.hibernate.dialect.H2Dialect",

            )

        // Create an EntityManagerFactory for our test
        emf = Persistence.createEntityManagerFactory("jdeploy-gui", config)
    }

    @AfterAll
    fun teardown() {
        emf.close()
    }
}
