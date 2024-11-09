package ca.weblite.jdeploy.app.repositories;

import ca.weblite.jdeploy.app.collections.ProjectSet;
import ca.weblite.jdeploy.app.records.Project;

import java.util.UUID;

public interface ProjectRepositoryInterface {
    Project findOneById(UUID id);

    ProjectSet findRecent();

    Project findOnebyPath(String path);
}
