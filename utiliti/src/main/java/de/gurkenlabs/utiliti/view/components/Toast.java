package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Toast extends JPanel {
  private static final int DISPLAY_DURATION = 4000;
  private static final int FADE_STEPS = 10;
  private static final int FADE_INTERVAL = 50;

  private Toast(String message, Runnable onUndo) {
    setLayout(new BorderLayout(8, 0));
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Style.COLOR_BORDER, 1),
        BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    setBackground(Style.COLOR_SURFACE);
    setOpaque(true);

    JLabel label = new JLabel(message);
    label.setFont(Style.getDefaultFont().deriveFont(Font.PLAIN, 12f));
    label.setForeground(Style.COLOR_TEXT);
    add(label, BorderLayout.CENTER);

    if (onUndo != null) {
      JButton undoBtn = new JButton(Resources.strings().get("panel_undo"));
      undoBtn.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
      undoBtn.setBorderPainted(false);
      undoBtn.setContentAreaFilled(false);
      undoBtn.setForeground(Style.COLOR_ACCENT_BLUE);
      undoBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
      undoBtn.addActionListener(e -> {
        onUndo.run();
        hideToast();
      });
      add(undoBtn, BorderLayout.EAST);
    }
  }

  public static void show(JRootPane rootPane, String message) {
    show(rootPane, message, null);
  }

  public static void show(JRootPane rootPane, String message, Runnable onUndo) {
    Toast toast = new Toast(message, onUndo);
    toast.setVisible(false);

    java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
    JPanel overlay = new JPanel(layout);
    overlay.setOpaque(false);
    overlay.setBounds(0, 0, rootPane.getWidth(), rootPane.getHeight());

    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.anchor = java.awt.GridBagConstraints.NORTH;
    gbc.insets = new java.awt.Insets(
        rootPane.getHeight() - 60, 20, 0, 20);
    overlay.add(toast, gbc);

    rootPane.getLayeredPane().add(overlay, javax.swing.JLayeredPane.POPUP_LAYER);
    toast.setVisible(true);
    rootPane.revalidate();
    rootPane.repaint();

    new Timer(true).schedule(new TimerTask() {
      @Override
      public void run() {
        SwingUtilities.invokeLater(() -> fadeOut(toast, overlay, rootPane));
      }
    }, DISPLAY_DURATION);
  }

  private void hideToast() {
    java.awt.Container overlay = getParent();
    if (overlay != null && overlay.getParent() != null) {
      overlay.getParent().remove(overlay);
      overlay.getParent().revalidate();
      overlay.getParent().repaint();
    }
  }

  private static void fadeOut(JPanel toast, JPanel overlay, JRootPane rootPane) {
    rootPane.getLayeredPane().remove(overlay);
    rootPane.getLayeredPane().revalidate();
    rootPane.getLayeredPane().repaint();
  }
}
