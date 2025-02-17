package ca.weblite.jdeploy.app.forms;

import ca.weblite.jdeploy.app.accounts.AccountInterface;

import javax.swing.*;

public class EditAccountPanel extends JPanel {
    private JTextField accountNameField;
    private JPasswordField tokenField;

    public EditAccountPanel(AccountInterface account) {
        super();

        // Create the layout
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        // Create the components
        JLabel accountNameLabel = new JLabel("Account Name");
        accountNameField = new JTextField(30);


        JLabel npmTokenLabel = new JLabel("Token");
        tokenField = new JPasswordField(30);

        // Set the account name
        accountNameField.setText(account.getAccountName());

        // Set the NPM token
        if (account.getAccessToken() != null) {
            tokenField.setText(account.getAccessToken());
        }

        // Set the layout
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(accountNameLabel)
                    .addComponent(npmTokenLabel)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(accountNameField)
                    .addComponent(tokenField)
                )
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(accountNameLabel)
                    .addComponent(accountNameField)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(npmTokenLabel)
                    .addComponent(tokenField)
                )
        );


    }


    public JPasswordField getTokenField() {
        return tokenField;
    }

    public JTextField getAccountNameField() {
        return accountNameField;
    }
}
