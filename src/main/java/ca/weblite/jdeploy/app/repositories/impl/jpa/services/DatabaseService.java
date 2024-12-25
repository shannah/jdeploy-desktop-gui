package ca.weblite.jdeploy.app.repositories.impl.jpa.services;

import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface;
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class DatabaseService {

    private EntityManager entityManager;

    private final EntityManagerFactory emf;

    private final JdeployAppConfigInterface config;

    public @Inject DatabaseService(
            EmfProviderInterface emfProvider,
            JdeployAppConfigInterface config
    ) {
        emf = emfProvider.getEntityManagerFactory();
        this.config = config;
    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }

    public <T> T executeInTransaction(Function<EntityManager, T> action) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        T result = null;

        try {
            tx.begin();
            result = action.apply(em);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e; // or handle exception as needed
        } finally {
            em.close();
        }

        return result;
    }

    public void close() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    public void migrate() {
        String jdbcUrl = config.getJdbcUrl();
        Flyway.configure()
                .dataSource(jdbcUrl, "", "")
                .load()
                .migrate();
    }
}
