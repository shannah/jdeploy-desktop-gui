package ca.weblite.jdeploy.app.factories;

import ca.weblite.jdeploy.app.records.Project;
import ca.weblite.jdeploy.app.records.ProjectEditorContext;
import ca.weblite.jdeploy.app.services.PackageJsonService;
import ca.weblite.jdeploy.gui.JDeployProjectEditor;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class ProjectEditorFactory {

    private final PackageJsonService packageJsonService;

    @Inject
    public ProjectEditorFactory(PackageJsonService packageJsonService) {
        this.packageJsonService = packageJsonService;
    }
    public JDeployProjectEditor createOne(Project project) throws IOException {
        JSONObject packageJson = packageJsonService.readOne(project.getPackageJsonPath());
        return new JDeployProjectEditor(
                new File(project.getPackageJsonPath()),
                packageJson,
                new ProjectEditorContext(project)
        );

    }
}
