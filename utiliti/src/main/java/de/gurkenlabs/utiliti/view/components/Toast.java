package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Toast extends JPanel {
  private static final int DISPLAY_DURATION = 4000;
  private static final int BOTTOM_MARGIN = 28;
  private static final int SIDE_MARGIN = 12;
  private static final Map<JRootPane, Toast> ACTIVE_TOASTS = new WeakHashMap<>();

  private final JRootPane rootPane;
  private final Timer dismissTimer;
  private final ComponentAdapter rootResizeListener;

  private Toast(JRootPane rootPane, String message, Runnable onUndo) {
    this.rootPane = rootPane;
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
    getAccessibleContext().setAccessibleName(message);

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

    this.rootResizeListener = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent event) {
        positionToast();
      }
    };
    this.dismissTimer = new Timer(DISPLAY_DURATION, event -> hideToast());
    this.dismissTimer.setRepeats(false);
  }

  public static void show(JRootPane rootPane, String message) {
    show(rootPane, message, null);
  }

  public static void show(String message) {
    show(message, null);
  }

  public static void show(String message, Runnable onUndo) {
    if (Game.window() != null && Game.window().getHostControl() instanceof JFrame window) {
      show(window.getRootPane(), message, onUndo);
    }
  }

  public static void show(JRootPane rootPane, String message, Runnable onUndo) {
    if (rootPane == null || message == null || message.isBlank()) {
      return;
    }
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(() -> show(rootPane, message, onUndo));
      return;
    }

    Toast previous = ACTIVE_TOASTS.remove(rootPane);
    if (previous != null) {
      previous.hideToast();
    }

    Toast toast = new Toast(rootPane, message, onUndo);
    ACTIVE_TOASTS.put(rootPane, toast);
    rootPane.addComponentListener(toast.rootResizeListener);
    rootPane.getLayeredPane().add(toast, JLayeredPane.POPUP_LAYER);
    toast.positionToast();
    toast.dismissTimer.start();
  }

  private void hideToast() {
    this.dismissTimer.stop();
    this.rootPane.removeComponentListener(this.rootResizeListener);
    ACTIVE_TOASTS.remove(this.rootPane, this);
    if (getParent() != null) {
      getParent().remove(this);
      this.rootPane.getLayeredPane().revalidate();
      this.rootPane.getLayeredPane().repaint();
    }
  }

  private void positionToast() {
    Dimension preferred = getPreferredSize();
    int availableWidth = Math.max(0, this.rootPane.getLayeredPane().getWidth() - 2 * SIDE_MARGIN);
    int width = Math.min(preferred.width, availableWidth);
    int x = Math.max(SIDE_MARGIN, (this.rootPane.getLayeredPane().getWidth() - width) / 2);
    int y = Math.max(
      SIDE_MARGIN,
      this.rootPane.getLayeredPane().getHeight() - preferred.height - BOTTOM_MARGIN);
    setBounds(x, y, width, preferred.height);
    revalidate();
    repaint();
  }
}
