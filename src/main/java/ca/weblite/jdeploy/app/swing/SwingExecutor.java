package ca.weblite.jdeploy.app.swing;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executor;

public class SwingExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
        if (EventQueue.isDispatchThread()) {
            command.run();
        } else {
            SwingUtilities.invokeLater(command);
        }
    }
}
