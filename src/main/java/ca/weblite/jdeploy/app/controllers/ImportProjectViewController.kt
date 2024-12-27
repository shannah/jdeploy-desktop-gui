package ca.weblite.jdeploy.app.controllers

import ca.weblite.jdeploy.app.forms.ImportProjectFormJ
import javax.swing.JComponent
import javax.swing.JFrame

class ImportProjectViewController(parentFrame: JFrame): JFrameViewController(parentFrame) {
    override fun initUI(): JComponent {
        val form = ImportProjectFormJ()

        return form
    }
}