package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.TransferHandler;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class MapPropertyPanel extends JPanel {
  private static final int CONTENT_WIDTH =
      PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH + PropertyPanel.CONTROL_WIDTH;

  private final AmbientLightPreviewPanel ambientlightPreview;
  private final JScrollPane scrollPane;
  private final ExpandableCard generalCard;
  private final JSpinner spinnerGravity;
  private final ColorComponent ambientColorComponent;
  private final JTextArea textFieldDesc;
  private final JTextField textFieldName;
  private final ColorComponent shadowColorComponent;
  private final JTextField textFieldTitle;
  private final JTable tableCustomProperties;
  private final DefaultTableModel model;
  private JTabbedPane tilesetTabs;
  private final TilesetTabsPanel tilesetPanel;
  private java.util.function.Consumer<IMap> tilesetsChanged = UI::mapTilesetsChanged;

  private transient IMap dataSource;
  private boolean binding;

  public MapPropertyPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    setOpaque(true);
    setBackground(Style.COLOR_BG);
    this.tilesetPanel = new TilesetTabsPanel();

    this.textFieldName = ControlBehavior.apply(new JTextField());
    this.textFieldTitle = ControlBehavior.apply(new JTextField());
    this.textFieldDesc = new JTextArea() {
      @Override public void updateUI() {
        super.updateUI();
        setBackground(Style.COLOR_SURFACE2);
        setForeground(Style.COLOR_TEXT);
        setCaretColor(Style.COLOR_TEXT);
      }
    };
    this.textFieldDesc.setLineWrap(true);
    this.textFieldDesc.setWrapStyleWord(true);
    this.textFieldDesc.setMargin(new java.awt.Insets(4, 4, 4, 4));
    JScrollPane scrollPaneDesc = new JScrollPane(this.textFieldDesc);
    scrollPaneDesc.setBorder(new RoundedBorder(Style.COLOR_BORDER, 8, 1));
    scrollPaneDesc.getViewport().setBackground(Style.COLOR_SURFACE2);

    this.spinnerGravity = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    ControlBehavior.apply(this.spinnerGravity);

    this.ambientlightPreview = new AmbientLightPreviewPanel();
    this.ambientColorComponent = new ColorComponent(AmbientLight.DEFAULT_COLOR);
    this.ambientColorComponent.addActionListener(
        a -> {
          this.ambientlightPreview.setAmbientColor(this.ambientColorComponent.getColor());
          this.saveChanges();
        });

    this.shadowColorComponent = new ColorComponent(StaticShadow.DEFAULT_COLOR);
    this.shadowColorComponent.addActionListener(
        a -> {
          this.ambientlightPreview.setStaticShadowColor(this.shadowColorComponent.getColor());
          this.saveChanges();
        });

    JButton buttonAdd = Style.textButton("+");
    JButton buttonRemove = Style.textButton("−");

    this.scrollPane = new JScrollPane();
    this.tableCustomProperties = createPropertiesTable();
    this.scrollPane.setViewportView(this.tableCustomProperties);
    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();

    buttonAdd.addActionListener(
        a -> {
          stopTableEditing();
          this.model.addRow(new Object[] {"", ""});
          this.saveChanges();
        });
    buttonRemove.addActionListener(
        a -> {
          stopTableEditing();
          int[] rows = this.tableCustomProperties.getSelectedRows();
          for (int i = 0; i < rows.length; i++) {
            this.model.removeRow(rows[i] - i);
          }
          this.saveChanges();
        });

    JPanel accordion = new JPanel();
    accordion.setLayout(new BoxLayout(accordion, BoxLayout.Y_AXIS));
    accordion.setOpaque(true);
    accordion.setBackground(Style.COLOR_BG);
    accordion.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    this.generalCard = new ExpandableCard(Resources.strings().get("panel_general"), createGeneralPanel(scrollPaneDesc), false);
    ExpandableCard lightingCard =
        new ExpandableCard("Lighting", createLightingPanel(), false);
    ExpandableCard propertiesCard =
        new ExpandableCard(Resources.strings().get("panel_customProperties"), createPropertiesPanel(buttonAdd, buttonRemove), false);

    this.generalCard.setContentInsets(8, 0, 8, 0);
    lightingCard.setContentInsets(8, 0, 8, 0);
    propertiesCard.setContentInsets(8, 0, 8, 0);
    ExpandableCard tilesetsCard = new ExpandableCard("Tilesets", createTilesetsPanel(), true);
    tilesetsCard.setContentInsets(8, 0, 8, 0);
    tilesetsCard.setFillsAvailableHeight(true);
    tilesetsCard.setHeaderTrailing(this.tilesetPanel.getCommands());

    accordion.add(this.generalCard);
    accordion.add(lightingCard);
    accordion.add(propertiesCard);
    accordion.add(tilesetsCard);

    JScrollPane hostScrollPane = new JScrollPane(accordion);
    hostScrollPane.setBorder(null);
    hostScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    hostScrollPane.getViewport().setBackground(Style.COLOR_BG);
    add(hostScrollPane, BorderLayout.CENTER);

    this.setupChangeListeners();
  }

  private JPanel createTilesetsPanel() {
    return this.tilesetPanel;
  }

  private JPanel createTilesetCommands() {
    JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    commands.setOpaque(false);
    this.tilesetTabs = new JTabbedPane();
    this.tilesetTabs.setTransferHandler(new TransferHandler() {
      @Override public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(AssetTransferable.ASSET_FLAVOR);
      }

      @Override public boolean importData(TransferSupport support) {
        try {
          Object asset = support.getTransferable().getTransferData(AssetTransferable.ASSET_FLAVOR);
          if (asset instanceof Tileset tileset) {
            addTileset(tileset);
            return true;
          }
        } catch (Exception ignored) {
          // Invalid drops leave the map unchanged.
        }
        return false;
      }
    });
    JButton add = Style.iconButton(Icons.ADD_16);
    add.setToolTipText("Add a project tileset to this map");
    add.addActionListener(e -> showAddTilesetMenu(add));
    JButton addAll = Style.iconButton(Icons.COPY_16);
    addAll.setToolTipText("Add all project tilesets to this map");
    addAll.addActionListener(e -> addAllTilesets());
    JButton create = Style.iconButton(Icons.ASSET_16);
    create.setToolTipText("Create a new tileset and add it to this map");
    create.addActionListener(e -> createTileset());
    JButton remove = Style.iconButton(Icons.DELETE_16);
    remove.setToolTipText("Remove the selected tileset from this map only");
    remove.addActionListener(e -> removeSelectedTileset(this.tilesetPanel.getSelectedTileset()));
    JLabel hint = new JLabel("Map tilesets");
    hint.setForeground(Style.COLOR_SUBTEXT);
    commands.add(hint);
    commands.add(add);
    commands.add(addAll);
    commands.add(create);
    commands.add(remove);
    return commands;
  }

  private JPanel createGeneralPanel(JComponent scrollPaneDesc) {
    return createForm(
        new JLabel[] {
            createLabel(Resources.strings().get("panel_name")),
            createLabel(Resources.strings().get("panel_title")),
            createLabel(Resources.strings().get("panel_description")),
            createLabel(Resources.strings().get("panel_gravity")),
        },
        new JComponent[] {
            this.textFieldName,
            this.textFieldTitle,
            scrollPaneDesc,
            this.spinnerGravity,
        },
        new int[] {
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT * 2,
            PropertyPanel.CONTROL_HEIGHT,
        });
  }

  private JPanel createLightingPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(
        createForm(
            new JLabel[] {
                createLabel(Resources.strings().get("panel_ambientlight")),
                createLabel(Resources.strings().get("panel_staticshadows")),
            },
            new JComponent[] {
                this.ambientColorComponent,
                this.shadowColorComponent,
            },
            new int[] {
                this.ambientColorComponent.getPreferredSize().height,
                this.shadowColorComponent.getPreferredSize().height,
            }));
    panel.add(Box.createVerticalStrut(6));
    panel.add(createAlignedControl(this.ambientlightPreview, this.ambientlightPreview.getPreferredSize().height));
    return panel;
  }

  private JPanel createPropertiesPanel(JButton buttonAdd, JButton buttonRemove) {
    int inset = PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH - 6;
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(createAlignedControl(this.scrollPane, 150, inset));
    panel.add(Box.createVerticalStrut(6));
    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    buttonRow.setOpaque(false);
    buttonRow.add(buttonAdd);
    buttonRow.add(buttonRemove);
    JPanel buttonWrapper = new JPanel(new BorderLayout());
    buttonWrapper.setOpaque(false);
    buttonWrapper.add(buttonRow, BorderLayout.WEST);
    panel.add(createAlignedControl(buttonWrapper, buttonRow.getPreferredSize().height, inset));
    return panel;
  }

  private JPanel createForm(JLabel[] labels, JComponent[] controls, int[] heights) {
    JPanel form = new JPanel();
    form.setOpaque(false);
    GroupLayout gl = new GroupLayout(form);
    form.setLayout(gl);
    gl.setAutoCreateGaps(false);

    GroupLayout.ParallelGroup labelGroup = gl.createParallelGroup(Alignment.TRAILING);
    GroupLayout.ParallelGroup controlGroup = gl.createParallelGroup(Alignment.LEADING);
    GroupLayout.SequentialGroup vertical = gl.createSequentialGroup();

    for (int i = 0; i < labels.length; i++) {
      labelGroup.addComponent(labels[i], PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH);
      controlGroup.addComponent(controls[i], PropertyPanel.CONTROL_MIN_WIDTH, PropertyPanel.CONTROL_WIDTH, Short.MAX_VALUE);
      vertical
          .addGroup(
              gl.createParallelGroup(Alignment.LEADING)
                  .addComponent(labels[i], heights[i], heights[i], heights[i])
                  .addComponent(controls[i], heights[i], heights[i], heights[i]))
          .addGap(PropertyPanel.CONTROL_MARGIN);
    }

    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addGroup(labelGroup)
            .addGap(PropertyPanel.GUTTER_WIDTH)
            .addGroup(controlGroup));
    gl.setVerticalGroup(vertical);

    setRowSize(form, form.getPreferredSize().height);
    return form;
  }

  private JPanel createAlignedControl(JComponent component, int height) {
    return createAlignedControl(component, height, PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH);
  }

  private JPanel createAlignedControl(JComponent component, int height, int inset) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);
    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addGap(inset)
            .addComponent(component, PropertyPanel.CONTROL_MIN_WIDTH, PropertyPanel.CONTROL_WIDTH, Integer.MAX_VALUE));
    gl.setVerticalGroup(
        gl.createSequentialGroup().addComponent(component, height, height, height));
    setRowSize(panel, height);
    return panel;
  }

  private static void setRowSize(JComponent component, int height) {
    component.setPreferredSize(new Dimension(0, height));
    component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    component.setAlignmentX(LEFT_ALIGNMENT);
  }

  private static JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Style.COLOR_TEXT);
    label.setHorizontalAlignment(SwingConstants.TRAILING);
    return label;
  }

  private JTable createPropertiesTable() {
    JTable table =
        new JTable() {
          private static final String EMPTY_TEXT = "No properties defined";

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
              g.setColor(Style.COLOR_PLACEHOLDER);
              FontMetrics fm = g.getFontMetrics();
              int x = (getWidth() - fm.stringWidth(EMPTY_TEXT)) / 2;
              int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
              g.drawString(EMPTY_TEXT, x, y);
            }
          }
        };
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setGridColor(Style.COLOR_BORDER);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setFillsViewportHeight(true);
    table.setModel(
        new DefaultTableModel(
            new Object[][] {},
            new String[] {Resources.strings().get("panel_name"), Resources.strings().get("panel_value")}));
    return table;
  }

  private void setupChangeListeners() {
    FocusAdapter saveOnFocusLost =
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            saveChanges();
          }
        };
    this.textFieldName.addFocusListener(saveOnFocusLost);
    this.textFieldTitle.addFocusListener(saveOnFocusLost);
    this.textFieldDesc.addFocusListener(saveOnFocusLost);
    this.textFieldName.addActionListener(e -> saveChanges());
    this.textFieldTitle.addActionListener(e -> saveChanges());
    this.spinnerGravity.addChangeListener(e -> saveChanges());
    this.model.addTableModelListener(e -> saveChanges());
  }

  public void bind(final IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
    String mapName = map.getName() != null && !map.getName().isBlank() ? map.getName() : "Unnamed map";
    this.generalCard.setTitle(Resources.strings().get("panel_general") + "  ·  " + mapName);
    refreshTilesets();
  }

  private void refreshTilesets() {
    this.tilesetPanel.bind(this.dataSource);
  }

  void refreshTilesets(IMap map) {
    if (this.dataSource == map) {
      refreshTilesets();
    }
  }

  void onTilesetsChanged(java.util.function.Consumer<IMap> listener) {
    this.tilesetsChanged = listener != null ? listener : _ -> {};
  }

  void showAddTilesetMenu(JButton owner) {
    if (Editor.instance().getGameFile() == null) {
      return;
    }
    JPopupMenu menu = new JPopupMenu();
    for (Tileset tileset : availableTilesets()) {
      if (this.dataSource != null && this.dataSource.getTilesets().stream()
          .anyMatch(existing -> java.util.Objects.equals(existing.getName(), tileset.getName()))) {
        continue;
      }
      JMenuItem item = new JMenuItem(tileset.getName());
      item.addActionListener(e -> addTileset(tileset));
      menu.add(item);
    }
    if (menu.getComponentCount() == 0) {
      JMenuItem empty = new JMenuItem("All project tilesets are already assigned");
      empty.setEnabled(false);
      menu.add(empty);
    }
    menu.show(owner, 0, owner.getHeight());
  }

  void addTileset(Tileset tileset) {
    if (this.dataSource == null || this.dataSource.getTilesets().stream()
        .anyMatch(existing -> java.util.Objects.equals(existing.getName(), tileset.getName()))) {
      return;
    }
    UndoManager.instance().mapChanging(this.dataSource);
    this.dataSource.getTilesets().add(tileset);
    UndoManager.instance().mapChanged(this.dataSource);
    this.tilesetsChanged.accept(this.dataSource);
  }

  void addAllTilesets() {
    for (Tileset tileset : availableTilesets()) {
      addTileset(tileset);
    }
  }

  private List<Tileset> availableTilesets() {
    if (Editor.instance().getGameFile() == null) {
      return List.of();
    }
    java.util.Map<String, Tileset> tilesets = new LinkedHashMap<>();
    for (Tileset tileset : Editor.instance().getGameFile().getTilesets()) {
      tilesets.putIfAbsent(tileset.getName(), tileset);
    }
    for (var map : Editor.instance().getGameFile().getMaps()) {
      for (ITileset tileset : map.getTilesets()) {
        if (tileset instanceof Tileset editableTileset) {
          tilesets.putIfAbsent(editableTileset.getName(), editableTileset);
        }
      }
    }
    return new ArrayList<>(tilesets.values());
  }

  void createTileset() {
    if (Editor.instance().getGameFile() == null) {
      return;
    }
    JTextField nameField = new JTextField("New Tileset");
    JTextField sourceField = new JTextField();
    JSpinner width = new JSpinner(new SpinnerNumberModel(16, 1, 4096, 1));
    JSpinner height = new JSpinner(new SpinnerNumberModel(16, 1, 4096, 1));
    JSpinner margin = new JSpinner(new SpinnerNumberModel(0, 0, 4096, 1));
    JSpinner spacing = new JSpinner(new SpinnerNumberModel(0, 0, 4096, 1));
    JCheckBox transparent = new JCheckBox("Transparent color");
    JButton color = new JButton("Choose color");
    final Color[] transparentColor = {null};
    color.setEnabled(false);
    transparent.addActionListener(e -> color.setEnabled(transparent.isSelected()));
    color.addActionListener(e -> transparentColor[0] = JColorChooser.showDialog(this, "Transparent color", transparentColor[0]));
    JButton browse = new JButton("Browse...");
    browse.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File image = chooser.getSelectedFile();
        sourceField.setText(image.getAbsolutePath());
        try {
          BufferedImage bufferedImage = ImageIO.read(image);
          if (bufferedImage != null) {
            // Keep the setup values valid for the selected image by default.
            width.setValue(Math.min(16, bufferedImage.getWidth()));
            height.setValue(Math.min(16, bufferedImage.getHeight()));
          }
        } catch (Exception ignored) {
          // The source remains editable if the image cannot be inspected here.
        }
      }
    });
    JPanel imageRow = new JPanel(new BorderLayout(4, 0));
    imageRow.add(sourceField, BorderLayout.CENTER);
    imageRow.add(browse, BorderLayout.EAST);
    JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
    form.add(new JLabel("Name")); form.add(nameField);
    form.add(new JLabel("Image source")); form.add(imageRow);
    form.add(new JLabel("Tile width")); form.add(width);
    form.add(new JLabel("Tile height")); form.add(height);
    form.add(new JLabel("Margin")); form.add(margin);
    form.add(new JLabel("Spacing")); form.add(spacing);
    form.add(transparent); form.add(color);
    if (JOptionPane.showConfirmDialog(this, form, "New Tileset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
      return;
    }
    Tileset tileset = new Tileset();
    String baseName = "New Tileset";
    int suffix = 1;
    String name = baseName;
    boolean exists = true;
    while (exists) {
      exists = false;
      for (Tileset existing : Editor.instance().getGameFile().getTilesets()) {
        if (name.equals(existing.getName())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        break;
      }
      suffix++;
      name = baseName + " " + suffix;
    }
    tileset.setName(name);
    int tileWidth = (int) width.getValue();
    int tileHeight = (int) height.getValue();
    int tileMargin = (int) margin.getValue();
    int tileSpacing = (int) spacing.getValue();
    tileset.setTileWidth(tileWidth);
    tileset.setTileHeight(tileHeight);
    tileset.setMargin(tileMargin);
    tileset.setSpacing(tileSpacing);
    MapImage mapImage = new MapImage();
    mapImage.setSource(sourceField.getText().trim());
    mapImage.setTransparentColor(transparent.isSelected() ? transparentColor[0] : null);
    tileset.setImage(mapImage);
    try {
      BufferedImage image = ImageIO.read(new File(mapImage.getSource()));
      if (image != null) {
        mapImage.setWidth(image.getWidth());
        mapImage.setHeight(image.getHeight());
        int columns = Math.max(1, (image.getWidth() - tileMargin * 2 + tileSpacing) / (tileWidth + tileSpacing));
        int rows = Math.max(0, (image.getHeight() - tileMargin * 2 + tileSpacing) / (tileHeight + tileSpacing));
        tileset.setColumns(columns);
        tileset.setTileCount(columns * rows);
      }
    } catch (Exception ignored) {
      tileset.setColumns(1);
      tileset.setTileCount(0);
    }
    Editor.instance().getGameFile().getTilesets().add(tileset);
    addTileset(tileset);
  }

  void removeSelectedTileset(Tileset selectedTileset) {
    if (this.dataSource == null || selectedTileset == null) {
      return;
    }
    if (!this.dataSource.getTilesets().contains(selectedTileset)) {
      return;
    }
    UndoManager.instance().mapChanging(this.dataSource);
    this.dataSource.getTilesets().remove(selectedTileset);
    UndoManager.instance().mapChanged(this.dataSource);
    this.tilesetsChanged.accept(this.dataSource);
  }

  public void saveChanges() {
    if (this.binding || this.dataSource == null) {
      return;
    }

    UndoManager.instance().mapChanging(this.dataSource);

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
    if (Game.world().environment() != null && Game.world().environment().getAmbientLight() != null) {
      Game.world().environment().getAmbientLight().setColor(this.ambientColorComponent.getColor());
    }
    if (Game.world().environment() != null && Game.world().environment().getStaticShadowLayer() != null) {
      Game.world().environment().getStaticShadowLayer().setColor(this.shadowColorComponent.getColor());
    }
    UndoManager.instance().mapChanged(this.dataSource);
  }

  private void stopTableEditing() {
    TableCellEditor editor = this.tableCustomProperties.getCellEditor();
    if (editor != null) {
      editor.stopCellEditing();
    }
  }

  private void setControlValues(final IMap map) {
    this.binding = true;
    try {
      this.model.setRowCount(0);
      this.textFieldDesc.setText(map.getStringValue(MapProperty.MAP_DESCRIPTION, null));
      this.textFieldTitle.setText(map.getStringValue(MapProperty.MAP_TITLE, null));
      this.textFieldName.setText(map.getName());
      this.ambientColorComponent.setColor(
          map.hasCustomProperty(MapProperty.AMBIENTCOLOR)
              ? map.getColorValue(MapProperty.AMBIENTCOLOR)
              : AmbientLight.DEFAULT_COLOR);
      this.shadowColorComponent.setColor(
          map.hasCustomProperty(MapProperty.SHADOWCOLOR)
              ? map.getColorValue(MapProperty.SHADOWCOLOR)
              : StaticShadow.DEFAULT_COLOR);

      this.spinnerGravity.setValue(map.getIntValue(MapProperty.GRAVITY, 0));

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
    } finally {
      this.binding = false;
    }
  }
}
