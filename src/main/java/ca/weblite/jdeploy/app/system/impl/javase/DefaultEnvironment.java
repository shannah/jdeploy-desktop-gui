package ca.weblite.jdeploy.app.system.impl.javase;

import ca.weblite.jdeploy.app.system.env.EnvironmentInterface;

import javax.inject.Singleton;

@Singleton
public class DefaultEnvironment implements EnvironmentInterface {
    @Override
    public String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    @Override
    public boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    @Override
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("nux");
    }
}
