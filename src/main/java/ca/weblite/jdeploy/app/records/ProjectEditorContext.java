package ca.weblite.jdeploy.app.records;

import ca.weblite.jdeploy.app.accounts.AccountServiceInterface;
import ca.weblite.jdeploy.app.accounts.AccountType;
import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.controllers.AccountChooserController;
import ca.weblite.jdeploy.app.controllers.WebPreviewController;
import ca.weblite.jdeploy.gui.JDeployProjectEditorContext;
import ca.weblite.jdeploy.publishTargets.PublishTargetInterface;
import ca.weblite.jdeploy.publishTargets.PublishTargetServiceInterface;
import ca.weblite.jdeploy.publishTargets.PublishTargetType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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

    @Override
    public boolean confirmPublish(Object parent) {
        String githubUrl = findGitHubPublishTargetUrl();
        if (githubUrl == null) {
            return true;
        }

        if (!hasJDeployWorkflow()) {
            return true;
        }

        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        final boolean[] result = {false};
        final boolean[] done = {false};
        final Object lock = new Object();

        SwingUtilities.invokeLater(() -> {
            String message = "<html><p style='width:400px'>"
                    + "This project has a GitHub Actions workflow that automatically builds "
                    + "and publishes your app when you create a release. "
                    + "We recommend creating a release on GitHub instead of publishing directly."
                    + "</p></html>";

            String[] options = {"Create Release on GitHub", "Publish Anyway", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                    frame,
                    message,
                    "Publish",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                // Open GitHub releases/new page
                try {
                    Desktop.getDesktop().browse(URI.create(githubUrl + "/releases/new"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result[0] = false;
            } else if (choice == 1) {
                result[0] = true;
            } else {
                result[0] = false;
            }

            synchronized (lock) {
                done[0] = true;
                lock.notify();
            }
        });

        while (!done[0]) {
            try {
                synchronized (lock) {
                    lock.wait(1000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        return result[0];
    }

    private String findGitHubPublishTargetUrl() {
        try {
            PublishTargetServiceInterface publishTargetService =
                    DIContext.get(PublishTargetServiceInterface.class);
            List<PublishTargetInterface> targets = publishTargetService
                    .getTargetsForProject(new File(project.getPath()).getAbsolutePath(), true);
            return targets.stream()
                    .filter(t -> t.getType() == PublishTargetType.GITHUB)
                    .map(PublishTargetInterface::getUrl)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean hasJDeployWorkflow() {
        Path workflowsDir = new File(project.getPath()).toPath()
                .resolve(".github")
                .resolve("workflows");
        if (!Files.isDirectory(workflowsDir)) {
            return false;
        }
        try (Stream<Path> files = Files.list(workflowsDir)) {
            return files
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".yml") || name.endsWith(".yaml");
                    })
                    .anyMatch(p -> {
                        try {
                            String content = Files.readString(p).toLowerCase();
                            return content.contains("jdeploy");
                        } catch (IOException e) {
                            return false;
                        }
                    });
        } catch (IOException e) {
            return false;
        }
    }
}
