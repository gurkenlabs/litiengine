package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.ITileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Controller;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityController;
import de.gurkenlabs.utiliti.controller.LayerController;
import de.gurkenlabs.utiliti.controller.MapController;
import de.gurkenlabs.utiliti.controller.PropertyInspector;
import de.gurkenlabs.utiliti.controller.Scroll;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.model.Cursors;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import de.gurkenlabs.utiliti.controller.tool.BucketFillTool;
import de.gurkenlabs.utiliti.controller.tool.EraserTool;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.StampBrushTool;
import de.gurkenlabs.utiliti.controller.tool.TerrainBrushTool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Style.Theme;
import de.gurkenlabs.utiliti.view.menus.CanvasPopupMenu;
import de.gurkenlabs.utiliti.view.menus.MainMenuBar;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class UI {
  private static final int INSPECTOR_BASE_WIDTH = 380;
  private static final int SCENE_GRAPH_MIN_WIDTH = 260;
  private static final int SCENE_GRAPH_MAX_WIDTH = 340;
  private static final int ASSET_PANEL_MIN_HEIGHT = 180;
  private static final int ASSET_PANEL_MAX_HEIGHT = 420;
  private static final int SPLITTER_SIZE = 4;

  private static final List<JComponent> orphanComponents = new CopyOnWriteArrayList<>();
  private static JPopupMenu canvasPopup;
  private static JPopupMenu objectSelectionPopup;
  private static Canvas objectSelectionPopupInvoker;
  private static int objectSelectionPopupX;
  private static int objectSelectionPopupY;
  private static AssetList assetComponent;

  private static MapObjectInspector mapObjectPanel;
  private static MapPropertyPanel mapPropertyPanel;
  private static LayerPropertyPanel layerPropertyPanel;
  private static LayerPropertyPanel tileLayerPropertyPanel;
  private static TilesetEditorPanel tilesetEditorPanel;
  private static TilesetTabsPanel tileLayerTilesetEditorPanel;
  private static SpriteEditorPanel spriteEditorPanel;
  private static JPanel inspectorHost;
  private static CardLayout inspectorCards;
  private static String activeInspectorCard = "objects";
  private static MapList mapSelectionPanel;
  private static SceneGraph sceneGraph;
  private static JComboBox<TmxMap> mapCombo;
  private static ViewportToolbar viewportToolbar;
  private static ViewportPanel viewportPanel;
  private static JButton inspectorBackButton;
  private static JButton inspectorForwardButton;
  private static KeyStroke inspectorBackShortcut;
  private static KeyStroke inspectorForwardShortcut;

  private static boolean initialized;

  private static volatile boolean loadingTheme;
  private static final Set<Object> themeOverrideKeys = new HashSet<>();

  private UI() {
  }

  /**
   * Adds an orphan component to the UI to ensure updating when switching themes even though the elements might not be part of the currently active
   * UI.
   *
   * @param component The orphan component to add.
   */
  public static void addOrphanComponent(JComponent component) {
    orphanComponents.add(component);
  }

  public static void removeOrphanComponent(JComponent component) {
    orphanComponents.remove(component);
  }

  public static boolean notifyPendingChanges() {
    Path resourceFile = Editor.instance().getCurrentResourceFile();
    if (Editor.instance().getChangedMaps().isEmpty() && !Editor.instance().isUnsavedProject()) {
      return true;
    }

    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_saveProjectMessage") + "\n" + resourceFile,
        Resources.strings().get("hud_saveProject"), JOptionPane.YES_NO_CANCEL_OPTION);

    if (n == JOptionPane.YES_OPTION) {
      Editor.instance().save(false);
    }

    return n != JOptionPane.CANCEL_OPTION && n != JOptionPane.CLOSED_OPTION;
  }

  public static boolean showRevertWarning() {
    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_revertChangesMessage"),
        Resources.strings().get("hud_revertChanges"), JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    initTools();
    Game.screens().display(Editor.instance());

    javax.swing.JComponent.setDefaultLocale(Locale.getDefault());
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    setDefaultSwingFont(Style.getDefaultFont());

    Tray.init();
    Cursors.initialize();
    setupInterface();
    Game.window().getHostControl().revalidate();

    UIManager.addPropertyChangeListener(e -> {
      for (JComponent component : orphanComponents) {
        SwingUtilities.updateComponentTreeUI(component);
      }
    });

    setTheme(Editor.preferences().getTheme());

    initialized = true;
  }

  public static PropertyInspector getInspector() {
    return mapObjectPanel;
  }

  public static void showObjectInspector() {
    if (inspectorCards == null || inspectorHost == null) {
      return;
    }
    inspectorCards.show(inspectorHost, "objects");
    activeInspectorCard = "objects";
    Editor.instance().getMapComponent().inspectorObjectShown(
      Editor.instance().getMapComponent().getFocusedMapObject());
  }

  public static void showMapProperties() {
    if (mapPropertyPanel == null) {
      return;
    }

    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      mapPropertyPanel.bind(Game.world().environment().getMap());
      if (sceneGraph != null) {
        sceneGraph.selectMap();
      }
    }
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "map");
      activeInspectorCard = "map";
    }
    IMap map = Game.world().environment() == null ? null : Game.world().environment().getMap();
    if (inspectorCards != null && inspectorHost != null) {
      Editor.instance().getMapComponent().inspectorMapShown(map);
    }
  }

  static void showMapTilesetMenu(javax.swing.JButton owner) {
    if (bindCurrentMapTilesetPanel()) {
      mapPropertyPanel.showAddTilesetMenu(owner);
    }
  }

  static void addAllMapTilesets() {
    if (bindCurrentMapTilesetPanel()) {
      mapPropertyPanel.addAllTilesets();
    }
  }

  static void createMapTileset() {
    if (bindCurrentMapTilesetPanel()) {
      mapPropertyPanel.createTileset();
    }
  }

  static void removeSelectedMapTileset(TilesetTabsPanel panel) {
    if (bindCurrentMapTilesetPanel()) {
      mapPropertyPanel.removeSelectedTileset(panel.getSelectedTileset());
    }
  }

  private static boolean bindCurrentMapTilesetPanel() {
    if (mapPropertyPanel == null || Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return false;
    }
    mapPropertyPanel.bind(Game.world().environment().getMap());
    return true;
  }

  public static void mapTilesetsChanged(IMap map) {
    if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
      javax.swing.SwingUtilities.invokeLater(() -> mapTilesetsChanged(map));
      return;
    }
    if (mapPropertyPanel != null) {
      mapPropertyPanel.refreshTilesets(map);
    }
    if (tileLayerTilesetEditorPanel != null) {
      tileLayerTilesetEditorPanel.bindIfMapChanged(map);
    }
    if (sceneGraph != null) {
      sceneGraph.refreshLayerCommandState();
    }
  }

  public static void showLayerProperties(ILayer layer) {
    if (layerPropertyPanel == null) {
      return;
    }

    if (layer instanceof ITileLayer) {
      tileLayerPropertyPanel.bind(layer);
      tileLayerTilesetEditorPanel.bindIfMapChanged(Game.world().environment().getMap());
      if (inspectorCards != null && inspectorHost != null) {
        inspectorCards.show(inspectorHost, "tileLayers");
        activeInspectorCard = "tileLayers";
      }
      if (inspectorCards != null && inspectorHost != null) {
        Editor.instance().getMapComponent().inspectorLayerShown(layer);
      }
      return;
    }

    layerPropertyPanel.bind(layer);
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "layers");
      activeInspectorCard = "layers";
    }
    if (inspectorCards != null && inspectorHost != null) {
      Editor.instance().getMapComponent().inspectorLayerShown(layer);
    }
  }

  public static void showTilesetInspector(Tileset tileset) {
    if (tilesetEditorPanel == null) {
      return;
    }

    tilesetEditorPanel.bind(tileset);
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "tilesets");
      activeInspectorCard = "tilesets";
    }
  }

  public static void showTileLayerTilesetInspector(Tileset tileset) {
    if (tileLayerTilesetEditorPanel == null) {
      return;
    }
    tileLayerTilesetEditorPanel.bind(Game.world().environment().getMap());
    tileLayerTilesetEditorPanel.select(tileset);
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "tileLayers");
      activeInspectorCard = "tileLayers";
    }
  }

  private static Tileset activeMapTileset() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return null;
    }
    return Game.world().environment().getMap().getTilesets().stream()
      .filter(Tileset.class::isInstance)
      .map(Tileset.class::cast)
      .findFirst()
      .orElse(null);
  }

  public static void hideTilesetInspector() {
    hideAssetInspector();
  }

  public static void hideAssetInspector() {
    if (!"tilesets".equals(activeInspectorCard) && !"sprites".equals(activeInspectorCard)) {
      return;
    }

    if (Editor.instance().getMapComponent().getFocusedMapObject() != null) {
      showObjectInspector();
    } else {
      showMapProperties();
    }
  }

  public static void showSpriteInspector(SpritesheetResource spritesheetResource) {
    if (spriteEditorPanel == null) {
      return;
    }

    spriteEditorPanel.bind(spritesheetResource);
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "sprites");
      activeInspectorCard = "sprites";
    }
    if (inspectorCards != null && inspectorHost != null) {
      Editor.instance().getMapComponent().inspectorSpriteShown(spritesheetResource);
    }
  }

  public static LayerController getLayerController() {
    return sceneGraph;
  }

  public static EntityController getEntityController() {
    return sceneGraph;
  }

  public static Controller getAssetController() {
    return assetComponent;
  }

  public static ViewportToolbar getViewportToolbar() {
    return viewportToolbar;
  }

  public static JPopupMenu getCanvasPopup() {
    return canvasPopup;
  }

  public static void refreshKeyBindings() {
    if (Game.window() != null && Game.window().getHostControl() != null) {
      KeyBindings.refresh(Game.window().getHostControl());
    }
    for (JComponent component : orphanComponents) {
      KeyBindings.refresh(component);
    }
    if (Game.window() != null && Game.window().getHostControl() instanceof JFrame window) {
      refreshInspectorNavigationShortcuts(window);
    }
  }

  public static MapController getMapController() {
    return mapSelectionPanel;
  }

  public static void setMapCombo(JComboBox<TmxMap> combo) {
    mapCombo = combo;
  }

  public static JComboBox<TmxMap> getMapCombo() {
    return mapCombo;
  }

  private static void setupInterface() {
    JFrame window = initWindow();
    installInspectorNavigationShortcuts(window);
    int winW = window.getSize().width;
    int winH = window.getSize().height;

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setVisible(false);
    window.remove(canvas);
    Component leftPanel = initLeftPanel();
    viewportToolbar = new ViewportToolbar(mapCombo);
    viewportPanel = new ViewportPanel(canvas);
    initDropTarget(canvas);

    Component renderSplitPanel = initRenderSplitPanel(viewportPanel, winH);

    int inspectorMinWidth = inspectorMinimumWidth();
    mapObjectPanel = new MapObjectInspector();
    mapObjectPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    mapPropertyPanel = new MapPropertyPanel();
    mapPropertyPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    layerPropertyPanel = new LayerPropertyPanel();
    layerPropertyPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    tileLayerPropertyPanel = new LayerPropertyPanel();
    tileLayerPropertyPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    tilesetEditorPanel = new TilesetEditorPanel();
    tilesetEditorPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    tileLayerTilesetEditorPanel = new TilesetTabsPanel();
    tileLayerTilesetEditorPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    ExpandableCard tileLayerTilesets = tileLayerPropertyPanel.addSection(
        Resources.strings().get("assettree_tilesets"), tileLayerTilesetEditorPanel, true);
    tileLayerTilesets.setFillsAvailableHeight(true);
    tileLayerTilesets.setHeaderTrailing(tileLayerTilesetEditorPanel.getCommands());
    spriteEditorPanel = new SpriteEditorPanel();
    spriteEditorPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));
    inspectorCards = new CardLayout();
    inspectorHost = new JPanel(inspectorCards);
    inspectorHost.add(mapObjectPanel, "objects");
    inspectorHost.add(mapPropertyPanel, "map");
    inspectorHost.add(layerPropertyPanel, "layers");
    javax.swing.JScrollPane tilesetInspectorScroll = new javax.swing.JScrollPane(tilesetEditorPanel);
    tilesetInspectorScroll.setBorder(null);
    tilesetInspectorScroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    tilesetInspectorScroll.getViewport().setBackground(Style.COLOR_BG);
    inspectorHost.add(tilesetInspectorScroll, "tilesets");
    inspectorHost.add(tileLayerPropertyPanel, "tileLayers");
    inspectorHost.add(spriteEditorPanel, "sprites");
    inspectorHost.setMinimumSize(new Dimension(inspectorMinWidth, 0));

    JLabel inspectorTitle = new JLabel(Resources.strings().get("panel_inspector"));
    inspectorTitle.setFont(inspectorTitle.getFont().deriveFont(Font.BOLD));
    JPanel inspectorHeader = new JPanel(new BorderLayout());
    inspectorHeader.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    inspectorHeader.add(inspectorTitle, BorderLayout.WEST);
    inspectorBackButton = Style.iconButton(Icons.BACK_16);
    inspectorBackButton.addActionListener(event -> Editor.instance().getMapComponent().navigateInspectorBack());
    inspectorForwardButton = Style.iconButton(Icons.FORWARD_16);
    inspectorForwardButton.addActionListener(event -> Editor.instance().getMapComponent().navigateInspectorForward());
    refreshInspectorNavigationShortcuts(window);
    Runnable updateInspectorNavigation = () -> {
      inspectorBackButton.setEnabled(Editor.instance().getMapComponent().canNavigateInspectorBack());
      inspectorForwardButton.setEnabled(Editor.instance().getMapComponent().canNavigateInspectorForward());
    };
    Editor.instance().getMapComponent().onInspectorNavigationChanged(updateInspectorNavigation);
    Editor.instance().getMapComponent().onMapLoaded(ignored -> updateInspectorNavigation.run());
    JPanel inspectorNavigation = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    inspectorNavigation.setOpaque(false);
    inspectorNavigation.add(inspectorBackButton);
    inspectorNavigation.add(inspectorForwardButton);
    inspectorHeader.add(inspectorNavigation, BorderLayout.EAST);
    updateInspectorNavigation.run();
    JPanel inspectorPanel = new JPanel(new BorderLayout());
    inspectorPanel.add(inspectorHeader, BorderLayout.NORTH);
    inspectorPanel.add(inspectorHost, BorderLayout.CENTER);
    inspectorPanel.setMinimumSize(new Dimension(inspectorMinWidth, 0));

    int prefInspectorW = Math.max(inspectorMinWidth, (int) (winW * 0.20));
    int prefHierarchyW = Math.max(SCENE_GRAPH_MIN_WIDTH, Math.min(SCENE_GRAPH_MAX_WIDTH, (int) (winW * 0.18)));
    int initialHierarchyW = Editor.preferences().getMainSplitterPosition() != 0
        ? Math.min(Editor.preferences().getMainSplitterPosition(), SCENE_GRAPH_MAX_WIDTH)
        : prefHierarchyW;
    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, renderSplitPanel);
    configureSplitPane(mainSplit);
    mainSplit.setContinuousLayout(false);
    mainSplit.setResizeWeight(0.0);
    mainSplit.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        Editor.preferences().setWidth(window.getWidth());
        Editor.preferences().setHeight(window.getHeight());
      }
    });
    mainSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
      int location = mainSplit.getDividerLocation();
      if (location > SCENE_GRAPH_MAX_WIDTH) {
        mainSplit.setDividerLocation(SCENE_GRAPH_MAX_WIDTH);
        location = SCENE_GRAPH_MAX_WIDTH;
      }
      Editor.preferences().setMainSplitter(location);
    });

    JPanel workspacePanel = new JPanel(new BorderLayout());
    workspacePanel.add(viewportToolbar, BorderLayout.NORTH);
    workspacePanel.add(mainSplit, BorderLayout.CENTER);

    JSplitPane centerRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, workspacePanel, inspectorPanel);
    configureSplitPane(centerRightSplit);
    centerRightSplit.setContinuousLayout(false);
    centerRightSplit.setResizeWeight(1.0);
    int initialInspectorDivider = initialInspectorDivider(
        winW, initialHierarchyW, inspectorMinWidth, prefInspectorW,
        Editor.preferences().getSelectionEditSplitter());

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);
    rootPanel.add(centerRightSplit, BorderLayout.CENTER);
    mainSplit.setDividerLocation(initialHierarchyW);
    centerRightSplit.setDividerLocation(initialInspectorDivider);
    centerRightSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
      int viewportDivider = centerRightSplit.getDividerLocation()
          - mainSplit.getDividerLocation() - SPLITTER_SIZE;
      Editor.preferences().setSelectionEditSplitter(Math.max(0, viewportDivider));
    });

    initPopupMenu(canvas);
    window.getRootPane().setBackground(Style.COLOR_BG);
    window.setJMenuBar(new MainMenuBar());
    canvas.setVisible(true);
    window.invalidate();
    window.validate();
  }

  private static void installInspectorNavigationShortcuts(JFrame window) {
    JComponent rootPane = window.getRootPane();
    rootPane.getActionMap().put("inspectorBack", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent event) {
        Editor.instance().getMapComponent().navigateInspectorBack();
      }
    });
    rootPane.getActionMap().put("inspectorForward", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent event) {
        Editor.instance().getMapComponent().navigateInspectorForward();
      }
    });
    Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
      if (!(event instanceof MouseEvent mouseEvent)
        || !(mouseEvent.getSource() instanceof Component source)
        || SwingUtilities.getWindowAncestor(source) != window) {
        return;
      }
      if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
        Editor.instance().getMapComponent().handleInspectorNavigationMousePressed(mouseEvent);
      } else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
        Editor.instance().getMapComponent().handleInspectorNavigationMouseReleased(mouseEvent);
      }
    }, AWTEvent.MOUSE_EVENT_MASK);
    refreshInspectorNavigationShortcuts(window);
  }

  private static void refreshInspectorNavigationShortcuts(JFrame window) {
    javax.swing.InputMap inputMap = window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    if (inspectorBackShortcut != null) {
      inputMap.remove(inspectorBackShortcut);
    }
    if (inspectorForwardShortcut != null) {
      inputMap.remove(inspectorForwardShortcut);
    }
    inspectorBackShortcut = KeyBindings.get(KeyBindings.Command.INSPECTOR_BACK);
    inspectorForwardShortcut = KeyBindings.get(KeyBindings.Command.INSPECTOR_FORWARD);
    if (inspectorBackShortcut != null) {
      inputMap.put(inspectorBackShortcut, "inspectorBack");
    }
    if (inspectorForwardShortcut != null) {
      inputMap.put(inspectorForwardShortcut, "inspectorForward");
    }
    if (inspectorBackButton != null) {
      inspectorBackButton.setToolTipText(shortcutTooltip("inspector_back", inspectorBackShortcut));
    }
    if (inspectorForwardButton != null) {
      inspectorForwardButton.setToolTipText(shortcutTooltip("inspector_forward", inspectorForwardShortcut));
    }
  }

  private static String shortcutTooltip(String resourceKey, KeyStroke shortcut) {
    String formatted = KeyBindings.format(shortcut);
    return formatted.isEmpty()
        ? Resources.strings().get(resourceKey)
        : Resources.strings().get(resourceKey) + " (" + formatted + ")";
  }

  private static JFrame initWindow() {
    JFrame window = ((JFrame) Game.window().getHostControl());
    window.setResizable(true);

    Game.addGameListener(new GameListener() {
      @Override public boolean terminating() {
        boolean terminate = notifyPendingChanges();
        if (terminate) {
          Editor.preferences().setFrameState(window.getExtendedState());
        }

        return terminate;
      }
    });

    window.setLocationRelativeTo(null);
    if (Editor.preferences().getFrameState() != java.awt.Frame.ICONIFIED && Editor.preferences().getFrameState() != java.awt.Frame.NORMAL) {
      window.setExtendedState(Editor.preferences().getFrameState());
    } else if (Editor.preferences().getWidth() != 0 && Editor.preferences().getHeight() != 0) {
      window.setSize(Editor.preferences().getWidth(), Editor.preferences().getHeight());
    }

    return window;
  }

  private static Component initRenderSplitPanel(JPanel renderPanel, int winH) {
    JSplitPane renderSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, initBottomPanel());
    configureSplitPane(renderSplitPanel);
    renderSplitPanel.setResizeWeight(1.0);
    if (Editor.preferences().getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(Editor.preferences().getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (winH * 0.72));
    }
    renderSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
      int location = constrainBottomDivider(
        renderSplitPanel.getHeight(), renderSplitPanel.getDividerSize(), renderSplitPanel.getDividerLocation());
      if (location != renderSplitPanel.getDividerLocation()) {
        renderSplitPanel.setDividerLocation(location);
      } else {
        Editor.preferences().setBottomSplitter(location);
      }
    });
    renderSplitPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent event) {
        renderSplitPanel.setDividerLocation(constrainBottomDivider(
          renderSplitPanel.getHeight(), renderSplitPanel.getDividerSize(), renderSplitPanel.getDividerLocation()));
      }
    });
    renderSplitPanel.setContinuousLayout(false);
    return renderSplitPanel;
  }

  static int constrainBottomDivider(int splitHeight, int dividerSize, int dividerLocation) {
    if (splitHeight <= 0 || dividerLocation < 0) {
      return dividerLocation;
    }
    int availableHeight = Math.max(0, splitHeight - dividerSize);
    int minimumLocation = Math.max(0, availableHeight - ASSET_PANEL_MAX_HEIGHT);
    int maximumLocation = Math.max(minimumLocation, availableHeight - ASSET_PANEL_MIN_HEIGHT);
    return Math.max(minimumLocation, Math.min(maximumLocation, dividerLocation));
  }

  static void configureSplitPane(JSplitPane splitPane) {
    splitPane.setBorder(null);
    splitPane.setDividerSize(SPLITTER_SIZE);
    splitPane.setUI(new BasicSplitPaneUI() {
      @Override public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
          {
            setBorder(null);
            setBackground(Style.COLOR_BG);
          }

          @Override public void paint(Graphics g) {
            g.setColor(Style.COLOR_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
          }
        };
      }
    });
    splitPane.setDividerSize(SPLITTER_SIZE);
  }

  private static Component initLeftPanel() {
    mapSelectionPanel = new MapList();
    sceneGraph = new SceneGraph();

    JComboBox<TmxMap> leftMapCombo = new JComboBox<>();
    leftMapCombo.setRenderer(new de.gurkenlabs.utiliti.view.renderers.MapListCellRenderer());
    leftMapCombo.setFont(Style.getDefaultFont());
    leftMapCombo.setPreferredSize(new Dimension(0, Style.CONTROL_HEIGHT));
    leftMapCombo.setMinimumSize(new Dimension(100, Style.CONTROL_HEIGHT));
    leftMapCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.CONTROL_HEIGHT));
    leftMapCombo.addActionListener(e -> {
      if (Boolean.TRUE.equals(leftMapCombo.getClientProperty("updating"))
          || Editor.instance().isLoading() || Editor.instance().getMapComponent().isLoading()) {
        return;
      }
      Object selected = leftMapCombo.getSelectedItem();
      if (selected instanceof TmxMap map) {
        if (Game.world().environment() != null && Game.world().environment().getMap() == map) {
          return;
        }
        Editor.instance().getMapComponent().loadEnvironment(map);
      }
    });
    Editor.instance().getMapComponent().onMapLoaded(map -> {
      leftMapCombo.putClientProperty("updating", true);
      for (int i = 0; i < leftMapCombo.getItemCount(); i++) {
        if (leftMapCombo.getItemAt(i) == map) {
          leftMapCombo.setSelectedIndex(i);
          leftMapCombo.putClientProperty("updating", false);
          return;
        }
      }
      leftMapCombo.putClientProperty("updating", false);
    });
    UI.setMapCombo(leftMapCombo);

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setOpaque(true);
    leftPanel.setBackground(Style.COLOR_BG);
    leftPanel.add(sceneGraph, BorderLayout.CENTER);
    leftPanel.setMinimumSize(new Dimension(SCENE_GRAPH_MIN_WIDTH, 120));
    leftPanel.setPreferredSize(new Dimension(SCENE_GRAPH_MIN_WIDTH, 0));
    leftPanel.setMaximumSize(new Dimension(SCENE_GRAPH_MAX_WIDTH, Integer.MAX_VALUE));

    return leftPanel;
  }

  private static void initTools() {
    ToolManager tm = ToolManager.instance();
    tm.register(new PointerTool());
    tm.register(new StampBrushTool());
    tm.register(new BucketFillTool());
    tm.register(new TerrainBrushTool());
    tm.register(new EraserTool());
  }

  private static void initDropTarget(Canvas canvas) {
    new DropTarget(canvas, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
      @Override public void dragEnter(DropTargetDragEvent event) {
        if (event.isDataFlavorSupported(AssetTransferable.ASSET_FLAVOR)) {
          event.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
          event.rejectDrag();
        }
      }

      @Override public void drop(DropTargetDropEvent event) {
        if (!event.isDataFlavorSupported(AssetTransferable.ASSET_FLAVOR)) {
          event.rejectDrop();
          return;
        }

        event.acceptDrop(DnDConstants.ACTION_COPY);
        boolean created = false;
        try {
          Object payload = event.getTransferable().getTransferData(AssetTransferable.ASSET_FLAVOR);
          for (Object asset : AssetTransferable.getAssets(payload)) {
            created |= Editor.instance().getMapComponent().addMapObjectAt(asset, event.getLocation());
          }
        } catch (Exception ex) {
          created = false;
        } finally {
          event.dropComplete(created);
        }
      }
    }, true);
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setOpaque(true);
    bottomPanel.setBackground(Style.COLOR_BG);
    bottomPanel.setMinimumSize(new Dimension(600, ASSET_PANEL_MIN_HEIGHT));
    bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ASSET_PANEL_MAX_HEIGHT));

    assetComponent = new AssetList();
    JPanel content = new JPanel(new CardLayout());
    content.add(assetComponent, "resources");
    ConsoleComponent consoleComponent = new ConsoleComponent();
    content.add(consoleComponent, "console");

    JToggleButton resourcesTab = createBottomTab(Resources.strings().get("assettree_assets"), true, false);
    JToggleButton consoleTab = createBottomTab(Resources.strings().get("assettree_console"), false, true);
    Runnable updateConsoleStatus =
        () -> {
          Runnable update =
              () -> {
                int warnings = consoleComponent.getLogHandler().getWarningCount();
                int errors = consoleComponent.getLogHandler().getErrorCount();
                consoleTab.putClientProperty("consoleWarnings", warnings);
                consoleTab.putClientProperty("consoleErrors", errors);
                consoleTab.setToolTipText(
                    warnings > 0 || errors > 0
                        ? Resources.strings().get("console_status", warnings, errors)
                        : null);
                consoleTab.repaint();
              };
          if (SwingUtilities.isEventDispatchThread()) {
            update.run();
          } else {
            SwingUtilities.invokeLater(update);
          }
        };
    consoleComponent.getLogHandler().addChangeListener(updateConsoleStatus);
    updateConsoleStatus.run();
    ButtonGroup tabs = new ButtonGroup();
    tabs.add(resourcesTab);
    tabs.add(consoleTab);

    JPanel tabButtons = new JPanel(new GridBagLayout());
    tabButtons.setOpaque(false);
    GridBagConstraints tabConstraints = new GridBagConstraints();
    tabConstraints.fill = GridBagConstraints.BOTH;
    tabConstraints.weighty = 1.0;
    tabButtons.add(resourcesTab, tabConstraints);
    tabButtons.add(consoleTab, tabConstraints);

    JPanel header = new JPanel(new BorderLayout()) {
      @Override
      public void updateUI() {
        super.updateUI();
        setBackground(Style.background());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
      }
    };
    header.setOpaque(true);
    Dimension headerSize = new Dimension(0, Style.CONTROL_HEIGHT + Style.SPACE_MEDIUM * 2);
    header.setMinimumSize(headerSize);
    header.setPreferredSize(headerSize);
    header.add(tabButtons, BorderLayout.WEST);
    header.add(assetComponent.getToolbar(), BorderLayout.CENTER);

    resourcesTab.addActionListener(e -> {
      ((CardLayout) content.getLayout()).show(content, "resources");
      assetComponent.getToolbar().setVisible(true);
    });
    consoleTab.addActionListener(e -> {
      ((CardLayout) content.getLayout()).show(content, "console");
      assetComponent.getToolbar().setVisible(false);
    });

    bottomPanel.add(header, BorderLayout.NORTH);
    bottomPanel.add(content, BorderLayout.CENTER);

    return bottomPanel;
  }

  private static JToggleButton createBottomTab(String text, boolean selected, boolean showIndicators) {
    JToggleButton tab = new JToggleButton(text, selected) {
      @Override
      protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          if (getModel().isRollover() || isSelected()) {
            g2.setColor(isSelected() ? Style.selection() : Style.hover());
            g2.fillRect(0, 0, getWidth(), getHeight());
          }
          g2.setColor(isSelected() ? Style.text() : Style.mutedText());
          g2.setFont(getFont());
          java.awt.FontMetrics metrics = g2.getFontMetrics();
          int warnings = showIndicators ? consoleCount(getClientProperty("consoleWarnings")) : 0;
          int errors = showIndicators ? consoleCount(getClientProperty("consoleErrors")) : 0;
          int badgeCount = (warnings > 0 ? 1 : 0) + (errors > 0 ? 1 : 0);
          int indicatorWidth =
              badgeCount > 0
                  ? Style.SPACE_MEDIUM + badgeCount * 18 + (badgeCount - 1) * 3
                  : 0;
          int contentWidth = metrics.stringWidth(getText()) + indicatorWidth;
          int textX = Math.max(0, (getWidth() - contentWidth) / 2);
          int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
          g2.drawString(getText(), textX, textY);
          int indicatorX = textX + metrics.stringWidth(getText()) + Style.SPACE_MEDIUM;
          if (warnings > 0) {
            paintConsoleBadge(
                g2, indicatorX, (getHeight() - 16) / 2, warnings, Style.COLOR_ORANGE);
            indicatorX += 21;
          }
          if (errors > 0) {
            paintConsoleBadge(g2, indicatorX, (getHeight() - 16) / 2, errors, Style.COLOR_RED);
          }
          if (isSelected()) {
            g2.setColor(Style.accent());
            g2.fillRect(0, getHeight() - 2, getWidth(), 2);
          }
        } finally {
          g2.dispose();
        }
      }
    };
    tab.setFont(Style.getHeaderFont());
    int horizontalPadding = Style.SPACE_MEDIUM;
    tab.setMargin(new java.awt.Insets(0, horizontalPadding, 0, horizontalPadding));
    tab.setBorder(BorderFactory.createEmptyBorder());
    tab.setOpaque(false);
    tab.setContentAreaFilled(false);
    tab.setBorderPainted(false);
    tab.setFocusPainted(false);
    tab.setRolloverEnabled(true);
    int textWidth = tab.getFontMetrics(tab.getFont()).stringWidth(text);
    int width = textWidth + horizontalPadding * 2 + (showIndicators ? 42 : 0);
    Dimension size = new Dimension(width, Style.CONTROL_HEIGHT + Style.SPACE_MEDIUM * 2);
    tab.setPreferredSize(size);
    tab.setMinimumSize(size);
    tab.setMaximumSize(size);
    return tab;
  }

  private static void paintConsoleBadge(Graphics2D graphics, int x, int y, int count, Color color) {
    graphics.setColor(color);
    graphics.fillRoundRect(x, y, 18, 16, 16, 16);
    graphics.setColor(Color.WHITE);
    graphics.setFont(Style.getHeaderFont().deriveFont(Font.BOLD, 10f));
    String text = count > 9 ? "9+" : Integer.toString(count);
    java.awt.FontMetrics metrics = graphics.getFontMetrics();
    graphics.drawString(
        text,
        x + (18 - metrics.stringWidth(text)) / 2,
        y + (16 - metrics.getHeight()) / 2 + metrics.getAscent());
  }

  private static int consoleCount(Object value) {
    return value instanceof Integer count ? count : 0;
  }

  private static void initPopupMenu(Canvas canvas) {
    canvasPopup = new CanvasPopupMenu();
    addOrphanComponent(canvasPopup);
    objectSelectionPopup = new JPopupMenu();
    addOrphanComponent(objectSelectionPopup);
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
      if (event.getID() == KeyEvent.KEY_RELEASED
        && event.getKeyCode() == KeyEvent.VK_ALT
        && objectSelectionPopup.isVisible()) {
        SwingUtilities.invokeLater(() -> {
          if (!objectSelectionPopup.isVisible() && objectSelectionPopupInvoker != null) {
            objectSelectionPopup.show(
              objectSelectionPopupInvoker, objectSelectionPopupX, objectSelectionPopupY);
          }
        });
      }
      return false;
    });

    canvas.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        showCanvasPopup(canvas, e);
      }

      @Override public void mouseReleased(MouseEvent e) {
        showCanvasPopup(canvas, e);
      }
    });
  }

  private static void showCanvasPopup(Canvas canvas, MouseEvent event) {
    if (!event.isPopupTrigger()) {
      return;
    }

    var mapComponent = Editor.instance().getMapComponent();
    mapComponent.setTransformMode(TransformMode.NONE);
    if (!event.isAltDown()) {
      canvasPopup.show(canvas, event.getX(), event.getY());
      return;
    }

    List<IMapObject> mapObjects = mapComponent.getMapObjectsAt(event.getPoint());
    if (mapObjects.size() == 1) {
      mapComponent.setFocus(mapObjects.getFirst(), true);
    } else if (mapObjects.size() > 1) {
      objectSelectionPopup.removeAll();
      for (IMapObject mapObject : mapObjects) {
        MapObjectType type = MapObjectType.get(mapObject.getType());
        String name = mapObject.getName() == null || mapObject.getName().isBlank()
          ? type.name() : mapObject.getName();
        JMenuItem item = new JMenuItem(name + " (#" + mapObject.getId() + ")", Icons.forMapObjectType(type));
        item.addActionListener(e -> {
          objectSelectionPopupInvoker = null;
          mapComponent.setFocus(mapObject, true);
        });
        objectSelectionPopup.add(item);
      }
      objectSelectionPopupInvoker = canvas;
      objectSelectionPopupX = event.getX();
      objectSelectionPopupY = event.getY();
      objectSelectionPopup.show(canvas, event.getX(), event.getY());
    }
    event.consume();
  }

  public static void initLookAndFeel() {
    LafManager.setDecorationsEnabled(true);
    setTheme(Editor.preferences().getTheme());
  }

  public static synchronized void setTheme(Theme theme) {
    if (loadingTheme) {
      return;
    }

    System.setProperty("darklaf.animatedLafChange", "false");
    loadingTheme = true;
    clearThemeOverrides();

    switch (theme) {
      case DARK -> {
        LafManager.install(new OneDarkTheme());
        applyAndTrackThemeOverrides(UI::applyTokyoNightOverrides);
      }
      case LIGHT -> {
        LafManager.install(new IntelliJTheme());
        applyAndTrackThemeOverrides(UI::applyLightOverrides);
      }
    }

    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setBackground(Style.workspaceBottom());
    }
    Editor.preferences().setTheme(theme);
    if (Game.window() != null && Game.window().getHostControl() != null) {
      SwingUtilities.updateComponentTreeUI(Game.window().getHostControl());
    }
    if (viewportPanel != null) {
      viewportPanel.refreshTheme();
    }
    if (viewportToolbar != null) {
      viewportToolbar.refreshTheme();
    }
    updateOrphanComponents();
    loadingTheme = false;
  }

  private static void applyTokyoNightOverrides() {
    // Rounded corners for modern look
    applyCompactMetrics();
    UIManager.put("Button.arc", 6);
    UIManager.put("Component.arc", 6);
    UIManager.put("TextComponent.arc", 5);
    UIManager.put("TabbedPane.arc", 5);

    // Panels - borderless design with subtle contrast
    UIManager.put("Panel.background", Style.COLOR_BG);
    UIManager.put("Panel.foreground", Style.COLOR_TEXT);
    UIManager.put("Editor.surface", Style.COLOR_SURFACE);
    UIManager.put("Editor.surfaceRaised", Style.COLOR_SURFACE2);
    UIManager.put("Editor.border", Style.COLOR_BORDER);
    UIManager.put("Editor.mutedText", Style.COLOR_SUBTEXT);
    UIManager.put("Editor.accent", Style.COLOR_ACCENT_BLUE);
    UIManager.put("Editor.hover", Style.COLOR_HOVER);
    UIManager.put("Editor.selection", Style.COLOR_SELECTION_INACTIVE);
    UIManager.put("Editor.workspaceTop", Style.COLOR_WORKSPACE_TOP);
    UIManager.put("Editor.workspaceBottom", Style.COLOR_WORKSPACE_BOTTOM);
    UIManager.put("Editor.assetExplorerBackground", Style.COLOR_ASSET_EXPLORER);
    UIManager.put("Editor.mapBacking", Style.COLOR_MAP_BACKING);
    UIManager.put("Editor.mapBorder", Style.COLOR_MAP_BORDER);
    Color INPUT_BG = Style.COLOR_INPUT_BG;
    UIManager.put("TextField.background", INPUT_BG);
    UIManager.put("TextField.foreground", Style.COLOR_TEXT);
    UIManager.put("TextField.caretForeground", Style.COLOR_ACCENT_BLUE);
    UIManager.put("TextArea.background", INPUT_BG);
    UIManager.put("TextArea.foreground", Style.COLOR_TEXT);
    UIManager.put("TextPane.background", Style.COLOR_ASSET_EXPLORER);
    UIManager.put("TextPane.foreground", Style.COLOR_TEXT);
    UIManager.put("FormattedTextField.background", INPUT_BG);
    UIManager.put("FormattedTextField.foreground", Style.COLOR_TEXT);
    UIManager.put("PasswordField.background", INPUT_BG);
    UIManager.put("PasswordField.foreground", Style.COLOR_TEXT);
    UIManager.put("ComboBox.background", INPUT_BG);
    UIManager.put("ComboBox.foreground", Style.COLOR_TEXT);
    UIManager.put("ComboBox.selectionBackground", Style.COLOR_SELECTION_INACTIVE);
    UIManager.put("ComboBox.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("List.background", Style.COLOR_BG);
    UIManager.put("List.foreground", Style.COLOR_TEXT);
    UIManager.put("List.selectionBackground", Style.COLOR_SELECTION_INACTIVE);
    UIManager.put("List.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("Table.background", Style.COLOR_SURFACE2);
    UIManager.put("Table.foreground", Style.COLOR_TEXT);
    UIManager.put("Table.selectionBackground", Style.COLOR_SELECTION_INACTIVE);
    UIManager.put("Table.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("Table.gridColor", Style.COLOR_BORDER);
    UIManager.put("Tree.background", Style.COLOR_BG);
    UIManager.put("Tree.foreground", Style.COLOR_TEXT);
    UIManager.put("Tree.selectionBackground", Style.COLOR_TRANSPARENT);
    UIManager.put("Tree.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("Tree.textBackground", Style.COLOR_BG);
    UIManager.put("Tree.textForeground", Style.COLOR_TEXT);

    // TabbedPane - modern minimal headers
    UIManager.put("TabbedPane.background", Style.COLOR_BG);
    UIManager.put("TabbedPane.foreground", Style.COLOR_SUBTEXT);
    UIManager.put("TabbedPane.selected", Style.COLOR_SURFACE);
    UIManager.put("TabbedPane.contentAreaColor", Style.COLOR_BG);
    UIManager.put("TabbedPane.borderColor", Style.COLOR_BG);
    UIManager.put("TabbedPane.tabAreaBackground", Style.COLOR_BG);

    // Labels & Buttons
    UIManager.put("Label.foreground", Style.COLOR_TEXT);
    UIManager.put("Button.background", Style.COLOR_SURFACE);
    UIManager.put("Button.foreground", Style.COLOR_TEXT);
    UIManager.put("Button.select", Style.COLOR_SELECT);
    UIManager.put("ToggleButton.background", Style.COLOR_SURFACE);
    UIManager.put("ToggleButton.foreground", Style.COLOR_TEXT);
    UIManager.put("ToggleButton.select", Style.COLOR_SELECT);
    UIManager.put("CheckBox.background", Style.COLOR_BG);
    UIManager.put("CheckBox.foreground", Style.COLOR_TEXT);
    UIManager.put("RadioButton.background", Style.COLOR_BG);
    UIManager.put("RadioButton.foreground", Style.COLOR_TEXT);

    // Menus
    applyMenuOverrides(
        Style.COLOR_SURFACE,
        Style.COLOR_SELECT,
        Style.COLOR_TEXT,
        Style.COLOR_DISABLED_TEXT,
        Style.COLOR_BORDER);
    UIManager.put("Windows.TitlePane.background", Style.COLOR_BG);
    UIManager.put("Windows.TitlePane.inactiveBackground", Style.COLOR_BG);
    UIManager.put("Windows.TitlePane.foreground", Style.COLOR_TEXT);
    UIManager.put("Windows.TitlePane.inactiveForeground", Style.COLOR_SUBTEXT);
    UIManager.put("Windows.TitlePane.borderColor", Style.COLOR_BG);
    // ScrollBars - thinner, cleaner
    UIManager.put("ScrollBar.background", Style.COLOR_BG);
    UIManager.put("ScrollBar.foreground", Style.COLOR_BORDER);
    UIManager.put("ScrollBar.track", Style.COLOR_BG);
    UIManager.put("ScrollBar.thumb", Style.COLOR_SCROLLBAR_THUMB);
    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollPane.background", Style.COLOR_BG);
    UIManager.put("Viewport.background", Style.COLOR_BG);

    // Misc
    UIManager.put("Separator.foreground", Style.COLOR_BORDER);
    UIManager.put("Spinner.background", INPUT_BG);
    UIManager.put("Spinner.foreground", Style.COLOR_TEXT);
    UIManager.put("Slider.background", Style.COLOR_BG);
    UIManager.put("Slider.foreground", Style.COLOR_ACCENT_BLUE);
    UIManager.put("ProgressBar.background", Style.COLOR_SURFACE);
    UIManager.put("ProgressBar.foreground", Style.COLOR_ACCENT_BLUE);
    UIManager.put("ToolTip.background", Style.COLOR_SURFACE);
    UIManager.put("ToolTip.foreground", Style.COLOR_TEXT);
    UIManager.put("OptionPane.background", Style.COLOR_BG);
    UIManager.put("OptionPane.foreground", Style.COLOR_TEXT);
    UIManager.put("InternalFrame.background", Style.COLOR_BG);
    UIManager.put("Desktop.background", Style.COLOR_BG);

    // SplitPane - cleaner dividers
    UIManager.put("SplitPane.background", Style.COLOR_BG);
    UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
    UIManager.put("SplitPane.dividerSize", SPLITTER_SIZE);
    UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
    UIManager.put("SplitPaneDivider.background", Style.COLOR_BG);
    UIManager.put("SplitPaneDivider.foreground", Style.COLOR_BG);
    UIManager.put("SplitPaneDivider.draggingColor", Style.COLOR_BG);
    UIManager.put("SplitPane.continuousLayout", true);
  }

  private static void applyLightOverrides() {
    applyCompactMetrics();
    UIManager.put("Button.arc", Style.CORNER_RADIUS);
    UIManager.put("Component.arc", Style.CORNER_RADIUS);
    UIManager.put("TextComponent.arc", Style.CORNER_RADIUS);
    UIManager.put("TabbedPane.arc", Style.CORNER_RADIUS);
    Color panel = UIManager.getColor("Panel.background");
    Color control = UIManager.getColor("TextField.background");
    Color separator = UIManager.getColor("Separator.foreground");
    Color text = UIManager.getColor("Label.foreground");
    UIManager.put("Editor.surface", control != null ? control : new Color(248, 248, 248));
    UIManager.put("Editor.surfaceRaised", panel != null ? panel.brighter() : Color.WHITE);
    UIManager.put("Editor.border", separator != null ? separator : new Color(205, 205, 205));
    UIManager.put("Editor.mutedText", text != null ? new Color(text.getRed(), text.getGreen(), text.getBlue(), 170) : Color.GRAY);
    UIManager.put("Editor.accent", new Color(53, 116, 242));
    UIManager.put("Editor.hover", new Color(53, 116, 242, 24));
    UIManager.put("Editor.selection", new Color(53, 116, 242, 40));
    UIManager.put("Editor.workspaceTop", new Color(226, 232, 239));
    UIManager.put("Editor.workspaceBottom", new Color(205, 214, 224));
    UIManager.put("Editor.assetExplorerBackground", Style.COLOR_ASSET_EXPLORER_LIGHT);
    UIManager.put("TextPane.background", Style.COLOR_ASSET_EXPLORER_LIGHT);
    UIManager.put("Editor.mapBacking", Color.WHITE);
    UIManager.put("Editor.mapBorder", new Color(105, 120, 136, 180));
    UIManager.put("Table.gridColor", Style.COLOR_LIGHT_GRID);
    applyMenuOverrides(
        panel != null ? panel : new Color(248, 248, 248),
        new Color(53, 116, 242, 48),
        text != null ? text : Color.DARK_GRAY,
        new Color(120, 120, 120),
        separator != null ? separator : new Color(205, 205, 205));
  }

  private static void applyMenuOverrides(
      Color background, Color selection, Color text, Color disabledText, Color border) {
    UIManager.put("MenuBar.background", Style.background());
    UIManager.put("MenuBar.foreground", text);
    UIManager.put("MenuBar.borderColor", Style.background());
    UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder());
    for (String prefix : List.of("Menu", "MenuItem", "CheckBoxMenuItem", "RadioButtonMenuItem")) {
      UIManager.put(prefix + ".background", background);
      UIManager.put(prefix + ".foreground", text);
      UIManager.put(prefix + ".disabledForeground", disabledText);
      UIManager.put(prefix + ".selectionBackground", selection);
      UIManager.put(prefix + ".selectionForeground", text);
      UIManager.put(prefix + ".acceleratorForeground", Style.mutedText());
      UIManager.put(prefix + ".acceleratorSelectionForeground", text);
    }
    UIManager.put("MenuItem.arc", Style.CORNER_RADIUS * 2);
    UIManager.put("Menu.arc", Style.CORNER_RADIUS * 2);
    UIManager.put("Menu.minPrefHeight", Style.CONTROL_HEIGHT);
    UIManager.put("MenuItem.minPrefHeight", Style.CONTROL_HEIGHT);
    UIManager.put("Menu.gap", Style.SPACE_MEDIUM);
    UIManager.put("PopupMenu.background", background);
    UIManager.put("PopupMenu.foreground", text);
    UIManager.put("PopupMenu.translucentBackground", background);
    UIManager.put("PopupMenu.borderColor", border);
    UIManager.put("PopupMenu.borderThickness", 1);
    UIManager.put("PopupMenu.borderRadius", Style.CORNER_RADIUS * 2);
    UIManager.put("PopupMenuSeparator.foreground", border);
    UIManager.put("PopupMenuSeparator.background", background);
  }

  private static void applyCompactMetrics() {
    UIManager.put("Component.minimumHeight", Style.CONTROL_HEIGHT);
    UIManager.put("Button.minimumHeight", Style.CONTROL_HEIGHT);
    UIManager.put("ComboBox.minimumHeight", Style.CONTROL_HEIGHT);
    UIManager.put("Spinner.minimumHeight", Style.CONTROL_HEIGHT);
    UIManager.put("TextField.minimumHeight", Style.CONTROL_HEIGHT);
    UIManager.put("Tree.rowHeight", Style.TREE_ROW_HEIGHT);
    UIManager.put("Table.rowHeight", 24);
    UIManager.put("TabbedPane.tabHeight", Style.CONTROL_HEIGHT);
    UIManager.put("ScrollBar.width", 10);
  }

  private static void clearThemeOverrides() {
    for (Object key : themeOverrideKeys) {
      UIManager.put(key, null);
    }
    themeOverrideKeys.clear();
  }

  private static void applyAndTrackThemeOverrides(Runnable overrides) {
    Map<Object, Object> before = new HashMap<>();
    for (Object key : UIManager.getDefaults().keySet()) {
      before.put(key, UIManager.get(key));
    }
    overrides.run();
    Set<Object> keys = new HashSet<>(before.keySet());
    keys.addAll(UIManager.getDefaults().keySet());
    for (Object key : keys) {
      if (!java.util.Objects.equals(before.get(key), UIManager.get(key))) {
        themeOverrideKeys.add(key);
      }
    }
  }

  private static int inspectorMinimumWidth() {
    return Math.max(320, Math.round(INSPECTOR_BASE_WIDTH * Editor.preferences().getUiScale()));
  }

  static int initialInspectorDivider(
      int windowWidth, int hierarchyWidth, int inspectorWidth, int preferredInspectorWidth, int persistedDivider) {
    int maximumDivider = Math.max(0, windowWidth - inspectorWidth);
    if (persistedDivider > 0) {
      return Math.min(persistedDivider + hierarchyWidth + SPLITTER_SIZE, maximumDivider);
    }
    return Math.max(0, windowWidth - preferredInspectorWidth);
  }

  private static void updateOrphanComponents() {
    for (JComponent component : orphanComponents) {
      SwingUtilities.updateComponentTreeUI(component);
    }
  }

  private static void setDefaultSwingFont(Font font) {
    Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();

      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put(key, new FontUIResource(font));
      }
    }
  }
}
