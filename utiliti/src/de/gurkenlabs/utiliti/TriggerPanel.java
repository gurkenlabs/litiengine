package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.util.ArrayUtilities;

public class TriggerPanel extends PropertyPanel<IMapObject> {
  private JTextField textFieldMessage;
  DefaultTableModel model;
  DefaultTableModel targetsModel;
  JComboBox<TriggerActivation> comboBoxActivationType;

  JCheckBox chckbxOneTimeOnly;
  private JTable table;
  private JTable tableTargets;

  public TriggerPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_trigger"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_message"));

    textFieldMessage = new JTextField();
    textFieldMessage.setColumns(10);

    comboBoxActivationType = new JComboBox<>();
    comboBoxActivationType.setModel(new DefaultComboBoxModel<TriggerActivation>(TriggerActivation.values()));

    JLabel lblActivation = new JLabel(Resources.get("panel_activation"));

    JScrollPane scrollPane = new JScrollPane();

    JButton button = new JButton("+");
    button.addActionListener(a -> model.addRow(new Object[] { 0 }));

    JButton button_1 = new JButton("-");
    button_1.addActionListener(a -> {
      int[] rows = table.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        model.removeRow(rows[i] - i);
      }
    });

    chckbxOneTimeOnly = new JCheckBox(Resources.get("panel_oneTimeOnly"));

    JScrollPane scrollPane1 = new JScrollPane();

    JButton button2 = new JButton("+");
    button2.addActionListener(a -> targetsModel.addRow(new Object[] { 0 }));

    JButton button3 = new JButton("-");
    button3.addActionListener(a -> {
      int[] rows = tableTargets.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        targetsModel.removeRow(rows[i] - i);
      }
    });

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.TRAILING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblActivation, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(chckbxOneTimeOnly, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                    .addComponent(comboBoxActivationType, 0, 95, Short.MAX_VALUE))))
                        .addGap(10))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGap(4)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                            .addComponent(button_1, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                .addComponent(button3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(button2, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                            .addComponent(button, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                        .addContainerGap()))));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldMessage, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblActivation, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxActivationType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(chckbxOneTimeOnly)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(button2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                        .addGap(4)
                        .addComponent(button3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(button, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                        .addGap(3)
                        .addComponent(button_1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(267, Short.MAX_VALUE)));

    tableTargets = new JTable();
    tableTargets.getTableHeader().setReorderingAllowed(false);
    tableTargets.setModel(new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            Resources.get("panel_targets")
        }) {
      Class[] columnTypes = new Class[] {
          Integer.class
      };

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    tableTargets.getColumnModel().getColumn(0).setResizable(false);
    scrollPane1.setViewportView(tableTargets);

    table = new JTable();
    table.getTableHeader().setReorderingAllowed(false);
    table.setModel(new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            Resources.get("panel_activators")
        }) {
      Class[] columnTypes = new Class[] {
          Integer.class
      };

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    table.getColumnModel().getColumn(0).setResizable(false);

    scrollPane.setViewportView(table);
    this.model = (DefaultTableModel) this.table.getModel();
    this.targetsModel = (DefaultTableModel) this.tableTargets.getModel();
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldMessage.setText("");
    this.comboBoxActivationType.setSelectedItem(TriggerActivation.COLLISION);
    this.model.setRowCount(0);
    this.targetsModel.setRowCount(0);
    this.chckbxOneTimeOnly.setSelected(false);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldMessage.setText(mapObject.getCustomProperty(MapObjectProperty.TRIGGERMESSAGE));

    String targets = mapObject.getCustomProperty(MapObjectProperty.TRIGGERTARGETS);
    for (int target : ArrayUtilities.getIntegerArray(targets)) {
      this.targetsModel.addRow(new Object[] { target });
    }

    String oneTime = mapObject.getCustomProperty(MapObjectProperty.TRIGGERONETIME);
    if (oneTime != null) {
      this.chckbxOneTimeOnly.setSelected(Boolean.valueOf(oneTime));
    }

    final TriggerActivation act = mapObject.getCustomProperty(MapObjectProperty.TRIGGERACTIVATION) == null ? TriggerActivation.COLLISION : TriggerActivation.valueOf(mapObject.getCustomProperty(MapObjectProperty.TRIGGERACTIVATION));
    this.comboBoxActivationType.setSelectedItem(act);

    String activators = mapObject.getCustomProperty(MapObjectProperty.TRIGGERACTIVATORS);
    for (int activator : ArrayUtilities.getIntegerArray(activators)) {
      this.model.addRow(new Object[] { activator });
    }
  }

  private void setupChangedListeners() {
    this.chckbxOneTimeOnly.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.TRIGGERONETIME, Boolean.toString(this.chckbxOneTimeOnly.isSelected()))));
    this.textFieldMessage.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setCustomProperty(MapObjectProperty.TRIGGERMESSAGE, textFieldMessage.getText())));
    this.textFieldMessage.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.TRIGGERMESSAGE, textFieldMessage.getText())));

    this.comboBoxActivationType.addActionListener(new MapObjectPropertyActionListener(m -> {
      TriggerActivation act = (TriggerActivation) this.comboBoxActivationType.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.TRIGGERACTIVATION, act.toString());
    }));

    this.model.addTableModelListener(t -> {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      List<String> activators = new ArrayList<>();
      for (int row = 0; row < model.getRowCount(); row++) {
        Object activator = model.getValueAt(row, 0);
        if (activator != null) {
          activators.add(activator.toString());
        }
      }

      getDataSource().setCustomProperty(MapObjectProperty.TRIGGERACTIVATORS, String.join(",", activators));
    });

    this.targetsModel.addTableModelListener(t -> {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      List<String> targets = new ArrayList<>();
      for (int row = 0; row < targetsModel.getRowCount(); row++) {
        Object target = targetsModel.getValueAt(row, 0);
        if (target != null) {
          targets.add(target.toString());
        }
      }

      getDataSource().setCustomProperty(MapObjectProperty.TRIGGERTARGETS, String.join(",", targets));
    });
  }
}
