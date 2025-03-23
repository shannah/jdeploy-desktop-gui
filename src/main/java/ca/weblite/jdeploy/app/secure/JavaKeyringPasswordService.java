package ca.weblite.jdeploy.app.secure;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;

import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

@Singleton
public class JavaKeyringPasswordService implements PasswordServiceInterface {

    private static final String SERVICE_NAME = "com.jdeploy";

    // Fallback storage using Preferences
    private final Preferences fallbackPrefs = Preferences.userRoot().node(SERVICE_NAME);

    @Override
    public CompletableFuture<char[]> getPassword(String name, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Keyring keyring = Keyring.create();
                String password = keyring.getPassword(SERVICE_NAME, name);
                if (password != null) {
                    return password.toCharArray();
                }
            } catch (BackendNotSupportedException | PasswordAccessException ignored) {
                // Fallback to Preferences
            }
            // Fallback retrieval
            String fallbackPassword = fallbackPrefs.get(name, null);
            return fallbackPassword != null ? fallbackPassword.toCharArray() : null;
        });
    }

    @Override
    public CompletableFuture<Void> setPassword(String name, char[] password) {
        return CompletableFuture.runAsync(() -> {
            String passStr = password != null ? new String(password) : null;

            try {
                Keyring keyring = Keyring.create();
                if (passStr == null || passStr.isEmpty()) {
                    keyring.deletePassword(SERVICE_NAME, name);
                } else {
                    keyring.setPassword(SERVICE_NAME, name, passStr);
                }
                // Successfully stored in keyring, remove from fallback
                fallbackPrefs.remove(name);
                return;
            } catch (BackendNotSupportedException | PasswordAccessException ignored) {
                // Fallback to Preferences
            }

            // Fallback storage
            if (passStr == null || passStr.isEmpty()) {
                fallbackPrefs.remove(name);
            } else {
                fallbackPrefs.put(name, passStr);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removePassword(String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                Keyring keyring = Keyring.create();
                keyring.deletePassword(SERVICE_NAME, name);
            } catch (BackendNotSupportedException | PasswordAccessException ignored) {
                // Fallback to Preferences
            }
            fallbackPrefs.remove(name);
        });
    }
}