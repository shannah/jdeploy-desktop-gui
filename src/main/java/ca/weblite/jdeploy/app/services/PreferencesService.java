package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.records.NpmAccount;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.prefs.Preferences;

@Singleton
public class PreferencesService {

    private static final String PROJECTS_KEY = "projects";
    public static final String NPM_ACCOUNTS_KEY = "npmAccounts";

    public static final String GITHUB_ACCOUNTS_KEY = "githubAccounts";

    private final PreferencesInterface rootPreferences;

    @Inject
    public PreferencesService(PreferencesInterface rootPreferences) {
        this.rootPreferences = rootPreferences;
    }

    public PreferencesInterface getProjectPreferences(Project project) {
        return rootPreferences.getSubPreferences(PROJECTS_KEY).getSubPreferences(project.getUuid().toString());
    }

    public PreferencesInterface getNpmAccountPreferences(UUID npmAccountId) {
        return rootPreferences.getSubPreferences(NPM_ACCOUNTS_KEY).getSubPreferences(npmAccountId.toString());
    }

    public PreferencesInterface getGitHubAccountPreferences(UUID githubAccountId) {
        return rootPreferences.getSubPreferences(GITHUB_ACCOUNTS_KEY).getSubPreferences(githubAccountId.toString());
    }

    public PreferencesInterface getRootPreferences() {
        return rootPreferences;
    }
}

