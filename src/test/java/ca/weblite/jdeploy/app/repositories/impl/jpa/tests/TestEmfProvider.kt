package ca.weblite.jdeploy.app.repositories.impl.jpa.tests

import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.codejargon.feather.Provides

class TestEmfProvider: EmfProviderInterface {

    private val emf: EntityManagerFactory by lazy {
        val config = mapOf(
            "jakarta.persistence.jdbc.url" to "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "jakarta.persistence.jdbc.driver" to "org.h2.Driver",
            // Use H2 Dialect
            "hibernate.dialect" to "org.hibernate.dialect.H2Dialect",

            )

        // Create an EntityManagerFactory for our test
        Persistence.createEntityManagerFactory("jdeploy-gui", config)
    }

    @Provides
    override fun getEntityManagerFactory(): EntityManagerFactory {
        return emf
    }
}