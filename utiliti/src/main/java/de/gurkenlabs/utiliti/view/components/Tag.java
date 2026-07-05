package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Tag extends JPanel {
  static final int CHIP_HEIGHT = 24;
  private static final int TAG_ARC = 12;

  private final JLabel lblText;
  private final JButton btnDelete;

  public Tag(String text) {
    this();
    this.setTag(text);
  }

  public Tag() {
    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
    setLayout(new BorderLayout(2, 0));
    setBackground(Style.COLOR_DEFAULT_TAG);
    setOpaque(false);

    this.lblText = new JLabel("New label");
    this.lblText.setForeground(Style.COLOR_TEXT);
    this.lblText.setFont(
      this.lblText.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.8f));
    add(this.lblText, BorderLayout.CENTER);

    this.btnDelete = new JButton("\u00D7");
    this.btnDelete.addActionListener(
      e -> {
        final Container parent = this.getParent();
        parent.remove(this);
        parent.revalidate();
      });
    this.btnDelete.setFont(this.btnDelete.getFont().deriveFont(11f));
    this.btnDelete.setForeground(Style.COLOR_SUBTEXT);
    this.btnDelete.setMargin(new Insets(0, 0, 0, 0));
    this.btnDelete.setContentAreaFilled(false);
    this.btnDelete.setBorderPainted(false);
    this.btnDelete.setFocusPainted(false);
    this.btnDelete.setBorder(null);
    this.btnDelete.setPreferredSize(new Dimension(16, 16));
    this.btnDelete.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    add(this.btnDelete, BorderLayout.EAST);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        setBackground(Style.COLOR_TAG_HOVER);
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setBackground(Style.COLOR_DEFAULT_TAG);
        repaint();
      }
    });
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, TAG_ARC, TAG_ARC);
    g2.setColor(Style.COLOR_TAG_BORDER);
    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, TAG_ARC, TAG_ARC);
    g2.dispose();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = CHIP_HEIGHT;
    return d;
  }

  @Override
  public String toString() {
    return this.getTag();
  }

  public void setTag(String tag) {
    this.lblText.setText(tag);
  }

  public String getTag() {
    return this.lblText.getText();
  }

}
