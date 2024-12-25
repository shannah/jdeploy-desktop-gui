package ca.weblite.jdeploy.app.repositories.impl.jpa.services;

import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface;
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface;
import ca.weblite.jdeploy.app.system.files.FileSystemInterface;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

@Singleton
public class DatabaseService {

    private EntityManager entityManager;

    private final EntityManagerFactory emf;

    private final JdeployAppConfigInterface config;

    private final FileSystemInterface fileSystem;

    public @Inject DatabaseService(
            EmfProviderInterface emfProvider,
            JdeployAppConfigInterface config,
            FileSystemInterface fileSystem
    ) {
        emf = emfProvider.getEntityManagerFactory();
        this.config = config;
        this.fileSystem = fileSystem;
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
        Path appDataPath = config.getAppDataPath();
        try {
            fileSystem.mkdir(appDataPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String jdbcUrl = config.getJdbcUrl();
        Flyway.configure()
                .dataSource(jdbcUrl, "", "")
                .load()
                .migrate();
    }
}
