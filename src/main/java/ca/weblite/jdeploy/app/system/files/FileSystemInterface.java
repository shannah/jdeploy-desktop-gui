package ca.weblite.jdeploy.app.system.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileSystemInterface {
    public InputStream openInputStream(String path) throws IOException;
    public OutputStream openOutputStream(String path) throws IOException;

    public boolean exists(String path);

    public boolean isDirectory(String path);
}
