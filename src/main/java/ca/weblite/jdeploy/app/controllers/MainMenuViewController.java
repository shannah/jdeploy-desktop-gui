package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.forms.MainMenu;
import ca.weblite.jdeploy.app.forms.MainMenuJ;
import ca.weblite.jdeploy.app.forms.TestForm;
import ca.weblite.jdeploy.app.services.Edt;

import javax.swing.*;

public class MainMenuViewController extends JFrameViewController {
    @Override
    protected JComponent initUI() {
        Edt edt = DIContext.get(Edt.class);

        MainMenuJ mainMenu = new MainMenuJ();

        mainMenu.getOpenButton().addActionListener(e -> {
            edt.invokeLater(new OpenProjectController(getFrame()));
        });
        return mainMenu;
        //return new TestForm().getMhPanel();
    }
}
