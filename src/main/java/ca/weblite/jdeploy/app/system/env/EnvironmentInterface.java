package ca.weblite.jdeploy.app.system.env;

public interface EnvironmentInterface {
    public String getUserHomeDirectory();
    public boolean isMac();

    public boolean isWindows();

    public boolean isLinux();
}
