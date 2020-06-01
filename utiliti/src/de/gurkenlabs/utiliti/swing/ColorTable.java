package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.github.weisj.darklaf.components.border.DarkBorders;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.SwingHelpers;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;

@SuppressWarnings("serial")
public class ColorTable extends PropertyPanel {
  private final DefaultTableModel model;
  private final JTable table;
  private final Box ctrlButtonBox;
  private final JButton btnAdd;
  private final JButton btnRemove;
  private final JButton btnEdit;
  private final JScrollPane scrollPanel;

  public ColorTable() {
    super();

    model = new DefaultTableModel(0, 1);
    table = new JTable(model);
    table.getColumnModel().getColumn(0).setCellRenderer(new ColorListCellRenderer());
    table.setTableHeader(null);
    scrollPanel = new JScrollPane(table);
    scrollPanel.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));

    ctrlButtonBox = Box.createVerticalBox();
    btnAdd = new JButton(Icons.ADD);
    btnAdd.setPreferredSize(BUTTON_SIZE);
    btnAdd.setMinimumSize(BUTTON_SIZE);
    btnAdd.setMaximumSize(BUTTON_SIZE);
    btnRemove = new JButton(Icons.DELETE);
    btnRemove.setPreferredSize(BUTTON_SIZE);
    btnRemove.setMinimumSize(BUTTON_SIZE);
    btnRemove.setMaximumSize(BUTTON_SIZE);
    btnEdit = new JButton(Icons.PENCIL);
    btnEdit.setPreferredSize(BUTTON_SIZE);
    btnEdit.setMinimumSize(BUTTON_SIZE);
    btnEdit.setMaximumSize(BUTTON_SIZE);

    ctrlButtonBox.add(btnAdd);
    ctrlButtonBox.add(btnRemove);
    ctrlButtonBox.add(btnEdit);
    GroupLayout grplayout = new GroupLayout(this);
    grplayout.setHorizontalGroup(grplayout.createSequentialGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    grplayout.setVerticalGroup(grplayout.createParallelGroup().addComponent(ctrlButtonBox).addComponent(scrollPanel));
    this.setLayout(grplayout);
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    model.setRowCount(0);
    table.clearSelection();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    setColors(mapObject.getStringValue(MapObjectProperty.Emitter.COLORS));
  }

  private void setColors(String commaSeparatedHexstrings) {
    model.setRowCount(0);
    for (String colorStr : commaSeparatedHexstrings.split(",")) {
      model.addRow(new Object[] { colorStr });
    }
  }

  private void setupChangedListeners() {
    btnAdd.addActionListener(a -> model.addRow(new Object[] { ColorHelper.encode(EmitterData.DEFAULT_COLOR.brighter()) }));
    btnRemove.addActionListener(a -> model.removeRow(table.getSelectedRow()));
    btnEdit.addActionListener(a -> {
      Color previousColor = ColorHelper.decode(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString());
      final Color result = JColorChooser.showDialog(null, Resources.strings().get("particle_editcolor"), previousColor);
      table.setValueAt(ColorHelper.encode(result), table.getSelectedRow(), table.getSelectedColumn());
    });
    setup(table, MapObjectProperty.Emitter.COLORS);

  }

  private class ColorListCellRenderer extends JTextField implements TableCellRenderer {
    Border focusBorder = DarkBorders.createLineBorder(1, 1, 1, 1);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      this.setColumns(9);
      this.setHorizontalAlignment(JTextField.CENTER);
      SwingHelpers.updateColorTextField(this, ColorHelper.decode((String) value));
      this.setBorder(hasFocus ? focusBorder : null);
      if (hasFocus) {
        ControlBehavior.apply(this);
      }
      return this;
    }

  }
}
