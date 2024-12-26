package ca.weblite.jdeploy.app.repositories.impl.jpa.di

import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.codejargon.feather.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmfProvider @Inject constructor(
    private val config: JdeployAppConfigInterface
) : EmfProviderInterface {

    private val emf: EntityManagerFactory by lazy {
        val configMap = mapOf(
            "jakarta.persistence.jdbc.url" to config.getJdbcUrl()
        )
        // Assuming you want to pass this map as properties to the factory creation
        Persistence.createEntityManagerFactory("jdeploy-gui", configMap)
    }

    @Provides
    override fun getEntityManagerFactory(): EntityManagerFactory {
        return emf
    }
}
