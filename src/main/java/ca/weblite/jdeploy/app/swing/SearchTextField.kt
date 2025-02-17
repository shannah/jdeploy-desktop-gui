package ca.weblite.jdeploy.app.swing
import javax.swing.*
import java.awt.*
import javax.swing.border.AbstractBorder

class SearchTextField(columns: Int = 15) : JTextField(columns) {
    var placeholder: String = "Search..."

    init {
        // Use a compound border:
        // - The outer border is rounded.
        // - The inner empty border provides padding (reserve some space for a search icon if desired).
        border = BorderFactory.createCompoundBorder(
            RoundedBorder(24),
            BorderFactory.createEmptyBorder(4,24,4,4)
        )
        // Optional: set a light background.
        background = Color(0xFAFAFA)
        // Remove the default focus painted border.
        focusTraversalKeysEnabled = false
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        // If no text is present and we don't have focus, paint the placeholder.
        if (text.isEmpty() && !hasFocus()) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.color = Color.GRAY
            // Compute position for placeholder text (adjust left inset as needed).
            val insets = insets
            val fm = g2.fontMetrics
            val x = insets.left
            val y = (height - fm.height) / 2 + fm.ascent
            g2.drawString(placeholder, x, y)
            g2.dispose()
        }
    }
}