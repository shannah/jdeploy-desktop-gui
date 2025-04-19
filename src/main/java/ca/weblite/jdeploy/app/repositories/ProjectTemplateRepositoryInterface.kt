package ca.weblite.jdeploy.app.repositories

import ca.weblite.jdeploy.app.records.ProjectTemplates

interface ProjectTemplateRepositoryInterface {
    suspend fun findAll(): ProjectTemplates
}