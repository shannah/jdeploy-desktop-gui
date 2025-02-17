package ca.weblite.jdeploy.app.controllers;

import javax.swing.*;

public abstract class JFrameViewController implements Runnable {
    private JComponent rootComponent;

    protected abstract JComponent initUI();

    private JFrame parentFrame;

    public JFrameViewController(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public JFrameViewController() {
        this.parentFrame = null;
    }

    public void show() {
        rootComponent = initUI();
        JFrame frame = new JFrame();
        if (parentFrame == null) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    parentFrame.setVisible(true);
                }
            });
        }

        frame.setContentPane(rootComponent);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setIconImage(
                new ImageIcon(
                        getClass().getResource("/ca/weblite/jdeploy/app/assets/icon.png")
                ).getImage()
        );
        onBeforeShow();
        frame.setVisible(true);
    }

    protected void onBeforeShow() {
    }

    @Override
    public void run() {
        show();
    }

    public JFrame getFrame() {
        return (JFrame)rootComponent.getTopLevelAncestor();
    }
}
