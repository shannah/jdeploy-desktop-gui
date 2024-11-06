/*
 * Created by JFormDesigner on Mon Nov 04 20:37:22 PST 2024
 */

package ca.weblite.jdeploy.app.forms;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.swingx.*;

/**
 * @author steve
 */
public class MainMenuJ extends JPanel {
    public MainMenuJ() {
        initComponents();
    }

    public JButton getOpenButton() {
        return openButton;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - Steve Hannah
        DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();
        splitPane1 = new JSplitPane();
        panel1 = new JPanel();
        openButton = new JButton();
        textField1 = new JTextField();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        panel2 = new JPanel();
        title1 = compFactory.createTitle("jDeploy");
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();

        //======== this ========
        setBorder ( new javax . swing. border .CompoundBorder ( new javax . swing. border .TitledBorder ( new javax . swing. border
        .EmptyBorder ( 0, 0 ,0 , 0) ,  "JF\u006frmDes\u0069gner \u0045valua\u0074ion" , javax. swing .border . TitledBorder. CENTER ,javax
        . swing. border .TitledBorder . BOTTOM, new java. awt .Font ( "D\u0069alog", java .awt . Font. BOLD ,
        12 ) ,java . awt. Color .red ) , getBorder () ) );  addPropertyChangeListener( new java. beans
        .PropertyChangeListener ( ){ @Override public void propertyChange (java . beans. PropertyChangeEvent e) { if( "\u0062order" .equals ( e.
        getPropertyName () ) )throw new RuntimeException( ) ;} } );
        setLayout(new BorderLayout());

        //======== splitPane1 ========
        {
            splitPane1.setLastDividerLocation(100);
            splitPane1.setDividerLocation(100);

            //======== panel1 ========
            {
                panel1.setLayout(new BorderLayout());

                //---- openButton ----
                openButton.setText("Open");
                panel1.add(openButton, BorderLayout.SOUTH);
                panel1.add(textField1, BorderLayout.NORTH);

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(list1);
                }
                panel1.add(scrollPane1, BorderLayout.CENTER);
            }
            splitPane1.setLeftComponent(panel1);

            //======== panel2 ========
            {
                panel2.setLayout(new VerticalLayout());
                panel2.add(title1);

                //---- button1 ----
                button1.setText("Create new project...");
                panel2.add(button1);

                //---- button2 ----
                button2.setText("Import project...");
                panel2.add(button2);

                //---- button3 ----
                button3.setText("Open project...");
                panel2.add(button3);
            }
            splitPane1.setRightComponent(panel2);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - Steve Hannah
    private JSplitPane splitPane1;
    private JPanel panel1;
    private JButton openButton;
    private JTextField textField1;
    private JScrollPane scrollPane1;
    private JList list1;
    private JPanel panel2;
    private JLabel title1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
