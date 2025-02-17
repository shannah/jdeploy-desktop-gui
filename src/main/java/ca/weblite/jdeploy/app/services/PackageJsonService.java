package ca.weblite.jdeploy.app.services;

import ca.weblite.jdeploy.app.system.files.FileSystemInterface;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Singleton
public class PackageJsonService {

    private final FileSystemInterface fileSystem;

    @Inject
    public PackageJsonService(FileSystemInterface fileSystem) {
        this.fileSystem = fileSystem;
    }
    public JSONObject readOne(String path) throws IOException {
        try (InputStream input = fileSystem.openInputStream(path)) {
            return new JSONObject(IOUtils.toString(input, StandardCharsets.UTF_8));
        }
    }
}
