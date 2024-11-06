package ca.weblite.jdeploy.app.system.preferences;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.prefs.Preferences;

@Singleton
public class DefaultPreferences implements PreferencesInterface {

    private final Preferences node;


    @Inject
    public DefaultPreferences(Preferences node) {
        this.node = node;
    }


    @Override
    public void set(String key, String value) {
        node.put(key, value);
    }

    @Override
    public String get(String key, String defaultValue) {
        return node.get(key, defaultValue);
    }

    @Override
    public void remove(String key) {
        node.remove(key);
    }

    @Override
    public void clear() throws IOException {
        try {
            node.clear();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public PreferencesInterface getSubPreferences(String key) {
        return new DefaultPreferences(node.node(key));
    }

    @Override
    public void commit() throws IOException {
        try {
            node.flush();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
