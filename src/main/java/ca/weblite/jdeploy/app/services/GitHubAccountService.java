package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.exceptions.NotFoundException;
import ca.weblite.jdeploy.app.records.GitHubAccount;
import ca.weblite.jdeploy.app.repositories.GitHubAccountRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class GitHubAccountService {
    private final GitHubAccountRepository gitHubAccountRepository;

    @Inject
    public GitHubAccountService(GitHubAccountRepository gitHubAccountRepository) {
        this.gitHubAccountRepository = gitHubAccountRepository;
    }

    public GitHubAccount findOneById(UUID uuid) throws NotFoundException {
        return gitHubAccountRepository.findOneById(uuid);
    }

    public GitHubAccount findOneByIdOrNull(UUID uuid) {
        return gitHubAccountRepository.findOneByIdOrNull(uuid);
    }
}
