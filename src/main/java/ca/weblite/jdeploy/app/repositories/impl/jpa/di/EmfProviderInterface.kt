package ca.weblite.jdeploy.app.repositories.impl.jpa.di

import org.codejargon.feather.Provides

interface EmfProviderInterface {
    @Provides
    fun getEntityManagerFactory(): jakarta.persistence.EntityManagerFactory
}