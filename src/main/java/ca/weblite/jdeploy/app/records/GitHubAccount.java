package ca.weblite.jdeploy.app.records;

import java.util.UUID;

public record GitHubAccount(
        String accountName,
        String username,
        char[] token,
        UUID uuid
) {
}
