package com.litiengine.utiliti.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class LabelListCellRenderer implements ListCellRenderer<JLabel> {
  @Override
  public Component getListCellRendererComponent(JList<? extends JLabel> list, JLabel value, int index, boolean isSelected, boolean cellHasFocus) {
    if (value != null) {
      return value;
    }

    return new JLabel();
  }
}