package ca.weblite.jdeploy.app.repositories;

import ca.weblite.jdeploy.app.exceptions.NotFoundException;
import ca.weblite.jdeploy.app.records.NpmAccount;
import ca.weblite.jdeploy.app.services.PreferencesService;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class NpmAccountRepository {
    private final PreferencesService preferencesService;

    @Inject
    public NpmAccountRepository(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public NpmAccount findOneById(UUID uuid) throws NotFoundException {
        PreferencesInterface npmAccountPreferences = preferencesService.getNpmAccountPreferences(uuid);
        if (npmAccountPreferences == null) {
            throw new NotFoundException("NpmAccount not found");
        }
        return createFromPreferences(npmAccountPreferences);
    }

    public NpmAccount findOneByIdOrNull(UUID uuid) {
        try {
            return findOneById(uuid);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private NpmAccount createFromPreferences(PreferencesInterface npmAccountPreferences) throws NotFoundException {
        if (npmAccountPreferences.get("name", null) == null) {
            throw new NotFoundException("NpmAccount not found");
        }
        if (npmAccountPreferences.get("uuid", null) == null) {
            throw new NotFoundException("NpmAccount not found");
        }
        return new NpmAccount(
                UUID.fromString(npmAccountPreferences.get("uuid", null)),
                npmAccountPreferences.get("name", null),
                null
        );
    }
}
