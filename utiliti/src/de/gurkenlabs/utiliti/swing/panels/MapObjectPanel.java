package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.TagPanel;

@SuppressWarnings("serial")
public class MapObjectPanel extends PropertyPanel {
  private final Map<MapObjectType, PropertyPanel> panels;
  private PropertyPanel currentPanel;
  private JTabbedPane tabbedPanel;
  private final CollisionPanel collisionPanel;
  private final CombatPanel combatPanel;
  private final MovementPanel movementPanel;
  private final CustomPanel customPanel;
  private final JTextField textFieldName;
  private final JComboBox<MapObjectType> comboBoxType;
  private final JSpinner spinnerY;
  private final JSpinner spinnerX;
  private final JSpinner spinnerWidth;
  private final JSpinner spinnerHeight;
  private final JLabel labelEntityID;
  private TagPanel tagPanel;
  private JLabel lblTags;

  public MapObjectPanel() {
    this.panels = new ConcurrentHashMap<>();
    this.panels.put(MapObjectType.PROP, new PropPanel());
    this.panels.put(MapObjectType.COLLISIONBOX, new CollisionBoxPanel());
    this.panels.put(MapObjectType.STATICSHADOW, new StaticShadowPanel());
    this.panels.put(MapObjectType.TRIGGER, new TriggerPanel());
    this.panels.put(MapObjectType.LIGHTSOURCE, new LightSourcePanel());
    this.panels.put(MapObjectType.SPAWNPOINT, new SpawnpointPanel());
    this.panels.put(MapObjectType.EMITTER, new EmitterPanel());
    this.panels.put(MapObjectType.CREATURE, new CreaturePanel());
    this.collisionPanel = new CollisionPanel();
    this.combatPanel = new CombatPanel();
    this.movementPanel = new MovementPanel();
    this.customPanel = new CustomPanel();

    setMinimumSize(new Dimension(250, 500));

    JLabel lblX = new JLabel(Resources.strings().get("panel_x"));
    JLabel lblYcoordinate = new JLabel(Resources.strings().get("panel_y"));
    JLabel lblWidth = new JLabel(Resources.strings().get("panel_width"));
    JLabel lblHeight = new JLabel(Resources.strings().get("panel_height"));
    JLabel lblName = new JLabel(Resources.strings().get("panel_name"));

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    JLabel lblType = new JLabel(Resources.strings().get("panel_type"));
    this.comboBoxType = new JComboBox<>();
    this.comboBoxType.setModel(new DefaultComboBoxModel<MapObjectType>(MapObjectType.values()));

    this.spinnerX = new JSpinner();
    this.spinnerY = new JSpinner();
    this.spinnerWidth = new JSpinner();
    this.spinnerHeight = new JSpinner();

    this.updateSpinnerModels();

    JLabel lblNewLabel = new JLabel("ID");
    lblNewLabel.setFont(lblNewLabel.getFont().deriveFont(Font.BOLD).deriveFont(12f));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont().deriveFont(12f));

    this.tagPanel = new TagPanel();

    lblTags = new JLabel("tags");

    tabbedPanel = new JTabbedPane(JTabbedPane.TOP);
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(
                    groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN)
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tabbedPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                            .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblX, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblType, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(
                                    groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tagPanel, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE).addComponent(labelEntityID)
                                        .addGroup(groupLayout.createSequentialGroup()
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerWidth, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE).addComponent(spinnerX, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                                            .addPreferredGap(ComponentPlacement.UNRELATED)
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)).addGap(0)
                                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerY, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE).addComponent(spinnerHeight, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)))
                                        .addComponent(comboBoxType, 0, 372, Short.MAX_VALUE).addComponent(textFieldName, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))))
                        .addGap(CONTROL_MARGIN)));

    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(labelEntityID, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE)).addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblX, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(spinnerX, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(spinnerY, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN).addComponent(tagPanel, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE)))
            .addGap(CONTROL_MARGIN).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(comboBoxType, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblType, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap()));
    setLayout(groupLayout);

    this.setupChangedListeners();
    this.comboBoxType.setSelectedItem(MapObjectType.AREA);
  }

  public void updateSpinnerModels() {
    if (Program.getUserPreferences().isSnapPixels()) {
      this.updateSpinnerModels(MapObjectPanel::getIntegerModel);
    } else {
      this.updateSpinnerModels(MapObjectPanel::getFloatModel);
    }
  }

  public MapObjectType getObjectType() {
    return (MapObjectType) this.comboBoxType.getSelectedItem();
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    this.isFocussing = true;
    if (this.currentPanel != null) {
      this.currentPanel.bind(this.getDataSource());
    }

    if (this.collisionPanel != null) {
      this.collisionPanel.bind(this.getDataSource());
    }
    if (this.combatPanel != null) {
      this.combatPanel.bind(this.getDataSource());
    }
    if (this.movementPanel != null) {
      this.movementPanel.bind(this.getDataSource());
    }

    this.customPanel.bind(this.getDataSource());
    this.isFocussing = false;
  }

  private void switchPanel(MapObjectType type) {
    if (type == null) {
      return;
    }
    PropertyPanel panel = this.panels.get(type);
    if (panel == null) {
      if (this.currentPanel != null) {
        this.tabbedPanel.remove(this.currentPanel);
        this.currentPanel.bind(null);
        this.currentPanel = null;
        this.tabbedPanel.revalidate();
        this.tabbedPanel.repaint();
      }
      this.tabbedPanel.remove(this.collisionPanel);
      this.tabbedPanel.remove(this.combatPanel);
      this.tabbedPanel.remove(this.movementPanel);
      this.tabbedPanel.remove(this.customPanel);

      return;
    }

    if (panel == this.currentPanel) {
      return;
    }

    if (this.currentPanel != null) {
      this.currentPanel.bind(null);
      this.tabbedPanel.remove(this.currentPanel);
    }
    tabbedPanel.addTab(Resources.strings().get(panel.getIdentifier()), panel.getIcon(), panel);

    if (type == MapObjectType.PROP || type == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.collisionPanel.getIdentifier()), this.collisionPanel.getIcon(), this.collisionPanel);
    } else {
      this.tabbedPanel.remove(this.collisionPanel);
    }

    if (type == MapObjectType.PROP || type == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.combatPanel.getIdentifier()), this.combatPanel);
    } else {
      this.tabbedPanel.remove(this.combatPanel);
    }

    if (type == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.movementPanel.getIdentifier()), this.movementPanel.getIcon(), this.movementPanel);
    } else {
      this.tabbedPanel.remove(this.movementPanel);
    }

    tabbedPanel.addTab(Resources.strings().get(this.customPanel.getIdentifier()), this.customPanel.getIcon(), this.customPanel);

    this.currentPanel = panel;
    this.currentPanel.bind(this.getDataSource());
    this.tabbedPanel.revalidate();
    this.tabbedPanel.repaint();
  }

  public void setMapObjectType(MapObjectType type) {
    this.comboBoxType.setSelectedItem(type);
  }

  @Override
  protected void clearControls() {
    // clear controls
    this.spinnerX.setValue(0);
    this.spinnerY.setValue(0);
    this.spinnerWidth.setValue(0);
    this.spinnerHeight.setValue(0);
    if (this.getDataSource() == null) {
      this.comboBoxType.setSelectedItem(MapObjectType.AREA);
    }
    this.textFieldName.setText("");
    this.labelEntityID.setText("####");
    this.comboBoxType.setEnabled(true);
    this.tagPanel.clear();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.spinnerX.setValue(mapObject.getLocation().getX());
    this.spinnerY.setValue(mapObject.getLocation().getY());
    this.spinnerWidth.setValue(mapObject.getWidth());
    this.spinnerHeight.setValue(mapObject.getHeight());

    MapObjectType type = MapObjectType.get(mapObject.getType());
    this.comboBoxType.setSelectedItem(type);
    this.textFieldName.setText(mapObject.getName());
    this.labelEntityID.setText(Integer.toString(mapObject.getId()));
    this.comboBoxType.setEnabled(false);

    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS));
  }

  private void setupChangedListeners() {
    comboBoxType.addItemListener(new MapObjectPropertyItemListener(m -> {
      MapObjectType type = (MapObjectType) comboBoxType.getSelectedItem();
      m.setType(type.toString());
    }));

    comboBoxType.addItemListener(e -> {
      MapObjectType type = (MapObjectType) comboBoxType.getSelectedItem();
      switchPanel(type);
    });

    this.textFieldName.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(new MapObjectPropertyActionListener(m -> m.setName(textFieldName.getText())));

    // TODO: wrap the value changing with the spin controls into one undo
    // operation.
    this.spinnerX.addChangeListener(new MapObjectPropertyChangeListener(m -> {

      m.setX(getSpinnerValue(spinnerX));
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerY.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setY(getSpinnerValue(spinnerY));
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setWidth(getSpinnerValue(spinnerWidth));
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setHeight(getSpinnerValue(spinnerHeight));
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.tagPanel.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));
  }

  private static float getSpinnerValue(JSpinner spinner) {
    if (spinner.getValue() instanceof Integer) {
      return (int) spinner.getValue();
    } else {
      return (float) (double) spinner.getValue();
    }
  }

  private static SpinnerNumberModel getFloatModel() {
    return new SpinnerNumberModel(0f, 0f, 10000f, 0.01f);
  }

  private static SpinnerNumberModel getIntegerModel() {
    return new SpinnerNumberModel(0, 0, 10000, 1);
  }

  private void updateSpinnerModels(Supplier<SpinnerNumberModel> supp) {
    this.spinnerX.setModel(supp.get());
    this.spinnerY.setModel(supp.get());
    this.spinnerWidth.setModel(supp.get());
    this.spinnerHeight.setModel(supp.get());
  }
}
