package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentListener;
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
import de.gurkenlabs.utiliti.controller.tool.AssetTransferable;
import de.gurkenlabs.utiliti.controller.tool.BucketFillTool;
import de.gurkenlabs.utiliti.controller.tool.EraserTool;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.StampBrushTool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Style.Theme;
import de.gurkenlabs.utiliti.view.menus.CanvasPopupMenu;
import de.gurkenlabs.utiliti.view.menus.MainMenuBar;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public final class UI {
  private static final int INSPECTOR_MIN_WIDTH = 420;

  private static final List<JComponent> orphanComponents = new CopyOnWriteArrayList<>();
  private static JPopupMenu canvasPopup;
  private static AssetList assetComponent;

  private static MapObjectInspector mapObjectPanel;
  private static MapPropertyPanel mapPropertyPanel;
  private static JPanel inspectorHost;
  private static CardLayout inspectorCards;
  private static MapList mapSelectionPanel;
  private static LayerList mapLayerList;
  private static EntityList entityList;

  private static boolean initialized;

  private static volatile boolean loadingTheme;

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

    Game.screens().display(Editor.instance());

    javax.swing.JComponent.setDefaultLocale(Locale.getDefault());
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    setDefaultSwingFont(Style.getDefaultFont());

    Tray.init();
    Game.window().cursor().set(Cursors.DEFAULT, 0, 0);
    Game.window().cursor().setOffsetX(0);
    Game.window().cursor().setOffsetY(0);
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
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "objects");
    }
  }

  public static void showMapProperties() {
    if (mapPropertyPanel == null) {
      return;
    }

    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      mapPropertyPanel.bind(Game.world().environment().getMap());
    }
    if (inspectorCards != null && inspectorHost != null) {
      inspectorCards.show(inspectorHost, "map");
    }
  }

  public static LayerController getLayerController() {
    return mapLayerList;
  }

  public static EntityController getEntityController() {
    return entityList;
  }

  public static Controller getAssetController() {
    return assetComponent;
  }

  public static JPopupMenu getCanvasPopup() {
    return canvasPopup;
  }

  public static MapController getMapController() {
    return mapSelectionPanel;
  }

  private static void initScrollBars(JPanel renderPane) {
    ScrollHandlerBar horizontalScroll = new ScrollHandlerBar(java.awt.Adjustable.HORIZONTAL);
    ScrollHandlerBar verticalScroll = new ScrollHandlerBar(java.awt.Adjustable.VERTICAL);

    EnvironmentListener environmentListener = new EnvironmentListener() {
      @Override public void loaded(Environment environment) {
        boolean hasMap = environment != null && environment.getMap() != null;
        horizontalScroll.setVisible(hasMap);
        verticalScroll.setVisible(hasMap);
      }

      @Override public void unloaded(Environment environment) {
        // prevent this event handler from blocking the UI thread while the game is shutting down
        if (!Game.hasStarted()) {
          return;
        }

        horizontalScroll.setVisible(false);
        verticalScroll.setVisible(false);
      }
    };
    environmentListener.loaded(Game.world().environment());
    Game.world().addListener(environmentListener);
    renderPane.add(horizontalScroll, BorderLayout.SOUTH);
    renderPane.add(verticalScroll, BorderLayout.EAST);

    Scroll.init(verticalScroll, horizontalScroll);
  }

  private static void setupInterface() {
    JFrame window = initWindow();
    int winW = window.getSize().width;
    int winH = window.getSize().height;

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (winW * 0.75), winH);

    window.remove(canvas);
    JPanel renderPanel;
    renderPanel = new JPanel(new BorderLayout());
    renderPanel.add(canvas);
    renderPanel.setMinimumSize(new Dimension(250, 100));
    initScrollBars(renderPanel);

    initTools();
    renderPanel.add(new ToolBar(), BorderLayout.NORTH);
    initDropTarget(renderPanel);

    Component leftPanel = initLeftPanel();
    Component renderSplitPanel = initRenderSplitPanel(renderPanel, winH);

    mapObjectPanel = new MapObjectInspector();
    mapObjectPanel.setMinimumSize(new Dimension(INSPECTOR_MIN_WIDTH, 0));
    mapPropertyPanel = new MapPropertyPanel();
    mapPropertyPanel.setMinimumSize(new Dimension(INSPECTOR_MIN_WIDTH, 0));
    inspectorCards = new CardLayout();
    inspectorHost = new JPanel(inspectorCards);
    inspectorHost.add(mapObjectPanel, "objects");
    inspectorHost.add(mapPropertyPanel, "map");
    inspectorHost.setMinimumSize(new Dimension(INSPECTOR_MIN_WIDTH, 0));

    int prefInspectorW = Math.max(INSPECTOR_MIN_WIDTH, (int) (winW * 0.20));
    JSplitPane centerRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, renderSplitPanel, inspectorHost);
    centerRightSplit.setContinuousLayout(true);
    centerRightSplit.setResizeWeight(1.0);
    centerRightSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
        evt -> Editor.preferences().setSelectionEditSplitter(centerRightSplit.getDividerLocation()));
    if (Editor.preferences().getSelectionEditSplitter() != 0) {
      centerRightSplit.setDividerLocation(
          Math.max(0, Math.min(Editor.preferences().getSelectionEditSplitter(), winW - INSPECTOR_MIN_WIDTH)));
    } else {
      centerRightSplit.setDividerLocation(winW - prefInspectorW);
    }

    int prefHierarchyW = Math.max(250, (int) (winW * 0.18));
    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerRightSplit);
    mainSplit.setContinuousLayout(true);
    mainSplit.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        Editor.preferences().setWidth(window.getWidth());
        Editor.preferences().setHeight(window.getHeight());
      }
    });
    mainSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
        evt -> Editor.preferences().setMainSplitter(mainSplit.getDividerLocation()));

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);
    rootPanel.add(mainSplit, BorderLayout.CENTER);
    rootPanel.add(StatusBar.create(), BorderLayout.SOUTH);
    mainSplit.setDividerLocation(
        Editor.preferences().getMainSplitterPosition() != 0
            ? Editor.preferences().getMainSplitterPosition()
            : prefHierarchyW);

    initPopupMenu(canvas);
    window.setJMenuBar(new MainMenuBar());
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
    if (Editor.preferences().getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(Editor.preferences().getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (winH * 0.72));
    }
    renderSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
        evt -> Editor.preferences().setBottomSplitter(renderSplitPanel.getDividerLocation()));
    renderSplitPanel.setContinuousLayout(true);
    return renderSplitPanel;
  }

  private static Component initLeftPanel() {
    mapSelectionPanel = new MapList();
    mapLayerList = new LayerList();
    entityList = new EntityList();

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setFont(Style.getHeaderFont());
    tabPane.add(entityList);
    tabPane.add(mapLayerList);
    tabPane.setMinimumSize(new Dimension(0, 120));

    JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    leftSplit.setContinuousLayout(true);
    leftSplit.setTopComponent(mapSelectionPanel);
    leftSplit.setBottomComponent(tabPane);
    leftSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
        evt -> Editor.preferences().setMapPanelSplitter(leftSplit.getDividerLocation()));
    if (Editor.preferences().getMapPanelSplitter() != 0) {
      leftSplit.setDividerLocation(Editor.preferences().getMapPanelSplitter());
    } else {
      leftSplit.setDividerLocation(120);
    }
    leftSplit.setMinimumSize(new Dimension(220, 0));

    return leftSplit;
  }

  private static void initTools() {
    ToolManager tm = ToolManager.instance();
    tm.register(new PointerTool());
    tm.register(new StampBrushTool());
    tm.register(new EraserTool());
    tm.register(new BucketFillTool());
  }

  private static void initDropTarget(JPanel renderPanel) {
    Canvas canvas = Game.window().getRenderComponent();
    renderPanel.setTransferHandler(new TransferHandler() {
      @Override public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(AssetTransferable.ASSET_FLAVOR);
      }
      @Override public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
          return false;
        }
        try {
          Object asset = support.getTransferable().getTransferData(AssetTransferable.ASSET_FLAVOR);
          java.awt.Point dropPoint = support.getDropLocation().getDropPoint();
          java.awt.Point canvasPoint = SwingUtilities.convertPoint(renderPanel, dropPoint, canvas);
          Editor.instance().getMapComponent().addMapObjectAt(asset, canvasPoint);
          return true;
        } catch (Exception ex) {
          return false;
        }
      }
    });
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();
    bottomTab.setFont(Style.getHeaderFont());

    assetComponent = new AssetList();
    bottomTab.addTab(Resources.strings().get("assettree_assets"), Icons.ASSET_32, assetComponent);
    bottomTab.addTab(Resources.strings().get("assettree_console"), Icons.CONSOLE_32, new ConsoleComponent());

    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    return bottomPanel;
  }

  private static void initPopupMenu(Canvas canvas) {
    canvasPopup = new CanvasPopupMenu();
    addOrphanComponent(canvasPopup);

    canvas.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          Editor.instance().getMapComponent().setTransformMode(TransformMode.NONE);
          canvasPopup.show(canvas, e.getX(), e.getY());
        }
      }

      @Override public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          Editor.instance().getMapComponent().setTransformMode(TransformMode.NONE);
          canvasPopup.show(canvas, e.getX(), e.getY());
        }
      }
    });
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

    switch (theme) {
      case DARK -> {
        LafManager.install(new OneDarkTheme());
        applyTokyoNightOverrides();
      }
      case LIGHT -> {
        LafManager.install(new IntelliJTheme());
        applyLightOverrides();
      }
    }

    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setBackground(UIManager.getColor("Panel.background"));
    }
    Editor.preferences().setTheme(theme);
    updateOrphanComponents();
    loadingTheme = false;
  }

  private static void applyTokyoNightOverrides() {
    // Rounded corners for modern look
    UIManager.put("Button.arc", 6);
    UIManager.put("Component.arc", 6);
    UIManager.put("TextComponent.arc", 5);
    UIManager.put("TabbedPane.arc", 5);

    // Panels - borderless design with subtle contrast
    UIManager.put("Panel.background", Style.COLOR_BG);
    UIManager.put("Panel.foreground", Style.COLOR_TEXT);
    Color INPUT_BG = new Color(48, 49, 55);
    UIManager.put("TextField.background", INPUT_BG);
    UIManager.put("TextField.foreground", Style.COLOR_TEXT);
    UIManager.put("TextField.caretForeground", Style.COLOR_ACCENT_BLUE);
    UIManager.put("TextArea.background", INPUT_BG);
    UIManager.put("TextArea.foreground", Style.COLOR_TEXT);
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
    UIManager.put("Tree.selectionBackground", Style.COLOR_SELECTION_INACTIVE);
    UIManager.put("Tree.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("Tree.textBackground", Style.COLOR_BG);
    UIManager.put("Tree.textForeground", Style.COLOR_TEXT);

    // TabbedPane - modern minimal headers
    UIManager.put("TabbedPane.background", Style.COLOR_BG);
    UIManager.put("TabbedPane.foreground", Style.COLOR_SUBTEXT);
    UIManager.put("TabbedPane.selected", Style.COLOR_SURFACE);
    UIManager.put("TabbedPane.contentAreaColor", Style.COLOR_BORDER);
    UIManager.put("TabbedPane.tabAreaBackground", Style.COLOR_BG);

    // Labels & Buttons
    UIManager.put("Label.foreground", Style.COLOR_TEXT);
    UIManager.put("Button.background", Style.COLOR_SURFACE);
    UIManager.put("Button.foreground", Style.COLOR_TEXT);
    UIManager.put("Button.select", new Color(59, 66, 97));
    UIManager.put("ToggleButton.background", Style.COLOR_SURFACE);
    UIManager.put("ToggleButton.foreground", Style.COLOR_TEXT);
    UIManager.put("ToggleButton.select", new Color(59, 66, 97));
    UIManager.put("CheckBox.background", Style.COLOR_BG);
    UIManager.put("CheckBox.foreground", Style.COLOR_TEXT);
    UIManager.put("RadioButton.background", Style.COLOR_BG);
    UIManager.put("RadioButton.foreground", Style.COLOR_TEXT);

    // Menus
    UIManager.put("Menu.background", Style.COLOR_BG);
    UIManager.put("Menu.foreground", Style.COLOR_TEXT);
    UIManager.put("Menu.selectionBackground", new Color(59, 66, 97));
    UIManager.put("Menu.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("MenuItem.background", Style.COLOR_BG);
    UIManager.put("MenuItem.foreground", Style.COLOR_TEXT);
    UIManager.put("MenuItem.selectionBackground", new Color(59, 66, 97));
    UIManager.put("MenuItem.selectionForeground", Style.COLOR_TEXT);
    UIManager.put("MenuItem.disabledForeground", new Color(98, 104, 128));
    UIManager.put("Menu.disabledForeground", new Color(98, 104, 128));
    UIManager.put("PopupMenu.background", Style.COLOR_SURFACE);
    UIManager.put("PopupMenu.foreground", Style.COLOR_TEXT);

    // ScrollBars - thinner, cleaner
    UIManager.put("ScrollBar.background", Style.COLOR_BG);
    UIManager.put("ScrollBar.foreground", Style.COLOR_BORDER);
    UIManager.put("ScrollBar.track", Style.COLOR_BG);
    UIManager.put("ScrollBar.thumb", new Color(65, 65, 75));
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
    UIManager.put("SplitPane.continuousLayout", true);
  }

  private static void applyLightOverrides() {
    // Light theme keeps the IntelliJ defaults, just ensure consistency
    UIManager.put("Table.gridColor", new Color(220, 220, 220));
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
