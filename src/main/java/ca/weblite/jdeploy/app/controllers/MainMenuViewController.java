package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.factories.ControllerFactory;
import ca.weblite.jdeploy.app.forms.MainMenuJ;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.services.Edt;
import ca.weblite.jdeploy.app.services.ProjectService;
import ca.weblite.jdeploy.app.swing.ResponsiveImagePanel;
import ca.weblite.jdeploy.app.views.mainMenu.ProjectListCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainMenuViewController extends JFrameViewController {

    private final ProjectService projectService;
    private final ControllerFactory controllerFactory;

    public MainMenuViewController() {
        this.projectService = DIContext.get(ProjectService.class);
        this.controllerFactory = DIContext.get(ControllerFactory.class);
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

        mainMenu.getCreateProjectButton().addActionListener(e -> {
            var newProjectController = new NewProjectController(getFrame());
            newProjectController.show();
        });

        mainMenu.getHeroGraphicWrapper().add(
                new ResponsiveImagePanel(
                        "/ca/weblite/jdeploy/app/assets/jdeploy-home-hero.png"
                )
        , BorderLayout.CENTER);

        return mainMenu;
        //return new TestForm().getMhPanel();
    }

    @Override
    protected void onBeforeShow() {
        getFrame().setTitle("jDeploy");
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
