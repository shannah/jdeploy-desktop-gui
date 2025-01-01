package com.github.shannah.jdeploydesktopgui;
import ca.weblite.jdeploy.app.controllers.MainMenuViewController;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule;
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class JdeployDesktopGui {

    public static void main(String[] args) {
        new JDeployDesktopGuiModule().install();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        DIContext.get(DatabaseService.class).migrate();
        SwingUtilities.invokeLater(() -> new MainMenuViewController().run());
    }
}
