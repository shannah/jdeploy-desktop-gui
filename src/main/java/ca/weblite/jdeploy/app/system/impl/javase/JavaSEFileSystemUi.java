package ca.weblite.jdeploy.app.system.impl.javase;

import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface;

import javax.inject.Singleton;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class JavaSEFileSystemUi implements FileSystemUiInterface {

    @Override
    public String openDirectoryDialog(
            Object parentWindow,
            String title,
            String startingDir,
            FilenameFilter filenameFilter
    ) {
        Window parent = (Window) parentWindow;
        FileDialog fd = new FileDialog((Frame)parent, title, FileDialog.LOAD);
        fd.setMultipleMode(false);
        if (startingDir != null) {
            fd.setDirectory(startingDir);
        }
        fd.setFilenameFilter((dir, name) -> {
            boolean isDir = Files.isDirectory(Path.of(dir.getAbsolutePath(), name));
            if (filenameFilter == null) {
                return isDir;
            }
            return isDir && filenameFilter.accept(dir, name);
        });

        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0].getAbsolutePath();
    }

    @Override
    public String openFileDialog(
            Object parentWindow,
            String title,
            String startingDir,
            FilenameFilter filenameFilter
    ) {
        Window parent = (Window) parentWindow;
        FileDialog fd = new FileDialog((Frame)parent, title, FileDialog.LOAD);
        fd.setMultipleMode(false);
        if (startingDir != null) {
            fd.setDirectory(startingDir);
        }
        fd.setFilenameFilter((dir, name) -> {
            boolean isFile = Files.isRegularFile(Path.of(dir.getAbsolutePath(), name));
            if (filenameFilter == null) {
                return isFile;
            }
            return isFile && filenameFilter.accept(dir, name);
        });

        fd.setVisible(true);

        File[] files = fd.getFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0].getAbsolutePath();
    }
}
