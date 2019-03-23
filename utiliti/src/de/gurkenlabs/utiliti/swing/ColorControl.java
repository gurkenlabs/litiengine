package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;

@SuppressWarnings("serial")
public class ColorControl extends JPanel {
  private static final String DEFAULT_COLOR = "#ffffffff";
  private final JButton btnSelectColor;
  private final JTextField textFieldColor;

  private final List<ActionListener> listeners;

  public ColorControl() {
    this.listeners = new ArrayList<>();
    this.textFieldColor = new JTextField();
    this.textFieldColor.setText(DEFAULT_COLOR);
    this.textFieldColor.setEditable(false);
    this.textFieldColor.setColumns(10);

    this.btnSelectColor = new JButton("...");
    this.btnSelectColor.addActionListener(a -> {
      Color current = ColorHelper.decode(this.textFieldColor.getText());
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLightColor"), current);
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB());
      this.textFieldColor.setText(h);
      for (ActionListener listener : this.listeners) {
        listener.actionPerformed(null);
      }
    });
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addComponent(btnSelectColor, 25, 25, 25)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(textFieldColor, GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, PropertyPanel.CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
            .addComponent(textFieldColor, GroupLayout.PREFERRED_SIZE, PropertyPanel.CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)))
    );
    setLayout(groupLayout);
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  public void removeActionListener(ActionListener listener) {
    this.listeners.remove(listener);
  }

  public String getHexColor() {
    return this.textFieldColor.getText();
  }

  public void setHexColor(String color) {
    this.textFieldColor.setText(color);
  }

  public void clear() {
    this.textFieldColor.setText(DEFAULT_COLOR);
  }
}
