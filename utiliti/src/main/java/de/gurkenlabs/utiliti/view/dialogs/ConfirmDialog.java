package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public final class ConfirmDialog extends EditorDialog {
  private boolean confirmed;

  private ConfirmDialog(
      Component parent,
      String title,
      String message,
      String confirmText,
      Icon icon,
      boolean destructive) {
    super(parent, title);
    this.setMinimumSize(new Dimension(610, 0));

    String[] messageParts = splitMessage(message);
    JPanel content = new JPanel(new BorderLayout(22, 0));
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(28, 28, 26, 28));
    if (icon != null) {
      content.add(new StatusIcon(icon, destructive), BorderLayout.WEST);
    }

    JPanel copy = new JPanel();
    copy.setOpaque(false);
    copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
    JTextArea prompt = dialogText(messageParts[0], Font.BOLD, 18f, Style.text());
    copy.add(prompt);
    if (!messageParts[1].isBlank()) {
      copy.add(Box.createVerticalStrut(8));
      JTextArea detail = dialogText(messageParts[1], Font.PLAIN, 15f, Style.mutedText());
      copy.add(detail);
    }
    content.add(copy, BorderLayout.CENTER);

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    JButton cancel = dialogButton(
      Resources.strings().get("dialog_cancel"),
      Style.ButtonVariant.SECONDARY,
      126);
    cancel.addActionListener(event -> this.close());
    footer.add(cancel);
    JButton confirm = dialogButton(
      confirmText,
      destructive ? Style.ButtonVariant.DESTRUCTIVE : Style.ButtonVariant.PRIMARY,
      164);
    confirm.setIcon(icon == null ? null : new TintedIcon(icon, destructive ? Style.COLOR_RED : Style.accent()));
    confirm.addActionListener(event -> {
      this.confirmed = true;
      this.close();
    });
    footer.add(confirm);
    this.getRootPane().setDefaultButton(confirm);

    this.body().add(content, BorderLayout.CENTER);
    this.body().add(footer, BorderLayout.SOUTH);
  }

  public static boolean show(String title, String message) {
    ConfirmDialog dialog = new ConfirmDialog(
      Game.window().getRenderComponent(),
      title,
      message,
      UIManager.getString("OptionPane.yesButtonText"),
      null,
      false);
    dialog.showCentered();
    return dialog.confirmed;
  }

  public static boolean showDestructive(
      String title, String message, String confirmText, Icon icon) {
    ConfirmDialog dialog = new ConfirmDialog(
      Game.window().getRenderComponent(), title, message, confirmText, icon, true);
    dialog.showCentered();
    return dialog.confirmed;
  }

  static String[] splitMessage(String message) {
    String normalized = message == null ? "" : message.trim();
    int newline = normalized.indexOf('\n');
    if (newline >= 0) {
      return new String[] {
        normalized.substring(0, newline).trim(), normalized.substring(newline + 1).trim()
      };
    }
    int sentence = normalized.indexOf("? ");
    if (sentence >= 0) {
      return new String[] {
        normalized.substring(0, sentence + 1).trim(), normalized.substring(sentence + 2).trim()
      };
    }
    return new String[] {normalized, ""};
  }

  private static JButton dialogButton(String text, Style.ButtonVariant variant, int width) {
    JButton button = new DialogButton(text);
    Style.styleButton(button, variant);
    button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
    int preferredWidth = Math.max(width, button.getPreferredSize().width + 24);
    Dimension size = new Dimension(preferredWidth, 42);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    return button;
  }

  private static JTextArea dialogText(String text, int fontStyle, float fontSize, java.awt.Color color) {
    JTextArea area = new WrappingTextArea(text);
    area.setFont(area.getFont().deriveFont(fontStyle, fontSize));
    area.setForeground(color);
    return area;
  }

  private static final class WrappingTextArea extends JTextArea {
    private static final int TEXT_WIDTH = 440;

    private WrappingTextArea(String text) {
      super(text);
      this.setEditable(false);
      this.setFocusable(false);
      this.setLineWrap(true);
      this.setWrapStyleWord(true);
      this.setOpaque(false);
      this.setBorder(null);
    }

    @Override
    public Dimension getPreferredSize() {
      this.setSize(TEXT_WIDTH, Short.MAX_VALUE);
      Dimension preferred = super.getPreferredSize();
      return new Dimension(TEXT_WIDTH, preferred.height);
    }

    @Override
    public Dimension getMaximumSize() {
      return this.getPreferredSize();
    }
  }

  private static final class DialogButton extends JButton {
    private DialogButton(String text) {
      super(text);
    }

    @Override
    protected void paintComponent(java.awt.Graphics graphics) {
      Style.paintButtonBackground(this, this.getModel(), graphics);
      super.paintComponent(graphics);
    }
  }

  private static final class StatusIcon extends JPanel {
    private final Icon icon;
    private final boolean destructive;

    private StatusIcon(Icon icon, boolean destructive) {
      this.icon = new TintedIcon(icon, destructive ? Style.COLOR_RED : Style.accent());
      this.destructive = destructive;
      this.setOpaque(false);
      this.setPreferredSize(new Dimension(64, 64));
    }

    @Override
    protected void paintComponent(java.awt.Graphics graphics) {
      java.awt.Graphics2D copy = (java.awt.Graphics2D) graphics.create();
      try {
        copy.setRenderingHint(
          java.awt.RenderingHints.KEY_ANTIALIASING,
          java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Color accent = this.destructive ? Style.COLOR_RED : Style.accent();
        copy.setColor(new java.awt.Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
        copy.fillOval(1, 1, 61, 61);
        copy.setColor(accent);
        copy.drawOval(1, 1, 61, 61);
        this.icon.paintIcon(
          this,
          copy,
          (this.getWidth() - this.icon.getIconWidth()) / 2,
          (this.getHeight() - this.icon.getIconHeight()) / 2);
      } finally {
        copy.dispose();
      }
    }
  }

  private record TintedIcon(Icon delegate, java.awt.Color color) implements Icon {
    @Override
    public int getIconWidth() {
      return this.delegate.getIconWidth();
    }

    @Override
    public int getIconHeight() {
      return this.delegate.getIconHeight();
    }

    @Override
    public void paintIcon(Component component, java.awt.Graphics graphics, int x, int y) {
      BufferedImage image = new BufferedImage(
        this.getIconWidth(), this.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D imageGraphics = image.createGraphics();
      try {
        this.delegate.paintIcon(component, imageGraphics, 0, 0);
        imageGraphics.setComposite(AlphaComposite.SrcIn);
        imageGraphics.setColor(this.color);
        imageGraphics.fillRect(0, 0, this.getIconWidth(), this.getIconHeight());
      } finally {
        imageGraphics.dispose();
      }
      graphics.drawImage(image, x, y, null);
    }
  }
}
