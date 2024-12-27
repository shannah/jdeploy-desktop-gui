package ca.weblite.jdeploy.app.views.mainMenu;

import ca.weblite.jdeploy.app.records.Project;

import javax.swing.*;
import java.awt.*;

public class ProjectListCellRenderer implements ListCellRenderer<Project> {

    private JLabel label = new JLabel();
    @Override
    public Component getListCellRendererComponent(JList<? extends Project> list, Project value, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(value.getName());
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        label.setOpaque(true);
        label.setFont(list.getFont());
        label.setEnabled(list.isEnabled());
        return label;
    }
}
