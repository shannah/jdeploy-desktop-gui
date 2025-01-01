/*
 * Created by JFormDesigner on Mon Nov 04 20:37:22 PST 2024
 */

package ca.weblite.jdeploy.app.forms;

import java.awt.*;
import javax.swing.*;

import ca.weblite.jdeploy.app.records.Project;
import com.jgoodies.forms.factories.*;
import org.jdesktop.swingx.*;

/**
 * @author steve
 */
public class MainMenuJ extends JPanel {
    public MainMenuJ() {
        initComponents();
    }

    public JButton getOpenRecentButton() {
        return openRecentButton;
    }

    public JButton getOpenButton() {
        return openButton;
    }
    
    public JList<Project> getRecentProjects() {
        return recentProjects;
    }

    public JButton getImportProject() {
        return importProject;
    }

    public JButton getCreateProjectButton() {
        return createProjectButton;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Steven Hannah
        DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
        splitPane1 = new JSplitPane();
        panel1 = new JPanel();
        openRecentButton = new JButton();
        textField1 = new JTextField();
        scrollPane1 = new JScrollPane();
        recentProjects = new JList<>();
        panel2 = new JPanel();
        title1 = compFactory.createTitle("jDeploy");
        createProjectButton = new JButton();
        importProject = new JButton();
        openButton = new JButton();

        //======== this ========
        setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.
        border.EmptyBorder(0,0,0,0), "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e",javax.swing.border.TitledBorder.CENTER
        ,javax.swing.border.TitledBorder.BOTTOM,new java.awt.Font("Dialo\u0067",java.awt.Font
        .BOLD,12),java.awt.Color.red), getBorder())); addPropertyChangeListener(
        new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e){if("borde\u0072"
        .equals(e.getPropertyName()))throw new RuntimeException();}});
        setLayout(new BorderLayout());

        //======== splitPane1 ========
        {
            splitPane1.setLastDividerLocation(200);
            splitPane1.setDividerLocation(200);
            splitPane1.setPreferredSize(new Dimension(640, 480));

            //======== panel1 ========
            {
                panel1.setLayout(new BorderLayout());

                //---- openRecentButton ----
                openRecentButton.setText("Open");
                panel1.add(openRecentButton, BorderLayout.SOUTH);
                panel1.add(textField1, BorderLayout.NORTH);

                //======== scrollPane1 ========
                {

                    //---- recentProjects ----
                    recentProjects.setPreferredSize(new Dimension(120, 80));
                    scrollPane1.setViewportView(recentProjects);
                }
                panel1.add(scrollPane1, BorderLayout.CENTER);
            }
            splitPane1.setLeftComponent(panel1);

            //======== panel2 ========
            {
                panel2.setLayout(new VerticalLayout());
                panel2.add(title1);

                //---- createProjectButton ----
                createProjectButton.setText("Create new project...");
                panel2.add(createProjectButton);

                //---- importProject ----
                importProject.setText("Import project...");
                panel2.add(importProject);

                //---- openButton ----
                openButton.setText("Open project...");
                panel2.add(openButton);
            }
            splitPane1.setRightComponent(panel2);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Steven Hannah
    private JSplitPane splitPane1;
    private JPanel panel1;
    private JButton openRecentButton;
    private JTextField textField1;
    private JScrollPane scrollPane1;
    private JList<Project> recentProjects;
    private JPanel panel2;
    private JLabel title1;
    private JButton createProjectButton;
    private JButton importProject;
    private JButton openButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
