package ca.weblite.jdeploy.app.collections;

import ca.weblite.jdeploy.app.records.Project;

import java.util.Arrays;
import java.util.Iterator;

public class ProjectSet implements Iterable<ca.weblite.jdeploy.app.records.Project>{
    private final java.util.Set<ca.weblite.jdeploy.app.records.Project> projects = new java.util.LinkedHashSet<>();

    public ProjectSet(Project... projects) {
        this.projects.addAll(Arrays.asList(projects));
    }

    @Override
    public Iterator<Project> iterator() {
        return projects.iterator();
    }
}
