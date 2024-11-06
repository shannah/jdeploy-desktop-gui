package ca.weblite.jdeploy.app.system.files;

import javax.inject.Singleton;
import java.io.FilenameFilter;

@Singleton
public interface FileSystemUiInterface {

    public interface FileSelectionValidationListener {
        public boolean validateSelectedFile(String path);
    }

    public String openDirectoryDialog(
            Object parentWindow,
            String title,
            String startingDir,
            FilenameFilter filenameFilter,
            FileSelectionValidationListener onFileSelected
    );
    public String openFileDialog(
            Object parentWindow,
            String title,
            String startingDir,
            FilenameFilter filenameFilter
    );
}
