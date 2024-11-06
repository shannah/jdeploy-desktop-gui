package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.exceptions.NotFoundException;
import ca.weblite.jdeploy.app.records.NpmAccount;
import ca.weblite.jdeploy.app.repositories.NpmAccountRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class NpmAccountService {
    private final NpmAccountRepository npmAccountRepository;

    @Inject
    public NpmAccountService(NpmAccountRepository npmAccountRepository) {
        this.npmAccountRepository = npmAccountRepository;
    }

    public NpmAccount findOneById(UUID uuid) throws NotFoundException {
        return npmAccountRepository.findOneById(uuid);
    }

    public NpmAccount findOneByIdOrNull(UUID uuid) {
        return npmAccountRepository.findOneByIdOrNull(uuid);
    }
}
