package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.PropertyInspector;
import de.gurkenlabs.utiliti.swing.TagPanel;
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public class MapObjectInspector extends PropertyPanel implements PropertyInspector {
  private final Map<MapObjectType, PropertyPanel> panels;
  private MapObjectType type;
  private PropertyPanel currentPanel;
  private JTabbedPane tabbedPanel;
  private final CollisionPanel collisionPanel;
  private final CombatPanel combatPanel;
  private final MovementPanel movementPanel;
  private final CustomPanel customPanel;
  private final JTextField textFieldName;
  private final JSpinner spinnerY;
  private final JSpinner spinnerX;
  private final JSpinner spinnerWidth;
  private final JSpinner spinnerHeight;
  private final JLabel labelEntityID;
  private TagPanel tagPanel;
  private JLabel lblLayer;
  private JLabel lblRendering;
  private JPanel panel_1;

  public MapObjectInspector() {
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
    JLabel lblTags = new JLabel(Resources.strings().get("panel_tags"));

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    this.spinnerX = new JSpinner();
    this.spinnerY = new JSpinner();
    this.spinnerWidth = new JSpinner();
    this.spinnerHeight = new JSpinner();

    this.updateSpinnerModels();

    this.tagPanel = new TagPanel();

    this.tabbedPanel = new JTabbedPane(JTabbedPane.TOP);

    panel_1 = new JPanel();

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup().addGap(5)
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tabbedPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE).addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false).addComponent(lblX, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE).addComponent(lblWidth, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                            .addComponent(lblName, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE).addComponent(lblTags, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tagPanel, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE).addComponent(textFieldName, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                            .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerWidth, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE).addComponent(spinnerX, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)).addGap(0)
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(spinnerY, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE).addComponent(spinnerHeight, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))))))
                    .addGap(5))
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGap(5).addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE).addGap(5)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(5).addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblX, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(spinnerX, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(spinnerY, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
            .addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
            .addGap(5).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)).addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tagPanel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)).addGap(5)
            .addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(108)));
    panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

    JLabel lblEntityId = new JLabel("ID");
    lblEntityId.setFont(lblEntityId.getFont().deriveFont(Font.BOLD).deriveFont(12f));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont().deriveFont(12f));

    this.lblLayer = new JLabel("");

    this.lblRendering = new JLabel("");
    this.lblRendering.setForeground(Color.GRAY);
    this.lblRendering.setFont(lblRendering.getFont().deriveFont(10f));

    this.lblLayer.setHorizontalAlignment(SwingConstants.TRAILING);
    this.lblLayer.setForeground(Color.GRAY);
    this.lblLayer.setFont(this.lblLayer.getFont().deriveFont(10f));

    panel_1.add(lblEntityId);
    panel_1.add(Box.createHorizontalStrut(47));
    panel_1.add(labelEntityID);
    panel_1.add(Box.createGlue());
    panel_1.add(lblRendering);
    panel_1.add(Box.createHorizontalStrut(15));
    panel_1.add(lblLayer);

    setLayout(groupLayout);

    this.setupChangedListeners();
    UI.getLayerController().onLayersChanged(map -> this.bind(this.getDataSource()));
  }

  public void updateSpinnerModels() {
    if (Program.preferences().snapToPixels()) {
      this.updateSpinnerModels(MapObjectInspector::getIntegerModel);
    } else {
      this.updateSpinnerModels(MapObjectInspector::getFloatModel);
    }
  }

  public MapObjectType getObjectType() {
    return this.type;
  }

  @Override
  public void refresh() {
    this.updateSpinnerModels();
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    this.isFocussing = true;

    if (mapObject != null) {
      MapObjectType t = MapObjectType.get(mapObject.getType());
      this.setMapObjectType(t != null ? t : MapObjectType.AREA);
    } else {
      this.setMapObjectType(MapObjectType.AREA);
    }

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

  private void switchPanel() {
    final MapObjectType currentType = this.getObjectType();
    if (currentType == null) {
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

    if (currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.collisionPanel.getIdentifier()), this.collisionPanel.getIcon(), this.collisionPanel);
    } else {
      this.tabbedPanel.remove(this.collisionPanel);
    }

    if (currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.combatPanel.getIdentifier()), this.combatPanel);
    } else {
      this.tabbedPanel.remove(this.combatPanel);
    }

    if (currentType == MapObjectType.CREATURE) {
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
    this.type = type;
    switchPanel();
  }

  @Override
  protected void clearControls() {
    // clear controls
    this.spinnerX.setValue(0);
    this.spinnerY.setValue(0);
    this.spinnerWidth.setValue(0);
    this.spinnerHeight.setValue(0);
    this.type = MapObjectType.AREA;

    this.textFieldName.setText("");
    this.labelEntityID.setText("####");
    this.lblLayer.setText("");
    this.lblRendering.setText("");
    this.tagPanel.clear();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.spinnerX.setValue(mapObject.getLocation().getX());
    this.spinnerY.setValue(mapObject.getLocation().getY());
    this.spinnerWidth.setValue(mapObject.getWidth());
    this.spinnerHeight.setValue(mapObject.getHeight());

    this.type = MapObjectType.get(mapObject.getType());
    this.textFieldName.setText(mapObject.getName());

    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS));

    this.labelEntityID.setText(Integer.toString(mapObject.getId()));

    this.lblLayer.setText("layer: " + mapObject.getLayer().getName());
    String info = this.getRendering(mapObject);
    if (info == null) {
      this.lblRendering.setText("");
    } else {
      this.lblRendering.setText("render: " + this.getRendering(mapObject));
    }
  }

  private String getRendering(IMapObject mapObject) {
    switch (MapObjectType.get(mapObject.getType())) {
    case PROP:
    case EMITTER:
    case CREATURE:
      RenderType renderType = mapObject.getEnumValue(MapObjectProperty.RENDERTYPE, RenderType.class, RenderType.NORMAL);
      if (mapObject.getLayer() != null && mapObject.getBoolValue(MapObjectProperty.RENDERWITHLAYER)) {
        return "layer (" + mapObject.getLayer().getRenderType() + ")";
      }
      return renderType.toString();
    default:
      return null;
    }
  }

  private void setupChangedListeners() {

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
