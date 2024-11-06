package ca.weblite.jdeploy.app.repositories;

import ca.weblite.jdeploy.app.records.GitHubAccount;
import ca.weblite.jdeploy.app.records.NpmAccount;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.records.ProjectSettings;
import ca.weblite.jdeploy.app.services.GitHubAccountService;
import ca.weblite.jdeploy.app.services.NpmAccountService;
import ca.weblite.jdeploy.app.services.PreferencesService;
import ca.weblite.jdeploy.app.services.ProjectService;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class ProjectSettingsRepository {
    private final PreferencesService preferencesService;

    private static final String NPM_ACCOUNT_KEY = "npmAccount";
    private static final String GITHUB_ACCOUNT_KEY = "gitHubAccount";

    private final NpmAccountService npmAccountService;

    private final GitHubAccountService gitHubAccountService;


    @Inject
    public ProjectSettingsRepository(
            PreferencesService preferencesService,
            NpmAccountService npmAccountService,
            GitHubAccountService gitHubAccountService
    ) {
        this.preferencesService = preferencesService;
        this.npmAccountService = npmAccountService;
        this.gitHubAccountService = gitHubAccountService;
    }

    public ProjectSettings findOne(Project project) {
        PreferencesInterface projectPreferences = preferencesService.getProjectPreferences(project);
        return createOneFromPreferences(project, projectPreferences);
    }

    private ProjectSettings createOneFromPreferences(Project project, PreferencesInterface projectPreferences) {
        String npmAccountId = projectPreferences.get(NPM_ACCOUNT_KEY, null);
        NpmAccount npmAccount = npmAccountId == null
                ? null
                : npmAccountService.findOneByIdOrNull(UUID.fromString(npmAccountId));

        String gitHubAccountId = projectPreferences.get(GITHUB_ACCOUNT_KEY, null);
        GitHubAccount gitHubAccount = gitHubAccountId == null
            ? null
            : gitHubAccountService.findOneByIdOrNull(UUID.fromString(gitHubAccountId));

        return new ProjectSettings(
                project,
                npmAccount,
                gitHubAccount
        );
    }
}
