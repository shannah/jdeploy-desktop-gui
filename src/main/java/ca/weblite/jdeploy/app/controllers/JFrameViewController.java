package ca.weblite.jdeploy.app.controllers;

import javax.swing.*;

public abstract class JFrameViewController implements Runnable {
    private JComponent rootComponent;

    protected abstract JComponent initUI();

    public void show() {
        rootComponent = initUI();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(rootComponent);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void run() {
        show();
    }

    public JFrame getFrame() {
        return (JFrame)rootComponent.getTopLevelAncestor();
    }
}
