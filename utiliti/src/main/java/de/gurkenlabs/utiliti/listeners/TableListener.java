package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.util.ArrayList;
import java.util.function.Predicate;
import javax.swing.JTable;

public class TableListener extends MabObjectPropertyTableModelListener {

  public TableListener(PropertyPanel propertyPanel, JTable table,
    Predicate<IMapObject> mapObjectStateCheck, String... mapObjectProperties) {
    super(propertyPanel, mapObjectStateCheck,
      m -> {
        int column = 0;
        for (String prop : mapObjectProperties) {
          ArrayList<Object> values = new ArrayList<>();
          for (int i = 0; i < table.getRowCount(); i++) {
            values.add(table.getValueAt(i, column));
          }
          m.setValue(prop, ArrayUtilities.join(values, ","));
          column++;
        }
      });
  }
}
