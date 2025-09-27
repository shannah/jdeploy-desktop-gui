package ca.weblite.jdeploy.app.forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple visual test for AboutDialog without dependencies
 */
public class AboutDialogVisualTest extends JDialog {
    private static final String DEFAULT_VERSION = "Unknown";
    private static final String DEFAULT_COMMIT = "Unknown";
    
    public AboutDialogVisualTest(Window parent) {
        super(parent, "About jDeploy", ModalityType.APPLICATION_MODAL);
        initializeComponents();
        setupLayout();
        setupBehavior();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Top panel with icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Icon panel (fallback text icon)
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel iconLabel = new JLabel("🚀", SwingConstants.CENTER);  // Using emoji as fallback
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        iconPanel.add(iconLabel);
        topPanel.add(iconPanel, BorderLayout.NORTH);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("jDeploy");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        titlePanel.add(titleLabel);
        topPanel.add(titlePanel, BorderLayout.CENTER);
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        
        // Version/commit info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        String versionText = getVersionText();
        JLabel versionLabel = new JLabel(versionText, SwingConstants.CENTER);
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        infoPanel.add(versionLabel, BorderLayout.CENTER);
        
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(okButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupBehavior() {
        // Set minimum size
        setMinimumSize(new Dimension(300, 200));
        setResizable(false);
        
        // Handle escape key to close dialog
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Set default button
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane().getComponent(1)).getComponent(0));
    }
    
    private String getVersionText() {
        String version = System.getProperty("jdeploy.app.version", DEFAULT_VERSION);
        String commitHash = System.getProperty("jdeploy.commitHash", DEFAULT_COMMIT);
        
        // If version starts with "0.0.0", show commit hash instead
        if (version.startsWith("0.0.0")) {
            return "Commit: " + commitHash;
        } else {
            return "Version: " + version;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Test different scenarios
            JFrame testFrame = new JFrame("AboutDialog Test");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(300, 200);
            testFrame.setLocationRelativeTo(null);
            
            JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Test 1: Regular version
            JButton test1Button = new JButton("Test Version 1.0.0");
            test1Button.addActionListener(e -> {
                System.setProperty("jdeploy.app.version", "1.0.0");
                System.setProperty("jdeploy.commitHash", "abc123def456");
                new AboutDialogVisualTest(testFrame).setVisible(true);
            });
            buttonPanel.add(test1Button);
            
            // Test 2: Development version (0.0.0)
            JButton test2Button = new JButton("Test Version 0.0.0");
            test2Button.addActionListener(e -> {
                System.setProperty("jdeploy.app.version", "0.0.0-SNAPSHOT");
                System.setProperty("jdeploy.commitHash", "commit123456789");
                new AboutDialogVisualTest(testFrame).setVisible(true);
            });
            buttonPanel.add(test2Button);
            
            // Test 3: No properties
            JButton test3Button = new JButton("Test No Properties");
            test3Button.addActionListener(e -> {
                System.clearProperty("jdeploy.app.version");
                System.clearProperty("jdeploy.commitHash");
                new AboutDialogVisualTest(testFrame).setVisible(true);
            });
            buttonPanel.add(test3Button);
            
            testFrame.add(buttonPanel);
            testFrame.setVisible(true);
        });
    }
}