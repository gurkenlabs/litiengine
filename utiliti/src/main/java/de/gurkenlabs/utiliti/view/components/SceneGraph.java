package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.OverlayScrollPane;
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
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityController;
import de.gurkenlabs.utiliti.controller.LayerController;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import de.gurkenlabs.utiliti.view.renderers.SceneGraphRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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

  private final JPanel searchPanel;
  private final JButton btnCollapse;
  private final JButton btnSearch;
  private final JTextField textField;
  private final Timer searchDebounce;

  private final JTree tree;
  private final DefaultTreeModel treeModel;
  private final DefaultMutableTreeNode nodeRoot;

  private boolean isFocussing;
  private boolean refreshing;

  private final java.util.Map<String, Integer> selectedLayers;
  private final java.util.List<Consumer<IMap>> layerChangedListeners;
  private final java.util.List<Consumer<IMap>> layerStructureChangedListeners;

  public SceneGraph() {
    super(new BorderLayout(0, 0));
    this.setName("Scene");
    this.selectedLayers = new java.util.concurrent.ConcurrentHashMap<>();
    this.layerChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.layerStructureChangedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    this.searchPanel = new JPanel(new BorderLayout(0, 0));

    this.btnCollapse = new JButton("");
    this.btnCollapse.setBorderPainted(false);
    this.btnCollapse.setContentAreaFilled(false);
    this.btnCollapse.setOpaque(false);
    this.btnCollapse.setMargin(new Insets(2, 2, 2, 2));
    this.btnCollapse.setIcon(Icons.COLLAPSE_24);
    this.btnCollapse.addActionListener(e -> collapseAll());

    final String searchDefault = Resources.strings().get("panel_entities_search_default");

    this.textField = new JTextField();
    this.textField.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, searchDefault);
    this.textField.setToolTipText(Resources.strings().get("panel_entities_search_hint"));
    this.textField.setColumns(10);
    this.textField.setBorder(null);
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

    this.btnSearch = new JButton("");
    this.btnSearch.setBorderPainted(false);
    this.btnSearch.setContentAreaFilled(false);
    this.btnSearch.setOpaque(false);
    this.btnSearch.setMargin(new Insets(2, 2, 2, 2));
    this.btnSearch.setIcon(Icons.SEARCH_24);
    this.btnSearch.addActionListener(e -> {
      searchDebounce.stop();
      search();
    });

    this.searchPanel.add(this.btnCollapse, BorderLayout.WEST);
    this.searchPanel.add(this.textField, BorderLayout.CENTER);
    this.searchPanel.add(this.btnSearch, BorderLayout.EAST);

    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
    getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent e) {
        textField.requestFocusInWindow();
        textField.selectAll();
      }
    });

    this.tree = new JTree();
    this.tree.setBorder(null);
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);
    this.tree.setCellRenderer(new SceneGraphRenderer());
    this.tree.setRowHeight((int) (this.tree.getRowHeight() * Editor.preferences().getUiScale()));

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
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
          showContextMenu(e);
        }
      }
    };
    this.tree.addMouseListener(ml);
    this.tree.setTransferHandler(new SceneTransferHandler());
    this.tree.setDragEnabled(true);

    this.add(new OverlayScrollPane(tree));
    this.add(this.searchPanel, BorderLayout.NORTH);

    Editor.instance().getMapComponent().onMapLoaded(map -> {
      this.refresh();
    });
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
      this.nodeRoot.removeAllChildren();

      Environment env = Game.world().environment();
      if (env == null || env.getMap() == null) {
        this.treeModel.reload();
        return;
      }

      IMap map = env.getMap();
      for (ILayer layer : map.getRenderLayers()) {
        if (layer == null) {
          continue;
        }
        DefaultMutableTreeNode layerNode = createLayerNode(layer);
        this.nodeRoot.add(layerNode);

        if (layer instanceof IMapObjectLayer objLayer) {
          List<IMapObject> objects = new ArrayList<>(objLayer.getMapObjects());
          objects.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
          for (IMapObject obj : objects) {
            if (obj == null) {
              continue;
            }
            IEntity entity = env.get(obj.getId());
            if (entity instanceof Entity ent) {
              DefaultMutableTreeNode objNode = new DefaultMutableTreeNode(
                  new SceneNode(obj, entity));
              layerNode.add(objNode);
            }
          }
        }
      }

      this.treeModel.reload();

      // restore per-map selection
      if (map != null && this.selectedLayers.containsKey(map.getName())) {
        int idx = this.selectedLayers.get(map.getName());
        if (idx >= 0 && idx < this.nodeRoot.getChildCount()) {
          DefaultMutableTreeNode target = (DefaultMutableTreeNode) this.nodeRoot.getChildAt(idx);
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
      return;
    }

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
    int absIdx = getAbsoluteIndex(map, afterNode);
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

    SceneNode(String name, Icon icon, ILayer layer, boolean visible, int objectCount) {
      this.name = name;
      this.icon = icon;
      this.layer = layer;
      this.mapObject = null;
      this.entity = null;
      this.visible = visible;
      this.objectCount = objectCount;
    }

    SceneNode(IMapObject mapObject, IEntity entity) {
      this.name = getEntityLabel(entity);
      this.icon = null;
      this.layer = null;
      this.mapObject = mapObject;
      this.entity = entity;
      this.visible = true;
      this.objectCount = 0;
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

    private static String getEntityLabel(IEntity entity) {
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
