package ca.weblite.jdeploy.app.npm;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NpmAccountServiceInterface {
    CompletableFuture<List<NpmAccountInterface>> getNpmAccounts();
    CompletableFuture<Void> saveNpmAccount(NpmAccountInterface account);

    CompletableFuture<Void> removeNpmAccount(NpmAccountInterface account);

    CompletableFuture<NpmAccountInterface> loadNpmAccount(NpmAccountInterface accountName);
}
