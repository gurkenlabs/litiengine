package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;
import de.gurkenlabs.litiengine.environment.tilemap.ITileOffset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Frame;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileAnimation;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
  private final TileGrid tileGrid;
  private final Timer animationTimer;
  private Tileset tileset;
  private boolean binding;

  public TilesetEditorPanel() {
    super(new BorderLayout(0, 8));
    setOpaque(true);
    setBackground(Style.COLOR_BG);
    setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

    JPanel info = new JPanel();
    info.setOpaque(false);
    info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
    this.titleLabel = new JLabel("No tileset selected");
    this.titleLabel.setForeground(Style.COLOR_TEXT);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD));
    this.metaLabel = new JLabel("Select a tileset asset to inspect its tiles.");
    this.metaLabel.setForeground(Style.COLOR_SUBTEXT);
    info.add(this.titleLabel);
    info.add(this.metaLabel);

    this.tilesetNameField = new JTextField();
    this.tilesetNameField.addActionListener(_ -> applyTilesetName());
    this.tilesetNameField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        applyTilesetName();
      }
    });
    JPanel namePanel = labeledField("Name", this.tilesetNameField);

    this.tileOffsetXSpinner = new JSpinner(new SpinnerNumberModel(0, -100000, 100000, 1));
    this.tileOffsetYSpinner = new JSpinner(new SpinnerNumberModel(0, -100000, 100000, 1));
    this.tileOffsetXSpinner.addChangeListener(_ -> applyTilesetRenderSettings());
    this.tileOffsetYSpinner.addChangeListener(_ -> applyTilesetRenderSettings());
    JPanel renderSettings = labeledOffsets();

    this.tilesetPropertyModel = createPropertyModel();
    this.tilesetPropertyModel.addTableModelListener(_ -> applyTilesetProperties());
    this.tilesetPropertyTable = createPropertyTable(this.tilesetPropertyModel);
    ExpandableCard tilesetProperties = new ExpandableCard("Tileset Properties", createTablePanel(this.tilesetPropertyTable, this.tilesetPropertyModel,
      () -> this.tilesetPropertyModel.addRow(new Object[] {"", ""})), false);
    tilesetProperties.setContentInsets(8, 0, 8, 0);

    JPanel tilesetControls = new JPanel(new BorderLayout(0, 6));
    tilesetControls.setOpaque(false);
    tilesetControls.add(info, BorderLayout.NORTH);
    tilesetControls.add(namePanel, BorderLayout.CENTER);
    JPanel lowerTilesetControls = new JPanel(new BorderLayout(0, 6));
    lowerTilesetControls.setOpaque(false);
    lowerTilesetControls.add(renderSettings, BorderLayout.NORTH);
    lowerTilesetControls.add(tilesetProperties, BorderLayout.CENTER);
    tilesetControls.add(lowerTilesetControls, BorderLayout.SOUTH);
    add(tilesetControls, BorderLayout.NORTH);

    this.tileGrid = new TileGrid();
    this.tileGrid.setSelectionChanged(this::updateSelectedTileControls);
    JScrollPane gridScroll = new JScrollPane(this.tileGrid);
    gridScroll.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    gridScroll.getViewport().setBackground(Style.COLOR_SURFACE);
    add(gridScroll, BorderLayout.CENTER);

    this.previewLabel = new JLabel("", SwingConstants.CENTER);
    this.previewLabel.setOpaque(true);
    this.previewLabel.setBackground(Style.COLOR_SURFACE);
    this.previewLabel.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    this.previewLabel.setPreferredSize(new Dimension(0, 112));

    this.detailLabel = new JLabel(" ");
    this.detailLabel.setForeground(Style.COLOR_SUBTEXT);

    this.typeField = new JTextField();
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
    this.animationModel = new DefaultTableModel(new Object[][] {}, new String[] {"Tile", "Duration"}) {
      @Override public Class<?> getColumnClass(int columnIndex) {
        return String.class;
      }
    };
    this.animationModel.addTableModelListener(_ -> applyAnimationFrames());
    this.animationTable = createPropertyTable(this.animationModel);

    JPanel typePanel = labeledField("Class", this.typeField);
    JPanel probabilityPanel = labeledField("Probability", this.probabilityField);

    JPanel selectedTilePanel = new JPanel(new BorderLayout(0, 8));
    selectedTilePanel.setOpaque(false);
    selectedTilePanel.add(this.previewLabel, BorderLayout.NORTH);
    JPanel controls = new JPanel(new BorderLayout(0, 6));
    controls.setOpaque(false);
    controls.add(this.detailLabel, BorderLayout.NORTH);
    JPanel tileMetadata = new JPanel(new GridLayout(2, 1, 0, 4));
    tileMetadata.setOpaque(false);
    tileMetadata.add(typePanel);
    tileMetadata.add(probabilityPanel);
    controls.add(tileMetadata, BorderLayout.CENTER);
    ExpandableCard propertyPanel = new ExpandableCard("Tile Properties", createTablePanel(this.tilePropertyTable, this.tilePropertyModel,
      () -> this.tilePropertyModel.addRow(new Object[] {"", ""})), false);
    propertyPanel.setContentInsets(8, 0, 8, 0);
    ExpandableCard animationPanel = new ExpandableCard("Tile Animation", createTablePanel(this.animationTable, this.animationModel,
      () -> this.animationModel.addRow(new Object[] {String.valueOf(Math.max(0, this.tileGrid.selectedTile)), "100"})), false);
    animationPanel.setContentInsets(8, 0, 8, 0);
    JPanel lowerTileControls = new JPanel();
    lowerTileControls.setLayout(new BoxLayout(lowerTileControls, BoxLayout.Y_AXIS));
    lowerTileControls.setOpaque(false);
    lowerTileControls.add(propertyPanel);
    lowerTileControls.add(javax.swing.Box.createVerticalStrut(6));
    lowerTileControls.add(animationPanel);
    controls.add(lowerTileControls, BorderLayout.SOUTH);
    selectedTilePanel.add(controls, BorderLayout.CENTER);
    add(selectedTilePanel, BorderLayout.SOUTH);

    this.animationTimer = new Timer(80, _ -> updateSelectedTilePreview());
    this.animationTimer.start();
  }

  public void bind(Tileset tileset) {
    this.tileset = tileset;
    this.tileGrid.bind(tileset);
    publishSelectedTile();
    if (tileset == null) {
      this.titleLabel.setText("No tileset selected");
      this.metaLabel.setText("Select a tileset asset to inspect its tiles.");
      clearTilesetControls();
      clearSelectedTileControls();
      return;
    }

    this.titleLabel.setText(tileset.getName() != null ? tileset.getName() : "Unnamed tileset");
    String source = tileset.getImage() != null ? tileset.getImage().getSource() : "No image";
    this.metaLabel.setText(
      source + "  •  " + tileset.getTileWidth() + "x" + tileset.getTileHeight()
        + "  •  " + tileset.getTileCount() + " tiles  •  " + tileset.getColumns() + " columns"
        + "  •  spacing " + tileset.getSpacing() + "  •  margin " + tileset.getMargin()
        + "  •  " + (tileset.isExternal() ? "external" : "embedded")
        + "  •  terrain sets " + (tileset.getTerrainSets() != null ? tileset.getTerrainSets().size() : 0));
    bindTilesetControls();
    updateSelectedTileControls();
  }

  int getSelectedTileIdForTest() {
    return this.tileGrid.selectedTile;
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

  private static DefaultTableModel createPropertyModel() {
    return new DefaultTableModel(new Object[][] {}, new String[] {"Name", "Value"}) {
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
    table.setRowHeight((int) (table.getRowHeight() * 1.1));
    return table;
  }

  private static JTextField metadataField(Runnable apply) {
    JTextField field = new JTextField();
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
    label.setForeground(Style.COLOR_TEXT);
    label.setPreferredSize(new Dimension(72, 24));
    panel.add(label, BorderLayout.WEST);
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel labeledOffsets() {
    JPanel panel = new JPanel(new BorderLayout(6, 0));
    panel.setOpaque(false);
    JLabel label = new JLabel("Offset");
    label.setForeground(Style.COLOR_TEXT);
    label.setPreferredSize(new Dimension(72, 24));
    panel.add(label, BorderLayout.WEST);

    JPanel values = new JPanel();
    values.setOpaque(false);
    values.setLayout(new BoxLayout(values, BoxLayout.X_AXIS));
    JLabel xLabel = new JLabel("x");
    JLabel yLabel = new JLabel("y");
    xLabel.setForeground(Style.COLOR_SUBTEXT);
    yLabel.setForeground(Style.COLOR_SUBTEXT);
    this.tileOffsetXSpinner.setMaximumSize(new Dimension(112, this.tileOffsetXSpinner.getPreferredSize().height));
    this.tileOffsetYSpinner.setMaximumSize(new Dimension(112, this.tileOffsetYSpinner.getPreferredSize().height));
    values.add(xLabel);
    values.add(javax.swing.Box.createHorizontalStrut(4));
    values.add(this.tileOffsetXSpinner);
    values.add(javax.swing.Box.createHorizontalStrut(12));
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
    propertyScroll.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    propertyPanel.add(propertyScroll, BorderLayout.CENTER);
    return propertyPanel;
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
      this.detailLabel.setText("No tile selected");
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
      ? entry.getCollisionInfo().getMapObjects().size() + " shapes"
      : "none";
    String animation = animationSummary(entry);

    this.binding = true;
    try {
      this.detailLabel.setText(
        "Tile " + localId + "  •  gid " + (this.tileset.getFirstGridId() + localId)
          + "  •  collision " + collision + "  •  animation " + animation);
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
    publishSelectedTile();
  }

  private void publishSelectedTile() {
    if (this.tileset == null || this.tileGrid.selectedTile < 0) {
      ToolManager.instance().setSelectedTileGid(0);
      return;
    }
    ToolManager.instance().setSelectedTileGid(this.tileset.getFirstGridId() + this.tileGrid.selectedTile);
    ToolManager.instance().getTools().stream()
      .filter(tool -> tool instanceof de.gurkenlabs.utiliti.controller.tool.StampBrushTool)
      .findFirst()
      .ifPresent(ToolManager.instance()::setActiveTool);
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
      return "none";
    }
    ITileAnimation animation = entry.getAnimation();
    if (animation.getFrames() == null || animation.getFrames().isEmpty()) {
      return "0 frames / 0ms";
    }
    int duration = 0;
    for (ITileAnimationFrame frame : animation.getFrames()) {
      if (frame != null) {
        duration += frame.getDuration();
      }
    }
    return animation.getFrames().size() + " frames / " + duration + "ms";
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
    this.titleLabel.setText(this.tileset.getName() != null && !this.tileset.getName().isBlank() ? this.tileset.getName() : "Unnamed tileset");
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
    private int selectedTile = -1;
    private Runnable selectionChanged;

    private TileGrid() {
      setOpaque(true);
      setBackground(Style.COLOR_SURFACE);
      addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
          selectTileAt(e.getX(), e.getY());
        }
      });
    }

    private void setSelectionChanged(Runnable selectionChanged) {
      this.selectionChanged = selectionChanged;
    }

    private void bind(Tileset tileset) {
      this.tileset = tileset;
      this.selectedTile = tileset != null && tileset.getTileCount() > 0 ? 0 : -1;
      revalidate();
      repaint();
    }

    private void selectTileAt(int x, int y) {
      if (this.tileset == null || this.tileset.getTileCount() <= 0) {
        return;
      }
      int cell = cellSize();
      int columns = columns();
      int col = x / cell;
      int row = y / cell;
      int tile = row * columns + col;
      if (tile < 0 || tile >= this.tileset.getTileCount()) {
        return;
      }
      this.selectedTile = tile;
      repaint();
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
          g2.fillOval(x + cell - 10, y + 3, 6, 6);
        }
        if (i == this.selectedTile) {
          g2.setColor(new Color(80, 145, 255, 50));
          g2.fillRect(x + 1, y + 1, cell - 2, cell - 2);
        }
        g2.setColor(i == this.selectedTile ? Style.COLOR_ACCENT_BLUE : Style.COLOR_BORDER);
        g2.drawRect(x, y, cell - 1, cell - 1);
      }
      g2.dispose();
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
      return Math.max(MIN_CELL_SIZE, Math.max(this.tileset.getTileWidth(), this.tileset.getTileHeight()) + CELL_PADDING * 2);
    }

    private int columns() {
      if (this.tileset == null || this.tileset.getColumns() <= 0) {
        return 1;
      }
      return this.tileset.getColumns();
    }
  }
}
