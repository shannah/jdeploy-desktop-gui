package ca.weblite.jdeploy.app.swing.painters

import org.jdesktop.swingx.painter.Painter
import java.awt.*
import java.awt.geom.RoundRectangle2D

class PillBackgroundPainter : Painter<Component> {

    override fun paint(g: Graphics2D, component: Component, width: Int, height: Int) {
        val antialias = RenderingHints.KEY_ANTIALIASING
        g.setRenderingHint(antialias, RenderingHints.VALUE_ANTIALIAS_ON)

        val padding = 1f
        val shapeWidth = width - 2 * padding
        val shapeHeight = height - 2 * padding
        val radius = shapeHeight

        val shape = RoundRectangle2D.Float(
            padding,
            padding,
            shapeWidth,
            shapeHeight,
            radius,
            radius
        )

        // Optional subtle shadow
        val shadowOffset = 1f
        g.color = Color(0, 0, 0, 30)
        val shadowShape = RoundRectangle2D.Float(
            padding + shadowOffset,
            padding + shadowOffset,
            shapeWidth,
            shapeHeight,
            radius,
            radius
        )
        g.fill(shadowShape)

        // Fill background
        g.color = component.background
        g.fill(shape)

        // Border stroke
        g.color = Color(0, 0, 0, 50)
        g.stroke = BasicStroke(1f)
        g.draw(shape)
    }
}


