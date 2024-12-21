package ca.weblite.jdeploy.app.repositories.impl.jpa.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class DatabaseService {

    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("jdeploy-gui");
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
}
