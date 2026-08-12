package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

final class ProjectLaunchDialog extends JDialog {
  private final JLabel statusLabel = new JLabel("Resolving Gradle project model...");
  private final AnimatedProgressBar progressBar = new AnimatedProgressBar();
  private final JButton cancelButton = new JButton("Cancel");
  private volatile boolean cancelled = false;
  private Runnable cancelHandler = () -> {};

  ProjectLaunchDialog(Window owner, String title) {
    super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(0, 12));
    panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    panel.setBackground(Style.background());

    this.statusLabel.setFont(Style.getDefaultFont().deriveFont(12f));
    this.statusLabel.setForeground(Style.text());

    this.cancelButton.addActionListener(e -> this.cancelAndClose());
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override public void windowClosing(java.awt.event.WindowEvent e) {
        cancelAndClose();
      }
    });
    Style.styleButton(this.cancelButton, Style.ButtonVariant.SECONDARY);
    this.cancelButton.setPreferredSize(new Dimension(90, 26));

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

  void onCancel(Runnable cancelHandler) {
    this.cancelHandler = cancelHandler == null ? () -> {} : cancelHandler;
  }

  private void cancelAndClose() {
    if (this.cancelled) return;
    this.cancelled = true;
    try {
      this.cancelHandler.run();
    } catch (Exception ignored) {
    }
    this.dispose();
  }

  void updateStatus(String text) {
    if (text == null || text.isBlank()) return;
    String clean = text.strip();
    if (clean.length() > 60) {
      clean = clean.substring(0, 57) + "...";
    }
    String finalStatus = clean;
    SwingUtilities.invokeLater(() -> {
      this.statusLabel.setText(finalStatus);
      this.statusLabel.repaint();
    });
  }

  boolean isCancelled() {
    return this.cancelled;
  }

  private static final class AnimatedProgressBar extends JComponent {
    private int phase = 0;
    private final javax.swing.Timer animTimer;

    AnimatedProgressBar() {
      this.setPreferredSize(new Dimension(340, 18));
      this.animTimer = new javax.swing.Timer(25, e -> {
        this.phase = (this.phase + 4) % 360;
        this.repaint();
      });
    }

    @Override
    public void addNotify() {
      super.addNotify();
      this.animTimer.start();
    }

    @Override
    public void removeNotify() {
      this.animTimer.stop();
      super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Style.COLOR_SURFACE);
        g2.fillRoundRect(0, 0, w, h, 6, 6);

        int pillW = Math.max(70, w / 3);
        double offsetRatio = (1.0 + Math.sin(Math.toRadians(this.phase))) / 2.0;
        int x = (int) (offsetRatio * (w - pillW));

        LinearGradientPaint paint = new LinearGradientPaint(
            x, 0, x + pillW, 0,
            new float[] { 0.0f, 0.5f, 1.0f },
            new Color[] {
                Style.COLOR_ACCENT_BLUE.darker(),
                Style.COLOR_ACCENT_BLUE.brighter(),
                Style.COLOR_ACCENT_BLUE.darker()
            }
        );
        g2.setPaint(paint);
        g2.fillRoundRect(x, 1, pillW, h - 2, 5, 5);

        g2.setColor(Style.border());
        g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
      } finally {
        g2.dispose();
      }
    }
  }
}
