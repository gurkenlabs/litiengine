package de.gurkenlabs.utiliti.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;

public record CenterIcon(Icon icon, Dimension size) implements Icon {

  @Override
  public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
    int px = x + (size.width - icon.getIconWidth()) / 2;
    int py = y + (size.height - icon.getIconHeight()) / 2;
    icon.paintIcon(c, g, px, py);
  }

  @Override
  public int getIconWidth() {
    return size.width;
  }

  @Override
  public int getIconHeight() {
    return size.height;
  }
}
