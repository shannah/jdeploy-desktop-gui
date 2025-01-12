package ca.weblite.jdeploy.app.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class ResponsiveImagePanel extends JPanel {
    private BufferedImage image;
    private int border = 10; // Border size

    public ResponsiveImagePanel() {

    }

    public ResponsiveImagePanel(String imagePath) {
        try {
            // Load the image
            image = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException | NullPointerException e) {
            System.err.println("Could not load image: " + e.getMessage());
        }
    }

    public void setImage(URL image) {
        try {
            this.image = ImageIO.read(image);
        } catch (IOException e) {
            System.err.println("Could not load image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;

            // Get available width and height
            int availableWidth = getWidth() - 2 * border;
            int availableHeight = getHeight() - 2 * border;

            // Get the containing window and calculate maxHeight as 50% of the window height
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                int maxHeight = (int) (window.getHeight() * 0.5);
                availableHeight = Math.min(availableHeight, maxHeight);
            }

            // Calculate the aspect ratio of the image
            double aspectRatio = (double) image.getWidth() / image.getHeight();

            // Calculate the dimensions to maintain the aspect ratio
            int newWidth = availableWidth;
            int newHeight = (int) (availableWidth / aspectRatio);

            if (newHeight > availableHeight) {
                newHeight = availableHeight;
                newWidth = (int) (availableHeight * aspectRatio);
            }

            // Center the image
            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;

            // Draw the image
            g2d.drawImage(image, x, y, newWidth, newHeight, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 300); // Default size
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); // Allow unlimited expansion
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0); // Allow the smallest possible size
    }
}