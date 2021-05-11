package de.gurkenlabs.utiliti.swing.panels;

import com.github.weisj.darklaf.ui.table.renderer.DarkTableCellEditor;
import com.github.weisj.darklaf.ui.table.renderer.DarkTableCellRenderer;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.utiliti.UndoManager;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class LayerTable extends JTable {
  protected static final String[] columns = new String[] {"visible", "name", "objects"};

  private static final TableCellEditor visibilityEditor =
      new DarkTableCellEditor(new JToggleButton());
  private static final TableCellEditor nameEditor = new DarkTableCellEditor(new JTextField());
  private int lastSelection = 0;
  private IMap map;

  public LayerTable() {
    super();
    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.setRowSelectionAllowed(true);
    this.setColumnSelectionAllowed(false);
    this.setModel(new DefaultTableModel(columns, 999));
    this.getModel().addTableModelListener(e -> this.updateLayers());
    this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    this.getColumnModel().getColumn(0).setMaxWidth(50);
    this.getColumnModel().getColumn(0).setMinWidth(50);
    this.getColumnModel().getColumn(0).setPreferredWidth(50);
    this.doLayout();
    this.resizeAndRepaint();
  }

  public void select(int selectedLayer) {
    this.changeSelection(selectedLayer, 0, false, false);
  }

  public void bind(IMap map) {
    this.map = map;
    Object[][] data = new Object[this.getMap().getMapObjectLayers().size()][columns.length];
    for (int row = 0; row < this.getMap().getMapObjectLayers().size(); row++) {
      boolean visible = this.getMap().getMapObjectLayers().get(row).isVisible();
      String name = this.getMap().getMapObjectLayers().get(row).getName();
      int objects = this.getMap().getMapObjectLayers().get(row).getMapObjects().size();

      data[row][0] = visible;
      data[row][1] = name;
      data[row][2] = objects;
    }
    ((DefaultTableModel) this.getModel()).setDataVector(data, columns);
  }

  @Override
  public int getSelectedRow() {
    int selection = super.getSelectedRow();
    if (selection != this.lastSelection) {
      this.lastSelection = selection;
    }
    return this.lastSelection;
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    super.setValueAt(aValue, row, column);
    this.updateLayers();
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return (column == 0 || column == 1) && super.isCellEditable(row, column);
  }

  @Override
  public TableCellEditor getCellEditor(final int row, final int column) {
    if (column == 0) {
      return visibilityEditor;
    } else if (column == 1) {
      return nameEditor;
    } else {
      return super.getCellEditor(row, column);
    }
  }

  @Override
  public TableCellRenderer getCellRenderer(final int row, final int column) {
    return new DarkTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c =
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (Game.world().environment() == null
            || Game.world().environment().getMap() == null
            || Game.world().environment().getMap().getMapObjectLayers().get(row) == null) {
          return c;
        }
        if (column == 0) {
          c.setBackground(
              Game.world().environment().getMap().getMapObjectLayers().get(row).getColor());
        }
        return c;
      }
    };
  }

  public IMap getMap() {
    return this.map;
  }

  private void updateLayers() {
    if (this.getMap() == null) {
      return;
    }
    boolean layersChanged = false;
    for (int row = 0; row < this.getMap().getMapObjectLayers().size(); row++) {
      if (this.getMap().getMapObjectLayers().get(row).isVisible()
              != (boolean) this.getModel().getValueAt(row, 0)
          || !this.getMap()
              .getMapObjectLayers()
              .get(row)
              .getName()
              .equals(this.getModel().getValueAt(row, 1).toString())) {
        layersChanged = true;
      }
      this.getMap()
          .getMapObjectLayers()
          .get(row)
          .setVisible((boolean) this.getModel().getValueAt(row, 0));
      this.getMap()
          .getMapObjectLayers()
          .get(row)
          .setName(this.getModel().getValueAt(row, 1).toString());
    }
    if (layersChanged) {
      UndoManager.instance().recordChanges();
    }
  }
}
