package ca.weblite.jdeploy.app.swing.components

import ca.weblite.jdeploy.app.swing.painters.PillBackgroundPainter
import ca.weblite.ktswing.extensions.createComponent
import ca.weblite.ktswing.style.Style
import ca.weblite.ktswing.style.Stylesheet
import org.jdesktop.swingx.JXLabel
import java.awt.Container
import javax.swing.BorderFactory

class TagLabel(): JXLabel() {
    init {
        backgroundPainter = PillBackgroundPainter()
        border = BorderFactory.createEmptyBorder(5, 15, 5, 15)
    }
}

fun Container.tagLabel(init: TagLabel.() -> Unit = {}): TagLabel =
    createComponent(factory = { TagLabel() }, init = init)

fun Stylesheet.tagLabel(selector: String, apply: TagLabel.() -> Unit): Style<TagLabel> =
    register(selector, TagLabel::class.java, apply)