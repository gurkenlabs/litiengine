package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Shared chrome for small, focused editor dialogs. */
abstract class EditorDialog extends JDialog {
  private static final int ARC = 20;
  private final JPanel body;

  protected EditorDialog(Component parent, String title) {
    this(parent, title, null);
  }

  protected EditorDialog(Component parent, String title, Icon titleIcon) {
    super(ownerOf(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
    this.setUndecorated(true);
    this.setResizable(false);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    // An opaque shaped window preserves LCD text antialiasing. A transparent window causes
    // rectangular artifacts behind Swing text on Windows.
    this.setBackground(Style.background());

    JPanel root = new RoundedPanel();
    root.setLayout(new BorderLayout());
    root.setBorder(new EmptyBorder(1, 1, 1, 1));

    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setOpaque(false);
    header.setBorder(new EmptyBorder(25, 28, 23, 22));
    JLabel heading = new JLabel(title);
    heading.setOpaque(false);
    heading.setIcon(titleIcon);
    heading.setIconTextGap(14);
    heading.setFont(heading.getFont().deriveFont(java.awt.Font.BOLD, 21f));
    heading.setForeground(Style.text());
    header.add(heading, BorderLayout.CENTER);

    JButton close = Style.iconButton(Icons.CROSS_16);
    close.setFocusable(false);
    close.setRequestFocusEnabled(false);
    close.setPreferredSize(new Dimension(34, 34));
    close.setMinimumSize(close.getPreferredSize());
    close.setMaximumSize(close.getPreferredSize());
    close.setToolTipText(Resources.strings().get("dialog_cancel"));
    close.getAccessibleContext().setAccessibleName(close.getToolTipText());
    close.addActionListener(event -> this.close());
    header.add(close, BorderLayout.EAST);
    root.add(header, BorderLayout.NORTH);

    this.body = new JPanel(new BorderLayout());
    this.body.setOpaque(false);
    this.body.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    root.add(this.body, BorderLayout.CENTER);
    this.setContentPane(root);

    MouseAdapter drag = new MouseAdapter() {
      private Point offset;

      @Override
      public void mousePressed(MouseEvent event) {
        this.offset = event.getPoint();
      }

      @Override
      public void mouseDragged(MouseEvent event) {
        if (this.offset != null) {
          Point screen = event.getLocationOnScreen();
          EditorDialog.this.setLocation(screen.x - this.offset.x, screen.y - this.offset.y);
        }
      }
    };
    header.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    header.addMouseListener(drag);
    header.addMouseMotionListener(drag);

    this.getRootPane().registerKeyboardAction(
      event -> this.close(),
      KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  protected final JPanel body() {
    return this.body;
  }

  protected void close() {
    this.dispose();
  }

  protected final void showCentered() {
    this.pack();
    Rectangle usableBounds = usableScreenBounds();
    this.setSize(
      Math.min(this.getWidth(), usableBounds.width),
      Math.min(this.getHeight(), usableBounds.height));
    try {
      this.setShape(new RoundRectangle2D.Double(
        0, 0, this.getWidth(), this.getHeight(), ARC, ARC));
    } catch (UnsupportedOperationException ignored) {
      // Some window managers do not support shaped windows; the dialog remains fully usable.
    }
    this.setLocationRelativeTo(this.getOwner());
    this.setLocation(
      Math.max(usableBounds.x, Math.min(this.getX(), usableBounds.x + usableBounds.width - this.getWidth())),
      Math.max(usableBounds.y, Math.min(this.getY(), usableBounds.y + usableBounds.height - this.getHeight())));
    this.setVisible(true);
  }

  private Rectangle usableScreenBounds() {
    GraphicsConfiguration configuration = this.getGraphicsConfiguration();
    if (configuration == null) {
      return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }
    Rectangle bounds = new Rectangle(configuration.getBounds());
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
    bounds.x += insets.left;
    bounds.y += insets.top;
    bounds.width -= insets.left + insets.right;
    bounds.height -= insets.top + insets.bottom;
    return bounds;
  }

  static Window ownerOf(Component parent) {
    if (parent instanceof Window window) {
      return window;
    }
    return parent == null ? null : SwingUtilities.getWindowAncestor(parent);
  }

  private static final class RoundedPanel extends JPanel {
    private RoundedPanel() {
      this.setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D copy = (Graphics2D) graphics.create();
      try {
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copy.setPaint(new GradientPaint(
          0, 0, Style.surface(), 0, Math.max(1, this.getHeight()), Style.background()));
        copy.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, ARC, ARC);
        copy.setColor(Style.border());
        copy.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, ARC, ARC);
      } finally {
        copy.dispose();
      }
      super.paintComponent(graphics);
    }
  }
}
