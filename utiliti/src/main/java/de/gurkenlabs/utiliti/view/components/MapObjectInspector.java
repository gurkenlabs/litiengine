package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.PropertyInspector;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class MapObjectInspector extends PropertyPanel implements PropertyInspector {
  private static final int SECTION_LABEL_WIDTH = PropertyPanel.LABEL_WIDTH;
  private static final int MAX_LAYER_LABEL_WIDTH =
      (int) (120 * Editor.preferences().getUiScale());

  private final Map<MapObjectType, PropertyPanel> panels;
  private MapObjectType type;
  private PropertyPanel currentPanel;

  private final ExpandableCard generalCard;
  private final ExpandableCard typeCard;
  private final ExpandableCard collisionCard;
  private final ExpandableCard combatCard;
  private final ExpandableCard movementCard;
  private final ExpandableCard customCard;

  private final CollisionPanel collisionPanel;
  private final CombatPanel combatPanel;
  private final MovementPanel movementPanel;
  private final CustomPanel customPanel;
  private final JTextField textFieldName;
  private final JComboBox<RenderType> renderType;
  private final JCheckBox checkBoxRenderWithLayer;
  private final JComboBox<ImplementationOption> implementation;
  private final JLabel labelImplementation;

  private final JLabel labelEntityID;
  private final JLabel labelTypeIcon;
  private final TagPanel tagPanel;
  private final JLabel lblLayer;
  private final JSpinner spnX;
  private final JSpinner spnY;
  private final JSpinner spnW;
  private final JSpinner spnH;
  private boolean updatingImplementation;

  public MapObjectInspector() {
    super();
    setBorder(null);
    setLayout(new BorderLayout());

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
    ControlBehavior.apply(this.renderType);
    this.checkBoxRenderWithLayer = new JCheckBox(Resources.strings().get("panel_renderwithlayer"));
    this.checkBoxRenderWithLayer.setOpaque(false);
    this.checkBoxRenderWithLayer.addActionListener(e -> updateRenderTypeEnabled());
    this.labelImplementation = new JLabel(Resources.strings().get("mapObjectInspector_implementation"));
    this.labelImplementation.setHorizontalAlignment(SwingConstants.TRAILING);
    this.implementation = new JComboBox<>();
    ControlBehavior.apply(this.implementation);
    this.implementation.setMaximumRowCount(9);
    this.implementation.setRenderer(new ImplementationRenderer());

    this.tagPanel = new TagPanel();

    JPanel headerContent = new JPanel();
    headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.X_AXIS));
    headerContent.setOpaque(false);

    JLabel lblEntityId = new JLabel(Resources.strings().get("panel_ID"));
    lblEntityId.setFont(lblEntityId.getFont().deriveFont(Font.BOLD));
    lblEntityId.setForeground(Style.mutedText());

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont());
    this.labelEntityID.setForeground(Style.text());

    this.labelTypeIcon = new JLabel(Icons.ENTITY_16);

    this.lblLayer = new JLabel("");
    this.lblLayer.setHorizontalAlignment(SwingConstants.TRAILING);
    this.lblLayer.setForeground(Style.mutedText());

    headerContent.add(Box.createHorizontalStrut(6));
    headerContent.add(labelTypeIcon);
    headerContent.add(Box.createHorizontalStrut(10));
    headerContent.add(lblLayer);
    headerContent.add(Box.createHorizontalStrut(12));
    headerContent.add(lblEntityId);
    headerContent.add(Box.createHorizontalStrut(4));
    headerContent.add(labelEntityID);
    headerContent.add(Box.createHorizontalStrut(6));

    this.spnX = new JSpinner(createCoordinateSpinnerModel());
    this.spnY = new JSpinner(createCoordinateSpinnerModel());
    this.spnW = new JSpinner(createSizeSpinnerModel());
    this.spnH = new JSpinner(createSizeSpinnerModel());

    ControlBehavior.apply(this.spnX);
    ControlBehavior.apply(this.spnY);
    ControlBehavior.apply(this.spnW);
    ControlBehavior.apply(this.spnH);

    // ---- build accordion ----
    JPanel accordion = new JPanel();
    accordion.setLayout(new BoxLayout(accordion, BoxLayout.Y_AXIS));
    accordion.setOpaque(true);
    accordion.setBackground(Style.background());
    accordion.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

    JPanel generalContent = new JPanel();
    generalContent.setLayout(new BoxLayout(generalContent, BoxLayout.Y_AXIS));
    generalContent.setOpaque(false);
    JPanel entityPanel = createEntityPanel();
    entityPanel.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, entityPanel.getPreferredSize().height));
    generalContent.add(entityPanel);

    JPanel sepTransform = createSectionSeparator(Resources.strings().get("panel_transform").toUpperCase(java.util.Locale.ROOT));
    sepTransform.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, sepTransform.getPreferredSize().height));
    generalContent.add(sepTransform);

    JPanel tfGrid = createTransformGrid();
    tfGrid.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, tfGrid.getPreferredSize().height));
    generalContent.add(tfGrid);

    this.generalCard =
        new ExpandableCard(
            Resources.strings().get("mapObjectInspector_mapObject"), generalContent, true);
    this.typeCard = new ExpandableCard("", new JPanel(), true);
    this.collisionCard =
        new ExpandableCard(
            Resources.strings().get("panel_collisionEntity"), this.collisionPanel, true);
    this.combatCard =
        new ExpandableCard(
            Resources.strings().get("panel_combatEntity"), this.combatPanel, true);
    this.movementCard =
        new ExpandableCard(
            Resources.strings().get("panel_mobileEntity"), this.movementPanel, true);
    this.customCard =
        new ExpandableCard(
            Resources.strings().get("panel_customProperties"), this.customPanel, true);

    generalCard.setContentInsets(8, 0, 8, 0);
    generalCard.setHeaderTrailing(headerContent);
    typeCard.setContentInsets(8, 0, 8, 0);
    collisionCard.setContentInsets(8, 0, 8, 0);
    combatCard.setContentInsets(8, 0, 8, 0);
    movementCard.setContentInsets(8, 0, 8, 0);
    customCard.setContentInsets(8, 0, 8, 0);

    typeCard.setVisible(false);
    collisionCard.setVisible(false);
    combatCard.setVisible(false);
    movementCard.setVisible(false);
    customCard.setVisible(false);

    accordion.add(generalCard);
    accordion.add(typeCard);
    accordion.add(collisionCard);
    accordion.add(combatCard);
    accordion.add(movementCard);
    accordion.add(customCard);

    JScrollPane scrollPane = new JScrollPane(accordion);
    scrollPane.setBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getViewport().setBackground(Style.background());
    add(scrollPane, BorderLayout.CENTER);

    this.setupChangedListeners();
    if (UI.getLayerController() != null) {
      UI.getLayerController().onLayersChanged(map -> Editor.instance().getMapComponent().refreshInspector());
    }
  }

  static SpinnerNumberModel createCoordinateSpinnerModel() {
    return new SpinnerNumberModel(0.0, null, (double) Short.MAX_VALUE, 1.0);
  }

  static SpinnerNumberModel createSizeSpinnerModel() {
    return new SpinnerNumberModel(0.0, 0.0, (double) Short.MAX_VALUE, 1.0);
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
    this.bindAll(mapObject == null ? List.of() : List.of(mapObject));
  }

  @Override
  public void bindAll(List<IMapObject> mapObjects) {
    List<IMapObject> targets = mapObjects == null ? List.of() : List.copyOf(mapObjects);
    super.bindAll(targets);

    MapObjectType commonType = targets.isEmpty() ? null : resolveType(targets.get(0).getType());
    MapObjectType candidateType = commonType;
    if (targets.stream().anyMatch(target -> resolveType(target.getType()) != candidateType)) {
      commonType = null;
    }
    this.setMapObjectType(commonType);

    if (this.currentPanel != null) {
      bindPanel(this.currentPanel, targets);
    }

    this.customPanel.setExcludedProperties(this.type == MapObjectType.EMITTER ? emitterProperties() : java.util.Set.of());

    boolean supportsCollisionAndCombat = commonType == MapObjectType.PROP || commonType == MapObjectType.CREATURE;
    bindPanel(this.collisionPanel, supportsCollisionAndCombat ? targets : List.of());
    bindPanel(this.combatPanel, supportsCollisionAndCombat ? targets : List.of());
    bindPanel(this.movementPanel, commonType == MapObjectType.CREATURE ? targets : List.of());

    bindPanel(this.customPanel, targets.size() == 1 ? targets : List.of());
    updateMultiEditState(targets);
  }

  private static void bindPanel(PropertyPanel panel, List<IMapObject> targets) {
    if (targets.size() == 1) {
      panel.bind(targets.get(0));
    } else {
      panel.bindAll(targets);
    }
  }

  private void updateMultiEditState(List<IMapObject> targets) {
    boolean multiEdit = targets.size() > 1;
    this.spnX.setEnabled(!multiEdit);
    this.spnY.setEnabled(!multiEdit);
    this.spnW.setEnabled(!multiEdit);
    this.spnH.setEnabled(!multiEdit);
    this.customCard.setVisible(!multiEdit && !targets.isEmpty());

    if (multiEdit) {
      this.labelEntityID.setText(Integer.toString(targets.size()));
      this.lblLayer.setText("");
      this.lblLayer.setToolTipText(null);
    }
    updateImplementationVisibility();
    updateRenderTypeEnabled();
  }

  private static java.util.Set<String> emitterProperties() {
    java.util.Set<String> properties = new java.util.HashSet<>();
    for (java.lang.reflect.Field field : MapObjectProperty.Particle.class.getFields()) {
      if (field.getType() == String.class) {
        try {
          properties.add((String) field.get(null));
        } catch (IllegalAccessException ignored) {
          // Public constants are expected; inaccessible fields are skipped.
        }
      }
    }
    return properties;
  }

  private JPanel createSectionSeparator(String label) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    JLabel title = new JLabel(label);
    title.setFont(title.getFont().deriveFont(10f));
    title.setForeground(Style.mutedText());
    title.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
    title.setHorizontalAlignment(SwingConstants.TRAILING);
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.setPreferredSize(new Dimension(SECTION_LABEL_WIDTH, 0));
    wrapper.add(title, BorderLayout.CENTER);
    panel.add(wrapper, BorderLayout.WEST);
    return panel;
  }

  private JPanel createTransformGrid() {
    JPanel grid = new JPanel();
    grid.setOpaque(false);
    GroupLayout gl = new GroupLayout(grid);
    grid.setLayout(gl);

    JLabel lblX = new JLabel(Resources.strings().get("panel_x"));
    lblX.setHorizontalAlignment(SwingConstants.TRAILING);
    JLabel lblY = new JLabel(Resources.strings().get("panel_y"));
    lblY.setHorizontalAlignment(SwingConstants.TRAILING);
    JLabel lblW = new JLabel(Resources.strings().get("mapObjectInspector_widthShort"));
    lblW.setHorizontalAlignment(SwingConstants.TRAILING);
    JLabel lblH = new JLabel(Resources.strings().get("mapObjectInspector_heightShort"));
    lblH.setHorizontalAlignment(SwingConstants.TRAILING);

    int transformLabelWidth = SECTION_LABEL_WIDTH;
    int secondaryLabelWidth = 24;
    int gap = CONTROL_MARGIN;

    gl.setAutoCreateGaps(false);
    gl.setHorizontalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
          .addComponent(lblX, transformLabelWidth, transformLabelWidth, transformLabelWidth)
          .addComponent(lblW, transformLabelWidth, transformLabelWidth, transformLabelWidth))
        .addGap(gap)
        .addGroup(gl.createParallelGroup()
          .addComponent(spnX, SPINNER_WIDTH, SPINNER_WIDTH, SPINNER_WIDTH)
          .addComponent(spnW, SPINNER_WIDTH, SPINNER_WIDTH, SPINNER_WIDTH))
        .addGap(gap)
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
          .addComponent(lblY, secondaryLabelWidth, secondaryLabelWidth, secondaryLabelWidth)
          .addComponent(lblH, secondaryLabelWidth, secondaryLabelWidth, secondaryLabelWidth))
        .addGap(gap)
        .addGroup(gl.createParallelGroup()
          .addComponent(spnY, SPINNER_WIDTH, SPINNER_WIDTH, SPINNER_WIDTH)
          .addComponent(spnH, SPINNER_WIDTH, SPINNER_WIDTH, SPINNER_WIDTH)));
    gl.setVerticalGroup(
      gl.createSequentialGroup()
        .addGap(2)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblX).addComponent(spnX)
          .addComponent(lblY).addComponent(spnY))
        .addGap(gap)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblW).addComponent(spnW)
          .addComponent(lblH).addComponent(spnH))
        .addGap(2));
    return grid;
  }

  private JPanel createEntityPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);

    JLabel lblName = new JLabel(Resources.strings().get("panel_name"));
    lblName.setHorizontalAlignment(SwingConstants.TRAILING);
    JLabel lblRenderType = new JLabel(Resources.strings().get("panel_rendertype"));
    lblRenderType.setHorizontalAlignment(SwingConstants.TRAILING);
    JLabel lblTags = new JLabel(Resources.strings().get("panel_tags"));
    lblTags.setHorizontalAlignment(SwingConstants.TRAILING);

    int gap = CONTROL_MARGIN;

    gl.setAutoCreateGaps(false);
    gl.setHorizontalGroup(
      gl.createSequentialGroup()
        .addGroup(gl.createParallelGroup(Alignment.TRAILING)
           .addComponent(lblName, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
           .addComponent(labelImplementation, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
           .addComponent(lblRenderType, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH)
          .addGap(PropertyPanel.CONTROL_HEIGHT)
          .addComponent(lblTags, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH, SECTION_LABEL_WIDTH))
        .addGap(gap)
        .addGroup(gl.createParallelGroup()
           .addComponent(textFieldName, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
           .addComponent(implementation, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
           .addComponent(renderType, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
          .addComponent(checkBoxRenderWithLayer, 0, CONTROL_WIDTH, Integer.MAX_VALUE)
          .addComponent(tagPanel, 0, CONTROL_WIDTH, Integer.MAX_VALUE)));
    gl.setVerticalGroup(
      gl.createSequentialGroup()
        .addGap(2)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
           .addComponent(lblName)
           .addComponent(textFieldName))
        .addGap(gap)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(labelImplementation)
          .addComponent(implementation))
        .addGap(gap)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblRenderType)
          .addComponent(renderType))
        .addGap(gap)
        .addComponent(checkBoxRenderWithLayer)
        .addGap(gap)
        .addGroup(gl.createParallelGroup(Alignment.CENTER)
          .addComponent(lblTags)
          .addComponent(tagPanel))
        .addGap(2));
    return panel;
  }

  private void switchPanel() {
    final MapObjectType currentType = this.getObjectType();
    if (currentType == null) {
      this.clearPanels();
      return;
    }

    PropertyPanel panel = this.panels.get(type);
    if (panel != null) {
      typeCard.setTitle(Resources.strings().get(panel.getIdentifier()));
      typeCard.setContent(panel);
      typeCard.setVisible(true);
    } else {
      typeCard.setVisible(false);
    }

    boolean showCollision =
        currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE;
    boolean showCombat =
        currentType == MapObjectType.PROP || currentType == MapObjectType.CREATURE;

    collisionCard.setVisible(showCollision);
    combatCard.setVisible(showCombat);
    movementCard.setVisible(currentType == MapObjectType.CREATURE);
    customCard.setVisible(true);

    this.currentPanel = panel != null ? panel : this.customPanel;
    revalidate();
    repaint();
  }

  private void clearPanels() {
    typeCard.setVisible(false);
    collisionCard.setVisible(false);
    combatCard.setVisible(false);
    movementCard.setVisible(false);
    customCard.setVisible(false);

    if (this.currentPanel != null) {
      this.currentPanel.bind(null);
      this.currentPanel = null;
    }

    revalidate();
    repaint();
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
    this.labelTypeIcon.setIcon(Icons.ENTITY_16);
    this.labelTypeIcon.setToolTipText(null);
    this.lblLayer.setText("");
    this.lblLayer.setToolTipText(null);
    this.renderType.setSelectedIndex(0);
    this.renderType.setEnabled(false);
    this.checkBoxRenderWithLayer.setSelected(false);
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
    this.type = resolveType(mapObject.getType());
    this.textFieldName.setText(mapObject.getName());
    this.spnX.setValue((double) mapObject.getX());
    this.spnY.setValue((double) mapObject.getY());
    this.spnW.setValue((double) mapObject.getWidth());
    this.spnH.setValue((double) mapObject.getHeight());
    this.tagPanel.bind(mapObject.getStringValue(MapObjectProperty.TAGS, null));

    this.labelEntityID.setText(Integer.toString(mapObject.getId()));
    this.labelTypeIcon.setIcon(Icons.forMapObjectType(this.type));
    this.labelTypeIcon.setToolTipText(this.type != null ? this.type.name() : null);
    String layerText = Resources.strings().get("panel_layer") + ": " + mapObject.getLayer();
    this.lblLayer.setText(elide(layerText, this.lblLayer.getFontMetrics(this.lblLayer.getFont())));
    this.lblLayer.setToolTipText(layerText);

    RenderType rt =
        mapObject.getEnumValue(
            MapObjectProperty.RENDERTYPE, RenderType.class, RenderType.NORMAL);
    if (rt != null) {
      this.renderType.setSelectedItem(rt);
    }
    this.checkBoxRenderWithLayer.setSelected(mapObject.getBoolValue(MapObjectProperty.RENDERWITHLAYER, false));
    updateImplementationOptions(mapObject);
    updateRenderTypeEnabled();
  }

  private void updateRenderTypeEnabled() {
    boolean supportsRenderType = getDataSources().isEmpty()
        ? this.type == MapObjectType.CREATURE || this.type == MapObjectType.EMITTER || this.type == MapObjectType.PROP
        : getDataSources().stream()
          .map(mapObject -> resolveType(mapObject.getType()))
          .allMatch(type -> type == MapObjectType.CREATURE || type == MapObjectType.EMITTER || type == MapObjectType.PROP);
    this.renderType.setEnabled(supportsRenderType && !this.checkBoxRenderWithLayer.isSelected());
  }

  private void updateImplementationOptions(IMapObject mapObject) {
    boolean supported = this.type == MapObjectType.CREATURE || this.type == MapObjectType.PROP;
    this.labelImplementation.setVisible(supported);
    this.implementation.setVisible(supported);
    if (!supported) {
      return;
    }

    this.updatingImplementation = true;
    try {
      this.implementation.removeAllItems();
      this.implementation.addItem(new ImplementationOption(
        null, Resources.strings().get("mapObjectInspector_builtinDefault"), null, null));
      Editor.instance().getProjectCodeIntegration().getDefinitions().stream()
        .filter(definition -> definition.baseType() == this.type)
        .forEach(definition -> this.implementation.addItem(new ImplementationOption(definition.id(), definition.displayName(), compactPackage(definition.className()), definition.className())));
      String selectedId = mapObject.getStringValue(MapObjectProperty.IMPLEMENTATION, null);
      for (int i = 0; i < this.implementation.getItemCount(); i++) {
        if (Objects.equals(this.implementation.getItemAt(i).id(), selectedId)) {
          this.implementation.setSelectedIndex(i);
          return;
        }
      }
      this.implementation.setSelectedIndex(0);
    } finally {
      this.updatingImplementation = false;
    }
  }

  private void updateImplementationVisibility() {
    boolean supported = this.type == MapObjectType.CREATURE || this.type == MapObjectType.PROP;
    this.labelImplementation.setVisible(supported);
    this.implementation.setVisible(supported);
  }

  private static MapObjectType resolveType(String mapObjectType) {
    return Editor.instance().getProjectCodeIntegration().getDefinitions().stream()
      .filter(definition -> definition.id().equals(mapObjectType))
      .map(de.gurkenlabs.utiliti.controller.ProjectCodeIntegration.Definition::baseType)
      .findFirst()
      .orElseGet(() -> MapObjectType.get(mapObjectType));
  }

  private static String elide(String value, FontMetrics metrics) {
    if (metrics.stringWidth(value) <= MAX_LAYER_LABEL_WIDTH) {
      return value;
    }
    String suffix = "...";
    int length = value.length();
    while (length > 0
        && metrics.stringWidth(value.substring(0, length) + suffix) > MAX_LAYER_LABEL_WIDTH) {
      length--;
    }
    return value.substring(0, length) + suffix;
  }

  private static String compactPackage(String className) {
    int classSeparator = className.lastIndexOf('.');
    if (classSeparator < 0) {
      return "";
    }
    String packageName = className.substring(0, classSeparator);
    int parentSeparator = packageName.lastIndexOf('.');
    return parentSeparator < 0 ? packageName : packageName.substring(parentSeparator + 1);
  }

  boolean isRenderTypeEnabledForTest() {
    return this.renderType.isEnabled();
  }

  boolean isTypeCardVisibleForTest() {
    return this.typeCard.isVisible();
  }

  boolean isCustomCardVisibleForTest() {
    return this.customCard.isVisible();
  }

  boolean areTransformControlsEnabledForTest() {
    return this.spnX.isEnabled() && this.spnY.isEnabled() && this.spnW.isEnabled() && this.spnH.isEnabled();
  }

  PropertyPanel getCurrentPanelForTest() {
    return this.currentPanel;
  }

  private void setupChangedListeners() {
    setup(renderType, MapObjectProperty.RENDERTYPE);
    setup(this.checkBoxRenderWithLayer, MapObjectProperty.RENDERWITHLAYER);

    this.spnX.addChangeListener(
        e -> {
          if (getDataSource() == null) {
            return;
          }
          double val = (double) spnX.getValue();
          if (getDataSource().getX() != val) {
            UndoManager.instance().mapObjectChanging(getDataSource());
            getDataSource().setX((float) val);
            Transform.updateAnchors();
            UndoManager.instance().mapObjectMoved(getDataSource());
            updateEnvironment();
          }
        });
    this.spnY.addChangeListener(
        e -> {
          if (getDataSource() == null) {
            return;
          }
          double val = (double) spnY.getValue();
          if (getDataSource().getY() != val) {
            UndoManager.instance().mapObjectChanging(getDataSource());
            getDataSource().setY((float) val);
            Transform.updateAnchors();
            UndoManager.instance().mapObjectMoved(getDataSource());
            updateEnvironment();
          }
        });
    this.spnW.addChangeListener(
        e -> {
          if (getDataSource() == null) {
            return;
          }
          double val = (double) spnW.getValue();
          if (getDataSource().getWidth() != val) {
            UndoManager.instance().mapObjectChanging(getDataSource());
            getDataSource().setWidth((float) val);
            Transform.updateAnchors();
            UndoManager.instance().mapObjectResized(getDataSource());
            updateEnvironment();
          }
        });
    this.spnH.addChangeListener(
        e -> {
          if (getDataSource() == null) {
            return;
          }
          double val = (double) spnH.getValue();
          if (getDataSource().getHeight() != val) {
            UndoManager.instance().mapObjectChanging(getDataSource());
            getDataSource().setHeight((float) val);
            Transform.updateAnchors();
            UndoManager.instance().mapObjectResized(getDataSource());
            updateEnvironment();
          }
        });

    this.textFieldName.addFocusListener(
        new MapObjectPropertyFocusListener(this.textFieldName,
          m -> !Objects.equals(m.getName(), textFieldName.getText()),
          m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(
        new MapObjectPropertyActionListener(
            m -> m.getName() == null || !m.getName().equals(textFieldName.getText()),
            m -> m.setName(textFieldName.getText())));

    this.tagPanel.addActionListener(
        new MapObjectPropertyActionListener(
            m ->
                !m.hasCustomProperty(MapObjectProperty.TAGS)
                    || !m.getStringValue(MapObjectProperty.TAGS, null)
                        .equals(this.tagPanel.getTagsString()),
             m -> m.setValue(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));

    this.implementation.addActionListener(new MapObjectPropertyActionListener(m -> {
      if (this.updatingImplementation) {
        return false;
      }
      ImplementationOption selected = (ImplementationOption) this.implementation.getSelectedItem();
      return !Objects.equals(m.getStringValue(MapObjectProperty.IMPLEMENTATION, null), selected == null ? null : selected.id());
    }, m -> {
      ImplementationOption selected = (ImplementationOption) this.implementation.getSelectedItem();
      if (selected == null || selected.id() == null) {
        m.removeProperty(MapObjectProperty.IMPLEMENTATION);
      } else {
        m.setValue(MapObjectProperty.IMPLEMENTATION, selected.id());
      }
    }));
  }

  private record ImplementationOption(String id, String displayName, String packageName, String className) {
    @Override
    public String toString() {
      return displayName;
    }
  }

  private static final class ImplementationRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (!(value instanceof ImplementationOption option)) {
        return label;
      }
      label.setToolTipText(option.className());
      if (index < 0 || option.packageName() == null) {
        label.setText(option.displayName());
        return label;
      }
      String packageColor = String.format("#%02x%02x%02x", Style.COLOR_SUBTEXT.getRed(), Style.COLOR_SUBTEXT.getGreen(), Style.COLOR_SUBTEXT.getBlue());
      label.setText("<html>" + option.displayName() + "<br><span style='color:" + packageColor + "; font-size:9px'>" + option.packageName() + "</span></html>");
      label.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
      return label;
    }
  }
}
