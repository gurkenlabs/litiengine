package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Frame;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.tool.TileStamp;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class TilesetEditorPanel extends JPanel {
  private final JLabel titleLabel;
  private final JLabel metaLabel;
  private final JTextField tilesetNameField;
  private final JSpinner tileOffsetXSpinner;
  private final JSpinner tileOffsetYSpinner;
  private final DefaultTableModel tilesetPropertyModel;
  private final JTable tilesetPropertyTable;
  private final JLabel previewLabel;
  private final JLabel detailLabel;
  private final JTextField typeField;
  private final JTextField probabilityField;
  private final DefaultTableModel tilePropertyModel;
  private final JTable tilePropertyTable;
  private final DefaultTableModel animationModel;
  private final JTable animationTable;
  private final JComboBox<WangSet> terrainSetCombo;
  private final JComboBox<WangColor> terrainCombo;
  private final JComboBox<TerrainType> terrainTypeCombo;
  private final JTextField terrainSetNameField;
  private final JTextField terrainNameField;
  private final JTextField terrainProbabilityField;
  private final JButton terrainColorButton;
  private final JButton[] terrainSlots;
  private final TileGrid tileGrid;
  private final JScrollPane gridScroll;
  private final JLabel zoomLabel;
  private final Timer animationTimer;
  private Runnable tilesetNameChanged = () -> {};
  private Tileset tileset;
  private boolean binding;

  public TilesetEditorPanel() {
    super(new BorderLayout(0, 8));
    setOpaque(true);
    setBackground(Style.background());
    setBorder(BorderFactory.createEmptyBorder(8, 8, 10, 8));

    JPanel info = new JPanel();
    info.setOpaque(false);
    info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
    this.titleLabel = new JLabel(Resources.strings().get("tilesetEditor_noTilesetSelected"));
    this.titleLabel.setForeground(Style.text());
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD));
    this.metaLabel = new JLabel(Resources.strings().get("tilesetEditor_selectTilesetHint"));
    this.metaLabel.setForeground(Style.mutedText());
    info.add(this.titleLabel);
    info.add(this.metaLabel);

    this.tilesetNameField = new JTextField();
    ControlBehavior.apply(this.tilesetNameField);
    this.tilesetNameField.addActionListener(_ -> applyTilesetName());
    this.tilesetNameField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        applyTilesetName();
      }
    });
    JPanel namePanel = labeledField(Resources.strings().get("panel_name"), this.tilesetNameField);

    this.tileOffsetXSpinner = new JSpinner(new SpinnerNumberModel(0, -100000, 100000, 1));
    this.tileOffsetYSpinner = new JSpinner(new SpinnerNumberModel(0, -100000, 100000, 1));
    ControlBehavior.apply(this.tileOffsetXSpinner);
    ControlBehavior.apply(this.tileOffsetYSpinner);
    this.tileOffsetXSpinner.addChangeListener(_ -> applyTilesetRenderSettings());
    this.tileOffsetYSpinner.addChangeListener(_ -> applyTilesetRenderSettings());
    JPanel renderSettings = new JPanel(new BorderLayout(0, 4));
    renderSettings.setOpaque(false);
    JPanel terrainHost = new JPanel(new BorderLayout());
    terrainHost.setOpaque(false);

    this.tilesetPropertyModel = createPropertyModel();
    this.tilesetPropertyModel.addTableModelListener(_ -> applyTilesetProperties());
    this.tilesetPropertyTable = createPropertyTable(this.tilesetPropertyModel);
    ExpandableCard tilesetProperties = new ExpandableCard(Resources.strings().get("tilesetEditor_tilesetProperties"), createTablePanel(this.tilesetPropertyTable, this.tilesetPropertyModel,
      () -> this.tilesetPropertyModel.addRow(new Object[] {"", ""})), false);
    tilesetProperties.setContentInsets(8, 0, 8, 0);

    JPanel tilesetControls = new JPanel();
    tilesetControls.setLayout(new BoxLayout(tilesetControls, BoxLayout.Y_AXIS));
    tilesetControls.setOpaque(false);
    tilesetControls.add(namePanel);
    tilesetControls.add(javax.swing.Box.createVerticalStrut(6));
    tilesetControls.add(renderSettings);
    tilesetControls.add(javax.swing.Box.createVerticalStrut(6));
    tilesetControls.add(tilesetProperties);
    tilesetControls.add(javax.swing.Box.createVerticalStrut(6));
    tilesetControls.add(terrainHost);
    add(tilesetControls, BorderLayout.NORTH);

    this.tileGrid = new TileGrid();
    this.tileGrid.setSelectionChanged(this::updateSelectedTileControls);
    this.gridScroll = new JScrollPane(this.tileGrid);
    this.gridScroll.setMinimumSize(new Dimension(0, 180));
    this.gridScroll.setPreferredSize(new Dimension(0, 360));
    this.gridScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 520));
    this.gridScroll.setAlignmentX(LEFT_ALIGNMENT);
    this.gridScroll.setBorder(BorderFactory.createLineBorder(Style.border()));
    this.gridScroll.getViewport().setBackground(Style.surface());
    JButton zoomOut = Style.iconButton(Icons.MINUS_16);
    JButton zoomIn = Style.iconButton(Icons.ADD_16);
    JButton fit = Style.iconButton(Icons.FIT_16);
    this.zoomLabel = new JLabel();
    zoomOut.addActionListener(e -> setGridZoom(this.tileGrid.zoom * 0.8f));
    zoomIn.addActionListener(e -> setGridZoom(this.tileGrid.zoom * 1.25f));
    fit.addActionListener(e -> fitGrid());
    zoomOut.setToolTipText(Resources.strings().get("menu_view_zoomOut"));
    zoomIn.setToolTipText(Resources.strings().get("menu_view_zoomIn"));
    fit.setToolTipText(Resources.strings().get("tilesetEditor_fitTileset"));
    JPanel zoomControls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 0));
    zoomControls.setOpaque(false);
    zoomControls.add(zoomOut);
    zoomControls.add(this.zoomLabel);
    zoomControls.add(zoomIn);
    zoomControls.add(fit);
    renderSettings.add(labeledOffsets(), BorderLayout.CENTER);
    renderSettings.add(zoomControls, BorderLayout.EAST);
    JPanel bodyPanel = new JPanel(new BorderLayout(0, 8));
    bodyPanel.setOpaque(false);
    bodyPanel.add(this.gridScroll, BorderLayout.CENTER);
    add(bodyPanel, BorderLayout.CENTER);

    this.previewLabel = new JLabel("", SwingConstants.CENTER);
    this.previewLabel.setOpaque(true);
    this.previewLabel.setBackground(Style.surface());
    this.previewLabel.setBorder(BorderFactory.createLineBorder(Style.border()));
    this.previewLabel.setPreferredSize(new Dimension(0, 112));

    this.detailLabel = new JLabel(" ");
    this.detailLabel.setForeground(Style.mutedText());

    this.typeField = new JTextField();
    ControlBehavior.apply(this.typeField);
    this.typeField.addActionListener(_ -> applyType());
    this.typeField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        applyType();
      }
    });

    this.probabilityField = metadataField(this::applyProbability);

    this.tilePropertyModel = createPropertyModel();
    this.tilePropertyModel.addTableModelListener(_ -> applyTileProperties());
    this.tilePropertyTable = createPropertyTable(this.tilePropertyModel);
    this.animationModel = new DefaultTableModel(new Object[][] {}, new String[] {
      Resources.strings().get("tilesetEditor_tile"), Resources.strings().get("tilesetEditor_duration")}) {
      @Override public Class<?> getColumnClass(int columnIndex) {
        return String.class;
      }
    };
    this.animationModel.addTableModelListener(_ -> applyAnimationFrames());
    this.animationTable = createPropertyTable(this.animationModel);
    this.terrainSetCombo = new JComboBox<>();
    this.terrainCombo = new JComboBox<>();
    this.terrainTypeCombo = new JComboBox<>(TerrainType.values());
    ControlBehavior.apply(this.terrainSetCombo);
    ControlBehavior.apply(this.terrainCombo);
    ControlBehavior.apply(this.terrainTypeCombo);
    this.terrainSetNameField = metadataField(this::applyTerrainSetName);
    this.terrainNameField = metadataField(this::applyTerrainName);
    this.terrainProbabilityField = metadataField(this::applyTerrainProbability);
    this.terrainColorButton = new JButton();
    this.terrainColorButton.setToolTipText(Resources.strings().get("tilesetEditor_chooseTerrainColor"));
    this.terrainColorButton.setPreferredSize(new Dimension(36, 24));
    this.terrainColorButton.setFocusable(true);
    this.terrainColorButton.getAccessibleContext().setAccessibleName(Resources.strings().get("tilesetEditor_terrainColor"));
    this.terrainColorButton.addActionListener(_ -> chooseTerrainColor());
    this.terrainSlots = new JButton[8];
    this.terrainSetCombo.addActionListener(_ -> updateTerrainControls());
    this.terrainCombo.addActionListener(_ -> updateTerrainSelectionControls());
    this.terrainTypeCombo.addActionListener(_ -> applyTerrainSetType());
    terrainHost.add(createTerrainPanel(), BorderLayout.CENTER);

    JPanel typePanel = labeledField(Resources.strings().get("tilesetEditor_class"), this.typeField);
    JPanel probabilityPanel = labeledField(Resources.strings().get("tilesetEditor_probability"), this.probabilityField);

    JPanel selectedTilePanel = new JPanel(new BorderLayout(0, 8));
    selectedTilePanel.setOpaque(false);
    selectedTilePanel.setAlignmentX(LEFT_ALIGNMENT);
    selectedTilePanel.add(this.previewLabel, BorderLayout.NORTH);
    JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.setOpaque(false);
    controls.add(this.detailLabel);
    controls.add(javax.swing.Box.createVerticalStrut(4));
    controls.add(typePanel);
    controls.add(javax.swing.Box.createVerticalStrut(4));
    controls.add(probabilityPanel);
    controls.add(javax.swing.Box.createVerticalStrut(6));
    ExpandableCard propertyPanel = new ExpandableCard(Resources.strings().get("tilesetEditor_tileProperties"), createTablePanel(this.tilePropertyTable, this.tilePropertyModel,
      () -> this.tilePropertyModel.addRow(new Object[] {"", ""})), false);
    propertyPanel.setContentInsets(8, 0, 8, 0);
    ExpandableCard animationPanel = new ExpandableCard(Resources.strings().get("tilesetEditor_tileAnimation"), createTablePanel(this.animationTable, this.animationModel,
      () -> this.animationModel.addRow(new Object[] {String.valueOf(Math.max(0, this.tileGrid.selectedTile)), "100"})), false);
    animationPanel.setContentInsets(8, 0, 8, 0);
    controls.add(propertyPanel);
    controls.add(javax.swing.Box.createVerticalStrut(6));
    controls.add(animationPanel);
    selectedTilePanel.add(controls, BorderLayout.CENTER);
    bodyPanel.add(selectedTilePanel, BorderLayout.SOUTH);

    this.animationTimer = new Timer(80, _ -> updateSelectedTilePreview());
    updateZoomLabel();
  }

  @Override public void addNotify() {
    super.addNotify();
    this.animationTimer.start();
  }

  @Override public void updateUI() {
    super.updateUI();
    setBackground(Style.background());
    if (this.titleLabel != null) {
      this.titleLabel.setForeground(Style.text());
    }
    if (this.metaLabel != null) {
      this.metaLabel.setForeground(Style.mutedText());
    }
    if (this.gridScroll != null) {
      this.gridScroll.setBorder(BorderFactory.createLineBorder(Style.border()));
      this.gridScroll.getViewport().setBackground(Style.surface());
    }
    if (this.previewLabel != null) {
      this.previewLabel.setBackground(Style.surface());
      this.previewLabel.setBorder(BorderFactory.createLineBorder(Style.border()));
    }
    if (this.detailLabel != null) {
      this.detailLabel.setForeground(Style.mutedText());
    }
  }

  @Override public void removeNotify() {
    this.animationTimer.stop();
    super.removeNotify();
  }

  public void bind(Tileset tileset) {
    this.tileset = tileset;
    this.tileGrid.bind(tileset);
    updateTerrainSets(null);
    javax.swing.SwingUtilities.invokeLater(this::fitGrid);
    publishSelectedTile();
    if (tileset == null) {
      this.titleLabel.setText(Resources.strings().get("tilesetEditor_noTilesetSelected"));
      this.metaLabel.setText(Resources.strings().get("tilesetEditor_selectTilesetHint"));
      clearTilesetControls();
      clearSelectedTileControls();
      return;
    }

    this.titleLabel.setText(tileset.getName() != null ? tileset.getName() : Resources.strings().get("tilesetEditor_unnamedTileset"));
    String source = tileset.getImage() != null ? tileset.getImage().getSource() : Resources.strings().get("tilesetEditor_noImage");
    this.metaLabel.setText(Resources.strings().get("tilesetEditor_tilesetSummary", source,
      String.valueOf(tileset.getTileWidth()), String.valueOf(tileset.getTileHeight()),
      String.valueOf(tileset.getTileCount())));
    bindTilesetControls();
    updateSelectedTileControls();
  }

  Tileset getTileset() {
    return this.tileset;
  }

  void dispose() {
    this.animationTimer.stop();
  }

  void onTilesetNameChanged(Runnable listener) {
    this.tilesetNameChanged = listener != null ? listener : () -> {};
  }

  private void setGridZoom(float zoom) {
    this.tileGrid.zoom = Math.max(0.25f, Math.min(4f, zoom));
    this.tileGrid.revalidate();
    this.tileGrid.repaint();
    updateZoomLabel();
  }

  private void fitGrid() {
    if (this.tileset == null || this.tileset.getTileCount() <= 0 || this.gridScroll.getViewport().getWidth() <= 0) {
      return;
    }
    int columns = this.tileset.getColumns();
    if (columns <= 0) {
      return;
    }
    int baseCell = Math.max(this.tileset.getTileWidth(), this.tileset.getTileHeight()) + TileGrid.CELL_PADDING * 2;
    setGridZoom((float) this.gridScroll.getViewport().getWidth() / (columns * baseCell));
  }

  private void updateZoomLabel() {
    this.zoomLabel.setText(Math.round(this.tileGrid.zoom * 100) + "%");
  }

  int getSelectedTileIdForTest() {
    return this.tileGrid.selectedTile;
  }

  void selectTileForTest(int tile) {
    this.tileGrid.selectTile(tile);
  }

  void selectTilesForTest(int anchor, int lead, boolean control, boolean shift) {
    this.tileGrid.selectTiles(anchor, lead, control, shift);
  }

  TileStamp getSelectedTileStampForTest() {
    return selectedTileStamp();
  }

  String getDetailTextForTest() {
    return this.detailLabel.getText();
  }

  String getTypeTextForTest() {
    return this.typeField.getText();
  }

  String getTilesetNameTextForTest() {
    return this.tilesetNameField.getText();
  }

  void setTilesetNameTextForTest(String name) {
    this.tilesetNameField.setText(name);
    applyTilesetName();
  }

  void setTypeTextForTest(String type) {
    this.typeField.setText(type);
    applyType();
  }

  String getProbabilityTextForTest() {
    return this.probabilityField.getText();
  }

  void setProbabilityTextForTest(String probability) {
    this.probabilityField.setText(probability);
    applyProbability();
  }

  void setTilesetOffsetsForTest(String offsetX, String offsetY) {
    this.tileOffsetXSpinner.setValue(Integer.parseInt(offsetX));
    this.tileOffsetYSpinner.setValue(Integer.parseInt(offsetY));
    applyTilesetRenderSettings();
  }

  void setCustomPropertyForTest(String name, String value) {
    this.tilePropertyModel.addRow(new Object[] {name, value});
  }

  void setTilesetCustomPropertyForTest(String name, String value) {
    this.tilesetPropertyModel.addRow(new Object[] {name, value});
  }

  void addAnimationFrameForTest(int tile, int duration) {
    this.animationModel.addRow(new Object[] {String.valueOf(tile), String.valueOf(duration)});
  }

  boolean isAnimationTileForTest(int tile) {
    return this.tileGrid.definesAnimation(tile);
  }

  boolean isSelectedAnimationFrameForTest(int tile) {
    return this.tileGrid.selectedAnimationFrameTiles().contains(tile);
  }

  void addTerrainSetForTest() {
    addTerrainSet();
  }

  void addTerrainForTest() {
    addTerrain();
  }

  void assignTerrainSlotForTest(int index) {
    applyTerrainSlot(index);
  }

  void setTerrainTypeForTest(TerrainType type) {
    this.terrainTypeCombo.setSelectedItem(type);
  }

  private static DefaultTableModel createPropertyModel() {
    return new DefaultTableModel(new Object[][] {}, new String[] {
      Resources.strings().get("panel_name"), Resources.strings().get("panel_value")}) {
      @Override public Class<?> getColumnClass(int columnIndex) {
        return String.class;
      }
    };
  }

  private static JTable createPropertyTable(DefaultTableModel model) {
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowHeight(24);
    return table;
  }

  private static JTextField metadataField(Runnable apply) {
    JTextField field = new JTextField();
    ControlBehavior.apply(field);
    field.addActionListener(_ -> apply.run());
    field.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        apply.run();
      }
    });
    return field;
  }

  private static JPanel labeledField(String labelText, JTextField field) {
    JPanel panel = new JPanel(new BorderLayout(6, 0));
    panel.setOpaque(false);
    JLabel label = new JLabel(labelText);
    label.setForeground(Style.text());
    label.setPreferredSize(new Dimension(82, Style.CONTROL_HEIGHT));
    panel.add(label, BorderLayout.WEST);
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel labeledOffsets() {
    JPanel panel = new JPanel(new BorderLayout(6, 0));
    panel.setOpaque(false);
    JLabel label = new JLabel(Resources.strings().get("panel_offset"));
    label.setForeground(Style.text());
    label.setPreferredSize(new Dimension(82, Style.CONTROL_HEIGHT));
    panel.add(label, BorderLayout.WEST);

    JPanel values = new JPanel();
    values.setOpaque(false);
    values.setLayout(new BoxLayout(values, BoxLayout.X_AXIS));
    JLabel xLabel = new JLabel(Resources.strings().get("panel_x"));
    JLabel yLabel = new JLabel(Resources.strings().get("panel_y"));
    xLabel.setForeground(Style.mutedText());
    yLabel.setForeground(Style.mutedText());
    Dimension offsetSize = new Dimension(72, Style.CONTROL_HEIGHT);
    this.tileOffsetXSpinner.setPreferredSize(offsetSize);
    this.tileOffsetXSpinner.setMaximumSize(offsetSize);
    this.tileOffsetYSpinner.setPreferredSize(offsetSize);
    this.tileOffsetYSpinner.setMaximumSize(offsetSize);
    values.add(xLabel);
    values.add(javax.swing.Box.createHorizontalStrut(4));
    values.add(this.tileOffsetXSpinner);
    values.add(javax.swing.Box.createHorizontalStrut(8));
    values.add(yLabel);
    values.add(javax.swing.Box.createHorizontalStrut(4));
    values.add(this.tileOffsetYSpinner);
    values.add(javax.swing.Box.createHorizontalGlue());
    panel.add(values, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTablePanel(JTable table, DefaultTableModel model, Runnable addAction) {
    JButton addProperty = Style.textButton("+");
    addProperty.addActionListener(_ -> addAction.run());
    JButton removeProperty = Style.textButton("−");
    removeProperty.addActionListener(_ -> removeSelectedProperty(table, model));

    JPanel propertyActions = new JPanel(new BorderLayout(6, 0));
    propertyActions.setOpaque(false);
    JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
    buttons.setOpaque(false);
    buttons.add(addProperty);
    buttons.add(removeProperty);
    propertyActions.add(buttons, BorderLayout.EAST);

    JPanel propertyPanel = new JPanel(new BorderLayout(0, 4));
    propertyPanel.setOpaque(false);
    propertyPanel.add(propertyActions, BorderLayout.NORTH);
    JScrollPane propertyScroll = new JScrollPane(table);
    propertyScroll.setPreferredSize(new Dimension(0, 96));
    propertyScroll.setBorder(BorderFactory.createLineBorder(Style.border()));
    propertyPanel.add(propertyScroll, BorderLayout.CENTER);
    return propertyPanel;
  }

  private JPanel createTerrainPanel() {
    JButton addSet = Style.textButton("+");
    addSet.setToolTipText(Resources.strings().get("tilesetEditor_addTerrainSet"));
    addSet.addActionListener(_ -> addTerrainSet());
    JButton removeSet = Style.textButton("-");
    removeSet.setToolTipText(Resources.strings().get("tilesetEditor_removeTerrainSet"));
    removeSet.addActionListener(_ -> removeTerrainSet());
    JButton addTerrain = Style.textButton("+");
    addTerrain.setToolTipText(Resources.strings().get("tilesetEditor_addTerrain"));
    addTerrain.addActionListener(_ -> addTerrain());
    JButton removeTerrain = Style.textButton("-");
    removeTerrain.setToolTipText(Resources.strings().get("tilesetEditor_removeTerrain"));
    removeTerrain.addActionListener(_ -> removeTerrain());

    JPanel sets = new JPanel(new BorderLayout(6, 0));
    sets.setOpaque(false);
    JLabel setLabel = fieldLabel(Resources.strings().get("tilesetEditor_set"), 56);
    sets.add(setLabel, BorderLayout.WEST);
    sets.add(this.terrainSetCombo, BorderLayout.CENTER);
    JPanel setButtons = new JPanel();
    setButtons.setLayout(new BoxLayout(setButtons, BoxLayout.X_AXIS));
    setButtons.setOpaque(false);
    this.terrainTypeCombo.setMaximumSize(new Dimension(120, this.terrainTypeCombo.getPreferredSize().height));
    setButtons.add(this.terrainTypeCombo);
    setButtons.add(javax.swing.Box.createHorizontalStrut(4));
    setButtons.add(addSet);
    setButtons.add(javax.swing.Box.createHorizontalStrut(4));
    setButtons.add(removeSet);
    sets.add(setButtons, BorderLayout.EAST);

    JPanel terrains = new JPanel(new BorderLayout(6, 0));
    terrains.setOpaque(false);
    terrains.add(fieldLabel(Resources.strings().get("tilesetEditor_terrain"), 56), BorderLayout.WEST);
    terrains.add(this.terrainCombo, BorderLayout.CENTER);
    JPanel terrainButtons = new JPanel();
    terrainButtons.setLayout(new BoxLayout(terrainButtons, BoxLayout.X_AXIS));
    terrainButtons.setOpaque(false);
    terrainButtons.add(this.terrainColorButton);
    terrainButtons.add(javax.swing.Box.createHorizontalStrut(4));
    terrainButtons.add(addTerrain);
    terrainButtons.add(javax.swing.Box.createHorizontalStrut(4));
    terrainButtons.add(removeTerrain);
    terrains.add(terrainButtons, BorderLayout.EAST);

    JPanel terrainMetadata = new JPanel(new GridLayout(3, 1, 0, 4));
    terrainMetadata.setOpaque(false);
    terrainMetadata.add(labeledField(Resources.strings().get("tilesetEditor_setName"), this.terrainSetNameField));
    terrainMetadata.add(labeledField(Resources.strings().get("panel_name"), this.terrainNameField));
    JPanel terrainProbability = labeledField(Resources.strings().get("tilesetEditor_probability"), this.terrainProbabilityField);
    terrainMetadata.add(terrainProbability);

    JPanel slots = new JPanel(new GridLayout(3, 3, 3, 3));
    slots.setOpaque(false);
    String[] labels = {
      Resources.strings().get("tilesetEditor_northShort"),
      Resources.strings().get("tilesetEditor_northEastShort"),
      Resources.strings().get("tilesetEditor_eastShort"),
      Resources.strings().get("tilesetEditor_southEastShort"),
      Resources.strings().get("tilesetEditor_southShort"),
      Resources.strings().get("tilesetEditor_southWestShort"),
      Resources.strings().get("tilesetEditor_westShort"),
      Resources.strings().get("tilesetEditor_northWestShort")};
    int[] layout = {7, 0, 1, 6, -1, 2, 5, 4, 3};
    for (int index : layout) {
      if (index < 0) {
        slots.add(new JLabel("", SwingConstants.CENTER));
        continue;
      }
      JButton slot = new JButton(labels[index]);
      Style.styleButton(slot, Style.ButtonVariant.SECONDARY);
      slot.getAccessibleContext().setAccessibleName(Resources.strings().get("tilesetEditor_assignTerrain", labels[index]));
      slot.addActionListener(_ -> applyTerrainSlot(index));
      this.terrainSlots[index] = slot;
      slots.add(slot);
    }
    slots.setPreferredSize(new Dimension(132, 96));
    slots.setMaximumSize(new Dimension(132, 96));

    JPanel selectors = new JPanel(new GridLayout(2, 1, 0, 4));
    selectors.setOpaque(false);
    selectors.add(sets);
    selectors.add(terrains);

    JPanel details = new JPanel(new BorderLayout(10, 0));
    details.setOpaque(false);
    details.add(slots, BorderLayout.WEST);
    details.add(terrainMetadata, BorderLayout.CENTER);

    JPanel content = new JPanel(new BorderLayout(0, 8));
    content.setOpaque(false);
    content.add(selectors, BorderLayout.NORTH);
    content.add(details, BorderLayout.CENTER);
    ExpandableCard panel = new ExpandableCard(Resources.strings().get("tilesetEditor_terrainEditing"), content, false);
    panel.setContentInsets(8, 0, 8, 0);
    return panel;
  }

  private static JLabel fieldLabel(String text, int width) {
    JLabel label = new JLabel(text);
    label.setForeground(Style.text());
    label.setPreferredSize(new Dimension(width, Style.CONTROL_HEIGHT));
    return label;
  }

  private void addTerrainSet() {
    if (this.tileset == null) {
      return;
    }
    WangSet terrainSet = new WangSet("Terrain Set " + (this.tileset.getOrCreateTerrainSets().size() + 1), TerrainType.MIXED);
    changeTileset(() -> this.tileset.getOrCreateTerrainSets().add(terrainSet));
    updateTerrainSets(terrainSet);
  }

  private void removeTerrainSet() {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    if (this.tileset == null || terrainSet == null) {
      return;
    }
    changeTileset(() -> this.tileset.getOrCreateTerrainSets().remove(terrainSet));
    updateTerrainSets(null);
  }

  private void addTerrain() {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    if (terrainSet == null) {
      return;
    }
    WangColor terrain = new WangColor("Terrain " + (terrainSet.getTerrains().size() + 1), Style.COLOR_GREEN);
    changeTileset(() -> terrainSet.getTerrains().add(terrain));
    updateTerrainControls();
    this.terrainCombo.setSelectedItem(terrain);
  }

  private void removeTerrain() {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    if (terrainSet == null || terrain == null) {
      return;
    }
    int removed = terrainSet.getTerrains().indexOf(terrain) + 1;
    changeTileset(() -> {
      terrainSet.getTerrains().remove(terrain);
      for (var wangTile : terrainSet.getWangTiles()) {
        int[] wangId = wangTile.getWangId();
        for (int i = 0; i < wangId.length; i++) {
          if (wangId[i] == removed) {
            wangId[i] = 0;
          } else if (wangId[i] > removed) {
            wangId[i]--;
          }
        }
        wangTile.setWangId(wangId);
      }
      terrainSet.getWangTiles().removeIf(wangTile -> java.util.Arrays.stream(wangTile.getWangId()).allMatch(id -> id == 0));
    });
    updateTerrainControls();
  }

  private void updateTerrainSets(WangSet selected) {
    this.binding = true;
    try {
      this.terrainSetCombo.removeAllItems();
      if (this.tileset != null && this.tileset.getTerrainSets() != null) {
        for (ITerrainSet terrainSet : this.tileset.getTerrainSets()) {
          if (terrainSet instanceof WangSet wangSet) {
            this.terrainSetCombo.addItem(wangSet);
          }
        }
      }
      this.terrainSetCombo.setSelectedItem(selected);
      if (selected == null && this.terrainSetCombo.getItemCount() > 0) {
        this.terrainSetCombo.setSelectedIndex(0);
      }
    } finally {
      this.binding = false;
    }
    updateTerrainControls();
  }

  private void updateTerrainControls() {
    if (this.binding) {
      return;
    }
    this.binding = true;
    try {
      WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
      WangColor selected = (WangColor) this.terrainCombo.getSelectedItem();
      this.terrainTypeCombo.setSelectedItem(terrainSet != null ? terrainSet.getType() : TerrainType.MIXED);
      this.terrainSetNameField.setText(terrainSet != null && terrainSet.getName() != null ? terrainSet.getName() : "");
      this.terrainCombo.removeAllItems();
      if (terrainSet != null) {
        for (var terrain : terrainSet.getTerrains()) {
          if (terrain instanceof WangColor color) {
            this.terrainCombo.addItem(color);
          }
        }
      }
      if (selected != null && terrainSet != null && terrainSet.getTerrains().contains(selected)) {
        this.terrainCombo.setSelectedItem(selected);
      } else if (this.terrainCombo.getItemCount() > 0) {
        this.terrainCombo.setSelectedIndex(0);
      }
    } finally {
      this.binding = false;
    }
    updateTerrainSelectionControls();
  }

  private void updateTerrainSelectionControls() {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    this.tileGrid.setTerrainSet(terrainSet);
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    ToolManager.instance().setSelectedTerrain(terrainSet, terrain);
    this.binding = true;
    try {
      this.terrainNameField.setText(terrain != null && terrain.getName() != null ? terrain.getName() : "");
      this.terrainProbabilityField.setText(terrain != null ? String.valueOf(terrain.getProbability()) : "");
      this.terrainColorButton.setBackground(terrain != null && terrain.getColor() != null ? terrain.getColor() : Style.COLOR_SURFACE);
      this.terrainColorButton.setEnabled(terrain != null);
    } finally {
      this.binding = false;
    }
    updateTerrainSlots();
  }

  private void applyTerrainSetType() {
    if (this.binding) {
      return;
    }
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    TerrainType type = (TerrainType) this.terrainTypeCombo.getSelectedItem();
    if (terrainSet == null || type == null || terrainSet.getType() == type) {
      return;
    }
    changeTileset(() -> terrainSet.setType(type));
    updateTerrainSlots();
  }

  private void applyTerrainSetName() {
    if (this.binding) {
      return;
    }
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    if (terrainSet == null) {
      return;
    }
    changeTileset(() -> terrainSet.setName(this.terrainSetNameField.getText().trim()));
    this.terrainSetCombo.repaint();
  }

  private void applyTerrainName() {
    if (this.binding) {
      return;
    }
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    if (terrain == null) {
      return;
    }
    changeTileset(() -> terrain.setName(this.terrainNameField.getText().trim()));
    this.terrainCombo.repaint();
    updateTerrainSlots();
  }

  private void applyTerrainProbability() {
    if (this.binding) {
      return;
    }
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    if (terrain == null) {
      return;
    }
    double probability = parseDouble(this.terrainProbabilityField.getText(), 1.0);
    if (!Double.isFinite(probability) || probability < 0) {
      this.terrainProbabilityField.setText(String.valueOf(terrain.getProbability()));
      return;
    }
    changeTileset(() -> terrain.setProbability(probability));
  }

  private void chooseTerrainColor() {
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    if (terrain == null) {
      return;
    }
    Color color = javax.swing.JColorChooser.showDialog(this, Resources.strings().get("tilesetEditor_terrainColorTitle"), terrain.getColor());
    if (color == null) {
      return;
    }
    changeTileset(() -> terrain.setColor(color));
    this.terrainColorButton.setBackground(color);
  }

  private void updateTerrainSlots() {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    if (terrainSet == null || this.tileset == null || this.tileGrid.selectedTile < 0) {
      for (JButton slot : this.terrainSlots) {
        slot.setText("");
        slot.setEnabled(false);
      }
      return;
    }
    var terrains = terrainSet.getTerrains(this.tileGrid.selectedTile);
    for (int index = 0; index < this.terrainSlots.length; index++) {
      boolean enabled = terrainSet.getType() == TerrainType.MIXED
        || terrainSet.getType() == TerrainType.EDGE && index % 2 == 0
        || terrainSet.getType() == TerrainType.CORNER && index % 2 == 1;
      WangColor terrain = terrains[index] instanceof WangColor color ? color : null;
      this.terrainSlots[index].setEnabled(enabled);
      this.terrainSlots[index].setText(terrain != null && terrain.getName() != null && !terrain.getName().isEmpty()
        ? terrain.getName().substring(0, 1).toUpperCase() : "-");
      this.terrainSlots[index].setToolTipText(terrain != null ? terrain.getName() : Resources.strings().get("tilesetEditor_emptyTerrain"));
    }
  }

  private void applyTerrainSlot(int index) {
    WangSet terrainSet = (WangSet) this.terrainSetCombo.getSelectedItem();
    WangColor terrain = (WangColor) this.terrainCombo.getSelectedItem();
    if (terrainSet == null || terrain == null || this.tileGrid.selectedTile < 0) {
      return;
    }
    int terrainIndex = terrainSet.getTerrains().indexOf(terrain) + 1;
    changeTileset(() -> {
      var wangTile = terrainSet.getOrCreateWangTile(this.tileGrid.selectedTile);
      wangTile.setTerrain(index, wangTile.getWangId()[index] == terrainIndex ? 0 : terrainIndex);
      terrainSet.removeWangTileIfEmpty(this.tileGrid.selectedTile);
    });
    updateTerrainSlots();
  }

  private void clearTilesetControls() {
    this.binding = true;
    try {
      this.tilesetNameField.setText("");
      this.tileOffsetXSpinner.setValue(0);
      this.tileOffsetYSpinner.setValue(0);
      this.tilesetPropertyModel.setRowCount(0);
    } finally {
      this.binding = false;
    }
  }

  private void bindTilesetControls() {
    this.binding = true;
    try {
      this.tilesetNameField.setText(this.tileset.getName() != null ? this.tileset.getName() : "");
      ITileOffset offset = this.tileset.getTileOffset();
      this.tileOffsetXSpinner.setValue(offset != null ? offset.getX() : 0);
      this.tileOffsetYSpinner.setValue(offset != null ? offset.getY() : 0);
      this.tilesetPropertyModel.setRowCount(0);
      for (Map.Entry<String, ICustomProperty> prop : new HashMap<>(this.tileset.getProperties()).entrySet()) {
        this.tilesetPropertyModel.addRow(new Object[] {prop.getKey(), prop.getValue().getAsString()});
      }
    } finally {
      this.binding = false;
    }
  }

  private void clearSelectedTileControls() {
    this.binding = true;
    try {
      this.detailLabel.setText(Resources.strings().get("tilesetEditor_noTileSelected"));
      this.previewLabel.setIcon(null);
      this.previewLabel.setText("");
      this.typeField.setText("");
      this.probabilityField.setText("");
      this.tilePropertyModel.setRowCount(0);
      this.animationModel.setRowCount(0);
    } finally {
      this.binding = false;
    }
  }

  private void updateSelectedTileControls() {
    if (this.tileset == null || this.tileGrid.selectedTile < 0) {
      clearSelectedTileControls();
      return;
    }

    int localId = this.tileGrid.selectedTile;
    ITilesetEntry entry = this.tileset.getTile(localId);
    String type = entry != null && entry.getType() != null && !entry.getType().isBlank() ? entry.getType() : "";
    String collision = entry != null && entry.getCollisionInfo() != null
      ? Resources.strings().get("tilesetEditor_collisionShapes",
        String.valueOf(entry.getCollisionInfo().getMapObjects().size()))
      : Resources.strings().get("tilesetEditor_none");
    String animation = animationSummary(entry);

    this.binding = true;
    try {
      this.detailLabel.setText(Resources.strings().get("tilesetEditor_tileSummary", String.valueOf(localId),
        "gid " + (this.tileset.getFirstGridId() + localId), collision, animation));
      this.typeField.setText(type);
      this.probabilityField.setText(entry instanceof TilesetEntry tilesetEntry ? String.valueOf(tilesetEntry.getProbability()) : "1.0");
      this.tilePropertyModel.setRowCount(0);
      this.animationModel.setRowCount(0);
      if (entry != null) {
        for (Map.Entry<String, ICustomProperty> prop : new HashMap<>(entry.getProperties()).entrySet()) {
          this.tilePropertyModel.addRow(new Object[] {prop.getKey(), prop.getValue().getAsString()});
        }
        if (entry.getAnimation() != null && entry.getAnimation().getFrames() != null) {
          for (ITileAnimationFrame frame : entry.getAnimation().getFrames()) {
            if (frame != null) {
              this.animationModel.addRow(new Object[] {String.valueOf(frame.getTileId()), String.valueOf(frame.getDuration())});
            }
          }
        }
      }
    } finally {
      this.binding = false;
    }

    updateSelectedTilePreview();
    updateTerrainSlots();
    publishSelectedTile();
  }

  private void publishSelectedTile() {
    if (this.tileset == null || this.tileGrid.selectedTile < 0) {
      ToolManager.instance().setSelectedTileStamp(TileStamp.empty(), 0);
      return;
    }
    ToolManager.instance().setSelectedTileStamp(
        selectedTileStamp(), this.tileset.getFirstGridId() + this.tileGrid.selectedTile);
  }

  void publishToolSelection() {
    int primaryGid = this.tileset != null && this.tileGrid.selectedTile >= 0
        ? this.tileset.getFirstGridId() + this.tileGrid.selectedTile
        : 0;
    ToolManager.instance().setToolSelection(
        selectedTileStamp(),
        primaryGid,
        (WangSet) this.terrainSetCombo.getSelectedItem(),
        (WangColor) this.terrainCombo.getSelectedItem());
  }

  private TileStamp selectedTileStamp() {
    return this.tileset != null
        ? this.tileGrid.createStamp(this.tileset.getFirstGridId())
        : TileStamp.empty();
  }

  private void updateSelectedTilePreview() {
    if (this.tileset == null || this.tileGrid.selectedTile < 0) {
      this.previewLabel.setIcon(null);
      return;
    }
    BufferedImage image = getPreviewImage(this.tileGrid.selectedTile);
    this.previewLabel.setIcon(image != null ? new ImageIcon(image.getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH)) : null);
  }

  private BufferedImage getPreviewImage(int tile) {
    ITilesetEntry entry = this.tileset.getTile(tile);
    if (entry != null && entry.getAnimation() != null && entry.getAnimation().getFrames() != null && !entry.getAnimation().getFrames().isEmpty()) {
      int frameTile = animatedFrameTile(entry.getAnimation());
      return this.tileGrid.getTileImage(frameTile);
    }
    return this.tileGrid.getTileImage(tile);
  }

  private static int animatedFrameTile(ITileAnimation animation) {
    int totalDuration = 0;
    for (ITileAnimationFrame frame : animation.getFrames()) {
      if (frame != null && frame.getDuration() > 0) {
        totalDuration += frame.getDuration();
      }
    }
    if (totalDuration <= 0) {
      return animation.getFrames().getFirst().getTileId();
    }
    long time = System.currentTimeMillis() % totalDuration;
    for (ITileAnimationFrame frame : animation.getFrames()) {
      if (frame == null || frame.getDuration() <= 0) {
        continue;
      }
      time -= frame.getDuration();
      if (time < 0) {
        return frame.getTileId();
      }
    }
    return animation.getFrames().getFirst().getTileId();
  }

  private TilesetEntry selectedEntry() {
    if (this.tileset == null || this.tileGrid.selectedTile < 0) {
      return null;
    }
    ITilesetEntry entry = this.tileset.getTile(this.tileGrid.selectedTile);
    return entry instanceof TilesetEntry tilesetEntry ? tilesetEntry : null;
  }

  private static String animationSummary(ITilesetEntry entry) {
    if (entry == null || entry.getAnimation() == null) {
      return Resources.strings().get("tilesetEditor_none");
    }
    ITileAnimation animation = entry.getAnimation();
    if (animation.getFrames() == null || animation.getFrames().isEmpty()) {
      return Resources.strings().get("tilesetEditor_animationSummary", "0", "0ms");
    }
    int duration = 0;
    for (ITileAnimationFrame frame : animation.getFrames()) {
      if (frame != null) {
        duration += frame.getDuration();
      }
    }
    return Resources.strings().get("tilesetEditor_animationSummary",
      String.valueOf(animation.getFrames().size()), duration + "ms");
  }

  private static String valueOrEmpty(String value) {
    return value != null ? value : "";
  }

  private void applyType() {
    if (this.binding) {
      return;
    }
    TilesetEntry entry = selectedEntry();
    if (entry == null) {
      return;
    }
    changeTileset(() -> entry.setType(this.typeField.getText().trim()));
    updateSelectedTileControls();
  }

  private void applyTilesetName() {
    if (this.binding || this.tileset == null) {
      return;
    }
    changeTileset(() -> this.tileset.setName(this.tilesetNameField.getText().trim()));
    this.titleLabel.setText(this.tileset.getName() != null && !this.tileset.getName().isBlank()
      ? this.tileset.getName() : Resources.strings().get("tilesetEditor_unnamedTileset"));
    this.tilesetNameChanged.run();
  }

  private void applyTilesetRenderSettings() {
    if (this.binding || this.tileset == null) {
      return;
    }
    changeTileset(() -> {
      this.tileset.setTileOffset((int) this.tileOffsetXSpinner.getValue(), (int) this.tileOffsetYSpinner.getValue());
    });
    bindTilesetControls();
  }

  private void applyProbability() {
    if (this.binding) {
      return;
    }
    TilesetEntry entry = selectedEntry();
    if (entry == null) {
      return;
    }
    changeTileset(() -> entry.setProbability(parseDouble(this.probabilityField.getText(), 1.0)));
    updateSelectedTileControls();
  }

  private static int parseInt(String value, int fallback) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static double parseDouble(String value, double fallback) {
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private void applyTilesetProperties() {
    if (this.binding || this.tileset == null) {
      return;
    }
    changeTileset(() -> applyProperties(this.tileset, this.tilesetPropertyModel));
  }

  private void applyTileProperties() {
    if (this.binding) {
      return;
    }
    TilesetEntry entry = selectedEntry();
    if (entry == null) {
      return;
    }
    changeTileset(() -> applyProperties(entry, this.tilePropertyModel));
  }

  private void applyAnimationFrames() {
    if (this.binding) {
      return;
    }
    TilesetEntry entry = selectedEntry();
    if (entry == null) {
      return;
    }
    List<ITileAnimationFrame> frames = new ArrayList<>();
    for (int row = 0; row < this.animationModel.getRowCount(); row++) {
      int tileId = parseInt(String.valueOf(this.animationModel.getValueAt(row, 0)), -1);
      int duration = parseInt(String.valueOf(this.animationModel.getValueAt(row, 1)), 100);
      if (tileId >= 0 && duration > 0) {
        frames.add(new Frame(tileId, duration));
      }
    }
    changeTileset(() -> entry.setAnimation(frames.isEmpty() ? null : new TileAnimation(frames)));
    updateSelectedTilePreview();
    this.tileGrid.repaint();
  }

  private void changeTileset(Runnable change) {
    Tileset before = new Tileset(this.tileset);
    change.run();
    Tileset after = new Tileset(this.tileset);
    if (Game.world().environment() != null) {
      UndoManager.instance().resourceChanged(
        () -> restoreTileset(before),
        () -> restoreTileset(after));
    }
  }

  private void restoreTileset(Tileset snapshot) {
    this.tileset.copyFrom(snapshot);
    bind(this.tileset);
  }

  private static void applyProperties(de.gurkenlabs.litiengine.environment.tilemap.ICustomPropertyProvider target, DefaultTableModel model) {
    List<String> setProperties = new ArrayList<>();
    for (int row = 0; row < model.getRowCount(); row++) {
      Object nameObj = model.getValueAt(row, 0);
      String name = nameObj != null ? nameObj.toString().trim() : "";
      Object valueObj = model.getValueAt(row, 1);
      String value = valueObj != null ? valueObj.toString() : "";
      if (!name.isEmpty()) {
        setProperties.add(name);
        target.setValue(name, value);
      }
    }
    target.getProperties().keySet().removeIf(p -> !setProperties.contains(p));
  }

  private void removeSelectedProperty(JTable table, DefaultTableModel model) {
    int row = table.getSelectedRow();
    if (row < 0 || row >= model.getRowCount()) {
      return;
    }
    model.removeRow(row);
  }

  private static final class TileGrid extends JPanel {
    private static final int CELL_PADDING = 4;
    private static final int MIN_CELL_SIZE = 24;
    private static final Color ANIMATION_TILE_COLOR = new Color(255, 181, 71);
    private static final Color SELECTED_ANIMATION_FRAME_FILL = new Color(155, 95, 255, 70);
    private static final Color SELECTED_ANIMATION_FRAME_BORDER = new Color(190, 150, 255);
    private Tileset tileset;
    private WangSet terrainSet;
    private int selectedTile = -1;
    private int selectionAnchor = -1;
    private final Set<Integer> selectedTiles = new TreeSet<>();
    private Set<Integer> selectionBeforeDrag = Set.of();
    private int dragStart = -1;
    private DragMode dragMode = DragMode.REPLACE;
    private boolean dragging;
    private Runnable selectionChanged;
    private float zoom = 1f;

    private TileGrid() {
      setOpaque(true);
      setBackground(Style.surface());
      addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) {
            return;
          }
          int tile = tileAt(e.getX(), e.getY(), false);
          if (tile < 0) {
            return;
          }
          beginSelection(tile, e.isControlDown(), e.isShiftDown());
        }

        @Override public void mouseReleased(MouseEvent e) {
          if (!dragging) {
            return;
          }
          int tile = tileAt(e.getX(), e.getY(), true);
          if (tile >= 0) {
            updateDragSelection(tile);
          }
          dragging = false;
          fireSelectionChanged();
        }
      });
      addMouseMotionListener(new MouseMotionAdapter() {
        @Override public void mouseDragged(MouseEvent e) {
          if (!dragging) {
            return;
          }
          int tile = tileAt(e.getX(), e.getY(), true);
          if (tile >= 0) {
            updateDragSelection(tile);
          }
        }
      });
    }

    @Override public void updateUI() {
      super.updateUI();
      setBackground(Style.surface());
    }

    private void setSelectionChanged(Runnable selectionChanged) {
      this.selectionChanged = selectionChanged;
    }

    private void bind(Tileset tileset) {
      this.tileset = tileset;
      this.selectedTile = tileset != null && tileset.getTileCount() > 0 ? 0 : -1;
      this.selectionAnchor = this.selectedTile;
      this.selectedTiles.clear();
      if (this.selectedTile >= 0) {
        this.selectedTiles.add(this.selectedTile);
      }
      revalidate();
      repaint();
    }

    private void setTerrainSet(WangSet terrainSet) {
      this.terrainSet = terrainSet;
      repaint();
    }

    private int tileAt(int x, int y, boolean clamp) {
      if (this.tileset == null || this.tileset.getTileCount() <= 0) {
        return -1;
      }
      int cell = cellSize();
      int columns = columns();
      int rows = (int) Math.ceil(this.tileset.getTileCount() / (double) columns);
      if (!clamp && (x < 0 || y < 0 || x >= columns * cell || y >= rows * cell)) {
        return -1;
      }
      int col = Math.clamp(Math.floorDiv(x, cell), 0, columns - 1);
      int row = Math.clamp(Math.floorDiv(y, cell), 0, rows - 1);
      int tile = row * columns + col;
      return Math.min(tile, this.tileset.getTileCount() - 1);
    }

    private void selectTile(int tile) {
      if (tile < 0 || tile >= this.tileset.getTileCount()) {
        return;
      }
      this.selectedTile = tile;
      this.selectionAnchor = tile;
      this.selectedTiles.clear();
      this.selectedTiles.add(tile);
      repaint();
      fireSelectionChanged();
    }

    private void selectTiles(int anchor, int lead, boolean control, boolean shift) {
      if (this.tileset == null
          || anchor < 0
          || anchor >= this.tileset.getTileCount()
          || lead < 0
          || lead >= this.tileset.getTileCount()) {
        return;
      }
      this.selectionAnchor = anchor;
      this.selectionBeforeDrag = control ? new TreeSet<>(this.selectedTiles) : Set.of();
      this.dragStart = anchor;
      this.dragMode = shift
          ? DragMode.REPLACE
          : control && this.selectedTiles.contains(anchor) ? DragMode.REMOVE
          : control ? DragMode.ADD
          : DragMode.REPLACE;
      updateDragSelection(lead);
      fireSelectionChanged();
    }

    private void beginSelection(int tile, boolean control, boolean shift) {
      this.selectionBeforeDrag = new TreeSet<>(this.selectedTiles);
      this.dragStart = tile;
      if (shift && this.selectionAnchor >= 0) {
        this.dragStart = this.selectionAnchor;
        this.dragMode = DragMode.REPLACE;
      } else if (control) {
        this.selectionAnchor = tile;
        this.dragMode = this.selectedTiles.contains(tile) ? DragMode.REMOVE : DragMode.ADD;
      } else {
        this.selectionAnchor = tile;
        this.dragMode = DragMode.REPLACE;
      }
      this.dragging = true;
      updateDragSelection(tile);
    }

    private void updateDragSelection(int lead) {
      Set<Integer> updated = this.dragMode == DragMode.REPLACE
          ? new TreeSet<>()
          : new TreeSet<>(this.selectionBeforeDrag);
      Set<Integer> rectangle = rectangularTiles(this.dragStart, lead);
      if (this.dragMode == DragMode.REMOVE) {
        updated.removeAll(rectangle);
      } else {
        updated.addAll(rectangle);
      }
      this.selectedTiles.clear();
      this.selectedTiles.addAll(updated);
      this.selectedTile = this.selectedTiles.contains(lead)
          ? lead
          : this.selectedTiles.stream().findFirst().orElse(-1);
      repaint();
    }

    private Set<Integer> rectangularTiles(int first, int second) {
      Set<Integer> rectangle = new TreeSet<>();
      int columns = columns();
      int minColumn = Math.min(first % columns, second % columns);
      int maxColumn = Math.max(first % columns, second % columns);
      int minRow = Math.min(first / columns, second / columns);
      int maxRow = Math.max(first / columns, second / columns);
      for (int row = minRow; row <= maxRow; row++) {
        for (int column = minColumn; column <= maxColumn; column++) {
          int tile = row * columns + column;
          if (tile < this.tileset.getTileCount()) {
            rectangle.add(tile);
          }
        }
      }
      return rectangle;
    }

    private TileStamp createStamp(int firstGid) {
      if (this.selectedTiles.isEmpty()) {
        return TileStamp.empty();
      }
      int columns = columns();
      int minColumn = this.selectedTiles.stream().mapToInt(tile -> tile % columns).min().orElse(0);
      int maxColumn = this.selectedTiles.stream().mapToInt(tile -> tile % columns).max().orElse(0);
      int minRow = this.selectedTiles.stream().mapToInt(tile -> tile / columns).min().orElse(0);
      int maxRow = this.selectedTiles.stream().mapToInt(tile -> tile / columns).max().orElse(0);
      int width = maxColumn - minColumn + 1;
      int height = maxRow - minRow + 1;
      List<Integer> gids = new ArrayList<>(width * height);
      for (int row = minRow; row <= maxRow; row++) {
        for (int column = minColumn; column <= maxColumn; column++) {
          int tile = row * columns + column;
          gids.add(tile < this.tileset.getTileCount() && this.selectedTiles.contains(tile)
              ? firstGid + tile
              : 0);
        }
      }
      return new TileStamp(width, height, gids);
    }

    private void fireSelectionChanged() {
      if (this.selectionChanged != null) {
        this.selectionChanged.run();
      }
    }

    @Override public Dimension getPreferredSize() {
      if (this.tileset == null || this.tileset.getTileCount() <= 0) {
        return new Dimension(360, 220);
      }
      int cell = cellSize();
      int columns = columns();
      int rows = (int) Math.ceil(this.tileset.getTileCount() / (double) columns);
      return new Dimension(columns * cell, rows * cell);
    }

    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (this.tileset == null) {
        return;
      }
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int cell = cellSize();
      int columns = columns();
      Set<Integer> selectedAnimationFrames = selectedAnimationFrameTiles();
      for (int i = 0; i < this.tileset.getTileCount(); i++) {
        int x = i % columns * cell;
        int y = i / columns * cell;
        BufferedImage image = getTileImage(i);
        if (image != null) {
          g2.drawImage(image, x + CELL_PADDING, y + CELL_PADDING, cell - CELL_PADDING * 2, cell - CELL_PADDING * 2, null);
        }
        paintTerrain(g2, i, x, y, cell);
        if (selectedAnimationFrames.contains(i)) {
          g2.setColor(SELECTED_ANIMATION_FRAME_FILL);
          g2.fillRect(x + 1, y + 1, cell - 2, cell - 2);
          g2.setColor(SELECTED_ANIMATION_FRAME_BORDER);
          g2.setStroke(new BasicStroke(2f));
          g2.drawRect(x + 1, y + 1, cell - 3, cell - 3);
          g2.setStroke(new BasicStroke(1f));
        }
        if (definesAnimation(i)) {
          g2.setColor(ANIMATION_TILE_COLOR);
          int triangleX = x + cell - 10;
          int triangleY = y + 4;
          g2.fillPolygon(
            new int[] {triangleX, triangleX, triangleX + 6},
            new int[] {triangleY, triangleY + 8, triangleY + 4},
            3);
        }
        boolean selected = this.selectedTiles.contains(i);
        if (selected) {
          g2.setColor(new Color(80, 145, 255, 50));
          g2.fillRect(x + 1, y + 1, cell - 2, cell - 2);
        }
        g2.setColor(selected ? Style.accent() : Style.border());
        g2.setStroke(new BasicStroke(i == this.selectedTile ? 2f : 1f));
        g2.drawRect(x, y, cell - 1, cell - 1);
        g2.setStroke(new BasicStroke(1f));
      }
      g2.dispose();
    }

    private void paintTerrain(Graphics2D g2, int tile, int x, int y, int cell) {
      if (this.terrainSet == null) {
        return;
      }
      var terrains = this.terrainSet.getTerrains(tile);
      int markerSize = Math.max(4, Math.round(cell * 0.16f));
      int[] offsetsX = {50, 78, 94, 78, 50, 22, 6, 22};
      int[] offsetsY = {6, 22, 50, 78, 94, 78, 50, 22};
      for (int index = 0; index < terrains.length; index++) {
        if (!(terrains[index] instanceof WangColor terrain) || terrain.getColor() == null) {
          continue;
        }
        int markerX = x + offsetsX[index] * (cell - markerSize) / 100;
        int markerY = y + offsetsY[index] * (cell - markerSize) / 100;
        g2.setColor(terrain.getColor());
        g2.fillOval(markerX, markerY, markerSize, markerSize);
        g2.setColor(Color.BLACK);
        g2.drawOval(markerX, markerY, markerSize, markerSize);
      }
    }

    private boolean definesAnimation(int tile) {
      if (this.tileset == null || tile < 0 || tile >= this.tileset.getTileCount()) {
        return false;
      }
      ITilesetEntry entry = this.tileset.getTile(tile);
      return entry != null && entry.getAnimation() != null && entry.getAnimation().getFrames() != null && !entry.getAnimation().getFrames().isEmpty();
    }

    private Set<Integer> selectedAnimationFrameTiles() {
      Set<Integer> frames = new HashSet<>();
      if (!definesAnimation(this.selectedTile)) {
        return frames;
      }
      ITilesetEntry entry = this.tileset.getTile(this.selectedTile);
      for (ITileAnimationFrame frame : entry.getAnimation().getFrames()) {
        if (frame != null && frame.getTileId() >= 0 && frame.getTileId() < this.tileset.getTileCount()) {
          frames.add(frame.getTileId());
        }
      }
      return frames;
    }

    private BufferedImage getTileImage(int tile) {
      if (this.tileset == null) {
        return null;
      }
      ITilesetEntry entry = this.tileset.getTile(tile);
      if (entry != null) {
        try {
          BufferedImage image = entry.getBasicImage();
          if (image != null) {
            return image;
          }
        } catch (RuntimeException ignored) {
          // Missing/incomplete tileset images should not break the inspector.
        }
      }
      Spritesheet sheet = this.tileset.getSpritesheet();
      return sheet != null ? sheet.getSprite(tile, this.tileset.getMargin(), this.tileset.getSpacing()) : null;
    }

    private int cellSize() {
      if (this.tileset == null) {
        return 32;
      }
      return Math.max(8, Math.round((Math.max(this.tileset.getTileWidth(), this.tileset.getTileHeight()) + CELL_PADDING * 2) * this.zoom));
    }

    private int columns() {
      if (this.tileset == null || this.tileset.getColumns() <= 0) {
        return 1;
      }
      return this.tileset.getColumns();
    }

    private enum DragMode {
      REPLACE,
      ADD,
      REMOVE
    }
  }
}
