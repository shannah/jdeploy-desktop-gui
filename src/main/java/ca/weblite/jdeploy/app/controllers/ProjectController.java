package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.factories.ProjectEditorFactory;
import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.services.Edt;
import ca.weblite.jdeploy.gui.JDeployProjectEditor;

import java.io.IOException;

public class ProjectController implements Runnable {
    private final Project project;

    private final ProjectEditorFactory projectEditorFactory;

    private final Edt edt;

    public ProjectController(
            Project project
    ) {
        this.project = project;
        this.projectEditorFactory = DIContext.get(ProjectEditorFactory.class);
        this.edt = DIContext.get(Edt.class);
    }

    @Override
    public void run() {
       try {
           JDeployProjectEditor editor = projectEditorFactory.createOne(project);
           editor.show();
       } catch (IOException e) {
           edt.invokeLater(new ErrorController(e));
       }
    }
}
