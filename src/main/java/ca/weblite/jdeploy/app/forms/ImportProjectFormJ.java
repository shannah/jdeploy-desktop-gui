/*
 * Created by JFormDesigner on Fri Dec 27 07:19:03 PST 2024
 */

package ca.weblite.jdeploy.app.forms;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author steve
 */
public class ImportProjectFormJ extends JPanel {
    public ImportProjectFormJ() {
        initComponents();
    }

    public JTextField getProjectDirectory() {
        return projectDirectory;
    }

    public JButton getBrowseProjectDirectory() {
        return browseProjectDirectory;
    }

    public JButton getImportButton() {
        return importButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JCheckBox getGenerateGitHubWorkflow() {
        return generateGitHubWorkflow;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Steven Hannah
        panel1 = new JPanel();
        label1 = new JLabel();
        projectDirectory = new JTextField();
        browseProjectDirectory = new JButton();
        generateGitHubWorkflow = new JCheckBox();
        panel2 = new JPanel();
        cancelButton = new JButton();
        importButton = new JButton();

        //======== this ========
        setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.
        border.EmptyBorder(0,0,0,0), "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn",javax.swing.border.TitledBorder.CENTER
        ,javax.swing.border.TitledBorder.BOTTOM,new java.awt.Font("Dia\u006cog",java.awt.Font
        .BOLD,12),java.awt.Color.red), getBorder())); addPropertyChangeListener(
        new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e){if("\u0062ord\u0065r"
        .equals(e.getPropertyName()))throw new RuntimeException();}});
        setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "default, $lcgap, 132dlu, $lcgap, default",
                "11*(default, $lgap), default"));

            //---- label1 ----
            label1.setText("Project Directory");
            panel1.add(label1, CC.xy(1, 1));
            panel1.add(projectDirectory, CC.xy(3, 1));

            //---- browseProjectDirectory ----
            browseProjectDirectory.setText("Select ...");
            panel1.add(browseProjectDirectory, CC.xy(5, 1));

            //---- generateGitHubWorkflow ----
            generateGitHubWorkflow.setText("Generate GitHub Workflow");
            panel1.add(generateGitHubWorkflow, CC.xy(3, 3));
        }
        add(panel1, BorderLayout.CENTER);

        //======== panel2 ========
        {
            panel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

            //---- cancelButton ----
            cancelButton.setText("Cancel");
            panel2.add(cancelButton);

            //---- importButton ----
            importButton.setText("Import");
            panel2.add(importButton);
        }
        add(panel2, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Steven Hannah
    private JPanel panel1;
    private JLabel label1;
    private JTextField projectDirectory;
    private JButton browseProjectDirectory;
    private JCheckBox generateGitHubWorkflow;
    private JPanel panel2;
    private JButton cancelButton;
    private JButton importButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
