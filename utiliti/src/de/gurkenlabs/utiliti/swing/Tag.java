package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.gurkenlabs.litiengine.Resources;

public class Tag extends JPanel {
  public static final BufferedImage DELETE_DISABLED = Resources.getImage("button-delete-disabledx7.png");
  public static final BufferedImage DELETE = Resources.getImage("button-deletex7.png");
  public static final Color DEFAULT_TAG_COLOR = new Color(99, 113, 118);
  public static final Color DEFAULT_TAG_HOVER_COLOR = DEFAULT_TAG_COLOR.darker();
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

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        panel.setBackground(DEFAULT_TAG_HOVER_COLOR);
      }

      @Override
      public void mouseExited(final MouseEvent e) {
        if (!deleteHovered) {
          panel.setBackground(DEFAULT_TAG_COLOR);
        }
      }
    });

    this.btnDelete = new JButton();
    this.btnDelete.addActionListener(e -> {
      final Container parent = this.getParent();
      parent.remove(this);
      parent.revalidate();
    });

    this.btnDelete.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(final MouseEvent e) {
        if (DELETE != null) {
          btnDelete.setIcon(new ImageIcon(DELETE));
        }

        panel.setBackground(DEFAULT_TAG_HOVER_COLOR);
        deleteHovered = true;
      }

      @Override
      public void mouseExited(final MouseEvent e) {
        if (!btnDelete.hasFocus() && DELETE_DISABLED != null) {
          btnDelete.setIcon(new ImageIcon(DELETE_DISABLED));
        }

        deleteHovered = false;
      }
    });

    this.btnDelete.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
        if (DELETE_DISABLED != null) {
          btnDelete.setIcon(new ImageIcon(DELETE_DISABLED));
        }
      }

      @Override
      public void focusGained(FocusEvent e) {
        if (DELETE != null) {
          btnDelete.setIcon(new ImageIcon(DELETE));
        }
      }
    });

    this.btnDelete.setMargin(new Insets(2, 5, 2, 5));
    this.btnDelete.setContentAreaFilled(false);
    this.btnDelete.setBorderPainted(false);
    this.btnDelete.setFocusPainted(false);
    this.btnDelete.setBorder(null);
    this.btnDelete.setPreferredSize(new Dimension(7, 7));
    if (DELETE_DISABLED != null) {
      this.btnDelete.setIcon(new ImageIcon(DELETE_DISABLED));
    }
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
