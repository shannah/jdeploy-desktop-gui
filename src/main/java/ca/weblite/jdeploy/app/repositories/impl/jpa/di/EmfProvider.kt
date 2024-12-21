package ca.weblite.jdeploy.app.repositories.impl.jpa.di

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.codejargon.feather.Provides

class EmfProvider: EmfProviderInterface {
    private val emf: EntityManagerFactory by lazy {
        Persistence.createEntityManagerFactory("jdeploy-gui")
    }

    @Provides
    override fun getEntityManagerFactory(): EntityManagerFactory {
        return emf
    }
}