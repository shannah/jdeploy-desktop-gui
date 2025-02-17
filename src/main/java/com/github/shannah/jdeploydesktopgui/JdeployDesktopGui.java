package com.github.shannah.jdeploydesktopgui;
import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface;
import ca.weblite.jdeploy.app.controllers.MainMenuViewController;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.app.forms.SplashScreen;
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class JdeployDesktopGui {

    public static void main(String[] args) {
        EventQueue.invokeLater(SplashScreen::new);
        new JDeployDesktopGuiModule().install();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        createApplicationFilesDirectory();
        DIContext.get(DatabaseService.class).migrate();
        SwingUtilities.invokeLater(() -> new MainMenuViewController().run());
    }

    private static void createApplicationFilesDirectory() {
        Path appDataPath = DIContext.get(JdeployAppConfigInterface.class).getAppDataPath();
        ca.weblite.jdeploy.app.system.files.FileSystemInterface fileSystem
                = DIContext.get(ca.weblite.jdeploy.app.system.files.FileSystemInterface.class);
        try {
            if (!fileSystem.isDirectory(appDataPath.toAbsolutePath().toString())) {
                fileSystem.mkdir(appDataPath.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
