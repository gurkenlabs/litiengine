package com.litiengine.utiliti.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import com.litiengine.environment.tilemap.IMap;
import com.litiengine.utiliti.UndoManager;

@SuppressWarnings("serial")
public class MapListCellRenderer extends JLabel implements ListCellRenderer<IMap> {
  public MapListCellRenderer() {
    setOpaque(true);
    setBorder(new EmptyBorder(2, 5, 3, 5));
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IMap> list, IMap map, int index, boolean isSelected, boolean cellHasFocus) {
    if (UndoManager.hasChanges(map)) {
      setText(map.getName() + " *");
    } else {
      setText(map.getName());
    }
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    return this;
  }

}
