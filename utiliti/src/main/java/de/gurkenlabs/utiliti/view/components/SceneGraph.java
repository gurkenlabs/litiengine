package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayerList;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityController;
import de.gurkenlabs.utiliti.controller.LayerController;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import de.gurkenlabs.utiliti.view.renderers.SceneGraphRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class SceneGraph extends JPanel implements EntityController, LayerController {
  private static final int ROW_ACTION_SIZE = 22;
  private static final int ROW_ACTION_GAP = 4;
  private static final int ROW_ACTION_RIGHT_INSET = 8;

  private static final String LAYER_TILE = "tile";
  private static final String LAYER_IMAGE = "image";
  private static final String LAYER_GROUP = "group";
  private static final String LAYER_OBJECT = "object";

  private enum FilterChip {
    ALL("scenegraph_filter_all", Icons.POINTER_16),
    TILES("scenegraph_filter_tiles", Icons.TILESET_16),
    PROPS("assettree_spritesheets_props", Icons.PROP_16),
    CREATURES("assettree_creatures", Icons.CREATURE_16),
    COLLISION("panel_collision", Icons.COLLISIONBOX_16),
    TRIGGERS("panel_mapselection_triggers", Icons.TRIGGER_16),
    SPAWNS("panel_mapselection_spawnpoints", Icons.SPAWNPOINT_16),
    AREAS("panel_mapselection_areas", Icons.MAPAREA_16),
    LIGHTS("panel_mapselection_lights", Icons.BULB_16),
    EMITTERS("assettree_emitters", Icons.EMITTER_16),
    SOUNDS("assettree_sounds", Icons.SOUND_16),
    SHADOWS("panel_mapselection_shadow", Icons.SHADOWBOX_16);

    private final String labelKey;
    private final Icon icon;

    FilterChip(String labelKey, Icon icon) {
      this.labelKey = labelKey;
      this.icon = icon;
    }

    private String getLabel() {
      return Resources.strings().get(this.labelKey);
    }
  }

  private final JPanel searchPanel;
  private final JPanel chipPanel;
  private final JButton btnAddLayer;
  private final JButton btnAddLayerMenu;
  private final JButton btnAddTileLayer;
  private final JButton btnRemoveLayer;
  private final JButton btnRaiseLayer;
  private final JButton btnLowerLayer;
  private final JButton btnTilesets;
  private final JToggleButton btnShowAllLayers;
  private final JButton btnCollapse;
  private final JButton btnDuplicateLayer;
  private final JButton btnMore;
  private final JTextField textField;
  private final Timer searchDebounce;
  private final java.util.Map<FilterChip, JToggleButton> filterButtons;
  private FilterChip activeFilter = FilterChip.ALL;

  private final JTree tree;
  private final JScrollPane treeScroll;
  private final DefaultTreeModel treeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private javax.swing.JWindow hoverPreviewWindow;
  private SpriteAnimationPreview hoverPreview;
  private final JTextField renameField;
  private SceneNode renamedNode;

  private boolean isFocussing;
  private boolean refreshing;

  private final java.util.Map<IMap, Integer> selectedLayers;
  private final java.util.Map<IMap, java.util.Set<Integer>> expandedLayers;
  private final java.util.Map<Integer, Integer> hierarchyGuideXs;
  private final java.util.List<Consumer<IMap>> layerChangedListeners;
  private final java.util.List<Consumer<IMap>> layerStructureChangedListeners;

  public SceneGraph() {
    super(new BorderLayout(0, 0));
    this.setName(Resources.strings().get("scenegraph_name"));
    this.selectedLayers = java.util.Collections.synchronizedMap(new java.util.IdentityHashMap<>());
    this.expandedLayers = java.util.Collections.synchronizedMap(new java.util.IdentityHashMap<>());
    this.hierarchyGuideXs = new java.util.HashMap<>();
    this.layerChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.layerStructureChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.filterButtons = new java.util.EnumMap<>(FilterChip.class);

    this.searchPanel = new JPanel(new BorderLayout(4, 0));
    this.searchPanel.setOpaque(false);
    this.searchPanel.setBorder(BorderFactory.createEmptyBorder(7, 8, 3, 8));

    this.btnAddLayer = createToolButton(Icons.ADD_16);
    this.btnAddLayer.setText(Resources.strings().get("toolbar_add"));
    this.btnAddLayer.setIconTextGap(5);
    this.btnAddLayer.setMargin(new Insets(0, 10, 0, 10));
    this.btnAddLayer.setPreferredSize(null);
    this.btnAddLayer.setMinimumSize(null);
    this.btnAddLayer.setMaximumSize(null);
    Dimension addLayerButtonSize = new Dimension(
        this.btnAddLayer.getPreferredSize().width, Style.CONTROL_HEIGHT);
    this.btnAddLayer.setPreferredSize(addLayerButtonSize);
    this.btnAddLayer.setMinimumSize(addLayerButtonSize);
    this.btnAddLayer.setMaximumSize(addLayerButtonSize);
    configureActionButton(this.btnAddLayer, Resources.strings().get("panel_addLayer"));
    this.btnAddLayer.addActionListener(e -> showAddLayerMenu(this.btnAddLayer));

    this.btnAddLayerMenu = createToolButton(new DropdownArrowIcon());
    this.btnAddLayerMenu.setPreferredSize(new Dimension(22, Style.CONTROL_HEIGHT));
    this.btnAddLayerMenu.setMinimumSize(this.btnAddLayerMenu.getPreferredSize());
    this.btnAddLayerMenu.setMaximumSize(this.btnAddLayerMenu.getPreferredSize());
    configureActionButton(this.btnAddLayerMenu, Resources.strings().get("panel_addLayer"));
    this.btnAddLayerMenu.addActionListener(e -> showAddLayerMenu(this.btnAddLayerMenu));

    this.btnAddTileLayer = createToolButton(Icons.TILESET_24);
    configureActionButton(this.btnAddTileLayer, Resources.strings().get("scenegraph_add_tile_layer"));
    this.btnAddTileLayer.addActionListener(e -> addTileLayer(getSelectedOrLastLayerNode()));

    this.btnRemoveLayer = createToolButton(Icons.DELETE_16);
    configureActionButton(this.btnRemoveLayer, Resources.strings().get("panel_removeLayer"));
    this.btnRemoveLayer.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        removeLayer(node);
      }
    });

    this.btnRaiseLayer = createToolButton(Icons.LIFT_16);
    configureActionButton(this.btnRaiseLayer, Resources.strings().get("panel_moveLayerUp"));
    this.btnRaiseLayer.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        moveLayerUp(node);
      }
    });

    this.btnLowerLayer = createToolButton(Icons.LOWER_16);
    configureActionButton(this.btnLowerLayer, Resources.strings().get("panel_moveLayerDown"));
    this.btnLowerLayer.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        moveLayerDown(node);
      }
    });

    this.btnTilesets = createToolButton(Icons.SPRITESHEET_16);
    configureActionButton(this.btnTilesets, Resources.strings().get("scenegraph_select_map_tileset"));
    this.btnTilesets.addActionListener(e -> showTilesetMenu(this.btnTilesets));

    this.btnShowAllLayers = Style.iconToggleButton(Icons.HIDEOTHER_16, false);
    configureActionButton(this.btnShowAllLayers, Resources.strings().get("panel_hideOtherLayers"));
    this.btnShowAllLayers.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (this.btnShowAllLayers.isSelected() && node != null) {
        hideOtherLayers(node);
      } else {
        setAllLayersVisible(true);
      }
    });

    this.btnCollapse = Style.iconButton(Icons.COLLAPSE_24);
    configureActionButton(this.btnCollapse, Resources.strings().get("scenegraph_collapse_all"));
    this.btnCollapse.addActionListener(e -> collapseAll());

    this.btnDuplicateLayer = createToolButton(Icons.COPY_16);
    configureActionButton(this.btnDuplicateLayer, Resources.strings().get("panel_duplicateLayer"));
    this.btnDuplicateLayer.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        duplicateLayer(node);
      }
    });

    this.btnMore = createToolButton(Icons.MISC_24);
    configureActionButton(this.btnMore, Resources.strings().get("scenegraph_actions"));
    this.btnMore.addActionListener(e -> showHeaderMenu(this.btnMore));

    final String searchDefault = Resources.strings().get("scenegraph_search_placeholder");

    this.textField = new JTextField() {
      @Override
      public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        putClientProperty("JComponent.outline", "none");
      }

      @Override
      protected void paintBorder(Graphics g) {
        // The parent search box owns the only visible border.
      }
    };
    this.textField.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, searchDefault);
    this.textField.setToolTipText(Resources.strings().get("panel_entities_search_hint"));
    this.textField.getAccessibleContext().setAccessibleName(Resources.strings().get("scenegraph_search"));
    this.textField.setColumns(10);
    this.textField.setBorder(BorderFactory.createEmptyBorder());
    this.textField.setOpaque(false);
    this.textField.putClientProperty("JComponent.outline", "none");
    this.searchDebounce = new Timer(300, e -> search());
    this.searchDebounce.setRepeats(false);
    this.textField.addActionListener(e -> {
      searchDebounce.stop();
      search();
    });
    this.textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) { searchDebounce.restart(); }
      @Override public void removeUpdate(DocumentEvent e) { searchDebounce.restart(); }
      @Override public void changedUpdate(DocumentEvent e) { searchDebounce.restart(); }
    });

    RoundedSearchBox searchBox = new RoundedSearchBox(this.textField, 0);
    configureActionButton(searchBox.getClearButton(), Resources.strings().get("scenegraph_clear_search"));
    searchBox.getClearButton().addActionListener(e -> {
      this.textField.setText("");
      this.searchDebounce.stop();
      search();
    });

    this.searchPanel.add(searchBox, BorderLayout.CENTER);

    this.chipPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 2));
    this.chipPanel.setOpaque(false);
    this.chipPanel.setBorder(BorderFactory.createEmptyBorder(1, 8, 3, 8));
    for (FilterChip chip : FilterChip.values()) {
      JToggleButton button = createFilterButton(chip);
      this.filterButtons.put(chip, button);
      this.chipPanel.add(button);
    }
    this.filterButtons.get(FilterChip.ALL).setSelected(true);
    updateFilterButtonStyles();

    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
    getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        textField.requestFocusInWindow();
        textField.selectAll();
      }
    });

    this.tree = new JTree() {
      private boolean paintingBaseRows;

      @Override
      public boolean isPathSelected(TreePath path) {
        return !this.paintingBaseRows && super.isPathSelected(path);
      }

      @Override
      public boolean isRowSelected(int row) {
        return !this.paintingBaseRows && super.isRowSelected(row);
      }

      @Override
      public boolean hasFocus() {
        return !this.paintingBaseRows && super.hasFocus();
      }

      @Override
      public int getLeadSelectionRow() {
        return this.paintingBaseRows ? -1 : super.getLeadSelectionRow();
      }

      @Override
      public TreePath getLeadSelectionPath() {
        return this.paintingBaseRows ? null : super.getLeadSelectionPath();
      }

      @Override
      public TreePath getAnchorSelectionPath() {
        return this.paintingBaseRows ? null : super.getAnchorSelectionPath();
      }

      @Override
      protected void paintComponent(Graphics g) {
        paintHierarchyConnectors(g);
        this.paintingBaseRows = true;
        try {
          super.paintComponent(g);
        } finally {
          this.paintingBaseRows = false;
        }
        paintRowStateBackgrounds(g);
        paintSelectionIndicators(g);
        paintRowActions(g);
        if (getTransferHandler() instanceof SceneTransferHandler handler) {
          handler.paintDropIndicator(this, g);
        }
      }
    };
    this.tree.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);
    this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    this.tree.putClientProperty("JTree.lineStyle", "None");
    this.tree.setToggleClickCount(0);
    this.tree.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
    this.renameField = new JTextField();
    this.renameField.addActionListener(e -> commitRename());
    this.renameField.addFocusListener(new java.awt.event.FocusAdapter() {
      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
        commitRename();
      }
    });
    this.tree.setCellRenderer(new SceneGraphRenderer());
    this.tree.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));
    this.tree.setBackground(Style.background());
    this.tree.setOpaque(false);
    this.tree.getAccessibleContext().setAccessibleName(Resources.strings().get("scenegraph_hierarchy"));
    this.tree.getAccessibleContext().setAccessibleDescription(
        Resources.strings().get("scenegraph_hierarchy_description"));

    this.nodeRoot = new DefaultMutableTreeNode("root");
    this.treeModel = new DefaultTreeModel(this.nodeRoot);
    this.tree.setModel(this.treeModel);

    this.tree.addTreeSelectionListener(e -> {
      final Environment env = Game.world().environment();
      if (env == null || this.refreshing || this.isFocussing) {
        return;
      }
      this.isFocussing = true;
      try {
        TreePath path = e.getNewLeadSelectionPath();
        if (path == null) {
          return;
        }
        Object last = path.getLastPathComponent();
        if (last instanceof DefaultMutableTreeNode dmtn) {
          Object userObj = dmtn.getUserObject();
          if (userObj instanceof SceneNode node) {
            if (node.isLayer()) {
              syncLayerSelection(node);
              Editor.instance().getMapComponent().showLayerInspector(node.getLayer());
            } else if (node.isMap()) {
              Editor.instance().getMapComponent().setFocus(null, true);
              ToolManager.instance().setActiveTileLayer(null);
              UI.showMapProperties();
            } else if (node.getMapObject() != null) {
              List<IMapObject> selectedObjects = getSelectedTreeMapObjects();
              Editor.instance().getMapComponent().setSelection(selectedObjects, true);
              Editor.instance().getMapComponent().setFocus(node.getMapObject(), false);
            }
          }
        }
        } finally {
          this.isFocussing = false;
        }
        updateLayerCommandState();
    });

    this.tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
      @Override
      public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
        saveCurrentExpansionState();
      }

      @Override
      public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
        saveCurrentExpansionState();
      }
    });

    MouseListener ml = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int selRow = rowAtY(tree, e.getY());
        if (selRow == -1) {
          return;
        }

        TreePath path = tree.getPathForRow(selRow);
        if (path == null) {
          return;
        }
        Object last = path.getLastPathComponent();
        if (!(last instanceof DefaultMutableTreeNode dmtn)) {
          return;
        }
        if (!(dmtn.getUserObject() instanceof SceneNode node)) {
          return;
        }

        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && !node.isSection()) {
          if (node.isLayer() && visibilityActionBounds(selRow).contains(e.getPoint())) {
            toggleLayerVisibility(node);
            return;
          }
          if (!node.isMap() && miscActionBounds(selRow).contains(e.getPoint())) {
            showRowActionMenu(e, node);
            return;
          }
        }

        if (e.getClickCount() == 2) {
          beginRename(path);
        }
      }
    };
    this.tree.addMouseListener(ml);
    this.tree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "renameSceneNode");
    this.tree.getActionMap().put("renameSceneNode", new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath selection = tree.getSelectionPath();
        if (selection != null) {
          beginRename(selection);
        }
      }
    });
    this.tree.getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "showSceneNodeActions");
    this.tree.getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK), "showSceneNodeActions");
    this.tree.getActionMap().put("showSceneNodeActions", new javax.swing.AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        showSelectedRowActionMenu();
      }
    });
    this.tree.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int row = tree.getRowForLocation(e.getX(), e.getY());
        Object old = tree.getClientProperty("SceneGraph.hoverRow");
        if (!(old instanceof Integer hovered) || hovered != row) {
          tree.putClientProperty("SceneGraph.hoverRow", row);
          if (old instanceof Integer oldRow) {
            repaintTreeRow(oldRow);
          }
          repaintTreeRow(row);
        }
        showHoverPreview(e, row);
      }
    });
    this.tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        Object old = tree.getClientProperty("SceneGraph.hoverRow");
        tree.putClientProperty("SceneGraph.hoverRow", -1);
        if (old instanceof Integer oldRow) {
          repaintTreeRow(oldRow);
        }
        hideHoverPreview();
      }
    });
    this.tree.setTransferHandler(new SceneTransferHandler());
    this.tree.setDragEnabled(true);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);
    topPanel.add(this.searchPanel, BorderLayout.NORTH);
    JScrollPane chipScroll = new JScrollPane(
        this.chipPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    chipScroll.setBorder(null);
    chipScroll.setOpaque(false);
    chipScroll.getViewport().setOpaque(false);
    chipScroll.getHorizontalScrollBar().setUnitIncrement(24);
    chipScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    chipScroll.setPreferredSize(new Dimension(0, 30));
    chipScroll.addMouseWheelListener(e -> {
      var horizontalBar = chipScroll.getHorizontalScrollBar();
      if (horizontalBar.getMaximum() > horizontalBar.getVisibleAmount()) {
        horizontalBar.setValue(horizontalBar.getValue() + e.getWheelRotation() * horizontalBar.getUnitIncrement());
        e.consume();
      }
    });
    topPanel.add(chipScroll, BorderLayout.CENTER);

    this.treeScroll = new JScrollPane(
        tree,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    treeScroll.setBorder(null);
    treeScroll.setOpaque(false);
    treeScroll.getViewport().setOpaque(true);
    treeScroll.getViewport().setBackground(Style.background());
    treeScroll.getViewport().addChangeListener(e -> {
      Point position = treeScroll.getViewport().getViewPosition();
      if (position.x != 0) {
        treeScroll.getViewport().setViewPosition(new Point(0, position.y));
      }
    });
    treeScroll.getVerticalScrollBar().setUnitIncrement(tree.getRowHeight());
    this.add(treeScroll, BorderLayout.CENTER);
    this.add(topPanel, BorderLayout.NORTH);
    this.add(createLayerCommandStrip(), BorderLayout.SOUTH);

    Editor.instance().getMapComponent().onMapLoaded(map -> {
      this.refresh();
    });
  }

  public JButton getCollapseButton() {
    return this.btnCollapse;
  }

  public JButton getAddLayerButton() {
    return this.btnAddLayer;
  }

  public JButton getDuplicateLayerButton() {
    return this.btnDuplicateLayer;
  }

  public JButton getMoreButton() {
    return this.btnMore;
  }

  private void showHeaderMenu(Component owner) {
    JPopupMenu popup = new JPopupMenu();
    addContextMenuItem(popup, Resources.strings().get("scenegraph_show_all_layers"), Icons.SHOW_24,
        () -> setAllLayersVisible(true));
    addContextMenuItem(popup, Resources.strings().get("scenegraph_collapse_all"), Icons.COLLAPSE_24,
        this::collapseAll);
    popup.show(owner, 0, owner.getHeight());
  }

  private JButton createToolButton(Icon icon) {
    return Style.iconButton(icon);
  }

  private static void configureActionButton(AbstractButton button, String purpose) {
    button.setToolTipText(purpose);
    button.setFocusable(true);
    button.setRequestFocusEnabled(true);
    button.getAccessibleContext().setAccessibleName(purpose);
  }

  private JPanel createLayerCommandStrip() {
    JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    commands.setOpaque(true);
    commands.setBackground(Style.surface());
    commands.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
    commands.add(createAddLayerControl());
    commands.add(this.btnRaiseLayer);
    commands.add(this.btnLowerLayer);
    commands.add(this.btnDuplicateLayer);
    commands.add(this.btnRemoveLayer);
    commands.add(this.btnShowAllLayers);
    commands.add(this.btnTilesets);
    return commands;
  }

  private JPanel createAddLayerControl() {
    JPanel control = new JPanel(new BorderLayout(0, 0)) {
      @Override
      protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Style.surface());
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), Style.CORNER_RADIUS * 2, Style.CORNER_RADIUS * 2);
          g2.setColor(Style.border());
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Style.CORNER_RADIUS * 2, Style.CORNER_RADIUS * 2);
        } finally {
          g2.dispose();
        }
        super.paintComponent(graphics);
      }
    };
    control.setOpaque(false);
    this.btnAddLayer.putClientProperty("Editor.groupedToolbarButton", true);
    this.btnAddLayerMenu.putClientProperty("Editor.groupedToolbarButton", true);
    control.add(this.btnAddLayer, BorderLayout.WEST);
    control.add(this.btnAddLayerMenu, BorderLayout.CENTER);
    return control;
  }

  private void showAddLayerMenu(Component anchor) {
    JPopupMenu popup = new JPopupMenu();
    SceneNode afterNode = getSelectedOrLastLayerNode();
    addContextMenuItem(popup, Resources.strings().get("scenegraph_tile_layer"), Icons.TILESET_16,
        () -> addTileLayer(afterNode));
    addContextMenuItem(popup, Resources.strings().get("scenegraph_object_layer"), Icons.LAYER_16,
        () -> addLayer(afterNode));
    addContextMenuItem(popup, Resources.strings().get("scenegraph_image_layer"), Icons.ASSET_16,
        () -> addImageLayer(afterNode));
    addContextMenuItem(popup, Resources.strings().get("scenegraph_group"), Icons.GROUP_16,
        () -> addGroup(afterNode));
    popup.show(anchor, 0, anchor.getHeight());
  }

  private JToggleButton createFilterButton(FilterChip chip) {
    JToggleButton button = Style.iconToggleButton(chip.icon, false);
    Style.styleButton(button, Style.ButtonVariant.GHOST);
    configureActionButton(button, chip.getLabel());
    button.getAccessibleContext().setAccessibleName(
        Resources.strings().get("scenegraph_filter_accessible", chip.getLabel()));
    button.setMargin(new Insets(2, 2, 2, 2));
    Dimension size = new Dimension(22, 22);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    button.addFocusListener(new java.awt.event.FocusAdapter() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        button.scrollRectToVisible(new Rectangle(0, 0, button.getWidth(), button.getHeight()));
      }
    });
    button.addActionListener(e -> {
      this.activeFilter = chip;
      for (java.util.Map.Entry<FilterChip, JToggleButton> entry : this.filterButtons.entrySet()) {
        entry.getValue().setSelected(entry.getKey() == chip);
      }
      updateFilterButtonStyles();
      refresh();
    });
    return button;
  }

  private void updateFilterButtonStyles() {
    for (java.util.Map.Entry<FilterChip, JToggleButton> entry : this.filterButtons.entrySet()) {
      boolean selected = entry.getKey() == this.activeFilter;
      JToggleButton button = entry.getValue();
      button.setBackground(selected ? Style.selection() : Style.raisedSurface());
      button.setForeground(Style.text());
      button.repaint();
    }
  }

  private void paintHierarchyConnectors(Graphics graphics) {
    this.hierarchyGuideXs.clear();
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.border());
      g2.setStroke(new BasicStroke(1f));
      for (int row = 0; row < this.tree.getRowCount(); row++) {
        TreePath parentPath = this.tree.getPathForRow(row);
        if (parentPath == null || !this.tree.isExpanded(parentPath)
            || !(parentPath.getLastPathComponent() instanceof DefaultMutableTreeNode parent)
            || !(parent.getUserObject() instanceof SceneNode parentNode)
            || !parentNode.isLayer()
            || parent.getChildCount() == 0) {
          continue;
        }
        Rectangle parentBounds = this.tree.getPathBounds(parentPath);
        if (parentBounds == null) {
          continue;
        }
        List<Rectangle> childBounds = new ArrayList<>();
        List<DefaultMutableTreeNode> visibleChildren = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
          DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
          TreePath childPath = parentPath.pathByAddingChild(childNode);
          Rectangle bounds = this.tree.getPathBounds(childPath);
          if (bounds != null && this.tree.isVisible(childPath)) {
            childBounds.add(bounds);
            visibleChildren.add(childNode);
          }
        }
        if (childBounds.isEmpty()) {
          continue;
        }
        Rectangle firstChild = childBounds.getFirst();
        int indent = Math.max(12, firstChild.x - parentBounds.x);
        int trunkX = firstChild.x - indent / 2;
        int parentY = parentBounds.y + parentBounds.height;
        int lastY = childBounds.getLast().y + childBounds.getLast().height / 2;
        g2.drawLine(trunkX, parentY, trunkX, lastY);
        for (int i = 0; i < childBounds.size(); i++) {
          Rectangle child = childBounds.get(i);
          DefaultMutableTreeNode childNode = visibleChildren.get(i);
          int childRow = this.tree.getRowForPath(parentPath.pathByAddingChild(childNode));
          if (childRow >= 0) {
            this.hierarchyGuideXs.put(childRow, trunkX);
          }
          int childY = child.y + child.height / 2;
          int endpoint = childNode.isLeaf() ? child.x + 16 : child.x - 3;
          g2.drawLine(trunkX, childY, endpoint, childY);
        }
      }
    } finally {
      g2.dispose();
    }
  }

  private void paintRowActions(Graphics g) {
    Rectangle clip = g.getClipBounds();
    if (clip == null || this.tree.getRowCount() == 0) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int first = Math.max(0, this.tree.getClosestRowForLocation(0, clip.y));
      int last = Math.min(this.tree.getRowCount() - 1, this.tree.getClosestRowForLocation(0, clip.y + clip.height));
      for (int row = first; row <= last; row++) {
        TreePath path = this.tree.getPathForRow(row);
        if (path == null) {
          continue;
        }
        Object pathComponent = path.getLastPathComponent();
        if (!(pathComponent instanceof DefaultMutableTreeNode dmtn)
            || !(dmtn.getUserObject() instanceof SceneNode node)
            || node.isSection() || node.isMap()) {
          continue;
        }
        Rectangle bounds = this.tree.getRowBounds(row);
        if (bounds == null) {
          continue;
        }
        boolean hovered = this.tree.getClientProperty("SceneGraph.hoverRow") instanceof Integer hover && hover == row;
        boolean selected = this.tree.isRowSelected(row);
        if (node.isLayer()) {
          Rectangle visibilityBounds = visibilityActionBounds(row);
          Icon visibilityIcon = node.isVisible() ? Icons.SHOW_16 : Icons.HIDE_16;
          visibilityIcon.paintIcon(this.tree, g2,
              visibilityBounds.x + (visibilityBounds.width - visibilityIcon.getIconWidth()) / 2,
              visibilityBounds.y + (visibilityBounds.height - visibilityIcon.getIconHeight()) / 2);
        }
        Rectangle miscBounds = miscActionBounds(row);
        g2.setColor(hovered || selected ? Style.text() : Style.mutedText());
        int cy = miscBounds.y + miscBounds.height / 2;
        int x = miscBounds.x + miscBounds.width / 2;
        g2.fillOval(x, cy - 5, 2, 2);
        g2.fillOval(x, cy - 1, 2, 2);
        g2.fillOval(x, cy + 3, 2, 2);
      }
    } finally {
      g2.dispose();
    }
  }

  private void paintRowStateBackgrounds(Graphics graphics) {
    if (this.tree.getRowCount() == 0) {
      return;
    }
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle visible = this.tree.getVisibleRect();
      Object hoverValue = this.tree.getClientProperty("SceneGraph.hoverRow");
      int hoveredRow = hoverValue instanceof Integer row ? row : -1;
      if (hoveredRow >= 0 && !this.tree.isRowSelected(hoveredRow)) {
        Rectangle bounds = this.tree.getRowBounds(hoveredRow);
        if (bounds != null && isPaintableSceneRow(hoveredRow)) {
          Rectangle action = miscActionBounds(hoveredRow);
          g2.setColor(Style.sceneRowHover());
          g2.fillRoundRect(
              bounds.x, bounds.y + 2,
              Math.max(1, action.x - bounds.x - 4),
              Math.max(1, bounds.height - 4),
              Style.CORNER_RADIUS, Style.CORNER_RADIUS);
        }
      }

      int groupStart = -1;
      for (int row = 0; row <= this.tree.getRowCount(); row++) {
        boolean selected = row < this.tree.getRowCount()
            && this.tree.isRowSelected(row) && isPaintableSceneRow(row);
        if (selected && groupStart < 0) {
          groupStart = row;
        } else if (!selected && groupStart >= 0) {
          paintSelectionGroup(g2, groupStart, row - 1);
          groupStart = -1;
        }
      }

    } finally {
      g2.dispose();
    }
  }

  private void paintSelectionGroup(Graphics2D graphics, int firstRow, int lastRow) {
    for (int row = firstRow; row <= lastRow; row++) {
      Rectangle bounds = this.tree.getRowBounds(row);
      if (bounds == null) {
        continue;
      }
      Rectangle action = miscActionBounds(row);
      int x = bounds.x;
      int y = bounds.y + 2;
      int width = Math.max(1, action.x - x - 4);
      int height = Math.max(1, bounds.height - 4);
      graphics.setColor(Style.sceneRowSelected());
      graphics.fillRoundRect(x, y, width, height, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
    }
  }

  private void paintSelectionIndicators(Graphics graphics) {
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle visible = this.tree.getVisibleRect();
      int leadRow = this.tree.getLeadSelectionRow();
      int groupStart = -1;
      for (int row = 0; row <= this.tree.getRowCount(); row++) {
        boolean selected = row < this.tree.getRowCount()
            && this.tree.isRowSelected(row) && isPaintableSceneRow(row);
        if (selected && groupStart < 0) {
          groupStart = row;
        } else if (!selected && groupStart >= 0) {
          paintSelectionGroupOutline(g2, visible, groupStart, row - 1);
          groupStart = -1;
        }
      }
      for (int row = 0; row < this.tree.getRowCount(); row++) {
        if (!this.tree.isRowSelected(row) || !isPaintableSceneRow(row)) {
          continue;
        }
        Rectangle bounds = this.tree.getRowBounds(row);
        TreePath path = this.tree.getPathForRow(row);
        if (bounds == null || path == null
            || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode)
            || !(treeNode.getUserObject() instanceof SceneNode node)) {
          continue;
        }
        if (node.getMapObject() != null) {
          int size = 12;
          int x = hierarchyGuideX(row, bounds) - size / 2;
          int y = bounds.y + (bounds.height - size) / 2;
          g2.setColor(Style.accent());
          g2.fillRoundRect(x, y, size, size, 3, 3);
          g2.setColor(Color.WHITE);
          g2.setStroke(new BasicStroke(1.1f));
          g2.drawLine(x + 3, y + size / 2, x + 6, y + size - 3);
          g2.drawLine(x + 6, y + size - 3, x + size - 3, y + 3);
        } else {
          g2.setColor(row == leadRow ? Style.accent() : Style.border());
          g2.fillRoundRect(
              visible.x + 4, bounds.y + 5, row == leadRow ? 3 : 2,
              Math.max(4, bounds.height - 10), 3, 3);
        }
      }
      if (leadRow >= 0 && this.tree.isRowSelected(leadRow)) {
        Rectangle bounds = this.tree.getRowBounds(leadRow);
        if (bounds != null) {
          int right = miscActionBounds(leadRow).x - 4;
          g2.setColor(Style.accent());
          g2.setStroke(new BasicStroke(1f));
          g2.drawRoundRect(
              bounds.x, bounds.y + 1, Math.max(1, right - bounds.x),
              Math.max(1, bounds.height - 2), Style.CORNER_RADIUS, Style.CORNER_RADIUS);
        }
      }
    } finally {
      g2.dispose();
    }
  }

  private void paintSelectionGroupOutline(
      Graphics2D graphics, Rectangle visible, int firstRow, int lastRow) {
    Rectangle first = this.tree.getRowBounds(firstRow);
    Rectangle last = this.tree.getRowBounds(lastRow);
    if (first == null || last == null) {
      return;
    }
    int left = Integer.MAX_VALUE;
    for (int row = firstRow; row <= lastRow; row++) {
      Rectangle bounds = this.tree.getRowBounds(row);
      if (bounds != null) {
        left = Math.min(left, hierarchyGuideX(row, bounds) - 18);
      }
    }
    int x = Math.max(visible.x + 3, left);
    int y = first.y + 1;
    int width = Math.max(1, visible.x + visible.width - x - 4);
    int height = Math.max(1, last.y + last.height - y - 1);
    graphics.setColor(Style.selectionOutline());
    graphics.setStroke(new BasicStroke(1f));
    graphics.drawRoundRect(x, y, width, height, Style.CORNER_RADIUS * 2, Style.CORNER_RADIUS * 2);
  }

  private int hierarchyGuideX(int row, Rectangle bounds) {
    Integer paintedGuide = this.hierarchyGuideXs.get(row);
    if (paintedGuide != null) {
      return paintedGuide;
    }
    TreePath path = this.tree.getPathForRow(row);
    TreePath parentPath = path != null ? path.getParentPath() : null;
    while (parentPath != null
        && (!(parentPath.getLastPathComponent() instanceof DefaultMutableTreeNode parentNode)
            || !(parentNode.getUserObject() instanceof SceneNode parentSceneNode)
            || !parentSceneNode.isLayer())) {
      parentPath = parentPath.getParentPath();
    }
    Rectangle parentBounds = parentPath != null ? this.tree.getPathBounds(parentPath) : null;
    if (parentBounds == null) {
      return bounds.x - 16;
    }
    int indent = Math.max(12, bounds.x - parentBounds.x);
    return bounds.x - indent / 2;
  }

  private void repaintTreeRow(int row) {
    Rectangle bounds = this.tree.getRowBounds(row);
    if (bounds == null) {
      return;
    }
    Rectangle visible = this.tree.getVisibleRect();
    this.tree.repaint(visible.x, bounds.y, visible.width, bounds.height);
  }

  private boolean isPaintableSceneRow(int row) {
    TreePath path = this.tree.getPathForRow(row);
    return path != null
        && path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode
        && treeNode.getUserObject() instanceof SceneNode node
        && !node.isSection();
  }

  static int rowAtY(JTree tree, int y) {
    int row = tree.getClosestRowForLocation(0, y);
    Rectangle bounds = row >= 0 ? tree.getRowBounds(row) : null;
    return bounds != null && y >= bounds.y && y < bounds.y + bounds.height ? row : -1;
  }

  private Rectangle visibilityActionBounds(int row) {
    Rectangle misc = miscActionBounds(row);
    return new Rectangle(
        misc.x - ROW_ACTION_GAP - ROW_ACTION_SIZE, misc.y, ROW_ACTION_SIZE, misc.height);
  }

  private Rectangle miscActionBounds(int row) {
    Rectangle visible = this.tree.getVisibleRect();
    Rectangle rowBounds = this.tree.getRowBounds(row);
    if (rowBounds == null) {
      return new Rectangle();
    }
    return new Rectangle(
        visible.x + visible.width - ROW_ACTION_RIGHT_INSET - ROW_ACTION_SIZE,
        rowBounds.y + 2,
        ROW_ACTION_SIZE,
        Math.max(0, rowBounds.height - 4));
  }

  private void showHoverPreview(MouseEvent event, int row) {
    TreePath path = this.tree.getPathForRow(row);
    if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode node)
        || !(node.getUserObject() instanceof SceneNode sceneNode)) {
      hideHoverPreview();
      return;
    }
    Spritesheet spritesheet = getPreviewSpritesheet(sceneNode.getEntity());
    if (spritesheet == null) {
      hideHoverPreview();
      return;
    }
    if (this.hoverPreviewWindow == null) {
      java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
      this.hoverPreviewWindow = new javax.swing.JWindow(owner);
      this.hoverPreview = new SpriteAnimationPreview();
      this.hoverPreview.setPreferredSize(new Dimension(144, 112));
      this.hoverPreviewWindow.add(this.hoverPreview);
      this.hoverPreviewWindow.setAlwaysOnTop(true);
      this.hoverPreviewWindow.setFocusableWindowState(false);
      this.hoverPreviewWindow.pack();
    }
    this.hoverPreview.setSpritesheet(spritesheet);
    this.hoverPreview.start();
    java.awt.Point location = event.getLocationOnScreen();
    this.hoverPreviewWindow.setLocation(location.x + 18, location.y + 18);
    this.hoverPreviewWindow.setVisible(true);
  }

  private void hideHoverPreview() {
    if (this.hoverPreviewWindow != null) {
      this.hoverPreviewWindow.setVisible(false);
    }
    if (this.hoverPreview != null) {
      this.hoverPreview.stop();
    }
  }

  @Override
  public void removeNotify() {
    hideHoverPreview();
    if (this.hoverPreviewWindow != null) {
      this.hoverPreviewWindow.dispose();
      this.hoverPreviewWindow = null;
      this.hoverPreview = null;
    }
    this.searchDebounce.stop();
    super.removeNotify();
  }

  private static Spritesheet getPreviewSpritesheet(IEntity entity) {
    if (entity instanceof Prop prop) {
      Spritesheet spritesheet = Resources.spritesheets().get(PropAnimationController.getSpriteName(prop, PropState.INTACT, true));
      if (spritesheet == null) {
        spritesheet = Resources.spritesheets().get(PropAnimationController.getSpriteName(prop, false));
      }
      if (spritesheet != null) {
        return spritesheet;
      }
      return Resources.spritesheets().get(s -> s.getName().startsWith(prop.getSpritesheetName() + "-")).stream().findFirst().orElse(null);
    }
    if (entity instanceof Creature creature) {
      Spritesheet spritesheet = Resources.spritesheets().get(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.IDLE));
      if (spritesheet != null) {
        return spritesheet;
      }
      return Resources.spritesheets().get(s -> s.getName().startsWith(creature.getSpritesheetName() + "-")).stream().findFirst().orElse(null);
    }
    return null;
  }

  @Override
  public void select(IMapObject mapObject) {
    if (this.isFocussing) {
      return;
    }
    if (mapObject == null) {
      tree.clearSelection();
      return;
    }

    List<IMapObject> selected = Editor.instance().getMapComponent().getSelectedMapObjects();
    if (!selected.isEmpty()) {
      syncTreeSelectionFromMap(selected, mapObject);
      treeScroll.getHorizontalScrollBar().setValue(0);
      return;
    }

    String layerName = mapObject.getLayer() != null ? mapObject.getLayer().getName() : null;
    if (layerName == null) {
      return;
    }

    Enumeration<?> nodes = this.nodeRoot.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      Object candidate = nodes.nextElement();
      if (!(candidate instanceof DefaultMutableTreeNode treeNode)
          || !(treeNode.getUserObject() instanceof SceneNode node)
          || node.getMapObject() == null
          || node.getMapObject().getId() != mapObject.getId()) {
        continue;
      }
      TreePath path = new TreePath(treeNode.getPath());
      tree.setSelectionPath(path);
      scrollPathVerticallyToVisible(path);
      return;
    }
  }

  public void selectMap() {
    if (this.isFocussing || this.refreshing) {
      return;
    }
    for (int i = 0; i < this.nodeRoot.getChildCount(); i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.nodeRoot.getChildAt(i);
      if (node.getUserObject() instanceof SceneNode sceneNode && sceneNode.isMap()) {
        TreePath path = new TreePath(node.getPath());
        if (!path.equals(this.tree.getSelectionPath())) {
          this.tree.setSelectionPath(path);
        }
        return;
      }
    }
  }

  void setFocussingForTest(boolean focussing) {
    this.isFocussing = focussing;
  }

  void selectLayerNodeForTest(ILayer layer) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(new SceneNode(layer.getName(), null, layer, layer.isVisible(), 0));
    this.nodeRoot.add(node);
    this.treeModel.reload();
    this.tree.setSelectionPath(new TreePath(node.getPath()));
  }

  void addMapNodeForTest(IMap map) {
    this.nodeRoot.add(new DefaultMutableTreeNode(new SceneNode(map)));
    this.treeModel.reload();
  }

  ILayer getSelectedLayerForTest() {
    TreePath path = this.tree.getSelectionPath();
    if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode node
        && node.getUserObject() instanceof SceneNode sceneNode) {
      return sceneNode.getLayer();
    }
    return null;
  }

  boolean hasTreeSelectionForTest() {
    return !this.tree.isSelectionEmpty();
  }

  int getSelectionModeForTest() {
    return this.tree.getSelectionModel().getSelectionMode();
  }

  void cacheLayerStateForTest(IMap map) {
    this.selectedLayers.put(map, 0);
    this.expandedLayers.put(map, java.util.Set.of(0));
  }

  boolean hasCachedLayerStateForTest(IMap map) {
    return this.selectedLayers.containsKey(map) || this.expandedLayers.containsKey(map);
  }

  static boolean moveMapObjectsForTest(
      List<IMapObject> objects, IMapObjectLayer targetLayer, int targetIndex) {
    return SceneTransferHandler.moveMapObjects(objects, targetLayer, targetIndex);
  }

  static boolean moveLayersForTest(List<ILayer> layers, ILayer target) {
    return SceneTransferHandler.moveLayers(layers, target, null);
  }


  @Override
  public void refresh(int mapId) {
    this.refresh();
  }

  @Override
  public void remove(IMapObject mapObject) {
    this.refresh();
  }

  @Override
  public void refresh() {
    if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
      javax.swing.SwingUtilities.invokeLater(this::refresh);
      return;
    }
    this.refreshing = true;
    try {
      // save expansion state before reload
      IMap oldMap = Game.world().environment() != null ? Game.world().environment().getMap() : null;
      if (oldMap != null) {
        saveExpansionState(oldMap);
      }

      this.nodeRoot.removeAllChildren();

      Environment env = Game.world().environment();
      if (env == null || env.getMap() == null) {
        this.treeModel.reload();
        return;
      }

      IMap map = env.getMap();
      DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(new SceneNode(map));
      this.nodeRoot.add(mapNode);
      for (ILayer layer : layersInDisplayOrder(map)) {
        DefaultMutableTreeNode layerNode = createLayerTreeNode(layer, env);
        if (layerNode != null) {
          mapNode.add(layerNode);
        }
      }

      this.treeModel.reload();
      this.tree.expandPath(new TreePath(mapNode.getPath()));
      restoreExpansionState(map);

      // select focused object first so we don't flash the layers card
      List<IMapObject> selectedObjects = Editor.instance().getMapComponent().getSelectedMapObjects();
      IMapObject focused = Editor.instance().getMapComponent().getFocusedMapObject();
      if (!selectedObjects.isEmpty()) {
        syncTreeSelectionFromMap(selectedObjects, focused);
        UI.showObjectInspector();
      } else {
        // restore per-map layer selection only when no entity is focused
        if (this.selectedLayers.containsKey(map)) {
          int idx = this.selectedLayers.get(map);
          DefaultMutableTreeNode target = getLayerNodeByRenderIndex(idx);
          if (target != null) {
            tree.setSelectionPath(new TreePath(target.getPath()));
          }
        }
      }
      updateLayerCommandState();
    } finally {
      this.refreshing = false;
    }
  }

  private List<IMapObject> getSelectedTreeMapObjects() {
    TreePath[] paths = this.tree.getSelectionPaths();
    if (paths == null) {
      return List.of();
    }
    return java.util.Arrays.stream(paths)
        .sorted(java.util.Comparator.comparingInt(this.tree::getRowForPath))
        .map(TreePath::getLastPathComponent)
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(SceneNode.class::isInstance)
        .map(SceneNode.class::cast)
        .map(SceneNode::getMapObject)
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private void syncTreeSelectionFromMap(List<IMapObject> selected, IMapObject focused) {
    List<TreePath> paths = new ArrayList<>();
    TreePath focusedPath = null;
    Enumeration<?> nodes = this.nodeRoot.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      Object candidate = nodes.nextElement();
      if (!(candidate instanceof DefaultMutableTreeNode treeNode)
          || !(treeNode.getUserObject() instanceof SceneNode node)
          || node.getMapObject() == null
          || !selected.contains(node.getMapObject())) {
        continue;
      }
      TreePath path = new TreePath(treeNode.getPath());
      if (node.getMapObject().equals(focused)) {
        focusedPath = path;
      } else {
        paths.add(path);
      }
    }
    if (focusedPath != null) {
      paths.add(focusedPath);
    }
    this.tree.setSelectionPaths(paths.toArray(TreePath[]::new));
    if (focusedPath != null) {
      scrollPathVerticallyToVisible(focusedPath);
    }
  }

  private void focusLayer(ILayer layer) {
    refresh();
    if (layer == null) {
      return;
    }
    DefaultMutableTreeNode target = findLayerNode(layer);
    if (target != null) {
      tree.setSelectionPath(new TreePath(target.getPath()));
    }
  }

  public void selectLayerForInspector(ILayer layer) {
    DefaultMutableTreeNode target = findLayerNode(layer);
    if (target == null || !(target.getUserObject() instanceof SceneNode node)) {
      return;
    }
    this.isFocussing = true;
    try {
      TreePath path = new TreePath(target.getPath());
      this.tree.setSelectionPath(path);
      this.syncLayerSelection(node);
      this.updateLayerCommandState();
      this.scrollPathVerticallyToVisible(path);
    } finally {
      this.isFocussing = false;
    }
  }

  private void updateLayerCommandState() {
    IMap map = getCurrentMap();
    SceneNode node = getSelectedLayerNode();
    boolean hasLayer = map != null && node != null && node.getLayer() != null;
    ILayerList parent = hasLayer ? getParentLayerList(map, node.getLayer()) : null;
    int index = parent != null ? parent.getRenderLayers().indexOf(node.getLayer()) : -1;
    this.btnRaiseLayer.setEnabled(
        hasLayer && index >= 0 && index < parent.getRenderLayers().size() - 1);
    this.btnLowerLayer.setEnabled(hasLayer && index > 0);
    this.btnDuplicateLayer.setEnabled(hasLayer);
    this.btnRemoveLayer.setEnabled(hasLayer);
    this.btnTilesets.setEnabled(map != null && !map.getTilesets().isEmpty());
    boolean isolated = hasLayer && isLayerIsolated(map, node.getLayer());
    this.btnShowAllLayers.setEnabled(hasLayer);
    this.btnShowAllLayers.setSelected(isolated);
    configureActionButton(this.btnShowAllLayers, Resources.strings().get(
        isolated ? "scenegraph_show_all_layers" : "panel_hideOtherLayers"));
  }

  void refreshLayerCommandState() {
    updateLayerCommandState();
  }

  @Override
  public IMapObjectLayer getCurrentLayer() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }

    IMapObject focus = Editor.instance().getMapComponent().getFocusedMapObject();
    if (focus != null && focus.getLayer() != null) {
      return focus.getLayer();
    }

    TreePath sel = tree.getSelectionPath();
    if (sel != null) {
      Object last = sel.getLastPathComponent();
      if (last instanceof DefaultMutableTreeNode dmtn && dmtn.getUserObject() instanceof SceneNode node) {
        if (node.isLayer() && node.getLayer() instanceof IMapObjectLayer objLayer) {
          return objLayer;
        }
        if (node.getMapObject() != null && node.getMapObject().getLayer() != null) {
          return node.getMapObject().getLayer();
        }
      }
    }

    IMap map = Game.world().environment().getMap();
    List<IMapObjectLayer> layers = map.getMapObjectLayers();
    return layers.isEmpty() ? null : layers.getFirst();
  }

  @Override
  public void clear() {
    this.selectedLayers.clear();
    this.expandedLayers.clear();
  }

  public void clearMapState(IMap map) {
    this.selectedLayers.remove(map);
    this.expandedLayers.remove(map);
  }

  @Override
  public void onLayersChanged(Consumer<IMap> consumer) {
    this.layerChangedListeners.add(consumer);
  }

  public void onLayerStructureChanged(Consumer<IMap> consumer) {
    this.layerStructureChangedListeners.add(consumer);
  }

  private void syncLayerSelection(SceneNode node) {
    if (node.getLayer() == null) {
      return;
    }
    IMap map = Game.world().environment().getMap();
    if (map == null) {
      return;
    }
    ToolManager.instance().setActiveTileLayer(node.getLayer() instanceof ITileLayer tileLayer ? tileLayer : null);
    int idx = 0;
    for (ILayer renderLayer : map.getRenderLayers()) {
      if (renderLayer == node.getLayer()) {
        this.selectedLayers.put(map, idx);
        break;
      }
      idx++;
    }

    // sync with LayerTable (hidden)
    // LayerList listens to its own table selection; we update via LayerController interface
  }

  private DefaultMutableTreeNode getLayerNodeByRenderIndex(int renderIndex) {
    if (renderIndex < 0) {
      return null;
    }
    IMap map = getCurrentMap();
    if (map == null || renderIndex >= map.getRenderLayers().size()) {
      return null;
    }
    return findLayerNode(map.getRenderLayers().get(renderIndex));
  }

  private DefaultMutableTreeNode findLayerNode(ILayer layer) {
    Enumeration<?> nodes = this.nodeRoot.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      Object candidate = nodes.nextElement();
      if (candidate instanceof DefaultMutableTreeNode treeNode
          && treeNode.getUserObject() instanceof SceneNode node
          && node.getLayer() == layer) {
        return treeNode;
      }
    }
    return null;
  }


  private ILayerList getParentLayerList(ILayerList parent, ILayer layer) {
    if (parent.getRenderLayers().contains(layer)) {
      return parent;
    }
    for (ILayer candidate : parent.getRenderLayers()) {
      if (candidate instanceof IGroupLayer group) {
        ILayerList result = getParentLayerList(group, layer);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private SceneNode getSelectedLayerNode() {
    TreePath sel = tree.getSelectionPath();
    if (sel == null) {
      return null;
    }
    Object last = sel.getLastPathComponent();
    if (last instanceof DefaultMutableTreeNode dmtn && dmtn.getUserObject() instanceof SceneNode node) {
      if (node.isLayer()) {
        return node;
      }
      if (node.getMapObject() != null && node.getMapObject().getLayer() != null) {
        return new SceneNode(
            node.getMapObject().getLayer().getName(),
            getLayerIcon(node.getMapObject().getLayer()),
            node.getMapObject().getLayer(),
            node.getMapObject().getLayer().isVisible(),
            node.getMapObject().getLayer().getMapObjects().size());
      }
    }
    return null;
  }

  private SceneNode getSelectedOrLastLayerNode() {
    SceneNode selected = getSelectedLayerNode();
    if (selected != null) {
      return selected;
    }
    for (int i = this.nodeRoot.getChildCount() - 1; i >= 0; i--) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) this.nodeRoot.getChildAt(i);
      if (child.getUserObject() instanceof SceneNode node && node.isLayer()) {
        return node;
      }
    }
    return null;
  }

  private DefaultMutableTreeNode createLayerNode(ILayer layer) {
    Icon icon = getLayerIcon(layer);
    String name = layer.getName();
    if (name == null || name.isEmpty()) {
      name = layer.getClass().getSimpleName();
    }

    int objCount = 0;
    if (layer instanceof IMapObjectLayer objLayer) {
      objCount = objLayer.getMapObjects().size();
    }

    boolean visible = layer.isVisible();
    SceneNode node = new SceneNode(name, icon, layer, visible, objCount);
    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
    return treeNode;
  }

  private DefaultMutableTreeNode createLayerTreeNode(ILayer layer, Environment env) {
    if (layer == null) {
      return null;
    }

    DefaultMutableTreeNode layerNode = createLayerNode(layer);
    if (layer instanceof IGroupLayer group) {
      for (ILayer child : layersInDisplayOrder(group)) {
        DefaultMutableTreeNode childNode = createLayerTreeNode(child, env);
        if (childNode != null) {
          layerNode.add(childNode);
        }
      }
    } else if (layer instanceof IMapObjectLayer objLayer) {
      List<IMapObject> objects = new ArrayList<>(objLayer.getMapObjects());
      for (IMapObject obj : objects) {
        if (obj == null) {
          continue;
        }
        IEntity entity = env.get(obj.getId());
        if (entity instanceof Entity ent && shouldIncludeObject(obj, entity)) {
          layerNode.add(new DefaultMutableTreeNode(new SceneNode(obj, entity)));
        }
      }
    }

    return shouldIncludeLayer(layer, null) || layerNode.getChildCount() > 0 ? layerNode : null;
  }

  static List<ILayer> layersInDisplayOrder(ILayerList parent) {
    return parent.getRenderLayers().reversed();
  }

  private boolean shouldIncludeLayer(ILayer layer, String queryOverride) {
    String query = queryOverride != null ? queryOverride : normalizedQuery();
    if (this.activeFilter == FilterChip.TILES) {
      return layer instanceof ITileLayer && (query.isEmpty()
          || layer.getName() != null && layer.getName().toLowerCase(Locale.ROOT).contains(query));
    }
    if (this.activeFilter != FilterChip.ALL && !(layer instanceof IMapObjectLayer)) {
      return false;
    }
    if (query.isEmpty()) {
      return this.activeFilter == FilterChip.ALL;
    }
    String name = layer.getName();
    if (name != null && name.toLowerCase(Locale.ROOT).contains(query)) {
      return true;
    }
    return this.activeFilter == FilterChip.ALL && layer instanceof IMapObjectLayer;
  }

  private boolean shouldIncludeObject(IMapObject obj, IEntity entity) {
    if (!matchesActiveFilter(obj)) {
      return false;
    }
    String query = normalizedQuery();
    if (query.isEmpty()) {
      return true;
    }
    if (query.startsWith("#") && query.length() > 1) {
      try {
        return obj.getId() == Integer.parseInt(query.substring(1));
      } catch (NumberFormatException ex) {
        return false;
      }
    }
    if (query.matches("-?\\d+")) {
      try {
        if (obj.getId() == Integer.parseInt(query)) {
          return true;
        }
      } catch (NumberFormatException ex) {
        return false;
      }
    }
    String entityName = entity != null ? entity.getName() : null;
    String objectName = obj.getName();
    return (entityName != null && entityName.toLowerCase(Locale.ROOT).contains(query))
        || (objectName != null && objectName.toLowerCase(Locale.ROOT).contains(query))
        || SceneNode.getEntityLabel(entity).toLowerCase(Locale.ROOT).contains(query);
  }

  private boolean matchesActiveFilter(IMapObject obj) {
    if (this.activeFilter == FilterChip.ALL) {
      return true;
    }
    MapObjectType type = MapObjectType.get(obj.getType());
    return switch (this.activeFilter) {
      case TILES -> false;
      case PROPS -> type == MapObjectType.PROP;
      case CREATURES -> type == MapObjectType.CREATURE;
      case COLLISION -> type == MapObjectType.COLLISIONBOX;
      case TRIGGERS -> type == MapObjectType.TRIGGER;
      case SPAWNS -> type == MapObjectType.SPAWNPOINT;
      case AREAS -> type == MapObjectType.AREA;
      case LIGHTS -> type == MapObjectType.LIGHTSOURCE;
      case EMITTERS -> type == MapObjectType.EMITTER;
      case SOUNDS -> type == MapObjectType.SOUNDSOURCE;
      case SHADOWS -> type == MapObjectType.STATICSHADOW;
      case ALL -> true;
    };
  }

  private String normalizedQuery() {
    String query = this.textField.getText();
    return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
  }

  private void expandAllRows() {
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
  }

  private void saveExpansionState(IMap map) {
    java.util.Set<Integer> expanded = new java.util.HashSet<>();
    int layerIndex = 0;
    for (int i = 0; i < tree.getRowCount(); i++) {
      TreePath path = tree.getPathForRow(i);
      if (path != null) {
        Object last = path.getLastPathComponent();
        if (last instanceof DefaultMutableTreeNode dmtn
            && dmtn.getUserObject() instanceof SceneNode node) {
          if (node.isLayer()) {
            if (tree.isExpanded(i)) {
              expanded.add(layerIndex);
            }
            layerIndex++;
          }
        }
      }
    }
    this.expandedLayers.put(map, expanded);
  }

  private void restoreExpansionState(IMap map) {
    java.util.Set<Integer> expanded = this.expandedLayers.get(map);
    if (expanded == null || expanded.isEmpty()) {
      expandAllRows();
      saveExpansionState(map);
      return;
    }
    int layerIndex = 0;
    for (int i = 0; i < tree.getRowCount(); i++) {
      TreePath path = tree.getPathForRow(i);
      if (path != null) {
        Object last = path.getLastPathComponent();
        if (last instanceof DefaultMutableTreeNode dmtn
            && dmtn.getUserObject() instanceof SceneNode node
            && node.isLayer()) {
          if (expanded.contains(layerIndex)) {
            tree.expandRow(i);
          }
          layerIndex++;
        }
      }
    }
  }

  private void saveCurrentExpansionState() {
    if (this.refreshing) {
      return;
    }
    IMap map = Game.world().environment() != null ? Game.world().environment().getMap() : null;
    if (map != null) {
      saveExpansionState(map);
    }
  }

  private static Icon getLayerIcon(ILayer layer) {
    if (layer instanceof ITileLayer) {
      return Icons.TILESET_16;
    } else if (layer instanceof IImageLayer) {
      return Icons.ASSET_16;
    } else if (layer instanceof IGroupLayer) {
      return Icons.GROUP_16;
    } else if (layer instanceof IMapObjectLayer) {
      return Icons.LAYER_16;
    }
    return Icons.LAYER_16;
  }

  private void collapseAll() {
    int row = tree.getRowCount() - 1;
    while (row >= 0) {
      tree.collapseRow(row);
      row--;
    }
  }

  private void search() {
    String query = this.textField.getText();
    if (query == null || query.isEmpty()) {
      refresh();
      return;
    }

    refresh();

    if (query.startsWith("#") && query.length() > 1) {
      Integer id = parseSearchId(query.substring(1));
      if (id != null) {
        searchById(id);
        return;
      }
    }

    if (query.matches("-?\\d+")) {
      Integer id = parseSearchId(query);
      if (id != null && searchById(id)) {
        return;
      }
    }

    searchByName(query);
  }

  private static Integer parseSearchId(String value) {
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  static Integer parseSearchIdForTest(String value) {
    return parseSearchId(value);
  }

  private boolean searchById(int id) {
    Enumeration<?> nodes = this.nodeRoot.preorderEnumeration();
    while (nodes.hasMoreElements()) {
      Object candidate = nodes.nextElement();
      if (candidate instanceof DefaultMutableTreeNode node
          && node.getUserObject() instanceof SceneNode sceneNode
          && sceneNode.getMapObject() != null && sceneNode.getMapObject().getId() == id) {
        selectAndScroll(node);
        return true;
      }
    }
    return false;
  }

  private void searchByName(String name) {
    String query = name.toLowerCase(Locale.ROOT);
    Enumeration<?> nodes = this.nodeRoot.preorderEnumeration();
    while (nodes.hasMoreElements()) {
      Object candidate = nodes.nextElement();
      if (candidate instanceof DefaultMutableTreeNode node
          && node.getUserObject() instanceof SceneNode sceneNode
          && sceneNode.getName() != null && sceneNode.getName().toLowerCase(Locale.ROOT).contains(query)) {
        selectAndScroll(node);
        return;
      }
    }
  }

  private void selectAndScroll(DefaultMutableTreeNode node) {
    TreePath path = new TreePath(node.getPath());
    if (tree.getSelectionPath() != null && tree.getSelectionPath().equals(path)) {
      return;
    }
    tree.setSelectionPath(path);
    TreePath selPath = tree.getSelectionPath();
    if (selPath == null || !tree.isVisible()) {
      return;
    }
    scrollPathVerticallyToVisible(selPath);
  }

  private void scrollPathVerticallyToVisible(TreePath path) {
    Rectangle bounds = tree.getPathBounds(path);
    if (bounds == null) {
      return;
    }
    tree.scrollRectToVisible(new Rectangle(0, bounds.y, 1, bounds.height));
  }

  private void showContextMenu(MouseEvent e) {
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    if (path == null) {
      return;
    }
    Object last = path.getLastPathComponent();
    if (!(last instanceof DefaultMutableTreeNode dmtn)) {
      return;
    }
    if (!(dmtn.getUserObject() instanceof SceneNode node)) {
      return;
    }
    if (!node.isLayer()) {
      return;
    }

    JPopupMenu popup = new JPopupMenu();
    String toggleLabel = Resources.strings().get(
        node.isVisible() ? "scenegraph_hide_layer" : "scenegraph_show_layer");
    Icon toggleIcon = node.isVisible() ? Icons.HIDE_24 : Icons.SHOW_24;
    addContextMenuItem(popup, toggleLabel, toggleIcon, () -> toggleLayerVisibility(node));
    addContextMenuItem(popup, Resources.strings().get("scenegraph_show_all_layers"), Icons.SHOW_24,
        () -> setAllLayersVisible(true));
    addContextMenuItem(popup, Resources.strings().get("panel_hideOtherLayers"), Icons.HIDEOTHER_24,
        () -> hideOtherLayers(node));
    popup.show(tree, e.getX(), e.getY());
  }

  private void showRowActionMenu(MouseEvent e, SceneNode node) {
    if (node.isLayer()) {
      showContextMenu(e);
    } else if (node.getMapObject() != null) {
      showMapObjectMenu(e, node.getMapObject());
    }
  }

  private void showSelectedRowActionMenu() {
    int row = this.tree.getLeadSelectionRow();
    TreePath path = row >= 0 ? this.tree.getPathForRow(row) : null;
    Rectangle bounds = row >= 0 ? this.tree.getRowBounds(row) : null;
    if (path == null || bounds == null
        || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode)
        || !(treeNode.getUserObject() instanceof SceneNode node)
        || node.isSection() || node.isMap()) {
      return;
    }
    Rectangle actionBounds = miscActionBounds(row);
    MouseEvent event = new MouseEvent(
        this.tree,
        MouseEvent.MOUSE_RELEASED,
        System.currentTimeMillis(),
        0,
        actionBounds.x + actionBounds.width / 2,
        actionBounds.y + actionBounds.height / 2,
        1,
        false,
        MouseEvent.BUTTON1);
    showRowActionMenu(event, node);
  }

  private void showMapObjectMenu(MouseEvent e, IMapObject mapObject) {
    boolean alreadySelected = Editor.instance().getMapComponent().getSelectedMapObjects().contains(mapObject);
    Editor.instance().getMapComponent().setFocus(mapObject, !alreadySelected);
    JPopupMenu popup = new JPopupMenu();
    addContextMenuItem(popup, Resources.strings().get("scenegraph_focus_object"), Icons.POINTER_16,
        () -> Editor.instance().getMapComponent().setFocus(mapObject, true));
    addContextMenuItem(popup, Resources.strings().get("menu_view_center"), Icons.SEARCH_16,
        () -> Editor.instance().getMapComponent().centerCameraOnFocus());
    popup.addSeparator();
    addContextMenuItem(popup, Resources.strings().get("menu_edit_copy"), Icons.COPY_16,
        () -> Editor.instance().getMapComponent().copy());
    addContextMenuItem(popup, Resources.strings().get("menu_edit_cut"), Icons.CUT_16,
        () -> Editor.instance().getMapComponent().cut());
    addContextMenuItem(popup, Resources.strings().get("menu_edit_delete"), Icons.DELETE_16,
        () -> Editor.instance().getMapComponent().delete());
    popup.show(tree, e.getX(), e.getY());
  }

  private void addContextMenuItem(JPopupMenu popup, String text, Icon icon, Runnable action) {
    javax.swing.JMenuItem item = new javax.swing.JMenuItem(text, icon);
    item.addActionListener(e -> action.run());
    popup.add(item);
  }

  private void beginRename(TreePath path) {
    if (!(path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode)
        || !(treeNode.getUserObject() instanceof SceneNode node)
        || node.isSection()) {
      return;
    }
    commitRename();
    Rectangle bounds = this.tree.getPathBounds(path);
    if (bounds == null) {
      return;
    }
    this.renamedNode = node;
    this.renameField.setText(node.isMap() ? node.getMap().getName() : node.isLayer() ? node.getLayer().getName() : node.getMapObject().getName());
    Rectangle visible = this.tree.getVisibleRect();
    int nameX = bounds.x + 26;
    int nameWidth = Math.max(1, visible.x + visible.width - nameX - 36);
    this.renameField.setBounds(nameX, bounds.y + 2, nameWidth, Math.max(1, bounds.height - 4));
    this.tree.add(this.renameField);
    this.tree.setComponentZOrder(this.renameField, 0);
    this.renameField.setVisible(true);
    this.tree.revalidate();
    this.tree.repaint();
    javax.swing.SwingUtilities.invokeLater(() -> {
      this.renameField.requestFocusInWindow();
      this.renameField.selectAll();
    });
  }

  private void commitRename() {
    if (this.renamedNode == null) {
      return;
    }
    SceneNode node = this.renamedNode;
    this.renamedNode = null;
    this.renameField.setVisible(false);
    this.tree.remove(this.renameField);
    String name = this.renameField.getText();
    String currentName = node.isMap() ? node.getMap().getName() : node.isLayer() ? node.getLayer().getName() : node.getMapObject().getName();
    if (java.util.Objects.equals(name, currentName)) {
      return;
    }
    if (node.isMap()) {
      UndoManager.instance().mapChanging(node.getMap());
      if (!Editor.instance().getMapComponent().renameMap(node.getMap(), name)) {
        return;
      }
      UndoManager.instance().mapChanged(node.getMap());
      fireLayerChanged();
    } else if (node.isLayer()) {
      UndoManager.instance().layerChanging(node.getLayer());
      node.getLayer().setName(name);
      UndoManager.instance().layerChanged(node.getLayer());
      fireLayerChanged();
    } else {
      UndoManager.instance().mapObjectChanging(node.getMapObject());
      node.getMapObject().setName(name);
      if (node.getEntity() != null) {
        node.getEntity().setName(name);
      }
      UndoManager.instance().mapObjectChanged(node.getMapObject());
      Editor.instance().getMapComponent().refreshInspector();
    }
    refresh();
  }

  private void addLayer(SceneNode afterNode) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer layer =
        new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer();
    layer.setName("new layer");
    addLayer(map, layer, afterNode);
  }

  private void addGroup(SceneNode afterNode) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer group = new de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer();
    group.setName("Group");
    addLayer(map, group, afterNode);
  }

  private void addImageLayer(SceneNode afterNode) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer layer = new de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer();
    layer.setName("Image Layer");
    addLayer(map, layer, afterNode);
  }

  private void addLayer(IMap map, ILayer layer, SceneNode afterNode) {
    ILayerList parent = afterNode != null && afterNode.getLayer() instanceof IGroupLayer group
        ? group : afterNode != null ? getParentLayerList(map, afterNode.getLayer()) : map;
    if (parent == null) {
      parent = map;
    }
    int absIdx = afterNode != null && parent.getRenderLayers().contains(afterNode.getLayer())
        ? parent.getRenderLayers().indexOf(afterNode.getLayer()) : parent.getRenderLayers().size() - 1;
    UndoManager.instance().layerStructureChanging(map);
    parent.addLayer(absIdx + 1, layer);
    focusLayer(layer);
    UndoManager.instance().layerStructureChanged(map);
    fireLayerStructureChanged();
  }

  private void addTileLayer(SceneNode afterNode) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer layer =
      new de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer(map.getWidth(), map.getHeight());
    layer.setName("Tile Layer");
    addLayer(map, layer, afterNode);
    ToolManager.instance().setActiveTileLayer(layer);
  }

  private void removeLayer(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    ILayerList parent = getParentLayerList(map, node.getLayer());
    if (parent == null) {
      return;
    }
    int index = parent.getRenderLayers().indexOf(node.getLayer());
    UndoManager undoManager = UndoManager.forMap(map);
    undoManager.layerStructureChanging(map);
    parent.removeLayer(node.getLayer());
    Editor.instance().getMapComponent().synchronizeEnvironmentEntities(map);
    ToolManager.instance().setActiveTileLayer(null);
    focusLayer(index > 0 ? parent.getRenderLayers().get(index - 1) : parent.getRenderLayers().isEmpty() ? null : parent.getRenderLayers().getFirst());
    undoManager.layerStructureChanged(map);
    long deletionRevision = undoManager.getRevision();
    fireLayerStructureChanged();

    Toast.show(
        this.getRootPane(),
        Resources.strings().get("panel_layerDeleted"),
        () -> undoManager.undoIfRevision(deletionRevision));
  }

  private void showTilesetMenu(Component anchor) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    JPopupMenu popup = new JPopupMenu();
    for (var tileset : map.getTilesets()) {
      if (tileset instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset editableTileset) {
        String name = editableTileset.getName();
        javax.swing.JMenuItem item = new javax.swing.JMenuItem(
            name == null || name.isBlank() ? Resources.strings().get("scenegraph_unnamed_tileset") : name,
            Icons.TILESET_16);
        item.addActionListener(e -> UI.showTileLayerTilesetInspector(editableTileset));
        popup.add(item);
      }
    }
    if (popup.getComponentCount() == 0) {
      javax.swing.JMenuItem empty = new javax.swing.JMenuItem(
          Resources.strings().get("scenegraph_no_editable_tilesets"));
      empty.setEnabled(false);
      popup.add(empty);
    }
    popup.show(anchor, 0, anchor.getHeight());
  }

  private void duplicateLayer(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    ILayer copied;
    if (node.getLayer() instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer tileLayer) {
      copied = new de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer(tileLayer);
    } else if (node.getLayer() instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer mapObjectLayer) {
      copied = new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer(mapObjectLayer);
    } else if (node.getLayer() instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer imageLayer) {
      copied = new de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer(imageLayer);
    } else if (node.getLayer() instanceof de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer groupLayer) {
      copied = new de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer(groupLayer);
    } else {
      return;
    }
    ILayerList parent = getParentLayerList(map, node.getLayer());
    if (parent == null) {
      return;
    }
    int absIdx = parent.getRenderLayers().indexOf(node.getLayer());
    UndoManager.instance().layerStructureChanging(map);
    parent.addLayer(absIdx + 1, copied);
    focusLayer(copied);
    Editor.instance().getMapComponent().synchronizeEnvironmentEntities(map);
    UndoManager.instance().layerStructureChanged(map);
    fireLayerStructureChanged();
  }

  private void hideOtherLayers(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    UndoManager undoManager = UndoManager.forMap(map);
    undoManager.layersChanging(map);
    setLayerVisibility(map, node.getLayer());
    undoManager.layersChanged(map);
    Transform.updateAnchors();
    refresh();
    fireLayerChanged();
  }

  private void setLayerVisibility(ILayerList parent, ILayer visibleLayer) {
    for (ILayer layer : parent.getRenderLayers()) {
      if (layer == visibleLayer) {
        layer.setVisible(true);
        if (layer instanceof IGroupLayer group) {
          setAllLayersVisible(group, true);
        }
      } else if (layer instanceof IGroupLayer group && containsLayer(group, visibleLayer)) {
        layer.setVisible(true);
        setLayerVisibility(group, visibleLayer);
      } else {
        layer.setVisible(false);
        if (layer instanceof IGroupLayer group) {
          setAllLayersVisible(group, false);
        }
      }
    }
  }

  private boolean containsLayer(ILayerList parent, ILayer layer) {
    for (ILayer candidate : parent.getRenderLayers()) {
      if (candidate == layer || candidate instanceof IGroupLayer group && containsLayer(group, layer)) {
        return true;
      }
    }
    return false;
  }

  private boolean isLayerIsolated(ILayerList parent, ILayer visibleLayer) {
    return hasOtherLayer(parent, visibleLayer) && hasIsolatedVisibility(parent, visibleLayer);
  }

  private boolean hasOtherLayer(ILayerList parent, ILayer visibleLayer) {
    for (ILayer layer : parent.getRenderLayers()) {
      if (layer == visibleLayer) {
        continue;
      }
      if (layer instanceof IGroupLayer group && containsLayer(group, visibleLayer)) {
        if (hasOtherLayer(group, visibleLayer)) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  private boolean hasIsolatedVisibility(ILayerList parent, ILayer visibleLayer) {
    for (ILayer layer : parent.getRenderLayers()) {
      if (layer == visibleLayer) {
        if (!layer.isVisible() || layer instanceof IGroupLayer group && !allLayersVisible(group)) {
          return false;
        }
      } else if (layer instanceof IGroupLayer group && containsLayer(group, visibleLayer)) {
        if (!layer.isVisible() || !hasIsolatedVisibility(group, visibleLayer)) {
          return false;
        }
      } else if (layer.isVisible()) {
        return false;
      }
    }
    return true;
  }

  boolean isLayerIsolatedForTest(ILayerList parent, ILayer visibleLayer) {
    return isLayerIsolated(parent, visibleLayer);
  }

  private boolean allLayersVisible(ILayerList parent) {
    for (ILayer layer : parent.getRenderLayers()) {
      if (!layer.isVisible() || layer instanceof IGroupLayer group && !allLayersVisible(group)) {
        return false;
      }
    }
    return true;
  }

  private void toggleLayerVisibility(SceneNode node) {
    if (node.getLayer() == null) {
      return;
    }
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    UndoManager undoManager = UndoManager.forMap(map);
    undoManager.layersChanging(map);
    node.getLayer().setVisible(!node.getLayer().isVisible());
    undoManager.layersChanged(map);
    refresh();
    fireLayerChanged();
  }

  private void setAllLayersVisible(boolean visible) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    UndoManager undoManager = UndoManager.forMap(map);
    undoManager.layersChanging(map);
    setAllLayersVisible(map, visible);
    undoManager.layersChanged(map);
    refresh();
    fireLayerChanged();
    updateLayerCommandState();
  }

  private void setAllLayersVisible(ILayerList parent, boolean visible) {
    for (ILayer layer : parent.getRenderLayers()) {
      layer.setVisible(visible);
      if (layer instanceof IGroupLayer group) {
        setAllLayersVisible(group, visible);
      }
    }
  }

  private void moveLayerUp(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    ILayerList parent = getParentLayerList(map, node.getLayer());
    if (parent == null) {
      return;
    }
    int absIdx = parent.getRenderLayers().indexOf(node.getLayer());
    if (absIdx >= parent.getRenderLayers().size() - 1) {
      return;
    }
    UndoManager.instance().layerStructureChanging(map);
    parent.removeLayer(node.getLayer());
    parent.addLayer(absIdx + 1, node.getLayer());
    focusLayer(node.getLayer());
    UndoManager.instance().layerStructureChanged(map);
    fireLayerStructureChanged();
  }

  private void moveLayerDown(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    ILayerList parent = getParentLayerList(map, node.getLayer());
    if (parent == null) {
      return;
    }
    int absIdx = parent.getRenderLayers().indexOf(node.getLayer());
    if (absIdx <= 0) {
      return;
    }
    UndoManager.instance().layerStructureChanging(map);
    parent.removeLayer(node.getLayer());
    parent.addLayer(absIdx - 1, node.getLayer());
    focusLayer(node.getLayer());
    UndoManager.instance().layerStructureChanged(map);
    fireLayerStructureChanged();
  }

  private void fireLayerChanged() {
    IMap map = getCurrentMap();
    for (Consumer<IMap> c : this.layerChangedListeners) {
      c.accept(map);
    }
  }

  private void fireLayerStructureChanged() {
    IMap map = getCurrentMap();
    for (Consumer<IMap> c : this.layerStructureChangedListeners) {
      c.accept(map);
    }
    fireLayerChanged();
  }

  private static IMap getCurrentMap() {
    if (Game.world().environment() == null) {
      return null;
    }
    return Game.world().environment().getMap();
  }

  private static int getAbsoluteIndex(IMap map, SceneNode node) {
    if (node.getLayer() == null) {
      return 0;
    }
    int idx = 0;
    for (ILayer renderLayer : map.getRenderLayers()) {
      if (renderLayer == node.getLayer()) {
        return idx;
      }
      idx++;
    }
    return 0;
  }

  private static int getAbsoluteIndex(IMap map, int objectLayerIndex) {
    if (map.getMapObjectLayers().size() <= 1) {
      return 0;
    }
    int mapObjectLayerIndex = 0;
    for (int i = 0; i < map.getRenderLayers().size(); i++) {
      if (mapObjectLayerIndex > objectLayerIndex) {
        return i;
      }
      if (IMapObjectLayer.class.isAssignableFrom(map.getRenderLayers().get(i).getClass())) {
        mapObjectLayerIndex++;
      }
    }
    return map.getRenderLayers().size();
  }

  public static class SceneNode {
    private final String name;
    private final Icon icon;
    private final ILayer layer;
    private final IMap map;
    private final IMapObject mapObject;
    private final IEntity entity;
    private final boolean visible;
    private final int objectCount;
    private final boolean section;

    SceneNode(String name, Icon icon, ILayer layer, boolean visible, int objectCount) {
      this.name = name;
      this.icon = icon;
      this.layer = layer;
      this.map = null;
      this.mapObject = null;
      this.entity = null;
      this.visible = visible;
      this.objectCount = objectCount;
      this.section = false;
    }

    SceneNode(IMapObject mapObject, IEntity entity) {
      this.name = getEntityLabel(entity);
      this.icon = null;
      this.layer = null;
      this.map = null;
      this.mapObject = mapObject;
      this.entity = entity;
      this.visible = true;
      this.objectCount = 0;
      this.section = false;
    }

    private SceneNode(String sectionName) {
      this.name = sectionName;
      this.icon = null;
      this.layer = null;
      this.map = null;
      this.mapObject = null;
      this.entity = null;
      this.visible = true;
      this.objectCount = 0;
      this.section = true;
    }

    SceneNode(IMap map) {
      this.name = map.getName() == null || map.getName().isBlank()
          ? Resources.strings().get("scenegraph_unnamed_map") : map.getName();
      this.icon = Icons.MAP_16;
      this.layer = null;
      this.map = map;
      this.mapObject = null;
      this.entity = null;
      this.visible = true;
      this.objectCount = 0;
      this.section = false;
    }

    static SceneNode section(String sectionName) {
      return new SceneNode(sectionName);
    }

    public boolean isSection() {
      return this.section;
    }

    public boolean isLayer() {
      return this.layer != null;
    }

    public boolean isMap() {
      return this.map != null;
    }

    public IMap getMap() {
      return this.map;
    }

    public String getName() {
      return this.name;
    }

    public Icon getIcon() {
      return this.icon;
    }

    public ILayer getLayer() {
      return this.layer;
    }

    public IMapObject getMapObject() {
      return this.mapObject;
    }

    public IEntity getEntity() {
      return this.entity;
    }

    public boolean isVisible() {
      if (this.layer != null) {
        return this.layer.isVisible();
      }
      return this.visible;
    }

    public int getObjectCount() {
      return this.objectCount;
    }

    public Color getLayerColor() {
      if (this.layer instanceof IMapObjectLayer objLayer) {
        return objLayer.getColor();
      }
      return null;
    }

    public static String getEntityLabel(IEntity entity) {
      if (entity == null) {
        return Resources.strings().get("scenegraph_null_entity");
      }
      String name = entity.getName();
      int id = entity.getMapId();
      if (name != null && !name.isEmpty()) {
        return name + " #" + id;
      }
      return entity.getClass().getSimpleName() + " #" + id;
    }

    @Override
    public String toString() {
      if (isSection()) {
        return name;
      }
      if (isLayer()) {
        if (objectCount > 0) {
          return Resources.strings().get("scenegraph_node_count", name, objectCount);
        }
        return name;
      }
      return name;
    }
  }

  private static class SceneTransferHandler extends TransferHandler {
    static final DataFlavor SCENE_SELECTION_FLAVOR =
        new DataFlavor(SceneSelection.class, "SceneSelection");
    private DropPlan dropPlan;

    @Override
    protected Transferable createTransferable(JComponent c) {
      if (!(c instanceof JTree tree)) {
        return null;
      }
      TreePath[] paths = tree.getSelectionPaths();
      if (paths == null || paths.length == 0) {
        return null;
      }
      List<SceneNode> nodes = java.util.Arrays.stream(paths)
          .sorted(java.util.Comparator.comparingInt(tree::getRowForPath))
          .map(TreePath::getLastPathComponent)
          .filter(DefaultMutableTreeNode.class::isInstance)
          .map(DefaultMutableTreeNode.class::cast)
          .map(DefaultMutableTreeNode::getUserObject)
          .filter(SceneNode.class::isInstance)
          .map(SceneNode.class::cast)
          .toList();
      List<ILayer> layers = nodes.stream().map(SceneNode::getLayer)
          .filter(java.util.Objects::nonNull).toList();
      List<IMapObject> objects = nodes.stream().map(SceneNode::getMapObject)
          .filter(java.util.Objects::nonNull).toList();
      if (!layers.isEmpty() && objects.isEmpty() && layers.size() == nodes.size()) {
        return new SceneSelectionTransferable(new SceneSelection(layers, List.of()));
      }
      if (!objects.isEmpty() && layers.isEmpty() && objects.size() == nodes.size()) {
        return new SceneSelectionTransferable(new SceneSelection(List.of(), objects));
      }
      return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
      return MOVE;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
      setDropPlan(null, c);
    }

    @Override
    public boolean canImport(TransferSupport support) {
      if (support.isDrop()) {
        support.setShowDropLocation(false);
      }
      DropPlan plan = createDropPlan(support);
      setDropPlan(plan, support.getComponent());
      return plan != null;
    }

    @Override
    public boolean importData(TransferSupport support) {
      if (support.isDrop()) {
        support.setShowDropLocation(false);
      }
      DropPlan plan = createDropPlan(support);
      setDropPlan(null, support.getComponent());
      if (plan == null) {
        return false;
      }

      try {
        SceneSelection selection = plan.selection();
        SceneNode targetNode = plan.targetNode();
        if (selection.isLayerSelection()) {
          if (targetNode.getLayer() == null || selection.layers().contains(targetNode.getLayer())) {
            return false;
          }
          return moveLayers(selection.layers(), targetNode.getLayer(), support.getComponent());
        } else if (selection.isObjectSelection()) {
          if (!moveMapObjects(selection.mapObjects(), plan.targetLayer(), plan.targetIndex())) {
            return false;
          }
          SceneGraph graph = findSceneGraph(support.getComponent());
          if (graph != null) {
            javax.swing.SwingUtilities.invokeLater(graph::refresh);
          }
          return true;
        }

        return false;
      } catch (Exception ex) {
        return false;
      }
    }

    private DropPlan createDropPlan(TransferSupport support) {
      if (!support.isDrop() || !support.isDataFlavorSupported(SCENE_SELECTION_FLAVOR)
        || !(support.getComponent() instanceof JTree tree)) {
        return null;
      }

      JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
      TreePath path = location.getPath();
      if (path == null || !(path.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode)
        || !(treeNode.getUserObject() instanceof SceneNode targetNode)) {
        return null;
      }

      SceneSelection selection = getDraggedSelection(support);
      if (selection == null) {
        return null;
      }
      if (selection.isLayerSelection()) {
        return targetNode.isLayer()
          ? new DropPlan(selection, targetNode, null, -1, path, IndicatorPosition.ON)
          : null;
      }

      if (targetNode.getMapObject() != null
        && treeNode.getParent() instanceof DefaultMutableTreeNode parent
        && parent.getUserObject() instanceof SceneNode parentNode
        && parentNode.getLayer() instanceof IMapObjectLayer targetLayer) {
        int targetIndex = targetLayer.getMapObjects().indexOf(targetNode.getMapObject());
        Rectangle bounds = tree.getPathBounds(path);
        boolean after = bounds != null && location.getDropPoint().y >= bounds.y + bounds.height / 2;
        if (after) {
          targetIndex++;
        }
        return targetIndex >= 0
          ? new DropPlan(selection, targetNode, targetLayer, targetIndex, path,
            after ? IndicatorPosition.AFTER : IndicatorPosition.BEFORE)
          : null;
      }

      if (targetNode.getLayer() instanceof IMapObjectLayer targetLayer) {
        return new DropPlan(selection, targetNode, targetLayer, targetLayer.getMapObjects().size(), path, IndicatorPosition.ON);
      }
      return null;
    }

    private void setDropPlan(DropPlan plan, Component component) {
      this.dropPlan = plan;
      if (component instanceof JTree tree) {
        tree.repaint();
      }
    }

    private void paintDropIndicator(JTree tree, Graphics graphics) {
      DropPlan plan = this.dropPlan;
      if (plan == null) {
        return;
      }
      Rectangle bounds = tree.getPathBounds(plan.indicatorPath());
      if (bounds == null) {
        return;
      }

      Graphics2D g = (Graphics2D) graphics.create();
      g.setColor(Style.accent());
      if (plan.indicatorPosition() == IndicatorPosition.ON) {
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(bounds.x, bounds.y, Math.max(1, tree.getWidth() - bounds.x - 4), bounds.height - 1, 4, 4);
      } else {
        int y = plan.indicatorPosition() == IndicatorPosition.BEFORE ? bounds.y : bounds.y + bounds.height - 1;
        g.fillRoundRect(bounds.x, y - 1, Math.max(1, tree.getWidth() - bounds.x - 4), 3, 3, 3);
      }
      g.dispose();
    }

    private static SceneSelection getDraggedSelection(TransferSupport support) {
      try {
        Object data = support.getTransferable().getTransferData(SCENE_SELECTION_FLAVOR);
        return data instanceof SceneSelection selection ? selection : null;
      } catch (Exception ex) {
        return null;
      }
    }

    private static boolean moveLayers(
        List<ILayer> selectedLayers, ILayer target, Component treeComponent) {
      IMap map = Game.world().environment().getMap();
      if (map == null || selectedLayers.isEmpty()) {
        return false;
      }
      List<ILayer> layers = selectedLayers.stream()
          .filter(layer -> selectedLayers.stream().noneMatch(
              candidate -> candidate != layer && candidate instanceof IGroupLayer group
                  && containsLayer(group, layer)))
          .toList();
      if (layers.contains(target) || layers.stream().anyMatch(
          layer -> layer instanceof IGroupLayer group && containsLayer(group, target))) {
        return false;
      }

      ILayerList destination = target instanceof IGroupLayer group ? group : getParentLayerList(map, target);
      if (destination == null || layers.stream().anyMatch(layer -> getParentLayerList(map, layer) == null)) {
        return false;
      }

      int insertionIndex = target instanceof IGroupLayer
          ? destination.getRenderLayers().size()
          : destination.getRenderLayers().indexOf(target) + 1;
      for (ILayer layer : layers) {
        ILayerList source = getParentLayerList(map, layer);
        int sourceIndex = source.getRenderLayers().indexOf(layer);
        if (source == destination && sourceIndex < insertionIndex) {
          insertionIndex--;
        }
      }
      java.util.Map<ILayerList, List<ILayer>> before = snapshotLayerStructure(map);
      for (ILayer layer : layers) {
        getParentLayerList(map, layer).removeLayer(layer);
      }
      List<ILayer> insertionOrder = new ArrayList<>(layers);
      java.util.Collections.reverse(insertionOrder);
      for (ILayer layer : insertionOrder) {
        destination.addLayer(Math.min(insertionIndex, destination.getRenderLayers().size()), layer);
        insertionIndex++;
      }
      java.util.Map<ILayerList, List<ILayer>> after = snapshotLayerStructure(map);
      if (before.equals(after)) {
        return false;
      }
      UndoManager.instance().resourceChanged(
          () -> restoreLayerStructure(map, before), () -> restoreLayerStructure(map, after));
      Transform.updateAnchors();
      // refresh tree to reflect new order
      SceneGraph graph = findSceneGraph(treeComponent);
      if (graph != null) {
        javax.swing.SwingUtilities.invokeLater(() -> {
          graph.refresh();
          graph.fireLayerStructureChanged();
        });
      }
      return true;
    }

    private static java.util.Map<ILayerList, List<ILayer>> snapshotLayerStructure(ILayerList root) {
      java.util.Map<ILayerList, List<ILayer>> snapshot = new java.util.LinkedHashMap<>();
      snapshotLayerStructure(root, snapshot);
      return snapshot;
    }

    private static void snapshotLayerStructure(
        ILayerList parent, java.util.Map<ILayerList, List<ILayer>> snapshot) {
      snapshot.put(parent, List.copyOf(parent.getRenderLayers()));
      for (ILayer layer : parent.getRenderLayers()) {
        if (layer instanceof IGroupLayer group) {
          snapshotLayerStructure(group, snapshot);
        }
      }
    }

    private static void restoreLayerStructure(
        ILayerList root, java.util.Map<ILayerList, List<ILayer>> snapshot) {
      for (ILayerList parent : snapshot.keySet()) {
        for (ILayer layer : List.copyOf(parent.getRenderLayers())) {
          parent.removeLayer(layer);
        }
      }
      restoreLayerChildren(root, snapshot);
      Transform.updateAnchors();
      UI.getEntityController().refresh();
    }

    private static void restoreLayerChildren(
        ILayerList parent, java.util.Map<ILayerList, List<ILayer>> snapshot) {
      for (ILayer layer : snapshot.getOrDefault(parent, List.of())) {
        parent.addLayer(layer);
        if (layer instanceof IGroupLayer group) {
          restoreLayerChildren(group, snapshot);
        }
      }
    }

    private static ILayerList getParentLayerList(ILayerList parent, ILayer layer) {
      if (parent.getRenderLayers().contains(layer)) {
        return parent;
      }
      for (ILayer candidate : parent.getRenderLayers()) {
        if (candidate instanceof IGroupLayer group) {
          ILayerList result = getParentLayerList(group, layer);
          if (result != null) {
            return result;
          }
        }
      }
      return null;
    }

    private static boolean containsLayer(ILayerList parent, ILayer layer) {
      for (ILayer candidate : parent.getRenderLayers()) {
        if (candidate == layer || candidate instanceof IGroupLayer group && containsLayer(group, layer)) {
          return true;
        }
      }
      return false;
    }

    private static boolean moveMapObjects(
        List<IMapObject> mapObjects, IMapObjectLayer targetLayer, int targetIndex) {
      if (mapObjects.isEmpty() || targetLayer == null) {
        return false;
      }
      java.util.LinkedHashSet<IMapObjectLayer> affected = new java.util.LinkedHashSet<>();
      affected.add(targetLayer);
      for (IMapObject mapObject : mapObjects) {
        if (mapObject.getLayer() != null) {
          affected.add(mapObject.getLayer());
        }
      }
      java.util.Map<IMapObjectLayer, List<IMapObject>> before = snapshotMapObjects(affected);
      int adjustedIndex = targetIndex - (int) mapObjects.stream()
          .filter(object -> object.getLayer() == targetLayer)
          .mapToInt(targetLayer.getMapObjects()::indexOf)
          .filter(index -> index >= 0 && index < targetIndex)
          .count();
      for (IMapObject mapObject : mapObjects) {
        if (mapObject.getLayer() != null) {
          mapObject.getLayer().removeMapObject(mapObject);
        }
      }
      adjustedIndex = Math.max(0, Math.min(adjustedIndex, targetLayer.getMapObjects().size()));
      for (IMapObject mapObject : mapObjects) {
        targetLayer.addMapObject(adjustedIndex++, mapObject);
        Game.world().environment().reloadFromMap(mapObject.getId());
      }
      java.util.Map<IMapObjectLayer, List<IMapObject>> after = snapshotMapObjects(affected);
      if (before.equals(after)) {
        return false;
      }
      UndoManager.instance().resourceChanged(
          () -> restoreMapObjects(before), () -> restoreMapObjects(after));
      return true;
    }

    private static java.util.Map<IMapObjectLayer, List<IMapObject>> snapshotMapObjects(
        java.util.Collection<IMapObjectLayer> layers) {
      java.util.Map<IMapObjectLayer, List<IMapObject>> snapshot = new java.util.LinkedHashMap<>();
      for (IMapObjectLayer layer : layers) {
        snapshot.put(layer, List.copyOf(layer.getMapObjects()));
      }
      return snapshot;
    }

    private static void restoreMapObjects(
        java.util.Map<IMapObjectLayer, List<IMapObject>> snapshot) {
      java.util.LinkedHashSet<IMapObject> objects = new java.util.LinkedHashSet<>();
      for (IMapObjectLayer layer : snapshot.keySet()) {
        for (IMapObject object : List.copyOf(layer.getMapObjects())) {
          layer.removeMapObject(object);
          objects.add(object);
        }
      }
      for (java.util.Map.Entry<IMapObjectLayer, List<IMapObject>> entry : snapshot.entrySet()) {
        for (IMapObject object : entry.getValue()) {
          entry.getKey().addMapObject(object);
          objects.add(object);
        }
      }
      for (IMapObject object : objects) {
        Game.world().environment().reloadFromMap(object.getId());
      }
      Transform.updateAnchors();
      UI.getEntityController().refresh();
    }

    private static SceneGraph findSceneGraph(Component c) {
      while (c != null) {
        if (c instanceof SceneGraph sg) {
          return sg;
        }
        c = c.getParent();
      }
      return null;
    }

    private enum IndicatorPosition {
      BEFORE,
      AFTER,
      ON
    }

    private record DropPlan(
      SceneSelection selection,
      SceneNode targetNode,
      IMapObjectLayer targetLayer,
      int targetIndex,
      TreePath indicatorPath,
      IndicatorPosition indicatorPosition) {
    }
  }

  private record SceneSelection(List<ILayer> layers, List<IMapObject> mapObjects) {
    private SceneSelection {
      layers = List.copyOf(layers);
      mapObjects = List.copyOf(mapObjects);
    }

    private boolean isLayerSelection() {
      return !this.layers.isEmpty() && this.mapObjects.isEmpty();
    }

    private boolean isObjectSelection() {
      return this.layers.isEmpty() && !this.mapObjects.isEmpty();
    }
  }

  private static class SceneSelectionTransferable implements Transferable {
    private final SceneSelection selection;

    SceneSelectionTransferable(SceneSelection selection) {
      this.selection = selection;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[]{SceneTransferHandler.SCENE_SELECTION_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return SceneTransferHandler.SCENE_SELECTION_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
      return selection;
    }
  }

  private static final class DropdownArrowIcon implements Icon {
    @Override
    public int getIconWidth() {
      return 8;
    }

    @Override
    public int getIconHeight() {
      return 6;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g2 = (Graphics2D) graphics.create();
      g2.setColor(component.isEnabled() ? Style.text() : Style.mutedText());
      g2.fillPolygon(new int[] {x, x + 8, x + 4}, new int[] {y, y, y + 6}, 3);
      g2.dispose();
    }
  }
}
