package ca.weblite.jdeploy.app.repositories;

import ca.weblite.jdeploy.app.exceptions.NotFoundException;
import ca.weblite.jdeploy.app.records.GitHubAccount;
import ca.weblite.jdeploy.app.services.PreferencesService;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class GitHubAccountRepository {
    private final PreferencesService preferencesService;

    @Inject
    public GitHubAccountRepository(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public GitHubAccount findOneById(UUID uuid) throws NotFoundException {
        PreferencesInterface gitHubAccountPreferences = preferencesService.getGitHubAccountPreferences(uuid);
        if (gitHubAccountPreferences == null) {
            throw new NotFoundException("GitHub account not found");
        }
        return createFromPreferences(gitHubAccountPreferences);
    }

    public GitHubAccount findOneByIdOrNull(UUID uuid) {
        try {
            return findOneById(uuid);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private GitHubAccount createFromPreferences(PreferencesInterface githubAccountPreferences) throws NotFoundException {
        if (githubAccountPreferences.get("accountName", null) == null) {
            throw new NotFoundException("GitHub account name not found");
        }
        if (githubAccountPreferences.get("uuid", null) == null) {
            throw new NotFoundException("GitHub account not found");
        }
        return new GitHubAccount(
                githubAccountPreferences.get("accountName", null),
                githubAccountPreferences.get("username", null),
                null,
                UUID.fromString(githubAccountPreferences.get("uuid", null))
        );
    }
}
