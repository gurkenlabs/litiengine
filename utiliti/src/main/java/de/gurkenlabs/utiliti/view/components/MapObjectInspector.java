package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.PropertyInspector;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class MapObjectInspector extends PropertyPanel implements PropertyInspector {
  private static final int SECTION_LABEL_WIDTH = 90;

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
  private final JSpinner spnX;
  private final JSpinner spnY;
  private final JSpinner spnW;
  private final JSpinner spnH;

  public MapObjectInspector() {
    super();
    this.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
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

    this.infoPanel = new JPanel(new BorderLayout());
    this.infoPanel.setOpaque(false);

    JPanel headerContent = new JPanel();
    headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.X_AXIS));
    headerContent.setOpaque(false);

    JLabel lblEntityId = new JLabel(Resources.strings().get("panel_ID"));
    lblEntityId.setFont(lblEntityId.getFont().deriveFont(Font.BOLD));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont());

    this.lblLayer = new JLabel("");
    this.lblLayer.setHorizontalAlignment(SwingConstants.TRAILING);
    this.lblLayer.setForeground(Color.LIGHT_GRAY);
    this.lblLayer.setFont(
        this.lblLayer.getFont().deriveFont(Style.getDefaultFont().getSize() * 0.75f));

    headerContent.add(lblEntityId);
    headerContent.add(Box.createHorizontalStrut(4));
    headerContent.add(labelEntityID);
    headerContent.add(Box.createHorizontalGlue());
    headerContent.add(lblLayer);

    JSeparator separator = new JSeparator();
    separator.setForeground(new Color(70, 70, 85));

    this.infoPanel.add(headerContent, BorderLayout.CENTER);
    this.infoPanel.add(separator, BorderLayout.SOUTH);

    this.spnX = new JSpinner(new SpinnerNumberModel(0.0, 0.0, (double) Short.MAX_VALUE, 1.0));
    this.spnY = new JSpinner(new SpinnerNumberModel(0.0, 0.0, (double) Short.MAX_VALUE, 1.0));
    this.spnW = new JSpinner(new SpinnerNumberModel(0.0, 0.0, (double) Short.MAX_VALUE, 1.0));
    this.spnH = new JSpinner(new SpinnerNumberModel(0.0, 0.0, (double) Short.MAX_VALUE, 1.0));

    ControlBehavior.apply(this.spnX);
    ControlBehavior.apply(this.spnY);
    ControlBehavior.apply(this.spnW);
    ControlBehavior.apply(this.spnH);

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
    // Do nothing
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);

    if (mapObject != null) {
      MapObjectType t = MapObjectType.get(mapObject.getType());
      this.setMapObjectType(t);
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
  }

  private LayoutManager createLayout() {
    JPanel entityPanel = createEntityPanel();
    JPanel transformGrid = createTransformGrid();

    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem(infoPanel),
            new LayoutItem(createSectionSeparator("GENERAL")),
            new LayoutItem(entityPanel, GroupLayout.PREFERRED_SIZE),
            new LayoutItem(createSectionSeparator("TRANSFORM")),
            new LayoutItem(transformGrid, GroupLayout.PREFERRED_SIZE),
            new LayoutItem(tabbedPanel, GroupLayout.PREFERRED_SIZE)
        };
    return this.createLayout(layoutItems);
  }

  private Component createSectionSeparator(String label) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    JLabel title = new JLabel(label);
    title.setFont(title.getFont().deriveFont(10f));
    title.setForeground(new Color(160, 160, 180));
    title.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
    panel.add(title, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTransformGrid() {
    JPanel grid = new JPanel();
    grid.setOpaque(false);
    GroupLayout gl = new GroupLayout(grid);
    grid.setLayout(gl);

    JLabel lblX = new JLabel(Resources.strings().get("panel_x"));
    JLabel lblY = new JLabel(Resources.strings().get("panel_y"));
    JLabel lblW = new JLabel(Resources.strings().get("panel_width"));
    JLabel lblH = new JLabel(Resources.strings().get("panel_height"));

    gl.setAutoCreateGaps(false);
    gl.setHorizontalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
          .addComponent(lblX, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
          .addComponent(lblW, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH))
        .addGap(8)
        .addGroup(gl.createParallelGroup()
          .addComponent(spnX, SPINNER_WIDTH, SPINNER_WIDTH, Integer.MAX_VALUE)
          .addComponent(spnW, SPINNER_WIDTH, SPINNER_WIDTH, Integer.MAX_VALUE))
        .addGap(8)
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
          .addComponent(lblY)
          .addComponent(lblH))
        .addGroup(gl.createParallelGroup()
          .addComponent(spnY, SPINNER_WIDTH, SPINNER_WIDTH, Integer.MAX_VALUE)
          .addComponent(spnH, SPINNER_WIDTH, SPINNER_WIDTH, Integer.MAX_VALUE)));
    gl.setVerticalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblX)
          .addComponent(spnX)
          .addComponent(lblY)
          .addComponent(spnY))
        .addGap(6)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblW)
          .addComponent(spnW)
          .addComponent(lblH)
          .addComponent(spnH)));
    return grid;
  }

  private JPanel createEntityPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);

    JLabel lblName = new JLabel(Resources.strings().get("panel_name"));
    JLabel lblRenderType = new JLabel(Resources.strings().get("panel_rendertype"));
    JLabel lblTags = new JLabel(Resources.strings().get("panel_tags"));

    gl.setAutoCreateGaps(false);
    gl.setHorizontalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
          .addComponent(lblName, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
          .addComponent(lblRenderType, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
          .addComponent(lblTags, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH))
        .addGap(8)
        .addGroup(gl.createParallelGroup()
          .addComponent(textFieldName, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
          .addComponent(renderType, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
          .addComponent(tagPanel, 0, CONTROL_WIDTH, Integer.MAX_VALUE)));
    gl.setVerticalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblName)
          .addComponent(textFieldName))
        .addGap(6)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblRenderType)
          .addComponent(renderType))
        .addGap(6)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblTags)
          .addComponent(tagPanel)));
    return panel;
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
    this.spnX.setValue(0.0);
    this.spnY.setValue(0.0);
    this.spnW.setValue(0.0);
    this.spnH.setValue(0.0);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }
    this.type = MapObjectType.get(mapObject.getType());
    this.textFieldName.setText(mapObject.getName());
    this.spnX.setValue((double) mapObject.getX());
    this.spnY.setValue((double) mapObject.getY());
    this.spnW.setValue((double) mapObject.getWidth());
    this.spnH.setValue((double) mapObject.getHeight());
    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS, null));

    this.labelEntityID.setText(Integer.toString(mapObject.getId()));
    this.lblLayer.setText("Layer: " + mapObject.getLayer());

    RenderType rt = mapObject.getEnumValue(MapObjectProperty.RENDERTYPE, RenderType.class, RenderType.NORMAL);
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

    this.spnX.addChangeListener(e -> {
      if (getDataSource() == null) { return; }
      double val = (double) spnX.getValue();
      if (getDataSource().getX() != val) {
        UndoManager.instance().mapObjectChanging(getDataSource());
        getDataSource().setX((float) val);
        Transform.updateAnchors();
        UndoManager.instance().mapObjectChanged(getDataSource());
        updateEnvironment();
      }
    });
    this.spnY.addChangeListener(e -> {
      if (getDataSource() == null) { return; }
      double val = (double) spnY.getValue();
      if (getDataSource().getY() != val) {
        UndoManager.instance().mapObjectChanging(getDataSource());
        getDataSource().setY((float) val);
        Transform.updateAnchors();
        UndoManager.instance().mapObjectChanged(getDataSource());
        updateEnvironment();
      }
    });
    this.spnW.addChangeListener(e -> {
      if (getDataSource() == null) { return; }
      double val = (double) spnW.getValue();
      if (getDataSource().getWidth() != val) {
        UndoManager.instance().mapObjectChanging(getDataSource());
        getDataSource().setWidth((float) val);
        Transform.updateAnchors();
        UndoManager.instance().mapObjectChanged(getDataSource());
        updateEnvironment();
      }
    });
    this.spnH.addChangeListener(e -> {
      if (getDataSource() == null) { return; }
      double val = (double) spnH.getValue();
      if (getDataSource().getHeight() != val) {
        UndoManager.instance().mapObjectChanging(getDataSource());
        getDataSource().setHeight((float) val);
        Transform.updateAnchors();
        UndoManager.instance().mapObjectChanged(getDataSource());
        updateEnvironment();
      }
    });

    this.textFieldName.addFocusListener(
        new MapObjectPropertyFocusListener(m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(
        new MapObjectPropertyActionListener(
            m -> m.getName() == null || !m.getName().equals(textFieldName.getText()),
            m -> m.setName(textFieldName.getText())));

    this.tagPanel.addActionListener(
        new MapObjectPropertyActionListener(
            m -> !m.hasCustomProperty(MapObjectProperty.TAGS) || !m.getStringValue(MapObjectProperty.TAGS, null).equals(this.tagPanel.getTagsString()),
            m -> m.setValue(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));
  }
}
