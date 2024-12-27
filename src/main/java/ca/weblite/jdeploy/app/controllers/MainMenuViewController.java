package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.forms.MainMenuJ;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.services.Edt;
import ca.weblite.jdeploy.app.services.ProjectService;
import ca.weblite.jdeploy.app.views.mainMenu.ProjectListCellRenderer;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MainMenuViewController extends JFrameViewController {

    private final ProjectService projectService;

    public MainMenuViewController() {
        this.projectService = DIContext.get(ProjectService.class);
    }
    @Override
    protected JComponent initUI() {
        Edt edt = DIContext.get(Edt.class);

        MainMenuJ mainMenu = new MainMenuJ();

        mainMenu.getOpenButton().addActionListener(e -> {
            edt.invokeLater(new OpenProjectController(getFrame()));
        });

        mainMenu.getRecentProjects().setModel(buildRecentProjectsModel());
        mainMenu.getRecentProjects().setCellRenderer(new ProjectListCellRenderer());

        Action openRecentAction = openRecentAction(mainMenu);

        // openRecentAction should only be enabled when there is a project in the recent projects list selected
        mainMenu.getRecentProjects().getSelectionModel().addListSelectionListener(e -> {
            openRecentAction.setEnabled(mainMenu.getRecentProjects().getSelectedValue() != null);
        });
        mainMenu.getOpenRecentButton().setAction(openRecentAction);

        mainMenu.getImportProject().addActionListener(e -> {
            new ImportProjectViewController(getFrame()).run();
        });

        return mainMenu;
        //return new TestForm().getMhPanel();
    }

    private ListModel<Project> buildRecentProjectsModel() {
        DefaultListModel<Project> model = new DefaultListModel<>();
        for (Project project: projectService.findRecent()) {
            model.addElement(project);
        }

        return model;
    }

    private Action openRecentAction(MainMenuJ mainMenu) {
        return new AbstractAction() {

            {
                putValue(Action.NAME, "Open");
                putValue(Action.SHORT_DESCRIPTION, "Open the selected recent project");
                setEnabled(false);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                Project project = mainMenu.getRecentProjects().getSelectedValue();
                if (project != null) {
                    new OpenProjectController(getFrame(), project.getPath()).run();
                }
            }
        };
    }
}
