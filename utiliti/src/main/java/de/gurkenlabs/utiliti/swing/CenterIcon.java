package de.gurkenlabs.utiliti.swing;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class CenterIcon implements Icon {

  private final Icon icon;
  private final Dimension size;

  public CenterIcon(final Icon icon, final Dimension size) {
    this.icon = icon;
    this.size = size;
  }

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
