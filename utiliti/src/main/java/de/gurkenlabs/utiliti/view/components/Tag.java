package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Tag extends JPanel {

  private final JLabel lblText;
  private final JButton btnDelete;

  public Tag(String text) {
    this();
    this.setTag(text);
  }

  public Tag() {
    setBorder(null);
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(false);

    JPanel chip = new JPanel();
    chip.setBackground(Style.COLOR_DEFAULT_TAG);
    chip.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
    chip.setOpaque(true);
    add(chip);

    this.lblText = new JLabel("New label");
    this.lblText.setForeground(Color.WHITE);
    this.lblText.setFont(
      this.lblText.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));
    chip.add(this.lblText);

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
    chip.add(this.btnDelete);
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
