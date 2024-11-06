package ca.weblite.jdeploy.app.system.impl.javase;

import ca.weblite.jdeploy.app.system.files.FileSystemInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaSEFileSystem implements FileSystemInterface {
    @Override
    public InputStream openInputStream(String path) throws IOException {
        return Files.newInputStream(Path.of(path));
    }

    @Override
    public OutputStream openOutputStream(String path) throws IOException {
        return Files.newOutputStream(Path.of(path));
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public boolean isDirectory(String path) {
        return Files.isDirectory(Path.of(path));
    }
}
