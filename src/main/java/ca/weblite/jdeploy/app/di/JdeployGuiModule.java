package ca.weblite.jdeploy.app.di;

import ca.weblite.jdeploy.app.accounts.AccountServiceInterface;
import ca.weblite.jdeploy.app.accounts.PreferencesAccountService;
import ca.weblite.jdeploy.app.secure.JavaKeyringPasswordService;
import ca.weblite.jdeploy.app.secure.PasswordServiceInterface;
import ca.weblite.jdeploy.app.system.env.EnvironmentInterface;
import ca.weblite.jdeploy.app.system.files.FileSystemInterface;
import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface;
import ca.weblite.jdeploy.app.system.impl.javase.DefaultEnvironment;
import ca.weblite.jdeploy.app.system.impl.javase.JavaSEFileSystem;
import ca.weblite.jdeploy.app.system.impl.javase.JavaSEFileSystemUi;
import ca.weblite.jdeploy.app.system.impl.mac.MacFileSystemUi;
import ca.weblite.jdeploy.app.system.preferences.DefaultPreferences;
import ca.weblite.jdeploy.app.system.preferences.PreferencesInterface;
import org.codejargon.feather.Provides;

import java.util.prefs.Preferences;

public class JdeployGuiModule {
    @Provides
    public EnvironmentInterface getEnvironment(DefaultEnvironment environment) {
        return environment;
    }

    @Provides
    public Preferences getPreferences() {
        return Preferences.userNodeForPackage(DIContext.class);
    }

    @Provides
    public PreferencesInterface getPreferences(DefaultPreferences preferences) {
        return preferences;
    }

    @Provides
    public FileSystemInterface getFileSystem(JavaSEFileSystem fileSystem) {
        return fileSystem;
    }

    @Provides
    public FileSystemUiInterface getFileSystemUiInterface(
            EnvironmentInterface environment
    ) {
        if (environment.isMac()) {
            return DIContext.get(MacFileSystemUi.class);
        }
        if (environment.isLinux()) {
            return DIContext.get(JavaSEFileSystemUi.class);
        }
        if (environment.isWindows()) {
            return DIContext.get(JavaSEFileSystemUi.class);
        }
        return DIContext.get(JavaSEFileSystemUi.class);
    }

    @Provides
    public AccountServiceInterface accountServiceInterface(PreferencesAccountService impl) {
        return impl;
    }
    @Provides
    public PasswordServiceInterface passwordServiceInterface(JavaKeyringPasswordService javaKeyringPasswordService) {
        return javaKeyringPasswordService;
    }
}

