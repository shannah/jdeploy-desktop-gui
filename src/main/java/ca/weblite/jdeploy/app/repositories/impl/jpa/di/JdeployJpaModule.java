package ca.weblite.jdeploy.app.repositories.impl.jpa.di;

import ca.weblite.jdeploy.app.repositories.GitHubAccountRepositoryInterface;
import ca.weblite.jdeploy.app.repositories.NpmAccountRepositoryInterface;
import ca.weblite.jdeploy.app.repositories.ProjectRepositoryInterface;
import ca.weblite.jdeploy.app.repositories.impl.jpa.repositories.JpaGitHubAccountRepository;
import ca.weblite.jdeploy.app.repositories.impl.jpa.repositories.JpaNpmAccountRepository;
import ca.weblite.jdeploy.app.repositories.impl.jpa.repositories.JpaProjectRepository;
import org.codejargon.feather.Provides;

import javax.inject.Inject;

public class JdeployJpaModule {
    @Provides
    public GitHubAccountRepositoryInterface getGitHubAccountRepository(JpaGitHubAccountRepository repository) {
        return repository;
    }

    @Provides
    public NpmAccountRepositoryInterface getNpmAccountRepository(JpaNpmAccountRepository repository) {
        return repository;
    }

    @Provides
    public ProjectRepositoryInterface getProjectRepository(JpaProjectRepository repository) {
        return repository;
    }
}
