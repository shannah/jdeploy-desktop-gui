package ca.weblite.jdeploy.app.forms;

import ca.weblite.jdeploy.app.accounts.AccountInterface;
import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AccountChooserDialog extends JDialog {

    // For demo, we keep references to some UI controls:
    private final JLabel subtitleLabel;
    private final JButton whyButton;
    private final JPanel accountListPanel;
    private final JButton continueButton;
    private final JButton addAccountButton;
    private final JButton closeButton;

    // The currently selected account
    private AccountInterface selectedAccount;
    // The currently selected account button (to highlight)
    private JButton selectedButton;

    private final Font headerFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font subHeaderFont = new Font("SansSerif", Font.PLAIN, 14);

    // Colors
    private final Color textColor = Color.BLACK;
    private final Color linkColor = new Color(0, 120, 220);
    private final Color selectedBackground = new Color(220, 240, 255);

    private final JLabel titleLabel;

    private List<AccountListener> accountListeners = new ArrayList<>();

    /**
     * Creates a modal, undecorated dialog.
     */
    public AccountChooserDialog(Frame parent, List<? extends AccountInterface> accounts) {
        super(parent, true);
        setUndecorated(true);

        /*
         * 1) Main content with BorderLayout
         *    - We'll use the NORTH region for the close button
         *    - The CENTER region for all the main content
         */
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        setContentPane(contentPanel);

        /*
         * 2) Top "close button" panel, aligned to the right
         */
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        northPanel.setOpaque(false);

        closeButton = createCloseButton(parent);
        northPanel.add(closeButton);

        contentPanel.add(northPanel, BorderLayout.NORTH);

        /*
         * 3) Main (center) content. We'll still use a BoxLayout to stack
         *    the "gitHub" title, subtitle, account list, etc.
         */
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Title: "GitHub", centered
        titleLabel = new JLabel("GitHub");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(8));

        // Subtitle: "Select an account"
        subtitleLabel = new JLabel("Select an account");
        subtitleLabel.setFont(subHeaderFont);
        subtitleLabel.setForeground(textColor);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        // "Why" link
        whyButton = createLinkButton("Why am I being asked to select an account?",
                Material.HELP_OUTLINE, linkColor);
        whyButton.setVisible(false);
        whyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(whyButton);
        centerPanel.add(Box.createVerticalStrut(20));

        // Account list
        accountListPanel = new JPanel();
        accountListPanel.setLayout(new BoxLayout(accountListPanel, BoxLayout.Y_AXIS));
        accountListPanel.setOpaque(false);
        accountListPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (AccountInterface account : accounts) {
            JPanel accountPanel = new JPanel();
            JButton accountBtn = createAccountButton(account);
            JPanel accountButtonsPanel = new JPanel();
            accountButtonsPanel.setLayout(new BoxLayout(accountButtonsPanel, BoxLayout.X_AXIS));
            JButton editAccountBtn = createEditAccountButton(account, accountBtn, accountPanel);
            JButton deleteAccountBtn = createDeleteAccountButton(account, accountBtn, accountPanel);
            editAccountBtn.setMargin(new Insets(0, 0, 0, 0));
            deleteAccountBtn.setMargin(new Insets(0, 0, 0, 0));

            Dimension buttonSize = new Dimension(24, 24); // or 20x20 depending on icon size

            for (JButton btn : new JButton[]{editAccountBtn, deleteAccountBtn}) {
                btn.setPreferredSize(buttonSize);
                btn.setMinimumSize(buttonSize);
                btn.setMaximumSize(buttonSize);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
            }

            accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.X_AXIS));
            accountPanel.setOpaque(false);
            accountPanel.add(accountBtn);
            accountPanel.add(Box.createRigidArea(new Dimension(30, 0)));
            accountPanel.add(Box.createHorizontalGlue());

            accountButtonsPanel.add(editAccountBtn);
            accountButtonsPanel.add(Box.createRigidArea(new Dimension(0, 0)));
            accountButtonsPanel.add(deleteAccountBtn);
            accountPanel.add(accountButtonsPanel);
            accountListPanel.add(accountPanel);
            accountListPanel.add(Box.createVerticalStrut(10));
        }
        centerPanel.add(accountListPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // "Continue" button
        continueButton = new JButton("Continue");
        continueButton.setEnabled(false);
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.setPreferredSize(new Dimension(120, 35));
        continueButton.addActionListener(e -> dispose());
        centerPanel.add(continueButton);
        centerPanel.add(Box.createVerticalStrut(20));

        // "Add a new account"
        addAccountButton = createLinkButton("Add a new account", null, linkColor);
        addAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(addAccountButton);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Final setup
        pack();
        setLocationRelativeTo(parent);
    }

    public void addAccountListener(AccountListener listener){
        accountListeners.add(listener);
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }

    /**
     * Shows the dialog (modal). Returns the selected account, or null if none.
     */
    public AccountInterface showDialog() {
        setVisible(true);
        return selectedAccount;
    }

    /**
     * Create a button for each account
     */
    private JButton createAccountButton(AccountInterface account) {
        JButton btn = new JButton(account.getAccountName());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Account icon
        FontIcon icon = FontIcon.of(Material.ACCOUNT_CIRCLE, 20, textColor);
        btn.setIcon(icon);

        // Let it expand horizontally
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // On click => select
        btn.addActionListener((ActionEvent e) -> {
            selectedAccount = account;
            continueButton.setEnabled(true);
            setSelectedButton(btn);
        });

        return btn;
    }

    private JButton createDeleteAccountButton(AccountInterface account, JButton viewButton, JComponent viewRow) {
        JButton btn = new JButton("");
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Account icon
        FontIcon icon = FontIcon.of(Material.DELETE, 20, Color.GRAY);
        btn.setIcon(icon);
        btn.setToolTipText("Delete account");

        // Let it expand horizontally
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // On click => select
        btn.addActionListener((ActionEvent e) -> {
            fireDeleteAccountEvent(account, () -> {
                accountListPanel.remove(viewRow);
                accountListPanel.revalidate();
                accountListPanel.repaint();
            });
        });

        return btn;
    }

    private JButton createEditAccountButton(AccountInterface account, JButton viewButton, JComponent viewRow) {
        JButton btn = new JButton("");
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Account icon
        FontIcon icon = FontIcon.of(Material.EDIT, 20, Color.GRAY);
        btn.setIcon(icon);
        btn.setToolTipText("Edit account");

        // Let it expand horizontally
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // On click => select
        btn.addActionListener((ActionEvent e) -> {
            fireEditAccountEvent(account, () -> {
                viewButton.setText(account.getAccountName());
                accountListPanel.revalidate();
                accountListPanel.repaint();
            });
        });

        return btn;
    }

    /**
     * Highlight the newly selected button, unhighlight old one.
     */
    private void setSelectedButton(JButton btn) {
        if (selectedButton != null) {
            selectedButton.setOpaque(false);
            selectedButton.setContentAreaFilled(false);
            selectedButton.repaint();
        }
        selectedButton = btn;
        selectedButton.setOpaque(true);
        selectedButton.setContentAreaFilled(true);
        selectedButton.setBackground(selectedBackground);
        selectedButton.repaint();
    }

    /**
     * Create link-styled JButtons.
     */
    private JButton createLinkButton(String text, Material iconType, Color linkColor) {
        JButton linkBtn = new JButton(text);
        linkBtn.setFocusPainted(false);
        linkBtn.setBorderPainted(false);
        linkBtn.setContentAreaFilled(false);
        linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        linkBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        linkBtn.setForeground(linkColor);

        if (iconType != null) {
            FontIcon icon = FontIcon.of(iconType, 16, linkColor);
            linkBtn.setIcon(icon);
        }

        // Underline on hover
        linkBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                linkBtn.setText("<html><div style='text-align:center;'>" +
                        "<u>" + text + "</u></div></html>");
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                linkBtn.setText(text);
            }
        });

        return linkBtn;
    }

    /**
     * Creates a "close" (X) button for the top-right (NORTH) region.
     */
    private JButton createCloseButton(Frame parent) {
        JButton closeBtn = new JButton();
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        FontIcon closeIcon = FontIcon.of(Material.CLOSE, 18, Color.GRAY);
        closeBtn.setIcon(closeIcon);

        // On click => clear selected & dispose
        closeBtn.addActionListener(e -> {
            selectedAccount = null;
            dispose();
            if (parent != null) {
                parent.requestFocus();
            }
        });
        return closeBtn;
    }

    // External getters if needed
    public JButton getAddAccountButton() {
        return addAccountButton;
    }
    public JButton getWhyButton() {
        return whyButton;
    }

    private void fireAccountEvent(AccountEvent evt){
        for (AccountListener listener : accountListeners){
            listener.handleAccountEvent(evt);
        }
    }

    private void fireDeleteAccountEvent(AccountInterface account, Runnable callback){
        fireAccountEvent(new DeleteAccountEvent(account, callback));
    }

    private void fireEditAccountEvent(AccountInterface account, Runnable callback){
        fireAccountEvent(new EditAccountEvent(account, callback));
    }


    public interface AccountListener extends EventListener {
        void handleAccountEvent(AccountEvent evt);
    }

    public static class AccountEvent {
        private final AccountInterface account;
        private final Runnable callback;

        public AccountEvent(AccountInterface account, Runnable callback){
            this.account = account;
            this.callback = callback;
        }
        public AccountInterface getAccount(){
            return account;
        }

        public void commit(){
            if (callback != null){
                callback.run();
            }
        }
    }

    public static class DeleteAccountEvent extends AccountEvent {
        public DeleteAccountEvent(AccountInterface account, Runnable callback){
            super(account, callback);
        }
    }

    public static class EditAccountEvent extends AccountEvent {
        public EditAccountEvent(AccountInterface account, Runnable callback){
            super(account, callback);
        }
    }
}
