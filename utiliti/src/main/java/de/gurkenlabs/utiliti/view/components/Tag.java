package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Tag extends JPanel {
  static final int CHIP_HEIGHT = 24;

  private final JLabel lblText;
  private final JButton btnDelete;

  public Tag(String text) {
    this();
    this.setTag(text);
  }

  public Tag() {
    setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
    setLayout(new BorderLayout(3, 0));
    setBackground(Style.COLOR_DEFAULT_TAG);
    setOpaque(true);

    this.lblText = new JLabel("New label");
    this.lblText.setForeground(Color.WHITE);
    this.lblText.setFont(
      this.lblText.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));
    add(this.lblText, BorderLayout.CENTER);

    this.btnDelete = new JButton();
    this.btnDelete.addActionListener(
      e -> {
        final Container parent = this.getParent();
        parent.remove(this);
        parent.revalidate();
      });
    this.btnDelete.setMargin(new Insets(0, 0, 0, 0));
    this.btnDelete.setContentAreaFilled(false);
    this.btnDelete.setBorderPainted(false);
    this.btnDelete.setFocusPainted(false);
    this.btnDelete.setBorder(null);
    this.btnDelete.setPreferredSize(new Dimension(12, 12));
    this.btnDelete.setIcon(Icons.DELETE_8);
    add(this.btnDelete, BorderLayout.EAST);
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
