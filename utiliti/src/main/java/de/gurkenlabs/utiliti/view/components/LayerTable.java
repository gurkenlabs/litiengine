package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.table.renderer.DarkTableCellEditor;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class LayerTable extends JTable {

  protected static final String[] columns = new String[] {"visible", "name", "objects", "color"};

  private static final TableCellEditor visibilityEditor =
      new DarkTableCellEditor(new JToggleButton());
  private static final TableCellEditor nameEditor = new DarkTableCellEditor(new JTextField());
  private int lastSelection = 0;
  private transient IMap map;

  public LayerTable() {
    super();
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    setFocusable(false);
    setShowGrid(false);
    setModel(new DefaultTableModel(columns, 0));
    getModel().addTableModelListener(e -> this.updateLayers());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) {
          return;
        }
        int row = rowAtPoint(e.getPoint());
        int column = columnAtPoint(e.getPoint());
        if (row < 0 || column != 3 || getMap() == null
            || row >= getMap().getMapObjectLayers().size()) {
          return;
        }
        IMapObjectLayer layer = getMap().getMapObjectLayers().get(row);
        Color newColor = JColorChooser.showDialog(
            LayerTable.this,
            Resources.strings().get("panel_selectLayerColor"),
            layer.getColor());
        if (newColor == null) {
          return;
        }
        layer.setColor(newColor);
        getModel().setValueAt(newColor, row, 3);
        UndoManager.instance().recordChanges();
      }
    });
    resizeAndRepaint();
  }

  public void select(int selectedLayer) {
    changeSelection(selectedLayer, 0, false, false);
  }

  public void bind(IMap map) {
    this.map = map;
    if (this.map == null) {
      ((DefaultTableModel) this.getModel()).setRowCount(0);
      return;
    }
    Object[][] data = new Object[getMap().getMapObjectLayers().size()][columns.length];
    for (int row = 0; row < getMap().getMapObjectLayers().size(); row++) {
      data[row][0] = getMap().getMapObjectLayers().get(row).isVisible();
      data[row][1] = getMap().getMapObjectLayers().get(row).getName();
      data[row][2] = getMap().getMapObjectLayers().get(row).getMapObjects().size();
      data[row][3] = getMap().getMapObjectLayers().get(row).getColor();
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
    updateLayers();
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return (column == 0 || column == 1) && super.isCellEditable(row, column);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component comp = super.prepareRenderer(renderer, row, column);
    if (column == 2) {
      ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
    }
    return comp;
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

  public IMap getMap() {
    return this.map;
  }

  private void updateLayers() {
    if (this.getMap() == null) {
      return;
    }
    boolean layersChanged = false;
    for (int row = 0; row < getMap().getMapObjectLayers().size(); row++) {
      if (getMap().getMapObjectLayers().get(row).isVisible() != (boolean) this.getModel()
          .getValueAt(row, 0)
          || !this.getMap()
              .getMapObjectLayers()
              .get(row)
              .getName()
              .equals(this.getModel().getValueAt(row, 1).toString())) {
        layersChanged = true;
      }
      getMap()
          .getMapObjectLayers()
          .get(row)
          .setVisible((boolean) getModel().getValueAt(row, 0));
      getMap()
          .getMapObjectLayers()
          .get(row)
          .setName(getModel().getValueAt(row, 1).toString());
    }
    if (layersChanged) {
      UndoManager.instance().recordChanges();
    }
  }

}
