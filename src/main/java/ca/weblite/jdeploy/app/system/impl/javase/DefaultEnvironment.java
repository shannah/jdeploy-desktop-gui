package ca.weblite.jdeploy.app.system.impl.javase;

import ca.weblite.jdeploy.app.system.env.EnvironmentInterface;

import javax.inject.Singleton;

@Singleton
public class DefaultEnvironment implements EnvironmentInterface {
    @Override
    public String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }
}
