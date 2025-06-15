package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.SwingHelpers;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.Color;
import java.awt.Dimension;
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
  private final JTextField textFieldColor;
  private final JSpinner spinnerAlpha;

  private final transient List<ActionListener> listeners;
  private static final String DEFAULT_COLOR = "#FFFFFFFF";

  public ColorComponent() {
    int height = (PropertyPanel.CONTROL_HEIGHT + PropertyPanel.CONTROL_MARGIN) * 2;
    this.setSize(200, height);
    this.setPreferredSize(new Dimension(200, height));
    this.listeners = new ArrayList<>();
    this.textFieldColor = ControlBehavior.apply(new JTextField());
    this.textFieldColor.setEditable(true);
    this.textFieldColor.setColumns(9);
    this.textFieldColor.addActionListener(a -> this.setColor(ColorHelper.decode(this.textFieldColor.getText())));

    this.btnSelectColor = new JButton();
    this.btnSelectColor.setIcon(Icons.COLOR_16);
    this.btnSelectColor.addActionListener(
        a -> {
          final Color result =
              JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), this.getColor());
          this.setColor(result);
        });

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
                    .addComponent(textFieldColor, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
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
    SwingHelpers.updateColorTextField(this.textFieldColor, color);
    this.spinnerAlpha.setValue(color.getAlpha());
    for (ActionListener listener : this.listeners) {
      listener.actionPerformed(null);
    }
  }

  public void clear() {
    this.textFieldColor.setText(DEFAULT_COLOR);
  }
}
