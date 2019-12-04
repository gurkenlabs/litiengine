package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.panels.AmbientLightPreviewPanel;

/**
 * The Class MapPropertyPanel.
 */
@SuppressWarnings("serial")
public class MapPropertyPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -7653565461739608921L;

  /**
   * Update color text field.
   *
   * @param textField
   *          the text field
   */
  private static void updateColorTextField(final JTextComponent textField) {
    final Color fromText = ColorHelper.decode(textField.getText(), true);
    final float[] hsb = Color.RGBtoHSB(fromText.getRed(), fromText.getGreen(), fromText.getBlue(), null);
    final Color contrastColor = hsb[2] > 0.7 ? Color.black : Color.white;
    textField.setBackground(fromText);
    textField.setForeground(contrastColor);
  }

  /** The data source. */
  private transient IMap dataSource;

  /** The model. */
  private DefaultTableModel model;

  /** The scroll pane. */
  private JScrollPane scrollPane;

  /** The spinner ambient alpha. */
  private JSpinner spinnerAmbientAlpha;

  /** The spinner gravity. */
  private final JSpinner spinnerGravity;

  /** The spinner shadow alpha. */
  private JSpinner spinnerShadowAlpha;

  /** The table custom properties. */
  private JTable tableCustomProperties;

  /** The text field ambient color. */
  private JTextField textFieldAmbientColor;
  private final JEditorPane textFieldDesc;

  /** The text field name. */
  private JTextField textFieldName;

  /** The text field shadow color. */
  private JTextField textFieldShadowColor;

  private AmbientLightPreviewPanel ambientlightPreview;

  /** The text field title. */
  private final JTextField textFieldTitle;

  /**
   * Create the dialog.
   */
  public MapPropertyPanel() {
    this.setSize(new Dimension(500,600));
    this.setPreferredSize(new Dimension(500,600));
    this.setPreferredSize(new Dimension(500,600));
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    this.scrollPane = new JScrollPane();

    final JButton buttonAdd = new JButton("+");
    buttonAdd.addActionListener(a -> this.model.addRow(new Object[] { "", "" }));

    final JButton buttonRemove = new JButton("-");
    buttonRemove.addActionListener(a -> {
      final int[] rows = this.tableCustomProperties.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        this.model.removeRow(rows[i] - i);
      }
    });
    this.ambientlightPreview = new AmbientLightPreviewPanel();

    this.tableCustomProperties = new JTable();
    this.tableCustomProperties.getTableHeader().setReorderingAllowed(false);
    this.tableCustomProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.scrollPane.setViewportView(this.tableCustomProperties);
    this.tableCustomProperties.setModel(new DefaultTableModel(new Object[][] {}, new String[] { Resources.strings().get("panel_name"), Resources.strings().get("panel_value") }) {
      boolean[] columnEditables = new boolean[] { true, true };
      Class<?>[] columnTypes = new Class<?>[] { String.class, String.class };

      @Override
      public Class<?> getColumnClass(final int columnIndex) {
        return this.columnTypes[columnIndex];
      }

      @Override
      public boolean isCellEditable(final int row, final int column) {
        return this.columnEditables[column];
      }
    });
    this.tableCustomProperties.getColumnModel().getColumn(0).setResizable(false);

    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();

    final JLabel lblMapName = new JLabel(Resources.strings().get("panel_name"));

    this.textFieldDesc = new JEditorPane();

    final JLabel lblMapTitle = new JLabel(Resources.strings().get("panel_title"));

    this.textFieldTitle = new JTextField();
    this.textFieldTitle.setColumns(10);

    final JLabel lblColor = new JLabel(Resources.strings().get("panel_color"));

    final JButton buttonAmbientColor = new JButton("");
    buttonAmbientColor.setIcon(Icons.COLORX16);
    buttonAmbientColor.addActionListener(a -> {
      final Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), ColorHelper.decode(this.textFieldAmbientColor.getText()));
      if (result == null) {
        return;
      }
      final String h = ColorHelper.encode(result);
      this.textFieldAmbientColor.setText(h);
      updateColorTextField(this.textFieldAmbientColor);
      this.ambientlightPreview.setAmbientColor(result);
      this.spinnerAmbientAlpha.setValue(result.getAlpha());
    });

    this.textFieldAmbientColor = new JTextField();
    this.textFieldAmbientColor.setText("#ffffffff");
    this.textFieldAmbientColor.setEditable(false);
    this.textFieldAmbientColor.setColumns(10);

    final JLabel lblAlpha = new JLabel(Resources.strings().get("panel_alpha"));

    this.spinnerAmbientAlpha = new JSpinner();
    this.spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    this.spinnerAmbientAlpha.addChangeListener(e -> {
      final Color oldColor = ColorHelper.decode(this.textFieldAmbientColor.getText());
      final Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) this.spinnerAmbientAlpha.getValue());
      final String hex = ColorHelper.encode(newColor);
      this.textFieldAmbientColor.setText(hex);
      this.ambientlightPreview.setAmbientColor(newColor);
    });

    final JLabel lblGeneral = new JLabel(Resources.strings().get("panel_general"));
    lblGeneral.setFont(Style.getDefaultFont());

    final JLabel lblAmbientLight = new JLabel(Resources.strings().get("panel_ambientlight"));
    lblAmbientLight.setFont(Style.getDefaultFont());

    final JLabel lblDesc = new JLabel(Resources.strings().get("panel_description"));

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    final JLabel lblStaticShadows = new JLabel(Resources.strings().get("panel_staticshadows"));
    lblStaticShadows.setFont(Style.getDefaultFont());

    final JLabel labelAlpha = new JLabel(Resources.strings().get("panel_alpha"));

    this.spinnerShadowAlpha = new JSpinner();
    this.spinnerShadowAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    this.spinnerShadowAlpha.addChangeListener(e -> {
      final Color oldColor = ColorHelper.decode(this.textFieldShadowColor.getText());
      final Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) this.spinnerShadowAlpha.getValue());
      final String hex = ColorHelper.encode(newColor);
      this.textFieldShadowColor.setText(hex);
      this.ambientlightPreview.setStaticShadowColor(newColor);
    });

    final JLabel labelColor = new JLabel("color");

    final JButton buttonColorShadow = new JButton();
    buttonColorShadow.setIcon(Icons.COLORX16);
    buttonColorShadow.addActionListener(a -> {
      final Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectShadowColor"), ColorHelper.decode(this.textFieldShadowColor.getText()));
      if (result == null) {
        return;
      }
      final String h = ColorHelper.encode(result);
      this.textFieldShadowColor.setText(h);
      updateColorTextField(this.textFieldShadowColor);
      this.ambientlightPreview.setStaticShadowColor(result);
      this.spinnerShadowAlpha.setValue(result.getAlpha());
    });

    this.textFieldShadowColor = new JTextField();
    this.textFieldShadowColor.setText("#ffffffff");
    this.textFieldShadowColor.setEditable(false);
    this.textFieldShadowColor.setColumns(10);

    final JLabel lblGravity = new JLabel(Resources.strings().get("panel_gravity"));

    this.spinnerGravity = new JSpinner();

    final JButton button = new JButton("+");

    final JButton button_1 = new JButton("-");

    final JLabel lblCustomProperties = new JLabel(Resources.strings().get("panel_customProperties"));
    lblCustomProperties.setFont(Style.getDefaultFont());

    final GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                .addGroup(groupLayout
                    .createParallelGroup(
                        Alignment.TRAILING)
                    .addGroup(
                        groupLayout.createSequentialGroup()
                            .addGroup(
                                groupLayout.createParallelGroup(Alignment.LEADING)
                                    .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false).addComponent(lblGeneral, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblMapName, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lblDesc, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblMapTitle, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE))
                                                .addComponent(lblGravity, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerGravity, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE).addComponent(textFieldName).addComponent(textFieldDesc, GroupLayout.PREFERRED_SIZE, 297,
                                                    GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(groupLayout.createSequentialGroup()
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                .addGroup(groupLayout.createSequentialGroup()
                                                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(labelColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)).addGap(18)
                                                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createSequentialGroup().addComponent(buttonColorShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE,
                                                            68, GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                                                    .addGroup(groupLayout.createSequentialGroup().addComponent(lblColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(buttonAmbientColor, 0, 0, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(groupLayout.createSequentialGroup().addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
                                                .addComponent(lblStaticShadows, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(ambientlightPreview, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)))
                                        .addGap(357))
                                    .addComponent(lblAmbientLight, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap())
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(
                            groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(lblCustomProperties, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup().addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(button).addComponent(button_1, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))))
                        .addGap(357)))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblGeneral).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMapName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMapTitle, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE).addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE).addComponent(textFieldDesc, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerGravity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblGravity)).addGap(30).addComponent(lblAmbientLight)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE).addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(buttonAmbientColor, GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblColor).addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 19, Short.MAX_VALUE)))
                    .addGap(22).addComponent(lblStaticShadows).addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE).addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(labelColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(buttonColorShadow, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(18))
                .addGroup(groupLayout.createSequentialGroup().addComponent(ambientlightPreview, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)))
            .addComponent(lblCustomProperties).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE).addGroup(groupLayout.createSequentialGroup().addComponent(button).addGap(4).addComponent(button_1))).addGap(89)));
    ambientlightPreview.setLayout(null);

    this.setLayout(groupLayout);
    this.setupChangedListeners();
  }

  /**
   * Bind.
   *
   * @param map
   *          the map
   */
  public void bind(final IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
  }

  /**
   * Save changes.
   */
  public void saveChanges() {
    if (this.dataSource == null) {
      return;
    }

    this.dataSource.setValue(MapProperty.MAP_DESCRIPTION, this.textFieldDesc.getText());
    this.dataSource.setValue(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setValue(MapProperty.GRAVITY, (int) this.spinnerGravity.getValue());
    this.dataSource.setValue(MapProperty.AMBIENTCOLOR, this.textFieldAmbientColor.getText());
    this.dataSource.setValue(MapProperty.SHADOWCOLOR, this.textFieldShadowColor.getText());
    this.dataSource.setName(this.textFieldName.getText());
  }

  /**
   * Sets the control values.
   *
   * @param map
   *          the new control values
   */
  private void setControlValues(final IMap map) {
    this.textFieldDesc.setText(map.getStringValue(MapProperty.MAP_DESCRIPTION));
    this.textFieldTitle.setText(map.getStringValue(MapProperty.MAP_TITLE));
    this.textFieldName.setText(map.getName());
    if (map.getStringValue(MapProperty.AMBIENTCOLOR) != null) {
      final String hexColor = map.getStringValue(MapProperty.AMBIENTCOLOR);
      final Color decodedColor = ColorHelper.decode(hexColor);
      this.textFieldAmbientColor.setText(hexColor);
      updateColorTextField(this.textFieldAmbientColor);
      this.ambientlightPreview.setAmbientColor(decodedColor);
      this.spinnerAmbientAlpha.setValue(decodedColor.getAlpha());
    }
    if (map.getStringValue(MapProperty.SHADOWCOLOR) != null) {
      final String hexColor = map.getStringValue(MapProperty.SHADOWCOLOR);
      this.textFieldShadowColor.setText(hexColor);
      final Color decodedColor = ColorHelper.decode(hexColor);
      updateColorTextField(this.textFieldShadowColor);
      this.ambientlightPreview.setStaticShadowColor(decodedColor);
      this.spinnerShadowAlpha.setValue(decodedColor.getAlpha());
    }

    this.spinnerGravity.setValue(map.getIntValue(MapProperty.GRAVITY));
  }

  /**
   * Setup changed listeners.
   */
  private void setupChangedListeners() {
    this.model.addTableModelListener(e -> this.updateCustomProperties());
  }

  /**
   * Update custom properties.
   */
  private void updateCustomProperties() {
    if (this.dataSource == null) {
      return;
    }

    final List<String> setProperties = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      final String name = (String) this.model.getValueAt(row, 0);
      final String value = (String) this.model.getValueAt(row, 1);
      if (name != null && !name.isEmpty()) {
        setProperties.add(name);
        this.dataSource.setValue(name, value);
      }
    }
    this.dataSource.getProperties().keySet().removeIf(p -> MapObjectProperty.isCustom(p) && !setProperties.contains(p));
  }
}
