package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

public class ColorComponent extends JPanel {
  private final JButton btnSelectColor;
  private final JButton btnClearColor;
  private final JTextField textFieldColor;
  private final JSpinner spinnerAlpha;
  private final Color clearColor;

  private final transient List<ActionListener> listeners;

  public ColorComponent() {
    this(Color.WHITE);
  }

  public ColorComponent(Color clearColor) {
    this.clearColor = clearColor;
    int height = (PropertyPanel.CONTROL_HEIGHT + PropertyPanel.CONTROL_MARGIN) * 2;
    this.setOpaque(false);
    this.setSize(PropertyPanel.CONTROL_WIDTH, height);
    this.setPreferredSize(new Dimension(PropertyPanel.CONTROL_WIDTH, height));
    this.listeners = new ArrayList<>();
    this.textFieldColor = ControlBehavior.apply(new JTextField());
    this.textFieldColor.setBackground(Style.COLOR_SURFACE2);
    this.textFieldColor.setForeground(Style.COLOR_TEXT);
    this.textFieldColor.setCaretColor(Style.COLOR_ACCENT_BLUE);
    this.textFieldColor.setEditable(true);
    this.textFieldColor.setColumns(9);
    this.textFieldColor.addActionListener(a -> this.setColor(ColorHelper.decode(this.textFieldColor.getText())));

    this.btnSelectColor = new ColorSwatchButton();
    this.btnSelectColor.setIcon(Icons.COLOR_16);
    styleColorActionButton(this.btnSelectColor);
    this.btnSelectColor.addActionListener(
        a -> {
          final Color result =
              JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), this.getColor());
          this.setColor(result);
        });

    this.btnClearColor = Style.iconButton(Icons.DELETE_16);
    this.btnClearColor.setToolTipText("Clear color");
    this.btnClearColor.addActionListener(a -> this.clear());

    final JLabel lblAlpha = new JLabel(Resources.strings().get("panel_alpha"));

    this.spinnerAlpha = new JSpinner();

    this.spinnerAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 5));
    this.spinnerAlpha.addChangeListener(
        a -> {
          final Color oldColor = this.getColor();
          if (oldColor == null) {
            return;
          }

          final Color newColor =
              new Color(
                  oldColor.getRed(),
                  oldColor.getGreen(),
                  oldColor.getBlue(),
                  (int) this.spinnerAlpha.getValue());
          this.setColor(newColor);
        });

    ControlBehavior.apply(this.spinnerAlpha);

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addComponent(
                        btnSelectColor,
                        PropertyPanel.CONTROL_HEIGHT,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(
                        btnClearColor,
                        PropertyPanel.CONTROL_HEIGHT,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(textFieldColor, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addComponent(lblAlpha)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(spinnerAlpha, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)));
    groupLayout.setVerticalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(
                                btnSelectColor,
                                PropertyPanel.CONTROL_HEIGHT,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(
                                btnClearColor,
                                PropertyPanel.CONTROL_HEIGHT,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(
                                textFieldColor,
                                GroupLayout.PREFERRED_SIZE,
                                PropertyPanel.CONTROL_HEIGHT,
                                GroupLayout.PREFERRED_SIZE))
                    .addGap(PropertyPanel.CONTROL_MARGIN)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblAlpha)
                            .addComponent(
                                spinnerAlpha,
                                PropertyPanel.CONTROL_HEIGHT,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))));
    setLayout(groupLayout);
  }

  private static void styleColorActionButton(JButton button) {
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(new RoundedBorder(Style.COLOR_BORDER, 8, 4));
  }

  private static final class ColorSwatchButton extends JButton {
    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground() != null ? getBackground() : Style.COLOR_SURFACE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.setColor(Style.COLOR_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
      } finally {
        g2.dispose();
      }
      super.paintComponent(g);
    }
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  public void removeActionListener(ActionListener listener) {
    this.listeners.remove(listener);
  }

  public int getAlpha() {
    return (int) this.spinnerAlpha.getValue();
  }

  public String getHexColor() {
    return this.textFieldColor.getText();
  }

  public Color getColor() {
    return ColorHelper.decode(this.textFieldColor.getText());
  }

  public void setColor(Color color) {
    if (color == null) {
      return;
    }
    this.textFieldColor.setText(ColorHelper.encode(color));
    this.textFieldColor.setBackground(Style.COLOR_SURFACE2);
    this.textFieldColor.setForeground(Style.COLOR_TEXT);
    this.btnSelectColor.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue()));
    this.spinnerAlpha.setValue(color.getAlpha());
    for (ActionListener listener : this.listeners) {
      listener.actionPerformed(null);
    }
  }

  public void clear() {
    this.setColor(this.clearColor);
  }
}
