package ca.weblite.jdeploy.app.forms

import ca.weblite.jdeploy.app.records.Template

interface TemplateTileDelegate {
    fun openTemplateSources(template: Template)
    fun openTemplateDemoDownloadPage(template: Template)
    fun openWebAppUrl(template: Template) {
        // Default implementation does nothing
    }
}