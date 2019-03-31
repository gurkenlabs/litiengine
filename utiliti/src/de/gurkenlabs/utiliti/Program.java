package de.gurkenlabs.utiliti;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.DefaultUncaughtExceptionHandler;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.Strings;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.UriUtilities;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.AssetPanel;
import de.gurkenlabs.utiliti.swing.AssetTree;
import de.gurkenlabs.utiliti.swing.ConsoleLogHandler;
import de.gurkenlabs.utiliti.swing.FileDrop;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.MapSelectionPanel;
import de.gurkenlabs.utiliti.swing.dialogs.GridEditPanel;
import de.gurkenlabs.utiliti.swing.dialogs.MapPropertyPanel;
import de.gurkenlabs.utiliti.swing.panels.MapObjectPanel;

public class Program {
  public static final Font TEXT_FONT = new JLabel().getFont().deriveFont(10f);

  private static final Logger log = Logger.getLogger(Program.class.getName());

  private static UserPreferenceConfiguration userPreferences;
  private static JScrollBar horizontalScroll;
  private static JScrollBar verticalScroll;
  private static TrayIcon trayIcon;

  private static Menu recentFiles;
  private static AssetPanel assetPanel;
  private static AssetTree assetTree;
  private static JPopupMenu canvasPopup;
  private static JPopupMenu addPopupMenu;
  private static JLabel statusBar;
  private static JTextField colorText;
  private static boolean isChanging;

  public static void main(String[] args) {

    Style.initSwingComponentStyle();

    Game.info().setName("utiLITI");
    Game.info().setSubTitle("LITIengine Creation Kit");
    Game.info().setVersion("v0.4.16-alpha");
    Resources.strings().setEncoding(Strings.ENCODING_UTF_8);

    initSystemTray();

    Game.config().getConfigurationGroups().add(new UserPreferenceConfiguration());
    Game.init(args);

    // the editor should never crash, even if an exception occurs
    Game.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(false));

    forceBasicEditorConfiguration();

    JOptionPane.setDefaultLocale(Locale.getDefault());

    userPreferences = Game.config().getConfigurationGroup("user_", UserPreferenceConfiguration.class);

    Game.world().camera().onZoomChanged(zoom -> userPreferences.setZoom((float) zoom));

    Game.screens().display(EditorScreen.instance());

    Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
    Game.window().getRenderComponent().setCursorOffsetX(0);
    Game.window().getRenderComponent().setCursorOffsetY(0);
    setupInterface();
    Game.start();
    Input.mouse().setGrabMouse(false);
    Input.keyboard().consumeAlt(true);
    handleArgs(args);

    if (!EditorScreen.instance().fileLoaded() && userPreferences.getLastGameFile() != null) {
      EditorScreen.instance().load(new File(userPreferences.getLastGameFile()));
    }
  }

  private static void forceBasicEditorConfiguration() {
    // force configuration elements that are crucial for the editor
    Game.graphics().setBaseRenderScale(1.0f);
    Game.config().debug().setDebugEnabled(true);
    Game.config().graphics().setGraphicQuality(Quality.VERYHIGH);
    Game.config().graphics().setReduceFramesWhenNotFocused(false);
  }

  public static void loadRecentFiles() {
    recentFiles.removeAll();
    for (String recent : userPreferences.getLastOpenedFiles()) {
      if (recent != null && !recent.isEmpty() && new File(recent).exists()) {
        MenuItem fileButton = new MenuItem(recent);
        fileButton.addActionListener(a -> {
          log.log(Level.INFO, "load " + fileButton.getLabel());
          EditorScreen.instance().load(new File(fileButton.getLabel()));
        });

        recentFiles.add(fileButton);
      }
    }
  }

  public static AssetPanel getAssetPanel() {
    return assetPanel;
  }

  public static AssetTree getAssetTree() {
    return assetTree;
  }

  public static JScrollBar getHorizontalScrollBar() {
    return horizontalScroll;
  }

  public static JScrollBar getVerticalcrollBar() {
    return verticalScroll;
  }

  public static void setTrayToolTip(String tooltipText) {
    if (!SystemTray.isSupported() || trayIcon == null) {
      return;
    }

    trayIcon.setToolTip(tooltipText);
  }

  public static UserPreferenceConfiguration getUserPreferences() {
    return userPreferences;
  }

  public static void setStatus(String status) {
    statusBar.setText(status);
  }

  public static void updateScrollBars() {
    horizontalScroll.setMinimum(0);
    horizontalScroll.setMaximum(Game.world().environment().getMap().getSizeInPixels().width);
    verticalScroll.setMinimum(0);
    verticalScroll.setMaximum(Game.world().environment().getMap().getSizeInPixels().height);
    horizontalScroll.setValue((int) Game.world().camera().getViewport().getCenterX());
    verticalScroll.setValue((int) Game.world().camera().getViewport().getCenterY());
  }

  public static boolean notifyPendingChanges() {
    String resourceFile = EditorScreen.instance().getCurrentResourceFile() != null ? EditorScreen.instance().getCurrentResourceFile() : "";
    if (EditorScreen.instance().getChangedMaps().isEmpty()) {
      return true;
    }

    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_saveProjectMessage") + "\n" + resourceFile, Resources.strings().get("hud_saveProject"), JOptionPane.YES_NO_CANCEL_OPTION);

    if (n == JOptionPane.YES_OPTION) {
      EditorScreen.instance().save(false);
    }

    return n != JOptionPane.CANCEL_OPTION && n != JOptionPane.CLOSED_OPTION;
  }

  private static void handleArgs(String[] args) {
    if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
      return;
    }

    // handle file loading
    try {
      Paths.get(args[0]);
    } catch (InvalidPathException e) {
      return;
    }

    File f = new File(args[0]);
    EditorScreen.instance().load(f);
  }

  private static void setupInterface() {
    JFrame window = initWindow();

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (window.getSize().width * 0.75), window.getSize().height);

    // remove canvas because we want to add a wrapping panel
    window.remove(canvas);

    initPopupMenus(canvas);

    JPanel renderPanel = new JPanel(new BorderLayout());
    renderPanel.add(canvas);
    renderPanel.setMinimumSize(new Dimension(300, 0));
    initScrollBars(renderPanel);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initRenderSplitPanel(renderPanel, window), initRightSplitPanel());
    split.setContinuousLayout(true);
    split.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        userPreferences.setWidth(window.getWidth());
        userPreferences.setHeight(window.getHeight());
      }
    });

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setMainSplitter(split.getDividerLocation()));

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);

    rootPanel.add(split, BorderLayout.CENTER);
    split.setDividerLocation(userPreferences.getMainSplitterPosition() != 0 ? userPreferences.getMainSplitterPosition() : (int) (window.getSize().width * 0.75));

    JToolBar toolbar = initToolBar();
    rootPanel.add(toolbar, BorderLayout.NORTH);

    window.setMenuBar(initMenuBar());
  }

  private static JFrame initWindow() {
    JFrame window = ((JFrame) Game.window().getHostControl());
    window.setResizable(true);

    Game.addGameListener(new GameListener() {
      @Override
      public boolean terminating() {
        boolean terminate = notifyPendingChanges();
        if (terminate) {
          getUserPreferences().setFrameState(window.getExtendedState());
        }

        return terminate;
      }
    });

    window.setLocationRelativeTo(null);
    if (userPreferences.getFrameState() != JFrame.ICONIFIED && userPreferences.getFrameState() != JFrame.NORMAL) {
      window.setExtendedState(userPreferences.getFrameState());
    } else if (userPreferences.getWidth() != 0 && userPreferences.getHeight() != 0) {
      window.setSize(userPreferences.getWidth(), userPreferences.getHeight());
    }

    return window;
  }

  private static MenuBar initMenuBar() {
    MenuBar menuBar = new MenuBar();
    Menu mnFile = initFileMenu();
    menuBar.add(mnFile);

    Menu mnView = initViewMenu();
    menuBar.add(mnView);

    Menu mnProject = initProjectMenu();
    menuBar.add(mnProject);

    Menu mnMap = initMapMenu();
    menuBar.add(mnMap);

    Menu mnHelp = initHelpMenu();
    menuBar.add(mnHelp);

    return menuBar;
  }

  private static Component initRenderSplitPanel(JPanel renderPanel, JFrame window) {
    JSplitPane renderSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, initBottomPanel());
    if (userPreferences.getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(userPreferences.getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (window.getSize().height * 0.75));
    }

    renderSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setBottomSplitter(renderSplitPanel.getDividerLocation()));
    renderSplitPanel.setContinuousLayout(true);
    return renderSplitPanel;
  }

  private static Component initRightSplitPanel() {
    final MapObjectPanel mapEditorPanel = new MapObjectPanel();
    final MapSelectionPanel mapSelectionPanel = new MapSelectionPanel();
    EditorScreen.instance().setMapEditorPanel(mapEditorPanel);
    EditorScreen.instance().setMapSelectionPanel(mapSelectionPanel);

    JSplitPane rightSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    rightSplitPanel.setMinimumSize(new Dimension(300, 0));
    rightSplitPanel.setBottomComponent(mapEditorPanel);
    rightSplitPanel.setTopComponent(mapSelectionPanel);
    rightSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setSelectionEditSplitter(rightSplitPanel.getDividerLocation()));
    if (userPreferences.getSelectionEditSplitter() != 0) {
      rightSplitPanel.setDividerLocation(userPreferences.getSelectionEditSplitter());
    }

    return rightSplitPanel;
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();

    bottomTab.addTab(Resources.strings().get("assettree_assets"), initAssetsComponent());
    bottomTab.addTab("Console", initConsole());
    bottomTab.setIconAt(0, Icons.ASSET);
    bottomTab.setIconAt(1, Icons.CONSOLE);

    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    statusBar = new JLabel("");
    statusBar.setPreferredSize(new Dimension(0, 16));
    statusBar.setFont(new Font(ConsoleLogHandler.CONSOLE_FONT, Font.PLAIN, 11));
    statusBar.setBorder(new EmptyBorder(0, 5, 0, 0));

    bottomPanel.add(statusBar, BorderLayout.SOUTH);
    return bottomPanel;
  }

  public static void updateStatusBar() {
    Point tile = Input.mouse().getTile();
    String positionX = "x: " + (int) Input.mouse().getMapLocation().getX() + "[" + tile.x + "]";
    String positionY = "y: " + (int) Input.mouse().getMapLocation().getY() + "[" + tile.y + "]";
    String status = String.format("%-14s %-14s", positionX, positionY) + String.format(" %-10s", (int) (Game.world().camera().getRenderScale() * 100) + "%");

    int size = EditorScreen.instance().getMapComponent().getSelectedMapObjects().size();
    if (size <= 0) {
      statusBar.setText("");
    } else {

      status += Resources.strings().get("status_selected_objects", size);
    }

    statusBar.setText(status);
  }

  private static void initScrollBars(JPanel renderPane) {
    horizontalScroll = new JScrollBar(JScrollBar.HORIZONTAL);
    renderPane.add(horizontalScroll, BorderLayout.SOUTH);
    verticalScroll = new JScrollBar(JScrollBar.VERTICAL);
    renderPane.add(verticalScroll, BorderLayout.EAST);

    horizontalScroll.addAdjustmentListener(e -> {
      if (EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }

      Point2D newFocus = new Point2D.Double(horizontalScroll.getValue(), Game.world().camera().getFocus().getY());
      Game.world().camera().setFocus(newFocus);
    });

    verticalScroll.addAdjustmentListener(e -> {
      if (EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }
      Point2D newFocus = new Point2D.Double(Game.world().camera().getFocus().getX(), verticalScroll.getValue());
      Game.world().camera().setFocus(newFocus);
    });
  }

  private static Menu initFileMenu() {
    Menu mnFile = new Menu(Resources.strings().get("menu_file"));

    MenuItem create = new MenuItem(Resources.strings().get("menu_createProject"));
    create.setShortcut(new MenuShortcut(KeyEvent.VK_N));
    create.addActionListener(a -> EditorScreen.instance().create());

    MenuItem load = new MenuItem(Resources.strings().get("menu_loadProject"));
    load.setShortcut(new MenuShortcut(KeyEvent.VK_O));
    load.addActionListener(a -> EditorScreen.instance().load());

    MenuItem save = new MenuItem(Resources.strings().get("menu_save"));
    save.setShortcut(new MenuShortcut(KeyEvent.VK_S));
    save.addActionListener(a -> EditorScreen.instance().save(false));

    MenuItem saveAs = new MenuItem(Resources.strings().get("menu_saveAs"));
    saveAs.addActionListener(a -> EditorScreen.instance().save(true));

    MenuItem exit = new MenuItem(Resources.strings().get("menu_exit"));
    exit.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
    exit.addActionListener(a -> System.exit(0));

    mnFile.add(load);
    mnFile.add(create);
    mnFile.add(save);
    mnFile.add(saveAs);
    mnFile.addSeparator();
    recentFiles = new Menu(Resources.strings().get("menu_recentFiles"));
    loadRecentFiles();
    mnFile.add(recentFiles);
    mnFile.addSeparator();
    mnFile.add(exit);
    return mnFile;
  }

  private static Menu initViewMenu() {
    Menu mnView = new Menu(Resources.strings().get("menu_view"));

    CheckboxMenuItem snapToPixels = new CheckboxMenuItem(Resources.strings().get("menu_snapPixels"));
    snapToPixels.setState(userPreferences.isSnapPixels());
    snapToPixels.addItemListener(e -> {
      userPreferences.setSnapPixels(snapToPixels.getState());
      EditorScreen.instance().getMapObjectPanel().updateSpinnerModels();
      EditorScreen.instance().getMapObjectPanel().bind(EditorScreen.instance().getMapComponent().getFocusedMapObject());
    });

    CheckboxMenuItem snapToGrid = new CheckboxMenuItem(Resources.strings().get("menu_snapGrid"));
    snapToGrid.setState(userPreferences.isSnapGrid());
    snapToGrid.addItemListener(e -> userPreferences.setSnapGrid(snapToGrid.getState()));

    CheckboxMenuItem renderGrid = new CheckboxMenuItem(Resources.strings().get("menu_renderGrid"));
    renderGrid.setState(userPreferences.isShowGrid());
    renderGrid.setShortcut(new MenuShortcut(KeyEvent.VK_G));
    renderGrid.addItemListener(e -> userPreferences.setShowGrid(renderGrid.getState()));

    CheckboxMenuItem renderCollision = new CheckboxMenuItem(Resources.strings().get("menu_renderCollisionBoxes"));
    renderCollision.setState(userPreferences.isRenderBoundingBoxes());
    renderCollision.setShortcut(new MenuShortcut(KeyEvent.VK_H));
    renderCollision.addItemListener(e -> userPreferences.setRenderBoundingBoxes(renderCollision.getState()));

    CheckboxMenuItem renderCustomMapObjects = new CheckboxMenuItem(Resources.strings().get("menu_renderCustomMapObjects"));
    renderCustomMapObjects.setState(userPreferences.isRenderCustomMapObjects());
    renderCustomMapObjects.setShortcut(new MenuShortcut(KeyEvent.VK_K));
    renderCustomMapObjects.addItemListener(e -> userPreferences.setRenderCustomMapObjects(renderCustomMapObjects.getState()));

    CheckboxMenuItem renderMapIds = new CheckboxMenuItem(Resources.strings().get("menu_renderMapIds"));
    renderMapIds.setState(userPreferences.isRenderMapIds());
    renderMapIds.setShortcut(new MenuShortcut(KeyEvent.VK_I));
    renderMapIds.addItemListener(e -> userPreferences.setRenderMapIds(renderMapIds.getState()));

    MenuItem setGrid = new MenuItem(Resources.strings().get("menu_gridSettings"));
    setGrid.addActionListener(a -> {
      GridEditPanel panel = new GridEditPanel(getUserPreferences().getGridLineWidth(), getUserPreferences().getGridColor());
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_gridSettings"), JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        getUserPreferences().setGridColor(ColorHelper.encode(panel.getGridColor()));
        getUserPreferences().setGridLineWidth(panel.getStrokeWidth());
      }
    });

    MenuItem zoomIn = new MenuItem(Resources.strings().get("menu_zoomIn"));
    zoomIn.setShortcut(new MenuShortcut(KeyEvent.VK_PLUS));
    zoomIn.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomIn());

    MenuItem zoomOut = new MenuItem(Resources.strings().get("menu_zoomOut"));
    zoomOut.setShortcut(new MenuShortcut(KeyEvent.VK_MINUS));
    zoomOut.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomOut());

    mnView.add(snapToPixels);
    mnView.add(snapToGrid);
    mnView.add(renderGrid);
    mnView.add(renderCollision);
    mnView.add(renderCustomMapObjects);
    mnView.add(renderMapIds);
    mnView.add(setGrid);
    mnView.addSeparator();
    mnView.add(zoomIn);
    mnView.add(zoomOut);

    return mnView;
  }

  private static Menu initProjectMenu() {
    Menu mnProject = new Menu(Resources.strings().get("menu_project"));

    CheckboxMenuItem compress = new CheckboxMenuItem(Resources.strings().get("menu_compressProjectFile"));
    compress.setState(userPreferences.isCompressFile());
    compress.addItemListener(e -> userPreferences.setCompressFile(compress.getState()));

    CheckboxMenuItem sync = new CheckboxMenuItem(Resources.strings().get("menu_syncMaps"));
    sync.setState(userPreferences.isSyncMaps());
    sync.addItemListener(e -> userPreferences.setSyncMaps(sync.getState()));

    MenuItem importSpriteFile = new MenuItem(Resources.strings().get("menu_assets_importSpriteFile"));
    importSpriteFile.addActionListener(a -> EditorScreen.instance().importSpriteFile());

    MenuItem importSprite = new MenuItem(Resources.strings().get("menu_assets_importSprite"));
    importSprite.addActionListener(a -> EditorScreen.instance().importSpriteSheets());

    MenuItem importTextureAtlas = new MenuItem(Resources.strings().get("menu_assets_importTextureAtlas"));
    importTextureAtlas.addActionListener(a -> EditorScreen.instance().importTextureAtlas());

    MenuItem importEmitters = new MenuItem(Resources.strings().get("menu_assets_importEmitters"));
    importEmitters.addActionListener(a -> EditorScreen.instance().importEmitters());

    MenuItem importBlueprints = new MenuItem(Resources.strings().get("menu_assets_importBlueprints"));
    importBlueprints.addActionListener(a -> EditorScreen.instance().importBlueprints());

    MenuItem importTilesets = new MenuItem(Resources.strings().get("menu_assets_importTilesets"));
    importTilesets.addActionListener(a -> EditorScreen.instance().importTilesets());

    MenuItem importSounds = new MenuItem(Resources.strings().get("menu_assets_importSounds"));
    importSounds.addActionListener(a -> EditorScreen.instance().importSounds());

    mnProject.add(importSprite);
    mnProject.add(importTextureAtlas);
    mnProject.add(importSpriteFile);
    mnProject.add(importEmitters);
    mnProject.add(importBlueprints);
    mnProject.add(importTilesets);
    mnProject.add(importSounds);
    mnProject.addSeparator();
    mnProject.add(compress);
    mnProject.add(sync);

    return mnProject;
  }

  private static Menu initMapMenu() {
    Menu mnMap = new Menu(Resources.strings().get("menu_map"));

    MenuItem imp = new MenuItem(Resources.strings().get("menu_import"));
    imp.addActionListener(a -> EditorScreen.instance().getMapComponent().importMap());

    MenuItem exp = new MenuItem(Resources.strings().get("menu_export"));
    exp.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    MenuItem saveMapSnapshot = new MenuItem(Resources.strings().get("menu_exportMapSnapshot"));
    saveMapSnapshot.setShortcut(new MenuShortcut(KeyEvent.VK_ENTER));
    saveMapSnapshot.addActionListener(a -> EditorScreen.instance().saveMapSnapshot());

    MenuItem reassignIDs = new MenuItem(Resources.strings().get("menu_reassignMapIds"));
    reassignIDs.addActionListener(a -> {
      try {
        int minID = Integer.parseInt(JOptionPane.showInputDialog(Resources.strings().get("panel_reassignMapIds"), 1));
        EditorScreen.instance().getMapComponent().reassignIds(Game.world().environment().getMap(), minID);
      } catch (Exception e) {
        log.log(Level.SEVERE, "No parseable Integer found upon reading the min Map ID input. Try again.");
      }

    });

    MenuItem del2 = new MenuItem(Resources.strings().get("menu_removeMap"));
    del2.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());

    MenuItem mapProps = new MenuItem(Resources.strings().get("menu_properties"));
    mapProps.setShortcut(new MenuShortcut(KeyEvent.VK_M));
    mapProps.addActionListener(a -> {
      if (EditorScreen.instance().getMapComponent().getMaps() == null || EditorScreen.instance().getMapComponent().getMaps().isEmpty()) {
        return;
      }

      MapPropertyPanel panel = new MapPropertyPanel();
      panel.bind(Game.world().environment().getMap());

      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_mapProperties"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        panel.saveChanges();

        final String colorProp = Game.world().environment().getMap().getStringValue(MapProperty.AMBIENTCOLOR);
        try {
          if (colorProp != null && !colorProp.isEmpty()) {
            Color ambientColor = ColorHelper.decode(colorProp);
            Game.world().environment().getAmbientLight().setColor(ambientColor);
          }
        } catch (final NumberFormatException nfe) {
          log.log(Level.SEVERE, nfe.getLocalizedMessage(), nfe);
        }

        UndoManager.instance().recordChanges();
        EditorScreen.instance().getMapComponent().loadMaps(EditorScreen.instance().getGameFile().getMaps());
        EditorScreen.instance().getMapComponent().loadEnvironment((Map) Game.world().environment().getMap());
      }
    });

    mnMap.add(imp);
    mnMap.add(exp);
    mnMap.add(saveMapSnapshot);
    mnMap.add(reassignIDs);
    mnMap.add(del2);
    mnMap.addSeparator();
    mnMap.add(mapProps);

    return mnMap;
  }

  private static Menu initHelpMenu() {
    Menu helpMenu = new Menu("Help");

    MenuItem tutorialMenuItem = new MenuItem(Resources.strings().get("menu_help_tutorial"));
    tutorialMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_tutorials"))));

    MenuItem docsMenuItem = new MenuItem(Resources.strings().get("menu_help_docs"));
    docsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_docs"))));

    MenuItem forumMenuItem = new MenuItem(Resources.strings().get("menu_help_forum"));
    forumMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_forum"))));

    MenuItem javadocsMenuItem = new MenuItem(Resources.strings().get("menu_help_javadocs"));
    javadocsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_javadocs"))));

    MenuItem bugMenuItem = new MenuItem(Resources.strings().get("menu_help_bug"));
    bugMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_bug"))));

    MenuItem releaseMenuItem = new MenuItem(Resources.strings().get("menu_help_releasenotes"));
    releaseMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_releasenotes"))));

    MenuItem patreonMenuItem = new MenuItem(Resources.strings().get("menu_help_patreon"));
    patreonMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_patreon"))));

    MenuItem payPalMenuItem = new MenuItem(Resources.strings().get("menu_help_paypal"));
    payPalMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_paypal"))));
    
    MenuItem aboutMenuItem = new MenuItem(Resources.strings().get("menu_help_about"));
    aboutMenuItem.addActionListener(event -> JOptionPane.showMessageDialog(((JFrame) Game.window().getHostControl()),
        Resources.strings().get("menu_help_abouttext") + "\n" + Resources.strings().get("menu_help_releases") + Resources.strings().getFrom("links", "link_LITIengine_releases") + "\n\n" + Resources.strings().get("copyright_gurkenlabs", "2019") + "\n" + Resources.strings().get("copyright_LITIengine"),
        Resources.strings().get("menu_help_about") + " " + Game.info().getVersion(), JOptionPane.INFORMATION_MESSAGE));

    helpMenu.add(tutorialMenuItem);
    helpMenu.add(docsMenuItem);
    helpMenu.add(forumMenuItem);
    helpMenu.add(javadocsMenuItem);
    helpMenu.addSeparator();
    helpMenu.add(releaseMenuItem);
    helpMenu.add(bugMenuItem);
    helpMenu.addSeparator();
    helpMenu.add(patreonMenuItem);
    helpMenu.add(payPalMenuItem);
    helpMenu.addSeparator();
    helpMenu.add(aboutMenuItem);

    return helpMenu;
  }

  private static Component initAssetsComponent() {
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    assetTree = new AssetTree();
    split.setLeftComponent(assetTree);
    assetPanel = new AssetPanel();

    new FileDrop(assetPanel, files -> {
      List<File> droppedImages = new ArrayList<>();
      for (File file : files) {
        // handle dropped image
        if (ImageFormat.isSupported(file)) {
          droppedImages.add(file);
        }
      }

      if (!droppedImages.isEmpty()) {
        EditorScreen.instance().importSpriteSheets(droppedImages.toArray(new File[droppedImages.size()]));
      }
    });

    JScrollPane scrollPane = new JScrollPane(assetPanel);

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setAssetsSplitter(split.getDividerLocation()));
    split.setDividerLocation(userPreferences.getMainSplitterPosition() != 0 ? userPreferences.getAssetsSplitter() : 200);

    split.setRightComponent(scrollPane);
    return split;
  }

  private static Component initConsole() {
    Logger root = Logger.getLogger("");
    JTextPane consoleTextArea = new JTextPane();
    JScrollPane consoleScrollPane = new JScrollPane();
    consoleScrollPane.setViewportBorder(null);
    consoleScrollPane.setViewportView(consoleTextArea);

    consoleTextArea.setEditable(false);
    consoleTextArea.setBackground(Style.COLOR_ASSETPANEL_BACKGROUND);
    consoleTextArea.setForeground(Color.WHITE);
    consoleTextArea.setAutoscrolls(true);
    root.addHandler(new ConsoleLogHandler(consoleTextArea));
    return consoleScrollPane;
  }

  private static JButton initButton(Icon icon, ActionListener listener) {
    JButton button = new JButton();
    button.setIcon(icon);
    button.addActionListener(listener);
    requestFocusOnMouseDown(button);
    return button;
  }

  private static JToolBar initToolBar() {
    // create basic icon toolbar
    JToolBar basicMenu = new JToolBar();

    JButton cr = initButton(Icons.CREATE, a -> EditorScreen.instance().create());
    JButton op = initButton(Icons.LOAD, a -> EditorScreen.instance().load());
    JButton sv = initButton(Icons.SAVE, a -> EditorScreen.instance().save(false));
    JButton undo = initButton(Icons.UNDO, a -> UndoManager.instance().undo());
    JButton redo = initButton(Icons.REDO, a -> UndoManager.instance().redo());
    undo.setEnabled(false);
    redo.setEnabled(false);

    JToggleButton place = new JToggleButton();
    place.setIcon(Icons.PLACEOBJECT);
    requestFocusOnMouseDown(place);

    JToggleButton ed = new JToggleButton();
    ed.setIcon(Icons.EDIT);
    ed.setSelected(true);
    requestFocusOnMouseDown(ed);

    JToggleButton mv = new JToggleButton();
    mv.setIcon(Icons.MOVE);
    mv.setEnabled(false);
    requestFocusOnMouseDown(mv);

    ed.addActionListener(a -> {
      ed.setSelected(true);
      place.setSelected(false);
      mv.setSelected(false);
      isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_EDIT);
      isChanging = false;

      Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
    });

    place.addActionListener(a -> {
      addPopupMenu.show(place, 0, place.getHeight());
      place.setSelected(true);
      ed.setSelected(false);
      mv.setSelected(false);
      isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
      isChanging = false;
    });

    mv.addActionListener(a -> {
      mv.setSelected(true);
      ed.setSelected(false);
      place.setSelected(false);
      isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_MOVE);
      isChanging = false;

      Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(i -> {
      if (isChanging) {
        return;
      }

      if (i == MapComponent.EDITMODE_CREATE) {
        ed.setSelected(false);
        mv.setSelected(false);
        place.setSelected(true);
        place.requestFocus();
        Game.window().getRenderComponent().setCursor(Cursors.ADD, 0, 0);
      }

      if (i == MapComponent.EDITMODE_EDIT) {
        place.setSelected(false);
        mv.setSelected(false);
        ed.setSelected(true);
        ed.requestFocus();
        Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
      }

      if (i == MapComponent.EDITMODE_MOVE) {
        if (!mv.isEnabled()) {
          return;
        }

        ed.setSelected(false);
        place.setSelected(false);
        mv.setSelected(true);
        mv.requestFocus();
        Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
      }
    });

    JButton del = new JButton();
    del.setIcon(Icons.DELETE);
    del.setEnabled(false);
    del.addActionListener(a -> EditorScreen.instance().getMapComponent().delete());

    // copy
    JButton cop = new JButton();
    cop.setIcon(Icons.COPY);
    cop.setEnabled(false);
    ActionListener copyAction = a -> EditorScreen.instance().getMapComponent().copy();
    cop.addActionListener(copyAction);
    cop.getModel().setMnemonic('C');
    KeyStroke keyStroke = KeyStroke.getKeyStroke('C', Event.CTRL_MASK, false);
    cop.registerKeyboardAction(copyAction, keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // paste
    JButton paste = new JButton();
    paste.setIcon(Icons.PASTE);

    ActionListener pasteAction = a -> EditorScreen.instance().getMapComponent().paste();
    paste.addActionListener(pasteAction);
    paste.getModel().setMnemonic('V');
    KeyStroke keyStrokePaste = KeyStroke.getKeyStroke('V', Event.CTRL_MASK, false);
    paste.registerKeyboardAction(pasteAction, keyStrokePaste, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // cut
    JButton cut = new JButton();
    cut.setIcon(Icons.CUT);
    cut.setEnabled(false);
    ActionListener cutAction = a -> EditorScreen.instance().getMapComponent().cut();
    cut.addActionListener(cutAction);
    cut.getModel().setMnemonic('X');
    KeyStroke keyStrokeCut = KeyStroke.getKeyStroke('X', Event.CTRL_MASK, false);
    cut.registerKeyboardAction(cutAction, keyStrokeCut, JComponent.WHEN_IN_FOCUSED_WINDOW);

    EditorScreen.instance().getMapComponent().onFocusChanged(mo -> {
      if (mv.isSelected()) {
        mv.setSelected(false);
        ed.setSelected(true);
      }

      mv.setEnabled(mo != null);
      del.setEnabled(mo != null);
      cop.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null));

    UndoManager.onUndoStackChanged(manager -> {
      EditorScreen.instance().getMapComponent().updateTransformControls();
      undo.setEnabled(manager.canUndo());
      redo.setEnabled(manager.canRedo());
    });

    JButton colorButton = new JButton();
    colorButton.setIcon(Icons.COLOR);
    colorButton.setEnabled(false);

    JSpinner spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setToolTipText("Adjust ambient alpha.");
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.setMaximumSize(new Dimension(50, 50));
    spinnerAmbientAlpha.setEnabled(true);
    spinnerAmbientAlpha.addChangeListener(e -> {
      if (Game.world().environment() == null || Game.world().environment().getMap() == null || isChanging) {
        return;
      }

      Game.world().environment().getAmbientLight().setAlpha((int) spinnerAmbientAlpha.getValue());
      String hex = ColorHelper.encode(Game.world().environment().getAmbientLight().getColor());
      colorText.setText(hex);
      Game.world().environment().getMap().setValue(MapProperty.AMBIENTCOLOR, hex);

    });

    colorText = new JTextField();
    colorText.setHorizontalAlignment(SwingConstants.CENTER);
    colorText.setMinimumSize(new Dimension(70, 20));
    colorText.setMaximumSize(new Dimension(70, 50));
    colorText.setEnabled(false);

    colorButton.addActionListener(a -> {
      if (Game.world().environment() == null || Game.world().environment().getMap() == null || isChanging) {
        return;
      }

      Color color = null;
      if (colorText.getText() != null && !colorText.getText().isEmpty()) {
        Color solid = ColorHelper.decode(colorText.getText());
        color = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), (int) spinnerAmbientAlpha.getValue());
      }
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), color);
      if (result == null) {
        return;
      }

      spinnerAmbientAlpha.setValue(result.getAlpha());

      Game.world().environment().getMap().setValue(MapProperty.AMBIENTCOLOR, colorText.getText());
      Game.world().environment().getAmbientLight().setColor(result);
      String hex = ColorHelper.encode(Game.world().environment().getAmbientLight().getColor());
      colorText.setText(hex);
    });

    EditorScreen.instance().getMapComponent().onMapLoaded(map -> {
      isChanging = true;
      colorButton.setEnabled(map != null);
      spinnerAmbientAlpha.setEnabled(map != null);

      String colorValue = map.getStringValue(MapProperty.AMBIENTCOLOR, "#00000000");
      colorText.setText(colorValue);
      Color color = ColorHelper.decode(colorText.getText());
      if (color != null) {
        spinnerAmbientAlpha.setValue(color.getAlpha());
      }

      isChanging = false;
    });

    basicMenu.add(cr);
    basicMenu.add(op);
    basicMenu.add(sv);
    basicMenu.addSeparator();

    basicMenu.add(undo);
    basicMenu.add(redo);
    basicMenu.addSeparator();

    basicMenu.add(place);
    basicMenu.add(ed);
    basicMenu.add(mv);
    basicMenu.add(del);
    basicMenu.add(cop);
    basicMenu.add(paste);
    basicMenu.add(cut);
    basicMenu.addSeparator();

    basicMenu.add(colorButton);
    basicMenu.add(Box.createHorizontalStrut(5));
    basicMenu.add(colorText);
    basicMenu.add(Box.createHorizontalStrut(5));
    basicMenu.add(spinnerAmbientAlpha);

    return basicMenu;
  }

  private static void requestFocusOnMouseDown(JComponent button) {
    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (button.isEnabled()) {
          button.requestFocus();
        }
      }
    });
  }

  private static void initAddMenu(JComponent addMenu) {
    JMenuItem addProp = new JMenuItem(Resources.strings().get("add_prop"), Icons.PROP);
    addProp.addActionListener(a -> setCreateMode(MapObjectType.PROP));

    JMenuItem addCreature = new JMenuItem(Resources.strings().get("add_creature"), Icons.CREATURE);
    addCreature.addActionListener(a -> setCreateMode(MapObjectType.CREATURE));

    JMenuItem addLight = new JMenuItem(Resources.strings().get("add_light"), Icons.LIGHT);
    addLight.addActionListener(a -> setCreateMode(MapObjectType.LIGHTSOURCE));

    JMenuItem addTrigger = new JMenuItem(Resources.strings().get("add_trigger"), Icons.TRIGGER);
    addTrigger.addActionListener(a -> setCreateMode(MapObjectType.TRIGGER));

    JMenuItem addSpawnpoint = new JMenuItem(Resources.strings().get("add_spawnpoint"), Icons.SPAWNPOINT);
    addSpawnpoint.addActionListener(a -> setCreateMode(MapObjectType.SPAWNPOINT));

    JMenuItem addCollisionBox = new JMenuItem(Resources.strings().get("add_collisionbox"), Icons.COLLISIONBOX);
    addCollisionBox.addActionListener(a -> setCreateMode(MapObjectType.COLLISIONBOX));

    JMenuItem addMapArea = new JMenuItem(Resources.strings().get("add_area"), Icons.MAPAREA);
    addMapArea.addActionListener(a -> setCreateMode(MapObjectType.AREA));

    JMenuItem addShadow = new JMenuItem(Resources.strings().get("add_shadow"), Icons.SHADOWBOX);
    addShadow.addActionListener(a -> setCreateMode(MapObjectType.STATICSHADOW));

    JMenuItem addEmitter = new JMenuItem(Resources.strings().get("add_emitter"), Icons.EMITTER);
    addEmitter.addActionListener(a -> setCreateMode(MapObjectType.EMITTER));

    addMenu.add(addProp);
    addMenu.add(addCreature);
    addMenu.add(addLight);
    addMenu.add(addTrigger);
    addMenu.add(addSpawnpoint);
    addMenu.add(addCollisionBox);
    addMenu.add(addMapArea);
    addMenu.add(addShadow);
    addMenu.add(addEmitter);
  }

  private static void setCreateMode(MapObjectType tpye) {
    EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
    EditorScreen.instance().getMapObjectPanel().setMapObjectType(tpye);
  }

  private static void initPopupMenus(Canvas canvas) {
    canvasPopup = new JPopupMenu();
    addPopupMenu = new JPopupMenu();
    JMenu addSubMenu = new JMenu("Add ...");
    addSubMenu.setIcon(Icons.ADD);
    initAddMenu(addPopupMenu);
    initAddMenu(addSubMenu);

    JMenuItem delete = new JMenuItem("Delete Entity", Icons.DELETEX16);
    delete.addActionListener(e -> EditorScreen.instance().getMapComponent().delete());
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.setEnabled(false);

    JMenuItem copy = new JMenuItem("Copy Entity", Icons.COPYX16);
    copy.addActionListener(e -> EditorScreen.instance().getMapComponent().copy());
    copy.setEnabled(false);
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

    JMenuItem cut = new JMenuItem("Cut Entity", Icons.CUTX16);
    cut.addActionListener(e -> EditorScreen.instance().getMapComponent().cut());
    cut.setEnabled(false);
    cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));

    JMenuItem paste = new JMenuItem("Paste Entity", Icons.PASTEX16);
    paste.addActionListener(e -> EditorScreen.instance().getMapComponent().paste());
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
    paste.setEnabled(false);

    JMenuItem blueprint = new JMenuItem("Define Blueprint", Icons.BLUEPRINT);
    blueprint.addActionListener(e -> EditorScreen.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

    canvasPopup.add(addSubMenu);
    canvasPopup.add(paste);
    canvasPopup.addSeparator();
    canvasPopup.add(copy);
    canvasPopup.add(cut);
    canvasPopup.add(delete);
    canvasPopup.addSeparator();
    canvasPopup.add(blueprint);

    EditorScreen.instance().getMapComponent().onFocusChanged(mo -> {
      copy.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      delete.setEnabled(mo != null);
      blueprint.setEnabled(mo != null);
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null));

    canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          canvasPopup.show(canvas, e.getX(), e.getY());
        }
      }
    });
  }

  private static void initSystemTray() {
    // add system tray icon with popup menu
    if (SystemTray.isSupported()) {
      SystemTray tray = SystemTray.getSystemTray();
      PopupMenu menu = new PopupMenu();
      MenuItem exitItem = new MenuItem(Resources.strings().get("menu_exit"));
      exitItem.addActionListener(a -> System.exit(0));
      menu.add(exitItem);

      trayIcon = new TrayIcon(Resources.images().get("litiengine-icon.png"), Game.info().toString(), menu);
      trayIcon.setImageAutoSize(true);
      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
  }
}
