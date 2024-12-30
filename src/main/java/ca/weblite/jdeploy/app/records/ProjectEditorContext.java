package ca.weblite.jdeploy.app.records;

import ca.weblite.jdeploy.app.di.DIContext;
import ca.weblite.jdeploy.app.controllers.NpmAccountChooserController;
import ca.weblite.jdeploy.app.npm.NpmAccountServiceInterface;
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
    public void promptForNpmToken(Object parent) {
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        final boolean[] accountChosen = {false};
        final boolean[] accountChosenResult = {false};
        final Object lock = new Object();

        Runnable runnablePublish = () -> {
            NpmAccountChooserController accountChooserController = new NpmAccountChooserController(
                    frame, DIContext.get(NpmAccountServiceInterface.class)
            );

            accountChooserController.show().thenAccept(account -> {
                if (account == null) {
                    accountChosen[0] = true;
                    synchronized (lock) {
                        lock.notify();
                        return;
                    }
                }
                setNpmToken(account.getNpmToken());
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
        if (!accountChosenResult[0]) {
            return;
        }
    }
}
