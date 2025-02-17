package ca.weblite.jdeploy.app.system.preferences;

import java.io.IOException;

public interface PreferencesInterface {
    void set(String key, String value);
    String get(String key, String defaultValue);
    void remove(String key);
    void clear() throws IOException;

    PreferencesInterface getSubPreferences(String key);

    void commit() throws IOException;
}
