package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;
import de.gurkenlabs.utiliti.UndoManager;

@SuppressWarnings("serial")
public class CustomPanel extends PropertyPanel {
  private JTable tableCustomProperties;
  private JScrollPane scrollPane;
  private DefaultTableModel model;

  public CustomPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_customProperties"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    this.scrollPane = new JScrollPane();

    JButton buttonAdd = new JButton("+");
    buttonAdd.addActionListener(a -> model.addRow(new Object[] { "", "" }));

    JButton buttonRemove = new JButton("-");
    buttonRemove.addActionListener(a -> {
      int[] rows = tableCustomProperties.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        model.removeRow(rows[i] - i);

      }
    });

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(buttonAdd).addPreferredGap(ComponentPlacement.RELATED).addComponent(buttonRemove, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(11).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)))
            .addContainerGap()));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(buttonRemove).addComponent(buttonAdd)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    this.tableCustomProperties = new JTable();
    this.tableCustomProperties.getTableHeader().setReorderingAllowed(false);
    this.tableCustomProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.scrollPane.setViewportView(tableCustomProperties);
    this.tableCustomProperties.setModel(new DefaultTableModel(new Object[][] {}, new String[] { Resources.get("panel_name"), Resources.get("panel_value") }) {
      Class[] columnTypes = new Class[] { String.class, String.class };
      boolean[] columnEditables = new boolean[] { true, true };

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return columnEditables[column];
      }
    });
    tableCustomProperties.getColumnModel().getColumn(0).setResizable(false);

    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.model.setRowCount(0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.clearControls();
    if (mapObject == null || mapObject.getCustomProperties() == null) {
      return;
    }
    for (Property prop : mapObject.getCustomProperties()) {
      if (MapObjectProperty.isCustom(prop.getName())) {
        this.model.addRow(new Object[] { prop.getName(), prop.getValue() });
      }
    }
  }

  private void setupChangedListeners() {
    this.model.addTableModelListener(e -> {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      UndoManager.instance().mapObjectChanging(getDataSource());
      List<String> setProperties = new ArrayList<>();
      for (int row = 0; row < model.getRowCount(); row++) {
        String name = (String) model.getValueAt(row, 0);
        String value = (String) model.getValueAt(row, 1);
        if (name != null && value != null && !name.isEmpty() && !value.isEmpty()) {
          setProperties.add(name);
          getDataSource().setCustomProperty(name, value);
        }
      }

      getDataSource().getCustomProperties().removeIf(p -> MapObjectProperty.isCustom(p.getName()) && !setProperties.contains(p.getName()));
      UndoManager.instance().mapObjectChanged(getDataSource());
    });
  }
}
