package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
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
import javax.swing.JSpinner;
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
import de.gurkenlabs.litiengine.util.ArrayUtilities;

@SuppressWarnings("serial")
public class TriggerPanel extends PropertyPanel {
  private JTextField textFieldMessage;
  private DefaultTableModel model;
  private DefaultTableModel targetsModel;
  private JComboBox<TriggerActivation> comboBoxActivationType;
  private JSpinner spinnerCooldown;

  private JCheckBox chckbxOneTimeOnly;
  private JTable table;
  private JTable tableTargets;

  public TriggerPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_trigger"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblShadowType = new JLabel(Resources.get("panel_message"));

    this.textFieldMessage = new JTextField();
    this.textFieldMessage.setColumns(10);

    this.comboBoxActivationType = new JComboBox<>();
    this.comboBoxActivationType.setModel(new DefaultComboBoxModel<TriggerActivation>(TriggerActivation.values()));

    JLabel lblActivation = new JLabel(Resources.get("panel_activation"));

    JScrollPane scrollPane = new JScrollPane();

    JButton buttonPlus = new JButton("+");
    buttonPlus.setMargin(new Insets(2, 7, 2, 7));
    buttonPlus.addActionListener(a -> model.addRow(new Object[] { 0 }));

    JButton buttonMinus = new JButton("-");
    buttonMinus.addActionListener(a -> {
      int[] rows = table.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        model.removeRow(rows[i] - i);
      }
    });

    this.chckbxOneTimeOnly = new JCheckBox(Resources.get("panel_oneTimeOnly"));

    JScrollPane scrollPane1 = new JScrollPane();

    JButton button2 = new JButton("+");
    button2.setMargin(new Insets(2, 7, 2, 7));
    button2.addActionListener(a -> targetsModel.addRow(new Object[] { 0 }));

    JButton button3 = new JButton("-");
    button3.addActionListener(a -> {
      int[] rows = tableTargets.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        targetsModel.removeRow(rows[i] - i);
      }
    });

    this.spinnerCooldown = new JSpinner();

    JLabel lblCooldown = new JLabel("cooldown");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldMessage, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                        .addGroup(groupLayout.createSequentialGroup().addComponent(lblActivation, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxOneTimeOnly, GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE).addComponent(comboBoxActivationType, 0, 365, Short.MAX_VALUE))))
                    .addGap(10))
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addGap(4)
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(button3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(button2, GroupLayout.PREFERRED_SIZE, 37, Short.MAX_VALUE))
                                .addComponent(buttonPlus, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE).addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)))
                        .addComponent(lblCooldown, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGap(10).addComponent(spinnerCooldown, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)).addGroup(Alignment.LEADING,
                        groupLayout.createSequentialGroup().addGap(10).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(scrollPane1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))))
                    .addContainerGap()))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblShadowType, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(textFieldMessage, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblActivation, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxActivationType, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxOneTimeOnly).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayout.createSequentialGroup().addComponent(button2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addGap(4).addComponent(button3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayout.createSequentialGroup().addComponent(buttonPlus, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addGap(4).addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerCooldown, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblCooldown, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addContainerGap(47, Short.MAX_VALUE)));

    this.tableTargets = new JTable();
    this.tableTargets.getTableHeader().setReorderingAllowed(false);
    this.tableTargets.setModel(new DefaultTableModel(new Object[][] {}, new String[] { Resources.get("panel_targets") }) {
      Class[] columnTypes = new Class[] { Integer.class };

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    this.tableTargets.getColumnModel().getColumn(0).setResizable(false);
    scrollPane1.setViewportView(tableTargets);

    this.table = new JTable();
    this.table.getTableHeader().setReorderingAllowed(false);
    this.table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { Resources.get("panel_activators") }) {
      Class[] columnTypes = new Class[] { Integer.class };

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    this.table.getColumnModel().getColumn(0).setResizable(false);

    scrollPane.setViewportView(table);
    this.model = (DefaultTableModel) this.table.getModel();
    this.targetsModel = (DefaultTableModel) this.tableTargets.getModel();
    this.setLayout(groupLayout);

    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.textFieldMessage.setText("");
    this.comboBoxActivationType.setSelectedItem(TriggerActivation.COLLISION);
    this.model.setRowCount(0);
    this.targetsModel.setRowCount(0);
    this.chckbxOneTimeOnly.setSelected(false);
    this.spinnerCooldown.setValue(0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.textFieldMessage.setText(mapObject.getStringProperty(MapObjectProperty.TRIGGER_MESSAGE));

    String targets = mapObject.getStringProperty(MapObjectProperty.TRIGGER_TARGETS);
    this.targetsModel.setRowCount(0);
    for (int target : ArrayUtilities.getIntegerArray(targets)) {
      this.targetsModel.addRow(new Object[] { target });
    }

    this.chckbxOneTimeOnly.setSelected(mapObject.getBoolProperty(MapObjectProperty.TRIGGER_ONETIME));

    final TriggerActivation act = mapObject.getStringProperty(MapObjectProperty.TRIGGER_ACTIVATION) == null ? TriggerActivation.COLLISION : TriggerActivation.valueOf(mapObject.getStringProperty(MapObjectProperty.TRIGGER_ACTIVATION));
    this.comboBoxActivationType.setSelectedItem(act);

    String activators = mapObject.getStringProperty(MapObjectProperty.TRIGGER_ACTIVATORS);
    this.model.setRowCount(0);
    for (int activator : ArrayUtilities.getIntegerArray(activators)) {
      this.model.addRow(new Object[] { activator });
    }

    this.spinnerCooldown.setValue(mapObject.getIntProperty(MapObjectProperty.TRIGGER_COOLDOWN));
  }

  private void setupChangedListeners() {
    this.chckbxOneTimeOnly.addActionListener(new MapObjectPropertyActionListener(m -> m.setProperty(MapObjectProperty.TRIGGER_ONETIME, this.chckbxOneTimeOnly.isSelected())));
    this.textFieldMessage.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setProperty(MapObjectProperty.TRIGGER_MESSAGE, textFieldMessage.getText())));
    this.textFieldMessage.addActionListener(new MapObjectPropertyActionListener(m -> m.setProperty(MapObjectProperty.TRIGGER_MESSAGE, textFieldMessage.getText())));

    this.comboBoxActivationType.addActionListener(new MapObjectPropertyActionListener(m -> {
      TriggerActivation act = (TriggerActivation) this.comboBoxActivationType.getSelectedItem();
      m.setProperty(MapObjectProperty.TRIGGER_ACTIVATION, act);
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

      getDataSource().setProperty(MapObjectProperty.TRIGGER_ACTIVATORS, String.join(",", activators));
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

      getDataSource().setProperty(MapObjectProperty.TRIGGER_TARGETS, String.join(",", targets));
    });

    this.spinnerCooldown.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setProperty(MapObjectProperty.TRIGGER_COOLDOWN, (int) this.spinnerCooldown.getValue())));
  }
}
