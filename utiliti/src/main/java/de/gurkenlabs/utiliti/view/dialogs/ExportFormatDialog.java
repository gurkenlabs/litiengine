package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/** A compact, visual chooser for the spritesheet export type. */
public final class ExportFormatDialog extends EditorDialog {
  public enum Format {
    XML,
    IMAGE
  }

  private Format selection;
  private final FormatButton xmlButton;
  private final FormatButton imageButton;

  private ExportFormatDialog(Component parent, String title, String prompt, String imageExtension) {
    super(parent, title, new ExportTitleIcon());
    this.setMinimumSize(new Dimension(700, 0));

    JPanel content = new JPanel(new BorderLayout(0, 30));
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(40, 34, 54, 34));

    JPanel introduction = new JPanel(new BorderLayout(0, 10));
    introduction.setOpaque(false);
    JLabel heading = new JLabel(prompt);
    heading.setOpaque(false);
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
    heading.setForeground(Style.text());
    introduction.add(heading, BorderLayout.NORTH);
    JLabel description = new JLabel(Resources.strings().get("assetpanel_export_format_description"));
    description.setOpaque(false);
    description.setFont(description.getFont().deriveFont(16f));
    description.setForeground(Style.mutedText());
    introduction.add(description, BorderLayout.SOUTH);
    content.add(introduction, BorderLayout.NORTH);

    JPanel choices = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
    choices.setOpaque(false);
    this.xmlButton = new FormatButton("XML", ".xml", new FileTypeIcon(false, true));
    this.imageButton = new FormatButton(
      imageExtension.substring(1).toUpperCase(java.util.Locale.ROOT),
      imageExtension,
      new FileTypeIcon(true, false));
    this.xmlButton.addActionListener(event -> this.choose(Format.XML));
    this.imageButton.addActionListener(event -> this.choose(Format.IMAGE));
    this.xmlButton.addFocusListener(new SelectionFocusListener(Format.XML));
    this.imageButton.addFocusListener(new SelectionFocusListener(Format.IMAGE));
    choices.add(this.xmlButton);
    choices.add(this.imageButton);
    content.add(choices, BorderLayout.CENTER);
    this.body().add(content, BorderLayout.CENTER);

    this.select(Format.XML, false);
    this.getRootPane().registerKeyboardAction(
      event -> this.select(Format.IMAGE, true),
      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);
    this.getRootPane().registerKeyboardAction(
      event -> this.select(Format.XML, true),
      KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  public static Format choose(String title, String prompt, String imageExtension) {
    ExportFormatDialog dialog = new ExportFormatDialog(
      Game.window().getRenderComponent(), title, prompt, normalizeExtension(imageExtension));
    dialog.showCentered();
    return dialog.selection;
  }

  static String normalizeExtension(String extension) {
    if (extension == null || extension.isBlank()) {
      return ".png";
    }
    return extension.startsWith(".") ? extension : "." + extension;
  }

  private void choose(Format format) {
    this.selection = format;
    this.close();
  }

  private void select(Format format, boolean requestFocus) {
    this.xmlButton.setSelectedStyle(format == Format.XML);
    this.imageButton.setSelectedStyle(format == Format.IMAGE);
    FormatButton selectedButton = format == Format.XML ? this.xmlButton : this.imageButton;
    this.getRootPane().setDefaultButton(selectedButton);
    if (requestFocus) {
      selectedButton.requestFocusInWindow();
    }
  }

  private final class SelectionFocusListener extends FocusAdapter {
    private final Format format;

    private SelectionFocusListener(Format format) {
      this.format = format;
    }

    @Override
    public void focusGained(FocusEvent event) {
      ExportFormatDialog.this.select(this.format, false);
    }
  }

  private static final class FormatButton extends JButton {
    private boolean selectedStyle;

    private FormatButton(String name, String extension, Icon icon) {
      this.setLayout(new BorderLayout(22, 0));
      JLabel iconLabel = new JLabel(icon);
      iconLabel.setOpaque(false);
      this.add(iconLabel, BorderLayout.WEST);
      JPanel copy = new JPanel(new BorderLayout(0, 5));
      copy.setOpaque(false);
      JLabel nameLabel = new JLabel(name);
      nameLabel.setOpaque(false);
      nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
      nameLabel.setForeground(Style.text());
      copy.add(nameLabel, BorderLayout.NORTH);
      JLabel extensionLabel = new JLabel(extension);
      extensionLabel.setOpaque(false);
      extensionLabel.setFont(extensionLabel.getFont().deriveFont(15f));
      extensionLabel.setForeground(Style.mutedText());
      copy.add(extensionLabel, BorderLayout.SOUTH);
      this.add(copy, BorderLayout.CENTER);
      this.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));
      Style.styleButton(this, Style.ButtonVariant.SECONDARY);
      Dimension size = new Dimension(280, 124);
      this.setPreferredSize(size);
      this.setMinimumSize(size);
      this.setMaximumSize(size);
      this.getAccessibleContext().setAccessibleName(name + " (" + extension + ")");
    }

    private void setSelectedStyle(boolean selectedStyle) {
      if (this.selectedStyle == selectedStyle) {
        return;
      }
      this.selectedStyle = selectedStyle;
      this.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D copy = (Graphics2D) graphics.create();
      try {
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color accent = Style.accent();
        boolean highlighted = this.selectedStyle || this.getModel().isRollover() || this.isFocusOwner();
        copy.setColor(highlighted
          ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 32)
          : Style.surface());
        copy.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 18, 18);
        copy.setColor(highlighted ? accent : Style.border());
        copy.setStroke(new BasicStroke(highlighted ? 1.5f : 1f));
        copy.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 18, 18);
        if (this.selectedStyle) {
          int diameter = 28;
          int badgeX = this.getWidth() - diameter - 14;
          int badgeY = 14;
          copy.setColor(accent);
          copy.fillOval(badgeX, badgeY, diameter, diameter);
          copy.setColor(Color.WHITE);
          copy.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
          copy.drawLine(badgeX + 8, badgeY + 14, badgeX + 12, badgeY + 18);
          copy.drawLine(badgeX + 12, badgeY + 18, badgeX + 20, badgeY + 9);
        }
      } finally {
        copy.dispose();
      }
      super.paintComponent(graphics);
    }
  }

  private static final class ExportTitleIcon implements Icon {
    @Override
    public int getIconWidth() {
      return 34;
    }

    @Override
    public int getIconHeight() {
      return 30;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D copy = (Graphics2D) graphics.create();
      try {
        copy.translate(x, y);
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copy.setColor(Style.accent());
        copy.setStroke(new BasicStroke(2f));
        copy.drawRoundRect(1, 1, 20, 20, 4, 4);
        copy.drawRoundRect(12, 9, 20, 20, 4, 4);
      } finally {
        copy.dispose();
      }
    }
  }

  private record FileTypeIcon(boolean image, boolean accented) implements Icon {
    @Override
    public int getIconWidth() {
      return 48;
    }

    @Override
    public int getIconHeight() {
      return 56;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D copy = (Graphics2D) graphics.create();
      try {
        copy.translate(x, y);
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copy.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        copy.setColor(this.accented ? Style.accent() : Style.mutedText());
        Path2D file = new Path2D.Double();
        file.moveTo(5, 1);
        file.lineTo(31, 1);
        file.lineTo(43, 13);
        file.lineTo(43, 54);
        file.lineTo(5, 54);
        file.closePath();
        copy.draw(file);
        copy.drawLine(31, 1, 31, 13);
        copy.drawLine(31, 13, 43, 13);
        if (this.image) {
          copy.drawRoundRect(11, 25, 26, 20, 2, 2);
          copy.fillOval(29, 29, 4, 4);
          Path2D mountains = new Path2D.Double();
          mountains.moveTo(13, 42);
          mountains.lineTo(20, 34);
          mountains.lineTo(25, 39);
          mountains.lineTo(29, 35);
          mountains.lineTo(36, 42);
          copy.draw(mountains);
        } else {
          copy.drawString("</>", 10, 40);
        }
      } finally {
        copy.dispose();
      }
    }
  }
}
