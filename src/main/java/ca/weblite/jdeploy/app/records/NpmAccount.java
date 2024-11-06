package ca.weblite.jdeploy.app.records;

import java.util.UUID;

public record NpmAccount(
        UUID uuid,
        String accountName,
        char[] token
) {
}
