package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityController;
import de.gurkenlabs.utiliti.controller.LayerController;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import de.gurkenlabs.utiliti.view.renderers.SceneGraphRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
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
import java.util.function.Consumer;
import javax.swing.Icon;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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

public final class SceneGraph extends JPanel implements EntityController, LayerController {

  private static final String LAYER_TILE = "tile";
  private static final String LAYER_IMAGE = "image";
  private static final String LAYER_GROUP = "group";
  private static final String LAYER_OBJECT = "object";

  private enum FilterChip {
    ALL("All", Icons.POINTER_16),
    TILES("Tiles", Icons.TILESET_16),
    PROPS("Props", Icons.PROP_16),
    CREATURES("Creatures", Icons.CREATURE_16),
    COLLISION("Collision", Icons.COLLISIONBOX_16),
    TRIGGERS("Triggers", Icons.TRIGGER_16),
    SPAWNS("Spawns", Icons.SPAWNPOINT_16),
    AREAS("Areas", Icons.MAPAREA_16),
    LIGHTS("Lights", Icons.BULB_16),
    EMITTERS("Emitters", Icons.EMITTER_16),
    SOUNDS("Sounds", Icons.SOUND_16),
    SHADOWS("Shadows", Icons.SHADOWBOX_16);

    private final String label;
    private final Icon icon;

    FilterChip(String label, Icon icon) {
      this.label = label;
      this.icon = icon;
    }
  }

  private final JPanel searchPanel;
  private final JPanel chipPanel;
  private final JLabel footerLabel;
  private final JButton btnAddLayer;
  private final JButton btnCollapse;
  private final JButton btnDuplicateLayer;
  private final JButton btnMore;
  private final JTextField textField;
  private final Timer searchDebounce;
  private final java.util.Map<FilterChip, JToggleButton> filterButtons;
  private FilterChip activeFilter = FilterChip.ALL;
  private int totalSceneItems;
  private int visibleSceneItems;
  private int totalLayers;

  private final JTree tree;
  private final JScrollPane treeScroll;
  private final DefaultTreeModel treeModel;
  private final DefaultMutableTreeNode nodeRoot;

  private boolean isFocussing;
  private boolean refreshing;

  private final java.util.Map<String, Integer> selectedLayers;
  private final java.util.Map<String, java.util.Set<String>> expandedLayers;
  private final java.util.List<Consumer<IMap>> layerChangedListeners;
  private final java.util.List<Consumer<IMap>> layerStructureChangedListeners;

  public SceneGraph() {
    super(new BorderLayout(0, 0));
    this.setName("Scene");
    this.selectedLayers = new java.util.concurrent.ConcurrentHashMap<>();
    this.expandedLayers = new java.util.concurrent.ConcurrentHashMap<>();
    this.layerChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.layerStructureChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.filterButtons = new java.util.EnumMap<>(FilterChip.class);

    this.searchPanel = new JPanel(new BorderLayout(4, 0));
    this.searchPanel.setOpaque(false);
    this.searchPanel.setBorder(BorderFactory.createEmptyBorder(7, 8, 3, 8));

    this.btnAddLayer = createToolButton(Icons.ADD_24);
    this.btnAddLayer.setToolTipText("Add Layer");
    this.btnAddLayer.addActionListener(e -> addLayer(getSelectedOrLastLayerNode()));

    this.btnCollapse = Style.iconButton(Icons.COLLAPSE_24);
    this.btnCollapse.setToolTipText("Collapse All");
    this.btnCollapse.addActionListener(e -> collapseAll());

    this.btnDuplicateLayer = createToolButton(Icons.COPY_24);
    this.btnDuplicateLayer.setToolTipText("Duplicate selected layer");
    this.btnDuplicateLayer.addActionListener(e -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        duplicateLayer(node);
      }
    });

    this.btnMore = createToolButton(Icons.MISC_24);
    this.btnMore.setToolTipText("Scene Graph Actions");
    this.btnMore.addActionListener(e -> showHeaderMenu(this.btnMore));

    final String searchDefault = "Search scene...";

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

    JPanel searchBox = new JPanel(new BorderLayout(8, 0)) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Style.COLOR_SURFACE);
          g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
          g2.setColor(Style.COLOR_BORDER);
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        } finally {
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
    searchBox.setOpaque(false);
    searchBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
    searchBox.setPreferredSize(new Dimension(0, 30));
    searchBox.setMinimumSize(new Dimension(0, 30));
    searchBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    this.textField.setBackground(Style.COLOR_SURFACE);
    JLabel searchIcon = new JLabel(Icons.SEARCH_16);
    searchIcon.setPreferredSize(new Dimension(16, 30));
    searchBox.add(searchIcon, BorderLayout.WEST);
    searchBox.add(this.textField, BorderLayout.CENTER);
    JButton clearSearch = Style.clearButton(Icons.CROSS_8);
    clearSearch.setPreferredSize(new Dimension(24, 28));
    clearSearch.setToolTipText("Clear search");
    clearSearch.addActionListener(e -> {
      this.textField.setText("");
      this.searchDebounce.stop();
      search();
    });
    searchBox.add(clearSearch, BorderLayout.EAST);

    this.searchPanel.add(searchBox, BorderLayout.CENTER);

    this.chipPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 2));
    this.chipPanel.setOpaque(false);
    this.chipPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
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
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintSelectionGutters(g);
        paintActionDots(g);
      }
    };
    this.tree.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);
    this.tree.setCellRenderer(new SceneGraphRenderer());
    this.tree.setRowHeight((int) (26 * Editor.preferences().getUiScale()));
    this.tree.setBackground(Style.COLOR_BG);

    this.nodeRoot = new DefaultMutableTreeNode("root");
    this.treeModel = new DefaultTreeModel(this.nodeRoot);
    this.tree.setModel(this.treeModel);

    this.tree.addTreeSelectionListener(e -> {
      final Environment env = Game.world().environment();
      if (env == null) {
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
            } else if (node.getMapObject() != null) {
              Editor.instance().getMapComponent().setFocus(node.getMapObject(), true);
            }
          }
        }
      } finally {
        this.isFocussing = false;
      }
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
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
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
          if (isActionColumnClick(e, selRow)) {
            showRowActionMenu(e, node);
            return;
          }
        }

        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && node.isLayer()) {
          // calculate the icon area: tree indentation + visibility icon width
          Rectangle cellBounds = tree.getRowBounds(selRow);
          if (cellBounds != null) {
            int indent = cellBounds.x;
            int iconEnd = indent + 20;
            if (e.getX() >= indent && e.getX() < iconEnd) {
              node.getLayer().setVisible(!node.getLayer().isVisible());
              UndoManager.instance().recordChanges();
              refresh();
              fireLayerChanged();
              return;
            }
          }
        }

        if (e.getClickCount() == 2) {
          Editor.instance().getMapComponent().centerCameraOnFocus();
        }
      }
    };
    this.tree.addMouseListener(ml);
    this.tree.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int row = tree.getRowForLocation(e.getX(), e.getY());
        Object old = tree.getClientProperty("SceneGraph.hoverRow");
        if (!(old instanceof Integer hovered) || hovered != row) {
          tree.putClientProperty("SceneGraph.hoverRow", row);
          tree.repaint();
        }
      }
    });
    this.tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        tree.putClientProperty("SceneGraph.hoverRow", -1);
        tree.repaint();
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
    topPanel.add(chipScroll, BorderLayout.CENTER);

    this.footerLabel = new JLabel();
    this.footerLabel.setForeground(Style.COLOR_SUBTEXT);

    this.treeScroll = new JScrollPane(
        tree,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    treeScroll.setBorder(null);
    treeScroll.setOpaque(false);
    treeScroll.getViewport().setOpaque(true);
    treeScroll.getViewport().setBackground(Style.COLOR_BG);
    treeScroll.getVerticalScrollBar().setUnitIncrement(tree.getRowHeight());
    this.add(treeScroll, BorderLayout.CENTER);
    this.add(topPanel, BorderLayout.NORTH);

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
    addContextMenuItem(popup, "Add Layer", Icons.ADD_24, () -> addLayer(getSelectedOrLastLayerNode()));
    addContextMenuItem(popup, "Duplicate Layer", Icons.COPY_24, () -> {
      SceneNode node = getSelectedLayerNode();
      if (node != null) {
        duplicateLayer(node);
      }
    });
    popup.addSeparator();
    addContextMenuItem(popup, "Show All Layers", Icons.SHOW_24, this::showAllLayers);
    addContextMenuItem(popup, "Collapse All", Icons.COLLAPSE_24, this::collapseAll);
    popup.show(owner, 0, owner.getHeight());
  }

  private JButton createToolButton(Icon icon) {
    return Style.iconButton(icon);
  }

  private JToggleButton createFilterButton(FilterChip chip) {
    JToggleButton button = new FilterPillButton(chip.icon);
    button.setFocusable(false);
    button.setToolTipText(chip.label);
    button.setMargin(new Insets(2, 2, 2, 2));
    button.setPreferredSize(new Dimension(22, 22));
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
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
      button.setBackground(selected ? Style.COLOR_SELECTION_INACTIVE : Style.COLOR_SURFACE2);
      button.setForeground(selected ? Color.WHITE : Style.COLOR_TEXT);
      button.repaint();
    }
  }

  private static final class FilterPillButton extends JToggleButton {
    private FilterPillButton(Icon icon) {
      super(icon);
      setFocusable(false);
      setOpaque(false);
      setContentAreaFilled(false);
      setBorderPainted(false);
      setFocusPainted(false);
      setMargin(new Insets(2, 2, 2, 2));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Style.paintButtonBackground(this, getModel(), g);
      super.paintComponent(g);
    }
  }

  private void paintActionDots(Graphics g) {
    Rectangle clip = g.getClipBounds();
    if (clip == null || this.tree.getRowCount() == 0) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int first = Math.max(0, this.tree.getClosestRowForLocation(0, clip.y));
      int last = Math.min(this.tree.getRowCount() - 1, this.tree.getClosestRowForLocation(0, clip.y + clip.height));
      Rectangle visible = this.tree.getVisibleRect();
      int buttonX = visible.x + visible.width - 30;
      for (int row = first; row <= last; row++) {
        TreePath path = this.tree.getPathForRow(row);
        if (path == null) {
          continue;
        }
        Object pathComponent = path.getLastPathComponent();
        if (!(pathComponent instanceof DefaultMutableTreeNode dmtn)
            || !(dmtn.getUserObject() instanceof SceneNode node)
            || node.isSection()) {
          continue;
        }
        Rectangle bounds = this.tree.getRowBounds(row);
        if (bounds == null) {
          continue;
        }
        boolean hovered = this.tree.getClientProperty("SceneGraph.hoverRow") instanceof Integer hover && hover == row;
        boolean selected = this.tree.isRowSelected(row);
        if (hovered || selected) {
          g2.setColor(selected ? Style.COLOR_SELECTION_INACTIVE : Style.COLOR_SURFACE2);
          g2.fillRoundRect(buttonX, bounds.y + 3, 22, bounds.height - 6, 7, 7);
          g2.setColor(selected ? Style.COLOR_BORDER : Style.COLOR_BORDER);
          g2.drawRoundRect(buttonX, bounds.y + 3, 22, bounds.height - 6, 7, 7);
        }
        g2.setColor(selected ? Style.COLOR_TEXT : Style.COLOR_SUBTEXT);
        int cy = bounds.y + bounds.height / 2;
        int x = buttonX + 10;
        g2.fillOval(x, cy - 5, 2, 2);
        g2.fillOval(x, cy - 1, 2, 2);
        g2.fillOval(x, cy + 3, 2, 2);
      }
    } finally {
      g2.dispose();
    }
  }

  private void paintSelectionGutters(Graphics g) {
    Rectangle visible = this.tree.getVisibleRect();
    if (visible == null || this.tree.getSelectionCount() == 0) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setColor(Style.COLOR_BG);
      for (int row : this.tree.getSelectionRows()) {
        Rectangle bounds = this.tree.getRowBounds(row);
        if (bounds == null || bounds.x <= visible.x) {
          continue;
        }
        g2.fillRect(visible.x, bounds.y, bounds.x - visible.x, bounds.height);
      }
    } finally {
      g2.dispose();
    }
  }

  @Override
  public void select(IMapObject mapObject) {
    if (this.isFocussing || mapObject == null) {
      if (mapObject == null) {
        tree.clearSelection();
      }
      return;
    }

    String layerName = mapObject.getLayer() != null ? mapObject.getLayer().getName() : null;
    if (layerName == null) {
      return;
    }

    for (int i = 0; i < nodeRoot.getChildCount(); i++) {
      DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) nodeRoot.getChildAt(i);
      if (!(layerNode.getUserObject() instanceof SceneNode sn) || !sn.isLayer()) {
        continue;
      }
      if (sn.getLayer() != mapObject.getLayer()) {
        continue;
      }
      for (int j = 0; j < layerNode.getChildCount(); j++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) layerNode.getChildAt(j);
        if (child.getUserObject() instanceof SceneNode childNode
            && childNode.getMapObject() != null
            && childNode.getMapObject().getId() == mapObject.getId()) {
          TreePath path = new TreePath(child.getPath());
          tree.setSelectionPath(path);
          tree.scrollPathToVisible(path);
          treeScroll.getHorizontalScrollBar().setValue(0);
          return;
        }
      }
    }
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
    this.refreshing = true;
    try {
      // save expansion state before reload
      IMap oldMap = Game.world().environment() != null ? Game.world().environment().getMap() : null;
      if (oldMap != null) {
        saveExpansionState(oldMap.getName());
      }

      this.nodeRoot.removeAllChildren();
      this.totalSceneItems = 0;
      this.visibleSceneItems = 0;
      this.totalLayers = 0;

      Environment env = Game.world().environment();
      if (env == null || env.getMap() == null) {
        this.treeModel.reload();
        updateFooter();
        return;
      }

      IMap map = env.getMap();
      for (ILayer layer : map.getRenderLayers()) {
        if (layer == null) {
          continue;
        }

        this.totalLayers++;
        DefaultMutableTreeNode layerNode = createLayerNode(layer);
        boolean includeLayer = shouldIncludeLayer(layer, null);

        if (layer instanceof IMapObjectLayer objLayer) {
          List<IMapObject> objects = new ArrayList<>(objLayer.getMapObjects());
          this.totalSceneItems += objects.size();
          objects.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
          for (IMapObject obj : objects) {
            if (obj == null) {
              continue;
            }
            IEntity entity = env.get(obj.getId());
            if (entity instanceof Entity ent && shouldIncludeObject(obj, entity)) {
              DefaultMutableTreeNode objNode = new DefaultMutableTreeNode(
                  new SceneNode(obj, entity));
              layerNode.add(objNode);
              this.visibleSceneItems++;
            }
          }
        }

        if (!includeLayer && layerNode.getChildCount() == 0) {
          continue;
        }

        this.nodeRoot.add(layerNode);
      }

      this.treeModel.reload();
      restoreExpansionState(map.getName());
      updateFooter();

      // restore per-map selection
      if (map != null && this.selectedLayers.containsKey(map.getName())) {
        int idx = this.selectedLayers.get(map.getName());
        DefaultMutableTreeNode target = getLayerNodeByRenderIndex(idx);
        if (target != null) {
          tree.setSelectionPath(new TreePath(target.getPath()));
        }
      }

      // select focused object
      IMapObject focused = Editor.instance().getMapComponent().getFocusedMapObject();
      if (focused != null) {
        this.select(focused);
      }
    } finally {
      this.refreshing = false;
    }
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
    int idx = 0;
    for (ILayer renderLayer : map.getRenderLayers()) {
      if (renderLayer == node.getLayer()) {
        this.selectedLayers.put(map.getName(), idx);
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
    int current = 0;
    for (int i = 0; i < this.nodeRoot.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) this.nodeRoot.getChildAt(i);
      if (child.getUserObject() instanceof SceneNode node && node.isLayer()) {
        if (current == renderIndex) {
          return child;
        }
        current++;
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

  private boolean shouldIncludeLayer(ILayer layer, String queryOverride) {
    String query = queryOverride != null ? queryOverride : normalizedQuery();
    if (this.activeFilter == FilterChip.TILES) {
      return layer instanceof ITileLayer && (query.isEmpty()
          || layer.getName() != null && layer.getName().toLowerCase().contains(query));
    }
    if (this.activeFilter != FilterChip.ALL && !(layer instanceof IMapObjectLayer)) {
      return false;
    }
    if (query.isEmpty()) {
      return this.activeFilter == FilterChip.ALL;
    }
    String name = layer.getName();
    return name != null && name.toLowerCase().contains(query);
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
    return (entityName != null && entityName.toLowerCase().contains(query))
        || (objectName != null && objectName.toLowerCase().contains(query))
        || SceneNode.getEntityLabel(entity).toLowerCase().contains(query);
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
    return query == null ? "" : query.trim().toLowerCase();
  }

  private void expandAllRows() {
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
  }

  private void saveExpansionState(String mapName) {
    java.util.Set<String> expanded = new java.util.HashSet<>();
    for (int i = 0; i < tree.getRowCount(); i++) {
      if (tree.isExpanded(i)) {
        TreePath path = tree.getPathForRow(i);
        if (path != null) {
          Object last = path.getLastPathComponent();
          if (last instanceof DefaultMutableTreeNode dmtn
              && dmtn.getUserObject() instanceof SceneNode node
              && node.isLayer()) {
            expanded.add(node.getName());
          }
        }
      }
    }
    this.expandedLayers.put(mapName, expanded);
  }

  private void restoreExpansionState(String mapName) {
    java.util.Set<String> expanded = this.expandedLayers.get(mapName);
    if (expanded == null || expanded.isEmpty()) {
      expandAllRows();
      saveExpansionState(mapName);
      return;
    }
    for (int i = 0; i < tree.getRowCount(); i++) {
      TreePath path = tree.getPathForRow(i);
      if (path != null) {
        Object last = path.getLastPathComponent();
        if (last instanceof DefaultMutableTreeNode dmtn
            && dmtn.getUserObject() instanceof SceneNode node
            && node.isLayer()) {
          if (expanded.contains(node.getName())) {
            tree.expandRow(i);
          }
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
      saveExpansionState(map.getName());
    }
  }

  private void updateFooter() {
    if (this.footerLabel == null) {
      return;
    }
    String itemText = this.visibleSceneItems == this.totalSceneItems
        ? this.totalSceneItems + " scene items"
        : this.visibleSceneItems + " of " + this.totalSceneItems + " scene items";
    this.footerLabel.setText(itemText + "  •  " + this.totalLayers + " layers");
  }

  private static Icon getLayerIcon(ILayer layer) {
    if (layer instanceof ITileLayer) {
      return Icons.TILESET_16;
    } else if (layer instanceof IImageLayer) {
      return Icons.ASSET_16;
    } else if (layer instanceof IGroupLayer) {
      return Icons.LAYER_16;
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
      try {
        searchById(Integer.parseInt(query.substring(1)));
        return;
      } catch (NumberFormatException ex) {
        // fall through
      }
    }

    if (query.matches("-?\\d+")) {
      if (searchById(Integer.parseInt(query))) {
        return;
      }
    }

    searchByName(query);
  }

  private boolean searchById(int id) {
    for (int i = 0; i < nodeRoot.getChildCount(); i++) {
      DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) nodeRoot.getChildAt(i);
      for (int j = 0; j < layerNode.getChildCount(); j++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) layerNode.getChildAt(j);
        if (child.getUserObject() instanceof SceneNode sn && sn.getMapObject() != null) {
          if (sn.getMapObject().getId() == id) {
            selectAndScroll(child);
            return true;
          }
        }
      }
    }
    return false;
  }

  private void searchByName(String name) {
    for (int i = 0; i < nodeRoot.getChildCount(); i++) {
      DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) nodeRoot.getChildAt(i);
      if (layerNode.getUserObject() instanceof SceneNode sn) {
        if (sn.getName() != null && sn.getName().toLowerCase().contains(name.toLowerCase())) {
          selectAndScroll(layerNode);
          return;
        }
      }
      for (int j = 0; j < layerNode.getChildCount(); j++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) layerNode.getChildAt(j);
        if (child.getUserObject() instanceof SceneNode childNode) {
          if (childNode.getName() != null && childNode.getName().toLowerCase().contains(name.toLowerCase())) {
            selectAndScroll(child);
            return;
          }
        }
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
    Rectangle bounds = tree.getPathBounds(selPath);
    if (bounds == null) {
      return;
    }
    bounds.height = tree.getVisibleRect().height;
    tree.scrollRectToVisible(bounds);
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
    addContextMenuItem(popup, "Add Layer", Icons.ADD_24, () -> addLayer(node));
    addContextMenuItem(popup, "Remove Layer", Icons.DELETE_24, () -> removeLayer(node));
    addContextMenuItem(popup, "Duplicate Layer", Icons.COPY_24, () -> duplicateLayer(node));
    addContextMenuItem(popup, "Rename Layer", Icons.RENAME_24, () -> renameLayer(node));
    addContextMenuItem(popup, "Set Color", Icons.COLOR_24, () -> setLayerColor(node));
    popup.addSeparator();
    String toggleLabel = node.isVisible() ? "Hide Layer" : "Show Layer";
    Icon toggleIcon = node.isVisible() ? Icons.HIDE_24 : Icons.SHOW_24;
    addContextMenuItem(popup, toggleLabel, toggleIcon, () -> toggleLayerVisibility(node));
    addContextMenuItem(popup, "Show All Layers", Icons.SHOW_24, () -> showAllLayers());
    addContextMenuItem(popup, "Hide Other Layers", Icons.HIDEOTHER_24, () -> hideOtherLayers(node));
    popup.addSeparator();
    addContextMenuItem(popup, "Move Up", Icons.LIFT_24, () -> moveLayerUp(node));
    addContextMenuItem(popup, "Move Down", Icons.LOWER_24, () -> moveLayerDown(node));

    popup.show(tree, e.getX(), e.getY());
  }

  private boolean isActionColumnClick(MouseEvent e, int row) {
    Rectangle visible = tree.getVisibleRect();
    int actionStart = visible.x + visible.width - 34;
    int actionEnd = visible.x + visible.width - 8;
    return row >= 0 && e.getX() >= actionStart && e.getX() <= actionEnd;
  }

  private void showRowActionMenu(MouseEvent e, SceneNode node) {
    if (node.isLayer()) {
      showContextMenu(e);
    } else if (node.getMapObject() != null) {
      showMapObjectMenu(e, node.getMapObject());
    }
  }

  private void showMapObjectMenu(MouseEvent e, IMapObject mapObject) {
    Editor.instance().getMapComponent().setFocus(mapObject, true);
    JPopupMenu popup = new JPopupMenu();
    addContextMenuItem(popup, "Focus Object", Icons.POINTER_16,
        () -> Editor.instance().getMapComponent().setFocus(mapObject, true));
    addContextMenuItem(popup, "Center Camera", Icons.SEARCH_16,
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

  private void addLayer(SceneNode afterNode) {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer layer =
        new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer();
    layer.setName("new layer");
    int absIdx = afterNode != null ? getAbsoluteIndex(map, afterNode) : map.getRenderLayers().size() - 1;
    map.addLayer(absIdx + 1, layer);
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerStructureChanged();
  }

  private void removeLayer(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    if (map.getMapObjectLayers().size() <= 1) {
      return;
    }
    int index = map.getRenderLayers().indexOf(node.getLayer());
    IMapObjectLayer copy = new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer(
        (de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer) node.getLayer());

    Editor.instance().getMapComponent().delete((IMapObjectLayer) node.getLayer());
    map.removeLayer(node.getLayer());
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerStructureChanged();

    Toast.show(
        this.getRootPane(),
        Resources.strings().get("panel_layerDeleted"),
        () -> {
          map.addLayer(index, copy);
          this.refresh();
          Editor.instance().getMapComponent().add(copy);
        });
  }

  private void duplicateLayer(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    if (!(node.getLayer() instanceof IMapObjectLayer)) {
      return;
    }
    de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer copied =
        new de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer(
            (de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer) node.getLayer());
    int absIdx = getAbsoluteIndex(map, node);
    map.addLayer(absIdx + 1, copied);
    refresh();
    Editor.instance().getMapComponent().add(copied);
    UndoManager.instance().recordChanges();
    fireLayerStructureChanged();
  }

  private void renameLayer(SceneNode node) {
    if (node.getLayer() == null) {
      return;
    }
    String newName = javax.swing.JOptionPane.showInputDialog(
        Resources.strings().get("panel_renameLayer"), node.getName());
    if (newName == null) {
      return;
    }
    node.getLayer().setName(newName);
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerChanged();
  }

  private void setLayerColor(SceneNode node) {
    if (!(node.getLayer() instanceof IMapObjectLayer objLayer)) {
      return;
    }
    Color newColor = JColorChooser.showDialog(
        null,
        Resources.strings().get("panel_selectLayerColor"),
        objLayer.getColor());
    if (newColor == null) {
      return;
    }
    objLayer.setColor(newColor);
    UndoManager.instance().recordChanges();
    fireLayerChanged();
  }

  private void hideOtherLayers(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    for (ILayer renderLayer : map.getRenderLayers()) {
      if (renderLayer instanceof IMapObjectLayer objLayer) {
        if (objLayer != node.getLayer()) {
          objLayer.setVisible(false);
        } else if (!objLayer.isVisible()) {
          objLayer.setVisible(true);
        }
      }
    }
    Transform.updateAnchors();
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerChanged();
  }

  private void toggleLayerVisibility(SceneNode node) {
    if (node.getLayer() == null) {
      return;
    }
    node.getLayer().setVisible(!node.getLayer().isVisible());
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerChanged();
  }

  private void showAllLayers() {
    IMap map = getCurrentMap();
    if (map == null) {
      return;
    }
    for (ILayer renderLayer : map.getRenderLayers()) {
      renderLayer.setVisible(true);
    }
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerChanged();
  }

  private void moveLayerUp(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    int absIdx = getAbsoluteIndex(map, node);
    if (absIdx <= 0) {
      return;
    }
    map.removeLayer(node.getLayer());
    map.addLayer(absIdx - 1, node.getLayer());
    refresh();
    UndoManager.instance().recordChanges();
    fireLayerStructureChanged();
  }

  private void moveLayerDown(SceneNode node) {
    IMap map = getCurrentMap();
    if (map == null || node.getLayer() == null) {
      return;
    }
    int absIdx = getAbsoluteIndex(map, node);
    if (absIdx >= map.getRenderLayers().size() - 1) {
      return;
    }
    map.removeLayer(node.getLayer());
    map.addLayer(absIdx + 1, node.getLayer());
    refresh();
    UndoManager.instance().recordChanges();
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
    private final IMapObject mapObject;
    private final IEntity entity;
    private final boolean visible;
    private final int objectCount;
    private final boolean section;

    SceneNode(String name, Icon icon, ILayer layer, boolean visible, int objectCount) {
      this.name = name;
      this.icon = icon;
      this.layer = layer;
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
      this.mapObject = null;
      this.entity = null;
      this.visible = true;
      this.objectCount = 0;
      this.section = true;
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
        return "null";
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
          return name + " (" + objectCount + ")";
        }
        return name;
      }
      return name;
    }
  }

  private static class SceneTransferHandler extends TransferHandler {
    static final DataFlavor SCENE_NODE_FLAVOR = new DataFlavor(SceneNode.class, "SceneNode");

    @Override
    protected Transferable createTransferable(JComponent c) {
      if (!(c instanceof JTree tree)) {
        return null;
      }
      TreePath path = tree.getSelectionPath();
      if (path == null) {
        return null;
      }
      Object last = path.getLastPathComponent();
      if (!(last instanceof DefaultMutableTreeNode dmtn)) {
        return null;
      }
      if (!(dmtn.getUserObject() instanceof SceneNode node)) {
        return null;
      }
      // allow dragging layer nodes (for reorder) or map object nodes (for move)
      if (node.isLayer() && node.getLayer() != null) {
        return new SceneNodeTransferable(node);
      }
      if (!node.isLayer() && node.getMapObject() != null) {
        return new SceneNodeTransferable(node);
      }
      return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
      return MOVE;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
    }

    @Override
    public boolean canImport(TransferSupport support) {
      if (!support.isDrop()) {
        return false;
      }
      if (!support.isDataFlavorSupported(SCENE_NODE_FLAVOR)) {
        return false;
      }

      TreePath dropPath = ((JTree.DropLocation) support.getDropLocation()).getPath();
      if (dropPath == null) {
        return false;
      }
      Object last = dropPath.getLastPathComponent();
      if (!(last instanceof DefaultMutableTreeNode dmtn)) {
        return false;
      }
      if (!(dmtn.getUserObject() instanceof SceneNode targetNode)) {
        return false;
      }

      // drop target must be a layer node
      return targetNode.isLayer();
    }

    @Override
    public boolean importData(TransferSupport support) {
      if (!canImport(support)) {
        return false;
      }

      try {
        Transferable t = support.getTransferable();
        Object data = t.getTransferData(SCENE_NODE_FLAVOR);
        if (!(data instanceof SceneNode draggedNode)) {
          return false;
        }

        TreePath dropPath = ((JTree.DropLocation) support.getDropLocation()).getPath();
        Object last = dropPath.getLastPathComponent();
        if (!(last instanceof DefaultMutableTreeNode dmtn)) {
          return false;
        }
        if (!(dmtn.getUserObject() instanceof SceneNode targetNode)) {
          return false;
        }
        if (!targetNode.isLayer() || targetNode.getLayer() == null) {
          return false;
        }

        if (draggedNode.isLayer() && draggedNode.getLayer() != null) {
          // layer reorder
          if (draggedNode.getLayer() == targetNode.getLayer()) {
            return false;
          }
          return reorderLayer(draggedNode.getLayer(), targetNode.getLayer(), support.getComponent());
        } else if (!draggedNode.isLayer() && draggedNode.getMapObject() != null) {
          // map object move to layer
          IMapObject draggedObj = draggedNode.getMapObject();
          if (!(targetNode.getLayer() instanceof IMapObjectLayer targetLayer)) {
            return false;
          }
          if (draggedObj.getLayer() == targetLayer) {
            return false;
          }
          moveMapObjectToLayer(draggedObj, targetLayer);
          // refresh tree to reflect the move
          SceneGraph graph = findSceneGraph(support.getComponent());
          if (graph != null) {
            graph.refresh();
          }
          return true;
        }

        return false;
      } catch (Exception ex) {
        return false;
      }
    }

    private static boolean reorderLayer(ILayer dragged, ILayer target, Component treeComponent) {
      IMap map = Game.world().environment().getMap();
      if (map == null) {
        return false;
      }
      List<ILayer> layers = new ArrayList<>(map.getRenderLayers());
      int dragIdx = layers.indexOf(dragged);
      int targetIdx = layers.indexOf(target);
      if (dragIdx < 0 || targetIdx < 0 || dragIdx == targetIdx) {
        return false;
      }

      UndoManager.instance().beginOperation();
      try {
        map.removeLayer(dragged);
        // recompute target index after removal
        List<ILayer> afterRemove = new ArrayList<>(map.getRenderLayers());
        int newTargetIdx = afterRemove.indexOf(target);
        if (newTargetIdx < 0) {
          newTargetIdx = afterRemove.size();
        }
        if (dragIdx < targetIdx) {
          map.addLayer(newTargetIdx + 1, dragged);
        } else {
          map.addLayer(newTargetIdx, dragged);
        }
      } finally {
        UndoManager.instance().endOperation();
      }
      Transform.updateAnchors();
      // refresh tree to reflect new order
      SceneGraph graph = findSceneGraph(treeComponent);
      if (graph != null) {
        graph.refresh();
        graph.fireLayerStructureChanged();
      }
      return true;
    }

    private static void moveMapObjectToLayer(IMapObject mapObject, IMapObjectLayer targetLayer) {
      UndoManager.instance().beginOperation();
      try {
        UndoManager.instance().mapObjectChanging(mapObject);
        targetLayer.addMapObject(mapObject);
        Game.world().environment().reloadFromMap(mapObject.getId());
        UndoManager.instance().mapObjectChanged(mapObject);
      } finally {
        UndoManager.instance().endOperation();
      }
      UI.getInspector().bind(Editor.instance().getMapComponent().getFocusedMapObject());
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
  }

  private static class SceneNodeTransferable implements Transferable {
    private final SceneNode node;

    SceneNodeTransferable(SceneNode node) {
      this.node = node;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[]{SceneTransferHandler.SCENE_NODE_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavor == SceneTransferHandler.SCENE_NODE_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
      return node;
    }
  }
}
