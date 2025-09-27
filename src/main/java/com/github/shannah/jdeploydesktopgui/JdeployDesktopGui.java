package com.github.shannah.jdeploydesktopgui;
import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface;
import ca.weblite.jdeploy.app.controllers.MainMenuViewController;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.app.forms.AboutDialog;
import ca.weblite.jdeploy.app.forms.SplashScreen;
import ca.weblite.jdeploy.app.repositories.DefaultProjectTemplateRepository;
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService;
import com.formdev.flatlaf.FlatLightLaf;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

import static org.freedesktop.dbus.utils.Util.isMacOs;

public class JdeployDesktopGui {

    public static void main(String[] args) {
        if (!isMacOs()) {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        }

        // Set up Desktop API About handler for macOS
        setupDesktopAboutHandler();

        EventQueue.invokeLater(()->{
            new SplashScreen().showSplash();
        });
        new JDeployDesktopGuiModule().install();

        createApplicationFilesDirectory();
        DIContext.get(DatabaseService.class).migrate();
        DIContext.get(DefaultProjectTemplateRepository.class).clearCacheBlocking();
        SwingUtilities.invokeLater(() -> new MainMenuViewController().run());
    }

    private static void setupDesktopAboutHandler() {
        // Check if Desktop is supported
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            // Check if About handler is supported (mainly for macOS)
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler(e -> {
                    // Show the custom About dialog
                    SwingUtilities.invokeLater(() -> {
                        // Get the active frame (if any) to use as parent
                        Frame activeFrame = null;
                        Frame[] frames = Frame.getFrames();
                        for (Frame frame : frames) {
                            if (frame.isActive()) {
                                activeFrame = frame;
                                break;
                            }
                        }

                        // If no active frame, use the first visible frame
                        if (activeFrame == null) {
                            for (Frame frame : frames) {
                                if (frame.isVisible()) {
                                    activeFrame = frame;
                                    break;
                                }
                            }
                        }

                        AboutDialog aboutDialog = new AboutDialog(activeFrame);
                        aboutDialog.setVisible(true);
                    });
                });
            }
        }
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
