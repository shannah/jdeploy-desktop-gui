package ca.weblite.jdeploy.app.swing

import java.awt.*
import javax.swing.border.AbstractBorder

class RoundedBorder(private val radius: Int) : AbstractBorder() {
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color.GRAY
        // Draw a rounded rectangle (adjust -1 to keep border fully visible)
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
        g2.dispose()
    }

    override fun getBorderInsets(c: Component): Insets {
        val pad = 0//radius / 2
        return Insets(pad, pad, pad, pad)
    }

    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        insets.left = radius / 2
        insets.top = radius / 2
        insets.right = radius / 2
        insets.bottom = radius / 2
        return insets
    }
}