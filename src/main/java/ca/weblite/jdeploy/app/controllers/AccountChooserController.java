package ca.weblite.jdeploy.app.controllers;

import ca.weblite.jdeploy.app.accounts.Account;
import ca.weblite.jdeploy.app.accounts.AccountInterface;
import ca.weblite.jdeploy.app.accounts.AccountServiceInterface;
import ca.weblite.jdeploy.app.accounts.AccountType;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.forms.AccountChooserDialog;
import ca.weblite.jdeploy.app.swing.SwingExecutor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AccountChooserController {

    private static final Executor EDT_EXECUTOR = new SwingExecutor();

    private final AccountServiceInterface accountService;

    private final Frame parentFrame;

    private AccountInterface selectedAccount;

    private final AccountType accountType;

    public AccountChooserController(
            Frame parentFrame,
            AccountServiceInterface accountService,
            AccountType accountType
    ) {
        this.accountService = accountService;
        this.parentFrame = parentFrame;
        this.accountType = accountType;
    }

    public AccountChooserController(
            Frame parentFrame,
            AccountType accountType
    ) {
        this(parentFrame, DIContext.get(AccountServiceInterface.class), accountType);
    }

    public CompletableFuture<AccountInterface> show() {
        return accountService.findAll().thenComposeAsync(this::showDialog, EDT_EXECUTOR);
    }

    public CompletableFuture<AccountInterface> showDialog(List<AccountInterface> accounts) {
        accounts = accounts.stream()
                .filter(a -> a.getAccountType() == accountType)
                .toList();
        AccountChooserDialog dialog = new AccountChooserDialog(parentFrame, accounts);
        switch (accountType) {
            case NPM:
                dialog.getTitleLabel().setText("npm");
                break;
            case GITHUB:
                dialog.getTitleLabel().setText("GitHub");
                break;
        }

        JButton newAccountButton = dialog.getAddAccountButton();
        newAccountButton.addActionListener(evt -> {
           EditAccountController controller = new EditAccountController(
                   dialog,
                   new Account("", null, accountType),
                   accountService
           ) {
               @Override
               protected void afterSave(AccountInterface account) {
                   selectedAccount = account;
                   dialog.dispose();
                   parentFrame.requestFocus();
               }
           };
           controller.show();

        });

        dialog.addAccountListener(accountEvent -> {
            if (accountEvent instanceof AccountChooserDialog.DeleteAccountEvent) {

                // Prompt the user to confirm deletion
                int result = JOptionPane.showConfirmDialog(
                        dialog,
                        "Are you sure you want to delete this account?",
                        "Delete Account",
                        JOptionPane.YES_NO_OPTION
                );

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                AccountInterface account = ((AccountChooserDialog.DeleteAccountEvent) accountEvent).getAccount();
                accountService.delete(account).thenAcceptAsync(r -> {
                    accountEvent.commit();
                }, EDT_EXECUTOR);

                return;
            }

            if (accountEvent instanceof AccountChooserDialog.EditAccountEvent) {
                EditAccountController controller = new EditAccountController(
                        dialog,
                        accountEvent.getAccount(),
                        accountService
                ) {
                    @Override
                    protected void afterSave(AccountInterface account) {
                        accountEvent.commit();
                    }
                };
                controller.show();

                return;
            }
        });
        AccountInterface sel = dialog.showDialog();
        if (sel != null) {
            selectedAccount = sel;
        }
        if (selectedAccount != null) {
            return accountService.loadToken(selectedAccount).thenApplyAsync(account -> {

                if (account == null) {
                    return selectedAccount;
                }
                return account;
            }, EDT_EXECUTOR);
        }
        return CompletableFuture.completedFuture(selectedAccount);
    }
}
