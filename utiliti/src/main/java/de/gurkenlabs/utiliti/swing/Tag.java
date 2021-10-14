package de.gurkenlabs.utiliti.swing;

import de.gurkenlabs.utiliti.Style;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Tag extends JPanel {

  private JPanel panel;
  private JLabel lblText;
  private JButton btnDelete;

  private boolean deleteHovered;

  public Tag(String text) {
    this();
    this.setTag(text);
  }

  public Tag() {
    setBorder(null);

    this.panel = new InternalTagPanel();
    this.panel.setBackground(Style.COLOR_DEFAULT_TAG);
    this.panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
    add(this.panel);

    this.lblText = new JLabel("New label");
    this.lblText.setForeground(Color.WHITE);
    this.lblText.setFont(
        this.lblText.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));
    this.panel.add(this.lblText);

    this.btnDelete = new JButton();
    this.btnDelete.addActionListener(
        e -> {
          final Container parent = this.getParent();
          parent.remove(this);
          parent.revalidate();
        });

    this.btnDelete.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(final MouseEvent e) {
            btnDelete.setIcon(Icons.DELETE_X7);

            deleteHovered = true;
          }

          @Override
          public void mouseExited(final MouseEvent e) {
            if (!btnDelete.hasFocus()) {
              btnDelete.setIcon(Icons.DELETE_X7_DISABLED);
            }

            deleteHovered = false;
          }
        });

    this.btnDelete.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            btnDelete.setIcon(Icons.DELETE_X7_DISABLED);
          }

          @Override
          public void focusGained(FocusEvent e) {
            btnDelete.setIcon(Icons.DELETE_X7);
          }
        });

    this.btnDelete.setMargin(new Insets(2, 0, 2, 0));
    this.btnDelete.setContentAreaFilled(false);
    this.btnDelete.setBorderPainted(false);
    this.btnDelete.setFocusPainted(false);
    this.btnDelete.setBorder(null);
    this.btnDelete.setPreferredSize(new Dimension(9, 9));
    this.btnDelete.setIcon(Icons.DELETE_X7_DISABLED);
    this.panel.add(this.btnDelete);
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
      return new Dimension(super.getMinimumSize().width, 17);
    }
  }
}
