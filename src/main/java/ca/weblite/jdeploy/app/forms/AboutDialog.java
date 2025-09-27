package ca.weblite.jdeploy.app.forms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutDialog extends JDialog {
    private static final String DEFAULT_VERSION = "Unknown";
    private static final String DEFAULT_COMMIT = "Unknown";
    
    public AboutDialog(Window parent) {
        super(parent, "About jDeploy", ModalityType.APPLICATION_MODAL);
        initializeComponents();
        setupLayout();
        setupBehavior();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Set the dialog icon
        try {
            setIconImage(new ImageIcon(getClass().getResource("/ca/weblite/jdeploy/app/assets/icon.png")).getImage());
        } catch (Exception e) {
            // Ignore if icon can't be loaded
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Top panel with icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Icon panel
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/ca/weblite/jdeploy/app/assets/icon.png"));
            // Scale the icon to a reasonable size for the dialog
            Image scaledImage = originalIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            JLabel iconLabel = new JLabel(scaledIcon);
            iconPanel.add(iconLabel);
        } catch (Exception e) {
            // Fallback text if icon can't be loaded
            JLabel iconLabel = new JLabel("jDeploy", SwingConstants.CENTER);
            iconLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
            iconPanel.add(iconLabel);
        }
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
}