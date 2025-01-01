/*
 * Created by JFormDesigner on Tue Dec 31 05:44:13 PST 2024
 */

package ca.weblite.jdeploy.app.forms;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author steve
 */
public class NewProjectForm extends JFrame {
    private Frame parentFrame;

    public NewProjectForm(Frame parentFrame) {
        this.parentFrame = parentFrame;
        initComponents();
    }

    public JTextField getArtifactId() {
        return artifactId;
    }

    public JTextField getProjectLocation() {
        return projectLocation;
    }

    public JComboBox getProjectTemplate() {
        return projectTemplate;
    }

    public JTextField getDisplayName() {
        return displayName;
    }

    public JTextField getGroupId() {
        return groupId;
    }

    public JButton getSelectProjectLocationButton() {
        return selectProjectLocationButton;
    }

    public JTextField getNpmProjectName() {
        return npmProjectName;
    }

    public JTextField getGithubRepositoryUrl() {
        return githubRepositoryUrl;
    }

    public JTextField getGithubReleasesRepositoryUrl() {
        return githubReleasesRepositoryUrl;
    }

    public JCheckBox getCreateGithubReleasesRepositoryCheckBox() {
        return createGithubReleasesRepositoryCheckBox;
    }

    public JCheckBox getCreateGithubRepositoryUrlCheckBox() {
        return createGithubRepositoryUrlCheckBox;
    }

    public JRadioButton getNpmRadioButton() {
        return npmRadioButton;
    }

    public JRadioButton getGitHubReleasesRadioButton() {
        return gitHubReleasesRadioButton;
    }

    public JButton getRefreshTemplatesButton() {
        return refreshTemplatesButton;
    }

    public JButton getCreateProjectButton() {
        return createProjectButton;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Steven Hannah
        DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
        panel1 = new JPanel();
        label1 = new JLabel();
        displayName = new JTextField();
        label3 = new JLabel();
        groupId = new JTextField();
        label4 = new JLabel();
        artifactId = new JTextField();
        label5 = new JLabel();
        projectLocation = new JTextField();
        selectProjectLocationButton = new JButton();
        label6 = new JLabel();
        projectTemplate = new JComboBox();
        refreshTemplatesButton = new JButton();
        separator1 = compFactory.createSeparator("Publish Settings");
        label7 = new JLabel();
        panel2 = new JPanel();
        npmRadioButton = new JRadioButton();
        gitHubReleasesRadioButton = new JRadioButton();
        separator2 = compFactory.createSeparator("npm Settings");
        label8 = new JLabel();
        npmProjectName = new JTextField();
        separator3 = compFactory.createSeparator("GitHub Settings");
        label9 = new JLabel();
        panel3 = new JPanel();
        githubRepositoryUrl = new JTextField();
        panel4 = new JPanel();
        createGithubRepositoryUrlCheckBox = new JCheckBox();
        label10 = new JLabel();
        panel5 = new JPanel();
        githubReleasesRepositoryUrl = new JTextField();
        panel6 = new JPanel();
        createGithubReleasesRepositoryCheckBox = new JCheckBox();
        panel7 = new JPanel();
        createProjectButton = new JButton();

        //======== this ========
        setTitle("Create New Application");
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setBorder(Borders.DIALOG);
            panel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing. border
            . EmptyBorder( 0, 0, 0, 0) , "JF\u006frmDes\u0069gner \u0045valua\u0074ion", javax. swing. border. TitledBorder. CENTER, javax
            . swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069alog" ,java .awt .Font .BOLD ,
            12 ), java. awt. Color. red) ,panel1. getBorder( )) ); panel1. addPropertyChangeListener (new java. beans
            . PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e) {if ("\u0062order" .equals (e .
            getPropertyName () )) throw new RuntimeException( ); }} );
            panel1.setLayout(new FormLayout(
                "2*(default, $lcgap), default",
                "13*(default, $lgap), default"));

            //---- label1 ----
            label1.setText("Application Display Name");
            panel1.add(label1, CC.xy(1, 3));
            panel1.add(displayName, CC.xywh(3, 3, 3, 1));

            //---- label3 ----
            label3.setText("Group ID");
            panel1.add(label3, CC.xy(1, 7));
            panel1.add(groupId, CC.xywh(3, 7, 3, 1));

            //---- label4 ----
            label4.setText("Artifact ID");
            panel1.add(label4, CC.xy(1, 9));
            panel1.add(artifactId, CC.xywh(3, 9, 3, 1));

            //---- label5 ----
            label5.setText("Project Location");
            panel1.add(label5, CC.xy(1, 11));
            panel1.add(projectLocation, CC.xy(3, 11));

            //---- selectProjectLocationButton ----
            selectProjectLocationButton.setText("Select ...");
            panel1.add(selectProjectLocationButton, CC.xy(5, 11));

            //---- label6 ----
            label6.setText("Project Template");
            panel1.add(label6, CC.xy(1, 13));
            panel1.add(projectTemplate, CC.xy(3, 13));

            //---- refreshTemplatesButton ----
            refreshTemplatesButton.setText("Refresh");
            panel1.add(refreshTemplatesButton, CC.xy(5, 13));
            panel1.add(separator1, CC.xywh(1, 15, 5, 1));

            //---- label7 ----
            label7.setText("Publish to:");
            panel1.add(label7, CC.xy(1, 17));

            //======== panel2 ========
            {
                panel2.setLayout(new FlowLayout());

                //---- npmRadioButton ----
                npmRadioButton.setText("npm");
                panel2.add(npmRadioButton);

                //---- gitHubReleasesRadioButton ----
                gitHubReleasesRadioButton.setText("GitHub Releases");
                panel2.add(gitHubReleasesRadioButton);
            }
            panel1.add(panel2, CC.xy(3, 17));
            panel1.add(separator2, CC.xywh(1, 19, 5, 1));

            //---- label8 ----
            label8.setText("Project name");
            panel1.add(label8, CC.xy(1, 21));
            panel1.add(npmProjectName, CC.xywh(3, 21, 3, 1));
            panel1.add(separator3, CC.xywh(1, 23, 5, 1));

            //---- label9 ----
            label9.setText("Repository URL");
            panel1.add(label9, CC.xy(1, 25));

            //======== panel3 ========
            {
                panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
                panel3.add(githubRepositoryUrl);

                //======== panel4 ========
                {
                    panel4.setLayout(new FlowLayout(FlowLayout.LEFT));

                    //---- createGithubRepositoryUrlCheckBox ----
                    createGithubRepositoryUrlCheckBox.setText("Create Now");
                    panel4.add(createGithubRepositoryUrlCheckBox);
                }
                panel3.add(panel4);
            }
            panel1.add(panel3, CC.xywh(3, 25, 3, 1));

            //---- label10 ----
            label10.setText("Releases Repository URL");
            panel1.add(label10, CC.xy(1, 27));

            //======== panel5 ========
            {
                panel5.setLayout(new BoxLayout(panel5, BoxLayout.Y_AXIS));
                panel5.add(githubReleasesRepositoryUrl);

                //======== panel6 ========
                {
                    panel6.setLayout(new FlowLayout(FlowLayout.LEFT));

                    //---- createGithubReleasesRepositoryCheckBox ----
                    createGithubReleasesRepositoryCheckBox.setText("Create Now");
                    panel6.add(createGithubReleasesRepositoryCheckBox);
                }
                panel5.add(panel6);
            }
            panel1.add(panel5, CC.xywh(3, 27, 3, 1));
        }
        contentPane.add(panel1, BorderLayout.CENTER);

        //======== panel7 ========
        {
            panel7.setLayout(new FlowLayout(FlowLayout.RIGHT));

            //---- createProjectButton ----
            createProjectButton.setText("Create Project");
            panel7.add(createProjectButton);
        }
        contentPane.add(panel7, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());

        //---- buttonGroup1 ----
        var buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(npmRadioButton);
        buttonGroup1.add(gitHubReleasesRadioButton);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Steven Hannah
    private JPanel panel1;
    private JLabel label1;
    private JTextField displayName;
    private JLabel label3;
    private JTextField groupId;
    private JLabel label4;
    private JTextField artifactId;
    private JLabel label5;
    private JTextField projectLocation;
    private JButton selectProjectLocationButton;
    private JLabel label6;
    private JComboBox projectTemplate;
    private JButton refreshTemplatesButton;
    private JComponent separator1;
    private JLabel label7;
    private JPanel panel2;
    private JRadioButton npmRadioButton;
    private JRadioButton gitHubReleasesRadioButton;
    private JComponent separator2;
    private JLabel label8;
    private JTextField npmProjectName;
    private JComponent separator3;
    private JLabel label9;
    private JPanel panel3;
    private JTextField githubRepositoryUrl;
    private JPanel panel4;
    private JCheckBox createGithubRepositoryUrlCheckBox;
    private JLabel label10;
    private JPanel panel5;
    private JTextField githubReleasesRepositoryUrl;
    private JPanel panel6;
    private JCheckBox createGithubReleasesRepositoryCheckBox;
    private JPanel panel7;
    private JButton createProjectButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
