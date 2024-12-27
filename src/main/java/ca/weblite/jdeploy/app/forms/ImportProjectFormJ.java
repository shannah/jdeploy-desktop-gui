/*
 * Created by JFormDesigner on Fri Dec 27 07:19:03 PST 2024
 */

package ca.weblite.jdeploy.app.forms;

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

    public JTextField getJarFilePath() {
        return jarFilePath;
    }

    public JButton getBrowseProjectDirectory() {
        return browseProjectDirectory;
    }

    public JButton getBrowseJarFilePath() {
        return browseJarFilePath;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Steven Hannah
        label1 = new JLabel();
        projectDirectory = new JTextField();
        browseProjectDirectory = new JButton();
        label2 = new JLabel();
        jarFilePath = new JTextField();
        browseJarFilePath = new JButton();
        label3 = new JLabel();
        textField3 = new JTextField();

        //======== this ========
        setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0
        ,0,0,0), "JF\u006frmDes\u0069gner \u0045valua\u0074ion",javax.swing.border.TitledBorder.CENTER,javax.swing.border.TitledBorder.BOTTOM
        ,new java.awt.Font("D\u0069alog",java.awt.Font.BOLD,12),java.awt.Color.red),
         getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e
        ){if("\u0062order".equals(e.getPropertyName()))throw new RuntimeException();}});
        setLayout(new FormLayout(
            "default, $lcgap, 132dlu, $lcgap, default",
            "2*(default, $lgap), default"));

        //---- label1 ----
        label1.setText("Project Directory");
        add(label1, CC.xy(1, 1));
        add(projectDirectory, CC.xy(3, 1));

        //---- browseProjectDirectory ----
        browseProjectDirectory.setText("...");
        add(browseProjectDirectory, CC.xy(5, 1));

        //---- label2 ----
        label2.setText("Jar File Location");
        add(label2, CC.xy(1, 3));
        add(jarFilePath, CC.xy(3, 3));

        //---- browseJarFilePath ----
        browseJarFilePath.setText("...");
        add(browseJarFilePath, CC.xy(5, 3));

        //---- label3 ----
        label3.setText("Project Name");
        add(label3, CC.xy(1, 5));
        add(textField3, CC.xy(3, 5));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Steven Hannah
    private JLabel label1;
    private JTextField projectDirectory;
    private JButton browseProjectDirectory;
    private JLabel label2;
    private JTextField jarFilePath;
    private JButton browseJarFilePath;
    private JLabel label3;
    private JTextField textField3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
