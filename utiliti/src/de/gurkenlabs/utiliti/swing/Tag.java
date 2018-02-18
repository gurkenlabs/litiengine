package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.gurkenlabs.litiengine.Resources;

public class Tag extends JPanel {
  public static final Color DEFAULT_TAG_COLOR = new Color(99, 113, 118);
  private JPanel panel;
  private JLabel lblText;

  public Tag(String text) {
    this();
    this.setTag(text);
  }

  public Tag() {
    setBorder(null);

    this.panel = new InternalTagPanel();
    this.panel.setBackground(DEFAULT_TAG_COLOR);
    this.panel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 2));
    add(this.panel);
    FlowLayout flowLayout = (FlowLayout) getLayout();
    flowLayout.setVgap(2);
    flowLayout.setHgap(2);
    flowLayout.setAlignment(FlowLayout.LEFT);

    this.lblText = new JLabel("New label");
    this.lblText.setForeground(Color.WHITE);
    this.lblText.setFont(this.lblText.getFont().deriveFont(10f));
    this.panel.add(this.lblText);

    JButton btnDelete = new JButton();
    btnDelete.addActionListener(e -> {
      final Container parent = this.getParent();
      parent.remove(this);
      parent.revalidate();
    });
    btnDelete.setMargin(new Insets(2, 5, 2, 5));
    btnDelete.setContentAreaFilled(false);
    btnDelete.setBorderPainted(false);
    btnDelete.setBorder(null);
    btnDelete.setPreferredSize(new Dimension(7, 7));
    BufferedImage img = Resources.getImage("button-deletex7.png");
    if (img != null) {
      btnDelete.setIcon(new ImageIcon(img));
    }
    this.panel.add(btnDelete);
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

  private class InternalTagPanel extends JPanel {
    @Override
    public Dimension getPreferredSize() {
      return new Dimension(super.getMinimumSize().width, 19);
    }
  }
}
