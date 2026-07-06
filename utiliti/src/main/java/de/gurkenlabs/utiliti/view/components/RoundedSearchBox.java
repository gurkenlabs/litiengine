package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    super(new BorderLayout(8, 0));
    this.textField = textField;

    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
    setPreferredSize(new Dimension(preferredWidth, 30));
    setMinimumSize(new Dimension(0, 30));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

    JLabel searchIcon = new JLabel(Icons.SEARCH_16);
    searchIcon.setPreferredSize(new Dimension(16, 30));
    add(searchIcon, BorderLayout.WEST);
    add(textField, BorderLayout.CENTER);

    this.clearButton = Style.clearButton(Icons.CROSS_8);
    this.clearButton.setPreferredSize(new Dimension(24, 28));
    this.clearButton.setToolTipText("Clear search");
    add(this.clearButton, BorderLayout.EAST);
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
      g2.setColor(Style.COLOR_SURFACE);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.setColor(Style.COLOR_BORDER);
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
    } finally {
      g2.dispose();
    }
    super.paintComponent(g);
  }
}
