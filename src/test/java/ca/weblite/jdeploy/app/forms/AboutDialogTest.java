package ca.weblite.jdeploy.app.forms;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test class to verify AboutDialog compilation and basic functionality
 */
public class AboutDialogTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            // Test version 1.0.0 (should show version)
            System.setProperty("jdeploy.app.version", "1.0.0");
            System.setProperty("jdeploy.commitHash", "abc123def456");
            testAboutDialog("Test 1: Version 1.0.0");
            
            // Test version 0.0.0 (should show commit hash)
            System.setProperty("jdeploy.app.version", "0.0.0-SNAPSHOT");
            System.setProperty("jdeploy.commitHash", "commit123456789");
            testAboutDialog("Test 2: Version 0.0.0");
            
            // Test with no system properties (should show defaults)
            System.clearProperty("jdeploy.app.version");
            System.clearProperty("jdeploy.commitHash");
            testAboutDialog("Test 3: No system properties");
        });
    }
    
    private static void testAboutDialog(String testName) {
        JFrame testFrame = new JFrame("Test Frame - " + testName);
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        testFrame.setSize(400, 200);
        testFrame.setLocationRelativeTo(null);
        
        JButton showAboutButton = new JButton("Show About Dialog");
        showAboutButton.addActionListener(e -> {
            AboutDialog dialog = new AboutDialog(testFrame);
            dialog.setVisible(true);
        });
        
        testFrame.add(showAboutButton);
        testFrame.setVisible(true);
    }
}