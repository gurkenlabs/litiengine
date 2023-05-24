package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.PropertyInspector;
import de.gurkenlabs.utiliti.handlers.Transform;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyActionListener;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyFocusListener;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
import de.gurkenlabs.utiliti.swing.TagPanel;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.panels.emission.EmitterPanel;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class MapObjectInspector extends PropertyPanel implements PropertyInspector {

  private final Map<MapObjectType, PropertyPanel> panels;
  private MapObjectType type;
  private PropertyPanel currentPanel;
  private final JTabbedPane tabbedPanel;
  private final CollisionPanel collisionPanel;
  private final CombatPanel combatPanel;
  private final MovementPanel movementPanel;
  private final CustomPanel customPanel;
  private final JTextField textFieldName;
  private final JComboBox<RenderType> renderType;

  private final JLabel labelEntityID;
  private final TagPanel tagPanel;
  private final JLabel lblLayer;
  private final JPanel infoPanel;
  private final DualSpinner transform;
  private final DualSpinner scale;

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
    this.panels.put(MapObjectType.SOUNDSOURCE, new SoundPanel());
    this.panels.put(MapObjectType.CREATURE, new CreaturePanel());
    this.collisionPanel = new CollisionPanel();
    this.combatPanel = new CombatPanel();
    this.movementPanel = new MovementPanel();
    this.customPanel = new CustomPanel();

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    ControlBehavior.apply(this.textFieldName);

    this.renderType = new JComboBox<>(RenderType.values());
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
    this.lblLayer.setFont(
      this.lblLayer.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    this.infoPanel.add(lblEntityId);
    this.infoPanel.add(Box.createHorizontalStrut(47));
    this.infoPanel.add(labelEntityID);
    this.infoPanel.add(Box.createGlue());
    this.infoPanel.add(lblLayer);
    SpinnerNumberModel xModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
    SpinnerNumberModel yModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
    this.transform =
      new DualSpinner(xModel, yModel,
        Resources.strings().get("panel_x"),
        Resources.strings().get("panel_y"));
    SpinnerNumberModel widthModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
    SpinnerNumberModel heightModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
    this.scale =
      new DualSpinner(widthModel, heightModel,
        Resources.strings().get("panel_width"),
        Resources.strings().get("panel_height"));

    setLayout(createLayout());
    this.setupChangedListeners();
    UI.getLayerController().onLayersChanged(map -> this.bind(this.getMapObject()));
  }

  @Override
  public MapObjectType getObjectType() {
    return this.type;
  }

  @Override
  public void refresh() {
    // Do nothing
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    this.transform.bind(mapObject);
    this.scale.bind(mapObject);
    if (mapObject != null) {
      MapObjectType t = MapObjectType.get(mapObject.getType());
      this.setMapObjectType(t);
    } else {
      this.setMapObjectType(null);
    }

    if (this.currentPanel != null) {
      this.currentPanel.bind(this.getMapObject());
    }

    if (this.collisionPanel != null) {
      this.collisionPanel.bind(this.getMapObject());
    }
    if (this.combatPanel != null) {
      this.combatPanel.bind(this.getMapObject());
    }
    if (this.movementPanel != null) {
      this.movementPanel.bind(this.getMapObject());
    }

    this.customPanel.bind(this.getMapObject());
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem(infoPanel),
        new LayoutItem("panel_rendertype", renderType),
        new LayoutItem("panel_transform", transform),
        new LayoutItem("panel_scale", scale),
        new LayoutItem("panel_name", textFieldName),
        new LayoutItem("panel_tags", tagPanel),
        new LayoutItem(tabbedPanel, GroupLayout.PREFERRED_SIZE)
      };
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
      tabbedPanel.addTab(
        Resources.strings().get(this.collisionPanel.getIdentifier()),
        this.collisionPanel.getIcon(),
        this.collisionPanel);
    } else {
      this.tabbedPanel.remove(this.collisionPanel);
    }

    // add/remove combat panel
    if (currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(
        Resources.strings().get(this.combatPanel.getIdentifier()), this.combatPanel);
    } else {
      this.tabbedPanel.remove(this.combatPanel);
    }

    // add/remove movement panel
    if (currentType == MapObjectType.CREATURE) {
      tabbedPanel.addTab(
        Resources.strings().get(this.movementPanel.getIdentifier()),
        this.movementPanel.getIcon(),
        this.movementPanel);
    } else {
      this.tabbedPanel.remove(this.movementPanel);
    }

    // always add custom panel
    tabbedPanel.addTab(
      Resources.strings().get(this.customPanel.getIdentifier()),
      this.customPanel.getIcon(),
      this.customPanel);

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
    this.textFieldName.setText("");
    this.labelEntityID.setText("####");
    this.lblLayer.setText("");
    this.renderType.setSelectedIndex(0);
    this.renderType.setEnabled(false);
    this.tagPanel.clear();
    this.transform.clearControls();
    this.scale.clearControls();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }
    this.type = MapObjectType.get(mapObject.getType());
    this.textFieldName.setText(mapObject.getName());
    this.transform.setValues(mapObject.getX(), mapObject.getY());
    this.scale.setValues(mapObject.getWidth(), mapObject.getHeight());
    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS));

    this.labelEntityID.setText(Integer.toString(mapObject.getId()));
    this.lblLayer.setText("Layer: " + mapObject.getLayer());

    RenderType rt = mapObject.getEnumValue(MapObjectProperty.RENDERTYPE, RenderType.class);
    boolean showRenderTypeControls =
      MapObjectType.get(mapObject.getType()) == MapObjectType.CREATURE
        || MapObjectType.get(mapObject.getType()) == MapObjectType.EMITTER
        || MapObjectType.get(mapObject.getType()) == MapObjectType.PROP;
    this.renderType.setEnabled(showRenderTypeControls);

    if (rt != null) {
      this.renderType.setSelectedItem(rt);
    }
  }

  private void setupChangedListeners() {
    setup(renderType, MapObjectProperty.RENDERTYPE);

    this.transform.addSpinnerListeners(
      m -> m.getX() != ((SpinnerNumberModel) transform.getSpinner1().getModel()).getNumber()
        .floatValue(),
      m -> m.getY() != ((SpinnerNumberModel) transform.getSpinner2().getModel()).getNumber()
        .floatValue(),
      m -> {
        m.setX(((SpinnerNumberModel) transform.getSpinner1().getModel()).getNumber().floatValue());
        Transform.updateAnchors();
      },
      m -> {
        m.setY(((SpinnerNumberModel) transform.getSpinner2().getModel()).getNumber().floatValue());
        Transform.updateAnchors();
      });

    this.scale.addSpinnerListeners(
      m -> m.getWidth() != ((SpinnerNumberModel) scale.getSpinner1().getModel()).getNumber()
        .floatValue(),
      m -> m.getHeight() != ((SpinnerNumberModel) scale.getSpinner2().getModel()).getNumber()
        .floatValue(),
      m -> {
        m.setWidth(((SpinnerNumberModel) scale.getSpinner1().getModel()).getNumber().floatValue());
        Transform.updateAnchors();
      },
      m -> {
        m.setHeight(((SpinnerNumberModel) scale.getSpinner2().getModel()).getNumber().floatValue());
        Transform.updateAnchors();
      });

    this.textFieldName.addFocusListener(
      new MapObjectPropertyFocusListener(getMapObject(), m -> true,
        m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(
      new MapObjectPropertyActionListener(getMapObject(),
        m -> m.getName() == null || !m.getName().equals(textFieldName.getText()),
        m -> m.setName(textFieldName.getText())));

    this.tagPanel.addActionListener(
      new MapObjectPropertyActionListener(getMapObject(),
        m -> !m.hasCustomProperty(MapObjectProperty.TAGS) || !m.getStringValue(
          MapObjectProperty.TAGS).equals(this.tagPanel.getTagsString()),
        m -> m.setValue(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));
  }
}
