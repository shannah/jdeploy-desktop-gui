package ca.weblite.jdeploy.app.records;

import ca.weblite.jdeploy.gui.JDeployProjectEditorContext;

public class ProjectEditorContext extends JDeployProjectEditorContext {
    private final ProjectSettings projectSettings;

    public ProjectEditorContext(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
    }

    public ProjectSettings projectSettings() {
        return projectSettings;
    }
}
