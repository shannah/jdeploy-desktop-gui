package ca.weblite.jdeploy.app.records;

import java.util.UUID;

public record ProjectSettings(
        Project project,
        NpmAccount npmAccount,
        GitHubAccount gitHubAccount
) {
}
