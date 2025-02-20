package ca.weblite.jdeploy.app.system.impl.mac;

import ca.weblite.jdeploy.app.system.impl.javase.JavaSEFileSystemUi;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MacFileSystemUi extends JavaSEFileSystemUi {

    @Override
    public String openDirectoryDialog(
            Object parentWindow,
            String title,
            String startingDir,
            FilenameFilter filenameFilter,
            FileSelectionValidationListener onFileSelected
    ) {
        System.setProperty("apple.awt.fileDialogForDirectories", "true"); // Enable directory selection

        Window parent = (Window) parentWindow;
        FileDialog fd = new FileDialog((Frame) parent, title, FileDialog.LOAD);
        fd.setMultipleMode(false);
        if (startingDir != null) {
            fd.setDirectory(startingDir);
        }

        // Set filename filter to accept only directories
        fd.setFilenameFilter((dir, name) -> {
            File file = new File(dir, name);
            boolean isDir = file.isDirectory();
            if (filenameFilter == null) {
                return isDir;
            }
            return isDir && filenameFilter.accept(dir, name);
        });

        fd.setVisible(true);
        File[] files = fd.getFiles();
        System.setProperty("apple.awt.fileDialogForDirectories", "false"); // Disable directory selection

        if (files == null || files.length == 0) {
            return null;
        }

        // Return the absolute path of the selected directory
        File selected = files[0];
        if (selected.isDirectory()) {
            return selected.getAbsolutePath();
        }

        return null;
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
