package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.SwingHelpers;

public class ColorListCellRenderer implements ListCellRenderer<Color> {

  public ColorListCellRenderer() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index, boolean isSelected, boolean cellHasFocus) {
    JTextField textFieldColor = new JTextField();
    SwingHelpers.updateColorTextField(textFieldColor,value);
    return textFieldColor;
  }

}
