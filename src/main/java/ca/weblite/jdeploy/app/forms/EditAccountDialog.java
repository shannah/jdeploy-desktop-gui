package ca.weblite.jdeploy.app.forms;

import ca.weblite.jdeploy.app.accounts.AccountInterface;

import javax.swing.*;
import java.awt.*;

public class EditAccountDialog extends JDialog {
    private EditAccountPanel editAccountPanel;
    private JButton saveButton;
    private JButton cancelButton;
    public EditAccountDialog(Window parent, AccountInterface account) {
        super(parent);
        // Create the layout
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        // Create the components
        editAccountPanel = new EditAccountPanel(account);
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        // Set the layout
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(editAccountPanel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(saveButton)
                    .addComponent(cancelButton)
                )
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(editAccountPanel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(cancelButton)
                )
        );
        pack();
    }

    public JTextField getAccountNameField() {
        return editAccountPanel.getAccountNameField();
    }

    public JPasswordField getTokenField() {
        return editAccountPanel.getTokenField();
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

}
