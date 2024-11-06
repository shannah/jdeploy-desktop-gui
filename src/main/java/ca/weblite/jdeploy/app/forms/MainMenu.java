package ca.weblite.jdeploy.app.forms;

import javax.swing.*;

public class MainMenu {
    private JButton newProjectButton;
    private JButton openButton2;
    private JTextField projectsFilter;
    private JList recentProjectsList;
    private JButton openButton;
    private JButton importButton;
    private JSplitPane splitPane;

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public JButton getOpenButton() {
        return openButton;
    }
}
