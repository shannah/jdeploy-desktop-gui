package ca.weblite.jdeploy.app.factories;

import ca.weblite.jdeploy.app.records.ProjectEditorContext;
import ca.weblite.jdeploy.app.records.ProjectSettings;
import ca.weblite.jdeploy.app.services.PackageJsonService;
import ca.weblite.jdeploy.app.system.files.FileSystemInterface;
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
    public JDeployProjectEditor createOne(ProjectSettings projectSettings) throws IOException {
        JSONObject packageJson = packageJsonService.readOne(projectSettings.project().getPackageJsonPath());
        return new JDeployProjectEditor(
                new File(projectSettings.project().getPackageJsonPath()),
                packageJson,
                new ProjectEditorContext(projectSettings)
        );

    }
}
