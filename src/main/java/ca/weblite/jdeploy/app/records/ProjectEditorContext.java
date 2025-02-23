package ca.weblite.jdeploy.app.records;

import ca.weblite.jdeploy.app.accounts.AccountServiceInterface;
import ca.weblite.jdeploy.app.accounts.AccountType;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.controllers.AccountChooserController;
import ca.weblite.jdeploy.app.controllers.WebPreviewController;
import ca.weblite.jdeploy.gui.JDeployProjectEditorContext;

import javax.swing.*;
import java.awt.*;

public class ProjectEditorContext extends JDeployProjectEditorContext {
    private final Project project;

    public ProjectEditorContext(Project projectSettings) {
        this.project = projectSettings;
    }

    public Project projectSettings() {
        return project;
    }

    @Override
    public boolean promptForNpmToken(Object parent) {
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        final boolean[] accountChosen = {false};
        final boolean[] accountChosenResult = {false};
        final Object lock = new Object();

        Runnable runnablePublish = () -> {
            AccountChooserController accountChooserController = new AccountChooserController(
                    frame,
                    DIContext.get(AccountServiceInterface.class),
                    AccountType.NPM
            );

            accountChooserController.show().thenAccept(account -> {
                if (account == null) {
                    accountChosen[0] = true;
                    synchronized (lock) {
                        lock.notify();
                        return;
                    }
                }
                setNpmToken(account.getAccessToken());
                accountChosen[0] = true;
                accountChosenResult[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            });

        };
        SwingUtilities.invokeLater(runnablePublish);

        while (!accountChosen[0]) {
            try {
                synchronized (lock) {
                    lock.wait(1000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return accountChosenResult[0];
    }

    @Override
    public boolean promptForGithubToken(Object parent) {
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        final boolean[] accountChosen = {false};
        final boolean[] accountChosenResult = {false};
        final Object lock = new Object();

        Runnable runnablePublish = () -> {
            AccountChooserController accountChooserController = new AccountChooserController(
                    frame,
                    DIContext.get(AccountServiceInterface.class),
                    AccountType.GITHUB
            );

            accountChooserController.show().thenAccept(account -> {
                if (account == null) {
                    accountChosen[0] = true;
                    synchronized (lock) {
                        lock.notify();
                        return;
                    }
                }
                setGithubToken(account.getAccessToken());
                accountChosen[0] = true;
                accountChosenResult[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            });

        };
        SwingUtilities.invokeLater(runnablePublish);

        while (!accountChosen[0]) {
            try {
                synchronized (lock) {
                    lock.wait(1000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return accountChosenResult[0];
    }

    @Override
    public boolean shouldDisplayPublishSettingsTab() {
        return true;
    }

    @Override
    public boolean useManagedNode() {
        return true;
    }

    @Override
    public boolean isWebPreviewSupported() {
        return true;
    }

    @Override
    public void showWebPreview(Frame projectEditorFrame) {
        new WebPreviewController((JFrame)projectEditorFrame, this).run();
    }
}
