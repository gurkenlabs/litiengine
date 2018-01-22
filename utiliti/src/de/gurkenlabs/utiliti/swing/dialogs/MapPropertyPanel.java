package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.utiliti.Program;

public class MapPropertyPanel extends JPanel {

  private JTextField textFieldDescription;
  private JTextField textFieldTitle;
  private JTextField textFieldAmbientColor;
  private JSpinner spinnerAmbientAlpha;
  private JTextField textFieldName;
  private JTable table;
  private transient IMap dataSource;
  DefaultTableModel model;

  /**
   * Create the dialog.
   */
  public MapPropertyPanel() {
    setBounds(100, 100, 450, 284);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    JLabel lblMapName = new JLabel("name");
    lblMapName.setFont(Program.TEXT_FONT);

    textFieldDescription = new JTextField();
    textFieldDescription.setFont(Program.TEXT_FONT);
    textFieldDescription.setColumns(10);

    JLabel lblMapTitle = new JLabel("title");
    lblMapTitle.setFont(Program.TEXT_FONT);

    textFieldTitle = new JTextField();
    textFieldTitle.setFont(Program.TEXT_FONT);
    textFieldTitle.setColumns(10);

    JLabel label = new JLabel("color");
    label.setFont(Program.TEXT_FONT);

    JButton button = new JButton("...");
    button.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, "Select an ambient color.", Color.decode(textFieldAmbientColor.getText()));
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB()).substring(2);
      textFieldAmbientColor.setText(h);
    });
    button.setFont(Program.TEXT_FONT.deriveFont(10f));

    textFieldAmbientColor = new JTextField();
    textFieldAmbientColor.setText("#ffffff");
    textFieldAmbientColor.setFont(Program.TEXT_FONT);
    textFieldAmbientColor.setEditable(false);
    textFieldAmbientColor.setColumns(10);

    JLabel lblAlpha = new JLabel("alpha");
    lblAlpha.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD));

    spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.setFont(Program.TEXT_FONT);

    JLabel lblNewLabel = new JLabel("General");
    lblNewLabel.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(12f));

    JLabel lblAmbientLight = new JLabel("Ambient Light");
    lblAmbientLight.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(12f));

    JLabel lblDesc = new JLabel("description");
    lblDesc.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD));

    textFieldName = new JTextField();
    textFieldName.setFont(Program.TEXT_FONT);
    textFieldName.setColumns(10);

    JLabel lblLayers = new JLabel("Mapobject Layers");
    lblLayers.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(12f));

    JScrollPane scrollPane = new JScrollPane();

    JButton buttonPlus = new JButton("+");
    buttonPlus.addActionListener(a -> {
      MapObjectLayer layer = new MapObjectLayer();
      layer.setName("new layer");
      dataSource.addMapObjectLayer(layer);
      model.addRow(new Object[] { layer.getName(), layer.getColor() });
    });
    buttonPlus.setFont(Program.TEXT_FONT);

    JButton buttonMinus = new JButton("-");
    buttonMinus.addActionListener(a -> {
      for (int removeIndex = 0; removeIndex < dataSource.getMapObjectLayers().size(); removeIndex++) {
        if (removeIndex == table.getSelectedRow()) {
          dataSource.removeMapObjectLayer(removeIndex);
          break;
        }
      }

      model.removeRow(table.getSelectedRow());
    });
    buttonMinus.setFont(Program.TEXT_FONT);
    GroupLayout glContentPanel = new GroupLayout(this);
    glContentPanel.setHorizontalGroup(
      glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
            .addComponent(lblNewLabel)
            .addGroup(glContentPanel.createSequentialGroup()
              .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                .addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
                .addGroup(glContentPanel.createParallelGroup(Alignment.TRAILING, false)
                  .addComponent(lblMapTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(lblMapName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
              .addGap(18)
              .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                .addComponent(textFieldName, 358, 358, 358)
                .addComponent(textFieldDescription, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)))
            .addGroup(glContentPanel.createSequentialGroup()
              .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                .addComponent(lblAmbientLight, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
                .addGroup(glContentPanel.createSequentialGroup()
                  .addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
                .addGroup(glContentPanel.createSequentialGroup()
                  .addComponent(label, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(button, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                  .addGap(6)
                  .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)))
              .addGap(33)
              .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(glContentPanel.createSequentialGroup()
                  .addComponent(lblLayers, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.RELATED, 132, Short.MAX_VALUE))
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addGroup(glContentPanel.createSequentialGroup()
                  .addComponent(buttonPlus)
                  .addGap(2)
                  .addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)))))
          .addContainerGap())
    );
    glContentPanel.setVerticalGroup(
      glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup()
          .addComponent(lblNewLabel)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblMapName)
            .addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblMapTitle)
            .addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(textFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(23)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblAmbientLight)
            .addComponent(lblLayers, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
          .addGap(9)
          .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
            .addGroup(glContentPanel.createSequentialGroup()
              .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
              .addGap(15)
              .addGroup(glContentPanel.createParallelGroup(Alignment.TRAILING)
                .addComponent(label, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                  .addComponent(button, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                  .addGroup(glContentPanel.createSequentialGroup()
                    .addGap(1)
                    .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)))))
            .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
            .addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
            .addComponent(buttonPlus, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    table = new JTable();
    table.setModel(new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            "layer", "color"
        }));
    model = (DefaultTableModel) table.getModel();
    table.setFont(Program.TEXT_FONT);
    scrollPane.setViewportView(table);
    this.setLayout(glContentPanel);
  }

  public void bind(IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      this.model.addRow(new Object[] { layer.getName(), layer.getColor() != null ? "#" + Integer.toHexString(layer.getColor().getRGB()).substring(2) : null });
    }
  }

  private void setControlValues(IMap map) {
    this.textFieldDescription.setText(map.getCustomProperty(MapProperty.MAP_DESCRIPTION));
    this.textFieldTitle.setText(map.getCustomProperty(MapProperty.MAP_TITLE));
    this.textFieldName.setText(map.getFileName());
    if (map.getCustomProperty(MapProperty.AMBIENTALPHA) != null) {
      this.spinnerAmbientAlpha.setValue(Integer.parseInt(map.getCustomProperty(MapProperty.AMBIENTALPHA)));
    }

    if (map.getCustomProperty(MapProperty.AMBIENTCOLOR) != null) {
      this.textFieldAmbientColor.setText(map.getCustomProperty(MapProperty.AMBIENTCOLOR));
    }
  }

  public void saveChanges() {
    if (this.dataSource == null) {
      return;
    }

    this.dataSource.setCustomProperty(MapProperty.MAP_DESCRIPTION, this.textFieldDescription.getText());
    this.dataSource.setCustomProperty(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setCustomProperty(MapProperty.AMBIENTALPHA, this.spinnerAmbientAlpha.getValue().toString());
    this.dataSource.setCustomProperty(MapProperty.AMBIENTCOLOR, this.textFieldAmbientColor.getText());
    this.dataSource.setFileName(this.textFieldName.getText());

    for (int row = 0; row < model.getRowCount(); row++) {
      String value = (String) model.getValueAt(row, 0);
      String color = (String) model.getValueAt(row, 1);
      if (value != null && !value.isEmpty()) {
        this.dataSource.getMapObjectLayers().get(row).setName(value);
      }

      this.dataSource.getMapObjectLayers().get(row).setColor(color);
    }

  }
}
