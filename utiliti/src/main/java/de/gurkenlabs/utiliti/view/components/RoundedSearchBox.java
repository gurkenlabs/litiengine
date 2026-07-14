package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A reusable rounded search box with a left search icon, center text field, and right clear button.
 */
public class RoundedSearchBox extends JPanel {
  private final JTextField textField;
  private final JButton clearButton;

  public RoundedSearchBox(JTextField textField, int preferredWidth) {
    super(new BorderLayout(Style.SPACE_MEDIUM, 0));
    this.textField = textField;

    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 2));
    setPreferredSize(new Dimension(preferredWidth, Style.CONTROL_HEIGHT));
    setMinimumSize(new Dimension(0, Style.CONTROL_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.CONTROL_HEIGHT));

    JLabel searchIcon = new JLabel(Icons.SEARCH_16);
    searchIcon.setPreferredSize(new Dimension(Style.ICON_SIZE, Style.CONTROL_HEIGHT));
    add(searchIcon, BorderLayout.WEST);
    add(textField, BorderLayout.CENTER);

    this.clearButton = Style.clearButton(Icons.CROSS_8);
    this.clearButton.setPreferredSize(new Dimension(24, 24));
    this.clearButton.setMinimumSize(new Dimension(24, 24));
    this.clearButton.setMaximumSize(new Dimension(24, 24));
    this.clearButton.setToolTipText("Clear search");
    add(this.clearButton, BorderLayout.EAST);

    this.textField.addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) { repaint(); }
      @Override public void focusLost(FocusEvent e) { repaint(); }
    });
  }

  public JTextField getTextField() {
    return this.textField;
  }

  public JButton getClearButton() {
    return this.clearButton;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.surface());
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
      g2.setColor(this.textField.isFocusOwner() ? Style.accent() : Style.border());
      g2.setStroke(new java.awt.BasicStroke(this.textField.isFocusOwner() ? 2f : 1f));
      int inset = this.textField.isFocusOwner() ? 1 : 0;
      g2.drawRoundRect(inset, inset, getWidth() - 1 - inset * 2, getHeight() - 1 - inset * 2,
          Style.CORNER_RADIUS, Style.CORNER_RADIUS);
    } finally {
      g2.dispose();
    }
    super.paintComponent(g);
  }
}
