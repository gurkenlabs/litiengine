package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Style;
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
  private JLabel lblRenderType;
  private JComboBox<RenderType> renderType;

  private final JLabel labelEntityID;
  private TagPanel tagPanel;
  private JLabel lblLayer;
  private JPanel infoPanel;

  private JPanel transformPanel, scalePanel;
  private JLabel lblX, lblY, lblWidth, lblHeight;
  private JSpinner x, y, width, height;

  public MapObjectInspector() {
    super();
    this.setBorder(STANDARDBORDER);
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

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    ControlBehavior.apply(this.textFieldName);

    this.lblRenderType = new JLabel(Resources.strings().get("panel_rendertype"));
    this.lblRenderType.setHorizontalAlignment(SwingConstants.LEADING);
    this.lblRenderType.setForeground(Color.LIGHT_GRAY);
    this.lblRenderType.setFont(this.lblRenderType.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    this.renderType = new JComboBox<RenderType>(RenderType.values());
    this.renderType.setMinimumSize(SMALL_CONTROL_SIZE);

    this.tagPanel = new TagPanel();

    this.tabbedPanel = new JTabbedPane(SwingConstants.TOP);
    this.tabbedPanel.setFont(Style.getHeaderFont());

    this.infoPanel = new JPanel();
    this.infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));

    JLabel lblEntityId = new JLabel(Resources.strings().get("panel_ID"));
    lblEntityId.setFont(lblEntityId.getFont().deriveFont(Font.BOLD));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont());

    this.lblLayer = new JLabel("");
    this.lblLayer.setHorizontalAlignment(SwingConstants.TRAILING);
    this.lblLayer.setForeground(Color.LIGHT_GRAY);
    this.lblLayer.setFont(this.lblLayer.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    this.infoPanel.add(lblEntityId);
    this.infoPanel.add(Box.createHorizontalStrut(47));
    this.infoPanel.add(labelEntityID);
    this.infoPanel.add(Box.createGlue());
    this.infoPanel.add(lblLayer);

    this.initSpinners();

    setLayout(createLayout());
    this.setupChangedListeners();
    UI.getLayerController().onLayersChanged(map -> this.bind(this.getDataSource()));
  }

  @Override
  public MapObjectType getObjectType() {
    return this.type;
  }

  @Override
  public void refresh() {
    // TODO Auto-generated method stub

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

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem(infoPanel), new LayoutItem("panel_rendertype", renderType), new LayoutItem("panel_transform", transformPanel), new LayoutItem("panel_scale", scalePanel), new LayoutItem("panel_name", textFieldName),
        new LayoutItem("panel_tags", tagPanel), new LayoutItem(tabbedPanel, GroupLayout.PREFERRED_SIZE) };
    return this.createLayout(layoutItems);
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
    this.type = null;
    this.x.setValue(0);
    this.y.setValue(0);
    this.width.setValue(0);
    this.height.setValue(0);
    this.textFieldName.setText("");
    this.labelEntityID.setText("####");
    this.lblLayer.setText("");
    this.renderType.setSelectedIndex(0);
    this.renderType.setVisible(false);
    this.tagPanel.clear();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {

    this.type = MapObjectType.get(mapObject.getType());
    this.textFieldName.setText(mapObject.getName());
    this.x.setValue(mapObject.getX());
    this.y.setValue(mapObject.getY());
    this.width.setValue(mapObject.getWidth());
    this.height.setValue(mapObject.getHeight());
    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS));

    this.labelEntityID.setText(Integer.toString(mapObject.getId()));

    this.lblLayer.setText("Layer: " + mapObject.getLayer().getName());

    RenderType rt = mapObject.getEnumValue(MapObjectProperty.RENDERTYPE, RenderType.class);
    boolean showRenderTypeControls = Game.world().environment().get(mapObject.getId()).getRenderType() != RenderType.NONE;
    this.lblRenderType.setVisible(showRenderTypeControls);
    this.renderType.setVisible(showRenderTypeControls);

    if (rt != null) {
      this.renderType.setSelectedItem(rt);
    }
  }

  private void setupChangedListeners() {
    setup(renderType, MapObjectProperty.RENDERTYPE);
    this.x.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setX(getSpinnerValue(x));
      Transform.updateAnchors();
    }));
    this.width.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setWidth(getSpinnerValue(width));
      Transform.updateAnchors();
    }));
    this.height.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setHeight(getSpinnerValue(height));
      Transform.updateAnchors();
    }));
    this.y.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setY(getSpinnerValue(y));
      Transform.updateAnchors();
    }));
    this.textFieldName.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(new MapObjectPropertyActionListener(m -> m.setName(textFieldName.getText())));

    this.tagPanel.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));
  }

  private void initSpinners() {
    x = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE, STEP_ONE));
    x.setMinimumSize(SPINNER_SIZE);
    lblX = new JLabel(Resources.strings().get("panel_x"));
    lblX.setMinimumSize(LABEL_SIZE);
    y = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE, STEP_ONE));
    y.setMinimumSize(SPINNER_SIZE);
    lblY = new JLabel(Resources.strings().get("panel_y"));
    lblY.setMinimumSize(LABEL_SIZE);

    transformPanel = new JPanel();
    GroupLayout grplayoutTransform = new GroupLayout(transformPanel);
    grplayoutTransform.setAutoCreateGaps(true);
    grplayoutTransform.setHorizontalGroup(grplayoutTransform.createSequentialGroup().addContainerGap().addComponent(lblX).addComponent(x).addComponent(lblY).addComponent(y).addContainerGap());
    grplayoutTransform.setVerticalGroup(grplayoutTransform.createParallelGroup().addComponent(lblX, GroupLayout.Alignment.CENTER).addComponent(x).addComponent(lblY, GroupLayout.Alignment.CENTER).addComponent(y));
    transformPanel.setLayout(grplayoutTransform);

    width = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE, STEP_ONE));
    width.setMinimumSize(SPINNER_SIZE);
    lblWidth = new JLabel(Resources.strings().get("panel_width"));
    lblWidth.setMinimumSize(LABEL_SIZE);
    height = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE, STEP_ONE));
    height.setMinimumSize(SPINNER_SIZE);
    lblHeight = new JLabel(Resources.strings().get("panel_height"));
    lblHeight.setMinimumSize(LABEL_SIZE);

    scalePanel = new JPanel();
    GroupLayout grplayoutScale = new GroupLayout(scalePanel);
    grplayoutScale.setAutoCreateGaps(true);
    grplayoutScale.setHorizontalGroup(grplayoutScale.createSequentialGroup().addContainerGap().addComponent(lblWidth).addComponent(width).addComponent(lblHeight).addComponent(height).addContainerGap());
    grplayoutScale.setVerticalGroup(grplayoutScale.createParallelGroup().addComponent(lblWidth, GroupLayout.Alignment.CENTER).addComponent(width).addComponent(lblHeight, GroupLayout.Alignment.CENTER).addComponent(height));
    scalePanel.setLayout(grplayoutScale);
  }

  private static float getSpinnerValue(JSpinner spinner) {
    if (spinner.getValue() instanceof Integer) {
      return ((Integer) spinner.getValue()).floatValue();
    } else if (spinner.getValue() instanceof Double) {
      return ((Double) spinner.getValue()).floatValue();
    } else {
      return (float) spinner.getValue();
    }
  }
}
