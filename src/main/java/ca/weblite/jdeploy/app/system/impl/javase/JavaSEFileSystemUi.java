package ca.weblite.jdeploy.app.system.impl.javase;

import ca.weblite.jdeploy.app.system.files.FileSystemUiInterface;

import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
            FilenameFilter filenameFilter,
            FileSystemUiInterface.FileSelectionValidationListener onFileSelected

    ) {
        Window parent = (Window) parentWindow;
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File selectedFile = getSelectedFile();
                if (
                        selectedFile == null
                                || onFileSelected == null
                                || onFileSelected.validateSelectedFile(selectedFile.getAbsolutePath())
                ) {
                    super.approveSelection();
                }

            }
        };

        // Set up file chooser to select directories only
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setDialogTitle(title);

        // Set the initial directory if provided
        if (startingDir != null) {
            fileChooser.setCurrentDirectory(new File(startingDir));
        }

        // Apply FilenameFilter if provided
        if (filenameFilter != null) {
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        // Check if the directory meets the criteria of filenameFilter
                        return filenameFilter.accept(file.getParentFile(), file.getName());
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return "Directories only";
                }
            });
        }

        // Show the dialog
        int result = fileChooser.showOpenDialog(parent);

        // Check if the user approved the selection
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            return selectedDirectory.getAbsolutePath();
        } else {
            return null; // If the user canceled the selection
        }
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
