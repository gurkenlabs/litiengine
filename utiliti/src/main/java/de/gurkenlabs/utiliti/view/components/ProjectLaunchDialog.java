package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

final class ProjectLaunchDialog extends JDialog {
  private final JLabel statusLabel = new JLabel("Resolving Gradle project model...");
  private final JProgressBar progressBar = new JProgressBar();
  private final JButton cancelButton = new JButton("Cancel");
  private volatile boolean cancelled = false;

  ProjectLaunchDialog(Window owner, String title) {
    super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(0, 12));
    panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    panel.setBackground(Style.background());

    this.statusLabel.setFont(Style.getDefaultFont().deriveFont(12f));
    this.statusLabel.setForeground(Style.text());

    this.progressBar.setIndeterminate(true);
    this.progressBar.setPreferredSize(new Dimension(340, 18));
    this.progressBar.setStringPainted(false);

    this.cancelButton.addActionListener(e -> {
      this.cancelled = true;
      this.dispose();
    });
    Style.styleButton(this.cancelButton, Style.ButtonVariant.SECONDARY);

    JPanel center = new JPanel(new BorderLayout(0, 8));
    center.setOpaque(false);
    center.add(this.statusLabel, BorderLayout.NORTH);
    center.add(this.progressBar, BorderLayout.CENTER);

    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    bottom.setOpaque(false);
    bottom.add(this.cancelButton);

    panel.add(center, BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);

    this.setContentPane(panel);
    this.pack();
    if (owner != null) {
      this.setLocationRelativeTo(owner);
    }
  }

  void updateStatus(String text) {
    if (text == null || text.isBlank()) return;
    SwingUtilities.invokeLater(() -> this.statusLabel.setText(text));
  }

  boolean isCancelled() {
    return this.cancelled;
  }
}
