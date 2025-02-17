package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.accounts.Account;
import ca.weblite.jdeploy.app.accounts.AccountInterface;
import ca.weblite.jdeploy.app.accounts.AccountServiceInterface;
import ca.weblite.jdeploy.app.forms.EditAccountDialog;
import ca.weblite.jdeploy.app.swing.SwingExecutor;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executor;

public abstract class EditAccountController {

    private static final Executor EDT_EXECUTOR = new SwingExecutor();

    private final EditAccountDialog dialog;

    private final Window parentFrame;

    private final AccountServiceInterface accountService;

    private final AccountInterface account;

    private AccountInterface newAccount;

    public EditAccountController(
            Window parentFrame,
            AccountInterface account,
            AccountServiceInterface accountService
    ) {
        this.account = account;
        this.parentFrame = parentFrame;
        this.dialog = new EditAccountDialog(parentFrame, account);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        this.accountService = accountService;
        setupSaveButton();
        setupCancelButton();

    }

    public void show() {

        dialog.setVisible(true);
    }

    protected abstract void afterSave(AccountInterface account);

    private AccountInterface getAccount() {
        return new Account(
                dialog.getAccountNameField().getText(),
                isEmpty(dialog.getTokenField())
                        ? null :
                        new String(dialog.getTokenField().getPassword()),
                account.getAccountType()
        );
    }

    private boolean isEmpty(JPasswordField field) {
        return field.getPassword().length == 0;
    }

    private boolean isAccountValid() {
        return !dialog.getAccountNameField().getText().isEmpty() && !isEmpty(dialog.getTokenField());
    }

    private void update() {
        dialog.getSaveButton().setEnabled(isAccountValid());
    }

    private void setupSaveButton() {
        dialog.getSaveButton().addActionListener(e -> {
            newAccount = getAccount();
            accountService.save(newAccount).thenAcceptAsync(result->{
                afterSave(newAccount);
                dialog.dispose();
                parentFrame.requestFocus();
            }, EDT_EXECUTOR).exceptionally(t -> {
                EventQueue.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "Failed to save account: " + t.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
                return null;
            });
        });
    }

    private void setupCancelButton() {
        dialog.getCancelButton().addActionListener(e -> {
            newAccount = null;
            dialog.dispose();
            parentFrame.requestFocus();
        });
    }
}
