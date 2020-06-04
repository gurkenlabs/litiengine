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
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.PropertyInspector;
import de.gurkenlabs.utiliti.handlers.Transform;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
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
  private JPanel infoPanel;

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

    ControlBehavior.apply(this.textFieldName);

    this.spinnerX = new JSpinner();
    this.spinnerY = new JSpinner();
    this.spinnerWidth = new JSpinner();
    this.spinnerHeight = new JSpinner();

    this.updateSpinnerModels();

    this.tagPanel = new TagPanel();

    this.tabbedPanel = new JTabbedPane(SwingConstants.TOP);
    this.tabbedPanel.setFont(Style.getHeaderFont());

    this.infoPanel = new JPanel();

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tabbedPanel, Alignment.LEADING, PANEL_WIDTH, PANEL_WIDTH, PANEL_WIDTH).addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false).addComponent(lblX, Alignment.LEADING, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE).addComponent(lblWidth, Alignment.LEADING, LABEL_WIDTH, LABEL_WIDTH, PANEL_WIDTH)
                        .addComponent(lblName, Alignment.LEADING, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE).addComponent(lblTags, Alignment.LEADING, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(tagPanel, CONTROL_MIN_WIDTH, CONTROL_WIDTH, PANEL_WIDTH).addComponent(textFieldName, CONTROL_MIN_WIDTH, CONTROL_WIDTH, PANEL_WIDTH).addGap(0)
                        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerWidth, CONTROL_MIN_WIDTH, CONTROL_WIDTH, CONTROL_WIDTH).addComponent(spinnerX, Alignment.TRAILING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, CONTROL_WIDTH))
                            .addPreferredGap(ComponentPlacement.UNRELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblHeight, LABEL_WIDTH, LABEL_WIDTH, LABEL_WIDTH).addComponent(lblYcoordinate, LABEL_WIDTH, LABEL_WIDTH, LABEL_WIDTH)).addGap(0)
                            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(spinnerY, Alignment.LEADING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, CONTROL_WIDTH).addComponent(spinnerHeight, Alignment.LEADING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, CONTROL_WIDTH))))))
                    .addGap(CONTROL_MARGIN))
                .addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN).addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, PANEL_WIDTH, Short.MAX_VALUE).addGap(CONTROL_MARGIN)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(CONTROL_MARGIN).addComponent(infoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblX, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(spinnerX, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(spinnerY, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(5).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tagPanel, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)).addGap(5)
            .addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(CONTROL_MARGIN)));
    this.infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));

    JLabel lblEntityId = new JLabel("ID");
    lblEntityId.setFont(lblEntityId.getFont().deriveFont(Font.BOLD));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont());

    this.lblLayer = new JLabel("");

    this.lblRendering = new JLabel("");
    this.lblRendering.setForeground(Color.LIGHT_GRAY);
    this.lblRendering.setFont(lblRendering.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    this.lblLayer.setHorizontalAlignment(SwingConstants.TRAILING);
    this.lblLayer.setForeground(Color.LIGHT_GRAY);
    this.lblLayer.setFont(this.lblLayer.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    this.infoPanel.add(lblEntityId);
    this.infoPanel.add(Box.createHorizontalStrut(47));
    this.infoPanel.add(labelEntityID);
    this.infoPanel.add(Box.createGlue());
    this.infoPanel.add(lblRendering);
    this.infoPanel.add(Box.createHorizontalStrut(15));
    this.infoPanel.add(lblLayer);

    setLayout(groupLayout);

    this.setupChangedListeners();
    UI.getLayerController().onLayersChanged(map -> this.bind(this.getDataSource()));
  }

  public void updateSpinnerModels() {
    if (Editor.preferences().snapToPixels()) {
      this.updateSpinnerModels(MapObjectInspector::getIntegerModel);
    } else {
      this.updateSpinnerModels(MapObjectInspector::getFloatModel);
    }
  }

  @Override
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
      this.setMapObjectType(t != null ? t : null);
    } else {
      this.setMapObjectType(null);
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
      this.clearPanels();
      return;
    }

    PropertyPanel panel = this.panels.get(type);
    if (this.currentPanel != null) {
      // panel is already selected
      if (panel == this.currentPanel) {
        return;
      }

      // clear current panel
      this.tabbedPanel.remove(this.currentPanel);
    }

    if (panel != null) {
      // add explicit map object panel
      tabbedPanel.addTab(Resources.strings().get(panel.getIdentifier()), panel.getIcon(), panel);
    }

    // add/remove collision panel
    if (currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.collisionPanel.getIdentifier()), this.collisionPanel.getIcon(), this.collisionPanel);
    } else {
      this.tabbedPanel.remove(this.collisionPanel);
    }

    // add/remove combat panel
    if (currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.combatPanel.getIdentifier()), this.combatPanel);
    } else {
      this.tabbedPanel.remove(this.combatPanel);
    }

    // add/remove movement panel
    if (currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(Resources.strings().get(this.movementPanel.getIdentifier()), this.movementPanel.getIcon(), this.movementPanel);
    } else {
      this.tabbedPanel.remove(this.movementPanel);
    }

    // always add custom panel
    tabbedPanel.addTab(Resources.strings().get(this.customPanel.getIdentifier()), this.customPanel.getIcon(), this.customPanel);

    this.currentPanel = panel != null ? panel : this.customPanel;
    this.tabbedPanel.revalidate();
    this.tabbedPanel.repaint();
  }

  private void clearPanels() {
    if (this.currentPanel != null) {
      this.tabbedPanel.remove(this.currentPanel);
      this.currentPanel.bind(null);
      this.currentPanel = null;
    }

    this.tabbedPanel.revalidate();
    this.tabbedPanel.repaint();

    this.tabbedPanel.remove(this.collisionPanel);
    this.tabbedPanel.remove(this.combatPanel);
    this.tabbedPanel.remove(this.movementPanel);
    this.tabbedPanel.remove(this.customPanel);
  }

  @Override
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
    this.type = null;

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

    this.lblLayer.setText("Layer: " + mapObject.getLayer().getName());
    String info = getRendering(mapObject);
    if (info == null) {
      this.lblRendering.setText("");
    } else {
      this.lblRendering.setText("Render: " + getRendering(mapObject));
    }
  }

  private static String getRendering(IMapObject mapObject) {
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
      Transform.updateAnchors();
    }));

    this.spinnerY.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setY(getSpinnerValue(spinnerY));
      Transform.updateAnchors();
    }));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setWidth(getSpinnerValue(spinnerWidth));
      Transform.updateAnchors();
    }));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setHeight(getSpinnerValue(spinnerHeight));
      Transform.updateAnchors();
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

    ControlBehavior.apply(this.spinnerX);
    ControlBehavior.apply(this.spinnerY);
    ControlBehavior.apply(this.spinnerWidth);
    ControlBehavior.apply(this.spinnerHeight);
  }
}
