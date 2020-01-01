package de.gurkenlabs.utiliti.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.UndoManager;

public class MapListCellRenderer extends JLabel implements ListCellRenderer<IMap> {
  public MapListCellRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IMap> list, IMap map, int index, boolean isSelected, boolean cellHasFocus) {
    if (UndoManager.hasChanges(map)) {
      setText(map.getName()+ " *");
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
