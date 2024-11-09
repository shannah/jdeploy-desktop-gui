package ca.weblite.jdeploy.app.records;

import ca.weblite.jdeploy.gui.JDeployProjectEditorContext;

public class ProjectEditorContext extends JDeployProjectEditorContext {
    private final Project project;

    public ProjectEditorContext(Project projectSettings) {
        this.project = projectSettings;
    }

    public Project projectSettings() {
        return project;
    }
}
