package de.gurkenlabs.utiliti.swing.dialogs;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.swing.ColorComponent;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
import de.gurkenlabs.utiliti.swing.panels.AmbientLightPreviewPanel;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class MapPropertyPanel extends JPanel {

  private AmbientLightPreviewPanel ambientlightPreview;

  private transient IMap dataSource;

  private DefaultTableModel model;

  private JScrollPane scrollPane;

  private final JSpinner spinnerGravity;

  private JTable tableCustomProperties;

  private ColorComponent ambientColorComponent;

  private final JEditorPane textFieldDesc;

  private JTextField textFieldName;

  private ColorComponent shadowColorComponent;

  private final JTextField textFieldTitle;

  public MapPropertyPanel() {
    this.setSize(new Dimension(330, 650));
    this.setPreferredSize(new Dimension(330, 650));
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    this.scrollPane = new JScrollPane();

    final JButton buttonAdd = new JButton("+");
    buttonAdd.addActionListener(a -> this.model.addRow(new Object[] {"", ""}));

    final JButton buttonRemove = new JButton("-");
    buttonRemove.addActionListener(
        a -> {
          final int[] rows = this.tableCustomProperties.getSelectedRows();
          for (int i = 0; i < rows.length; i++) {
            this.model.removeRow(rows[i] - i);
          }
        });
    this.ambientlightPreview = new AmbientLightPreviewPanel();
    this.ambientColorComponent = new ColorComponent();
    this.ambientColorComponent.addActionListener(
        a -> this.ambientlightPreview.setAmbientColor(this.ambientColorComponent.getColor()));

    this.tableCustomProperties = new JTable();
    this.tableCustomProperties.getTableHeader().setReorderingAllowed(false);
    this.tableCustomProperties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.scrollPane.setViewportView(this.tableCustomProperties);
    this.tableCustomProperties.setModel(
        new DefaultTableModel(
            new Object[][] {},
            new String[] {
                Resources.strings().get("panel_name"), Resources.strings().get("panel_value")
            }));

    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();

    final JLabel lblMapName = new JLabel(Resources.strings().get("panel_name"));

    final JLabel lblMapTitle = new JLabel(Resources.strings().get("panel_title"));

    this.textFieldTitle = new JTextField();
    ControlBehavior.apply(this.textFieldTitle);
    this.textFieldTitle.setColumns(10);

    final JLabel lblGeneral = new JLabel(Resources.strings().get("panel_general"));
    lblGeneral.setFont(Style.getDefaultFont());

    final JLabel lblAmbientLight = new JLabel(Resources.strings().get("panel_ambientlight"));
    lblAmbientLight.setFont(Style.getDefaultFont());

    final JLabel lblDesc = new JLabel(Resources.strings().get("panel_description"));

    this.textFieldName = new JTextField();
    ControlBehavior.apply(this.textFieldName);
    this.textFieldName.setColumns(10);

    final JLabel lblStaticShadows = new JLabel(Resources.strings().get("panel_staticshadows"));
    lblStaticShadows.setFont(Style.getDefaultFont());

    final JLabel lblGravity = new JLabel(Resources.strings().get("panel_gravity"));

    this.spinnerGravity = new JSpinner();
    ControlBehavior.apply(this.spinnerGravity);

    final JLabel lblCustomProperties =
        new JLabel(Resources.strings().get("panel_customProperties"));
    lblCustomProperties.setFont(Style.getDefaultFont());
    this.shadowColorComponent = new ColorComponent();
    this.shadowColorComponent.addActionListener(
        a -> this.ambientlightPreview.setStaticShadowColor(this.shadowColorComponent.getColor()));

    JScrollPane scrollPaneDesc = new JScrollPane();

    final GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.LEADING)
                            .addGroup(
                                groupLayout
                                    .createSequentialGroup()
                                    .addComponent(lblGeneral)
                                    .addPreferredGap(
                                        ComponentPlacement.RELATED,
                                        263,
                                        GroupLayout.PREFERRED_SIZE))
                            .addGroup(
                                groupLayout
                                    .createParallelGroup(Alignment.LEADING)
                                    .addGroup(
                                        groupLayout
                                            .createSequentialGroup()
                                            .addComponent(
                                                lblCustomProperties,
                                                GroupLayout.DEFAULT_SIZE,
                                                160,
                                                Short.MAX_VALUE)
                                            .addGap(52)
                                            .addComponent(buttonAdd)
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addComponent(
                                                buttonRemove,
                                                GroupLayout.PREFERRED_SIZE,
                                                41,
                                                GroupLayout.PREFERRED_SIZE))
                                    .addGroup(
                                        groupLayout
                                            .createSequentialGroup()
                                            .addGroup(
                                                groupLayout
                                                    .createParallelGroup(Alignment.TRAILING)
                                                    .addComponent(
                                                        lblGravity,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        lblMapTitle,
                                                        Alignment.LEADING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        lblMapName,
                                                        Alignment.LEADING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        lblAmbientLight,
                                                        Alignment.LEADING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        lblStaticShadows,
                                                        Alignment.LEADING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        lblDesc,
                                                        Alignment.LEADING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        110,
                                                        Short.MAX_VALUE))
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addGroup(
                                                groupLayout
                                                    .createParallelGroup(Alignment.LEADING)
                                                    .addComponent(
                                                        scrollPaneDesc,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        183,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        spinnerGravity,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        183,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        ambientColorComponent,
                                                        Alignment.TRAILING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        shadowColorComponent,
                                                        Alignment.TRAILING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        textFieldTitle,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        183,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        textFieldName,
                                                        Alignment.TRAILING,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        183,
                                                        Short.MAX_VALUE))
                                            .addGap(3))
                                    .addGroup(
                                        groupLayout
                                            .createParallelGroup(Alignment.TRAILING, false)
                                            .addComponent(
                                                scrollPane,
                                                Alignment.LEADING,
                                                0,
                                                0,
                                                Short.MAX_VALUE)
                                            .addComponent(
                                                ambientlightPreview,
                                                Alignment.LEADING,
                                                GroupLayout.PREFERRED_SIZE,
                                                300,
                                                GroupLayout.PREFERRED_SIZE))))
                    .addGap(233)));
    groupLayout.setVerticalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblGeneral)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblMapName, GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                            .addComponent(
                                textFieldName,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addGap(7)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.TRAILING)
                            .addGroup(
                                groupLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        textFieldTitle,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(ComponentPlacement.RELATED))
                            .addGroup(
                                Alignment.LEADING,
                                groupLayout
                                    .createSequentialGroup()
                                    .addComponent(
                                        lblMapTitle,
                                        GroupLayout.PREFERRED_SIZE,
                                        18,
                                        GroupLayout.PREFERRED_SIZE)
                                    .addGap(8)))
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(
                                scrollPaneDesc,
                                GroupLayout.PREFERRED_SIZE,
                                47,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(
                                lblDesc,
                                GroupLayout.PREFERRED_SIZE,
                                16,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(
                                spinnerGravity,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(
                                lblGravity,
                                GroupLayout.PREFERRED_SIZE,
                                17,
                                GroupLayout.PREFERRED_SIZE))
                    .addGap(15)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(
                                ambientColorComponent,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAmbientLight))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(lblStaticShadows)
                            .addComponent(
                                shadowColorComponent,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(
                        ambientlightPreview,
                        GroupLayout.PREFERRED_SIZE,
                        116,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblCustomProperties)
                            .addComponent(buttonRemove)
                            .addComponent(buttonAdd))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(
                        scrollPane, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
                    .addGap(47)));

    this.textFieldDesc = new JEditorPane();
    scrollPaneDesc.setViewportView(textFieldDesc);
    this.ambientlightPreview.setLayout(null);

    this.setLayout(groupLayout);
  }

  public void bind(final IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
  }

  public void saveChanges() {
    if (this.dataSource == null) {
      return;
    }

    this.dataSource.setValue(MapProperty.MAP_DESCRIPTION, this.textFieldDesc.getText());
    this.dataSource.setValue(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setValue(MapProperty.GRAVITY, (int) this.spinnerGravity.getValue());
    this.dataSource.setValue(MapProperty.AMBIENTCOLOR, this.ambientColorComponent.getHexColor());
    this.dataSource.setValue(MapProperty.SHADOWCOLOR, this.shadowColorComponent.getHexColor());
    this.dataSource.setName(this.textFieldName.getText());

    final List<String> setProperties = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      final String name = (String) this.model.getValueAt(row, 0);
      final String value = (String) this.model.getValueAt(row, 1);
      if (name != null && !name.isEmpty()) {
        setProperties.add(name);
        this.dataSource.setValue(name, value);
      }
    }
    this.dataSource
        .getProperties()
        .keySet()
        .removeIf(p -> !setProperties.contains(p) && MapProperty.isCustom(p));
  }

  private void setControlValues(final IMap map) {
    this.textFieldDesc.setText(map.getStringValue(MapProperty.MAP_DESCRIPTION));
    this.textFieldTitle.setText(map.getStringValue(MapProperty.MAP_TITLE));
    this.textFieldName.setText(map.getName());
    if (map.getStringValue(MapProperty.AMBIENTCOLOR) != null) {
      this.ambientColorComponent.setColor(map.getColorValue(MapProperty.AMBIENTCOLOR));
    }
    if (map.getStringValue(MapProperty.SHADOWCOLOR) != null) {
      this.shadowColorComponent.setColor(map.getColorValue(MapProperty.SHADOWCOLOR));
    }

    this.spinnerGravity.setValue(map.getIntValue(MapProperty.GRAVITY));

    for (Map.Entry<String, ICustomProperty> prop : map.getProperties().entrySet()) {
      if (prop.getKey().equals(MapProperty.AMBIENTCOLOR)
          || prop.getKey().equals(MapProperty.GRAVITY)
          || prop.getKey().equals(MapProperty.MAP_DESCRIPTION)
          || prop.getKey().equals(MapProperty.MAP_TITLE)
          || prop.getKey().equals(MapProperty.SHADOWCOLOR)) {
        continue;
      }
      this.model.addRow(new Object[] {prop.getKey(), prop.getValue().getAsString()});
    }
  }
}
