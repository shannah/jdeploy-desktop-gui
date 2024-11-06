package ca.weblite.jdeploy.app.collections;

public class ProjectSettingsSet implements java.lang.Iterable<ca.weblite.jdeploy.app.records.ProjectSettings>{
    private final java.util.Set<ca.weblite.jdeploy.app.records.ProjectSettings> projectSettings = new java.util.LinkedHashSet<>();

    public ProjectSettingsSet(ca.weblite.jdeploy.app.records.ProjectSettings... projectSettings) {
        this.projectSettings.addAll(java.util.Arrays.asList(projectSettings));
    }

    public java.util.Iterator<ca.weblite.jdeploy.app.records.ProjectSettings> iterator() {
        return projectSettings.iterator();
    }
}
