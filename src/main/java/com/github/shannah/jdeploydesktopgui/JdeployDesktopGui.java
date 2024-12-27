package com.github.shannah.jdeploydesktopgui;
import ca.weblite.jdeploy.app.controllers.MainMenuViewController;
import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.forms.MainMenu;
import ca.weblite.jdeploy.app.repositories.impl.jpa.services.DatabaseService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JdeployDesktopGui {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        DIContext.get(DatabaseService.class).migrate();
        SwingUtilities.invokeLater(() -> new MainMenuViewController().run());
    }
}
