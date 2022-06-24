package de.gurkenlabs.utiliti.swing;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.UndoManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class MapListCellRenderer extends JLabel implements ListCellRenderer<IMap> {
  public MapListCellRenderer() {
    setOpaque(true);
    setBorder(new EmptyBorder(2, 5, 3, 5));
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends IMap> list, IMap map, int index, boolean isSelected, boolean cellHasFocus) {
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
