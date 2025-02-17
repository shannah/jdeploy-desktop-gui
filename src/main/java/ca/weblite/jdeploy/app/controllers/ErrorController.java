package ca.weblite.jdeploy.app.controllers;

import javax.swing.*;

public class ErrorController implements Runnable {
    private final Throwable exception;

    public ErrorController(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public void run() {
        // Display error in a JOptionPane
        exception.printStackTrace(System.err);
        JOptionPane.showMessageDialog(
                null,
                exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

    }

}
