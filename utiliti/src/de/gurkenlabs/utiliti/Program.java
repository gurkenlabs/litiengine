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
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.AssetPanel;
import de.gurkenlabs.utiliti.swing.AssetTree;
import de.gurkenlabs.utiliti.swing.ColorChooser;
import de.gurkenlabs.utiliti.swing.dialogs.GridEditPanel;
import de.gurkenlabs.utiliti.swing.dialogs.MapPropertyPanel;
import de.gurkenlabs.utiliti.swing.panels.MapObjectPanel;

public class Program {
  public static final Font TEXT_FONT = new JLabel().getFont().deriveFont(10f);
  public static final BufferedImage CURSOR = Resources.getImage("cursor.png");
  public static final BufferedImage CURSOR_MOVE = Resources.getImage("cursor-move.png");
  public static final BufferedImage CURSOR_SELECT = Resources.getImage("cursor-select.png");
  public static final BufferedImage CURSOR_LOAD = Resources.getImage("cursor-load.png");
  public static final BufferedImage CURSOR_TRANS_HORIZONTAL = Resources.getImage("cursor-trans-horizontal.png");
  public static final BufferedImage CURSOR_TRANS_VERTICAL = Resources.getImage("cursor-trans-vertical.png");
  public static final BufferedImage CURSOR_TRANS_DIAGONAL_LEFT = Resources.getImage("cursor-trans-315.png");
  public static final BufferedImage CURSOR_TRANS_DIAGONAL_RIGHT = Resources.getImage("cursor-trans-45.png");

  private static UserPreferenceConfiguration userPreferences;
  private static JScrollBar horizontalScroll;
  private static JScrollBar verticalScroll;
  private static TrayIcon trayIcon;

  private static final Logger log = Logger.getLogger(Program.class.getName());
  private static Menu recentFiles;
  private static AssetPanel assetPanel;
  private static AssetTree assetTree;
  private static JPopupMenu canvasPopup;
  private static JLabel statusBar;
  private static boolean isChanging;

  public static void main(String[] args) {

    try {
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    Game.getInfo().setName("utiLITI");
    Game.getInfo().setSubTitle("litiengine creation kit");
    Game.getInfo().setVersion("v0.4.8-alpha");

    initSystemTray();

    Game.getConfiguration().getConfigurationGroups().add(new UserPreferenceConfiguration());
    Game.init();
    Game.getInfo().setDefaultRenderScale(1.0f);
    JOptionPane.setDefaultLocale(Locale.getDefault());

    userPreferences = Game.getConfiguration().getConfigurationGroup("user_");
    Game.getCamera().onZoomChanged(zoom -> {
      userPreferences.setZoom(zoom);
    });

    // Game.getScreenManager().setIconImage(Resources.getImage("pixel-icon-utility.png"));

    Game.getScreenManager().addScreen(EditorScreen.instance());
    Game.getScreenManager().displayScreen("EDITOR");

    Game.getScreenManager().getRenderComponent().setCursor(CURSOR, 0, 0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetX(0);
    Game.getScreenManager().getRenderComponent().setCursorOffsetY(0);
    setupInterface();
    Game.start();
    Input.mouse().setGrabMouse(false);
    Input.keyboard().consumeAlt(true);
    handleArgs(args);

    if (!EditorScreen.instance().fileLoaded() && userPreferences.getLastGameFile() != null) {
      EditorScreen.instance().load(new File(userPreferences.getLastGameFile()));
    }
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

  public static TrayIcon getTrayIcon() {
    return trayIcon;
  }

  public static UserPreferenceConfiguration getUserPreferences() {
    return userPreferences;
  }

  public static void setStatus(String status) {
    statusBar.setText(status);
  }

  public static void updateScrollBars() {
    horizontalScroll.setMinimum(0);
    horizontalScroll.setMaximum(Game.getEnvironment().getMap().getSizeInPixels().width);
    verticalScroll.setMinimum(0);
    verticalScroll.setMaximum(Game.getEnvironment().getMap().getSizeInPixels().height);
    horizontalScroll.setValue((int) Game.getCamera().getViewPort().getCenterX());
    verticalScroll.setValue((int) Game.getCamera().getViewPort().getCenterY());
  }

  private static boolean exit() {
    String resourceFile = EditorScreen.instance().getCurrentResourceFile() != null ? EditorScreen.instance().getCurrentResourceFile() : "";
    int n = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), Resources.get("hud_saveProjectMessage") + "\n" + resourceFile, Resources.get("hud_saveProject"), JOptionPane.YES_NO_CANCEL_OPTION);

    if (n == JOptionPane.YES_OPTION) {
      EditorScreen.instance().save(false);
    }

    boolean exit = n != JOptionPane.CANCEL_OPTION && n != JOptionPane.CLOSED_OPTION;
    if (exit) {
      getUserPreferences().setFrameState(((JFrame) Game.getScreenManager()).getExtendedState());
    }

    return exit;
  }

  private static void handleArgs(String[] args) {
    if (args.length == 0) {
      return;
    }

    for (int i = 0; i < args.length; i++) {
      if (args[i] == null || args[i].isEmpty()) {
        continue;
      }

      // handle file loading
      if (i == 0) {
        if (args[i] == null || args[i].isEmpty()) {
          continue;
        }

        try {
          Paths.get(args[i]);
        } catch (InvalidPathException e) {
          continue;
        }

        File f = new File(args[i]);
        EditorScreen.instance().load(f);
      }
    }
  }

  private static void setupInterface() {
    MenuBar menuBar = new MenuBar();
    JFrame window = ((JFrame) Game.getScreenManager());
    Game.onTerminating(s -> exit());
    window.setResizable(true);

    window.setMenuBar(menuBar);

    if (userPreferences.getWidth() != 0 && userPreferences.getHeight() != 0) {
      window.setSize(userPreferences.getWidth(), userPreferences.getHeight());
    }

    if (userPreferences.getFrameState() != JFrame.ICONIFIED && userPreferences.getFrameState() != JFrame.NORMAL) {
      window.setExtendedState(userPreferences.getFrameState());
    }

    Canvas canvas = Game.getScreenManager().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (window.getSize().width * 0.75), window.getSize().height);
    window.remove(canvas);
    JPanel renderPane = new JPanel(new BorderLayout());
    renderPane.add(canvas);
    renderPane.setMinimumSize(new Dimension(300, 0));

    initCanvasPopupMenu(canvas);

    JPanel contentPane = new JPanel(new BorderLayout());

    window.setContentPane(contentPane);

    horizontalScroll = new JScrollBar(JScrollBar.HORIZONTAL);
    renderPane.add(horizontalScroll, BorderLayout.SOUTH);
    verticalScroll = new JScrollBar(JScrollBar.VERTICAL);
    renderPane.add(verticalScroll, BorderLayout.EAST);

    horizontalScroll.addAdjustmentListener(e -> {
      if (EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }

      Point2D newFocus = new Point2D.Double(horizontalScroll.getValue(), Game.getCamera().getFocus().getY());
      Game.getCamera().setFocus(newFocus);
    });

    verticalScroll.addAdjustmentListener(e -> {
      if (EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }
      Point2D newFocus = new Point2D.Double(Game.getCamera().getFocus().getX(), verticalScroll.getValue());
      Game.getCamera().setFocus(newFocus);
    });

    MapObjectPanel mapEditorPanel = new MapObjectPanel();
    mapEditorPanel.setupControls();
    MapSelectionPanel mapSelectionPanel = new MapSelectionPanel();
    JSplitPane mapWrap = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mapWrap.setMinimumSize(new Dimension(300, 0));
    mapWrap.setBottomComponent(mapEditorPanel);
    mapWrap.setTopComponent(mapSelectionPanel);
    mapWrap.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setSelectionEditSplitter(mapWrap.getDividerLocation()));
    if (userPreferences.getSelectionEditSplitter() != 0) {
      mapWrap.setDividerLocation(userPreferences.getSelectionEditSplitter());
    }

    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();

    bottomTab.addTab("Assets", initAssetsComponent());
    bottomTab.addTab("Console", initConsole());
    bottomTab.setIconAt(0, new ImageIcon(Resources.getImage("asset.png")));
    bottomTab.setIconAt(1, new ImageIcon(Resources.getImage("console.png")));

    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    statusBar = new JLabel("");
    statusBar.setPreferredSize(new Dimension(0, 16));
    statusBar.setFont(new Font(ConsoleLogHandler.CONSOLE_FONT, Font.PLAIN, 10));
    bottomPanel.add(statusBar, BorderLayout.SOUTH);
    EditorScreen.instance().getMapComponent().onSelectionChanged(selection -> {
      if (selection.isEmpty()) {
        statusBar.setText("");
      } else {
        statusBar.setText(" " + selection.size() + " selected objects");
      }
    });

    JSplitPane rendersplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPane, bottomPanel);
    if (userPreferences.getBottomSplitter() != 0) {
      rendersplit.setDividerLocation(userPreferences.getBottomSplitter());
    } else {
      rendersplit.setDividerLocation((int) (window.getSize().height * 0.75));
    }

    rendersplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setBottomSplitter(rendersplit.getDividerLocation()));
    rendersplit.setContinuousLayout(true);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rendersplit, mapWrap);
    split.setContinuousLayout(true);
    split.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        userPreferences.setWidth(window.getWidth());
        userPreferences.setHeight(window.getHeight());
      }
    });

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setMainSplitter(split.getDividerLocation()));

    contentPane.add(split, BorderLayout.CENTER);
    split.setDividerLocation(userPreferences.getMainSplitterPosition() != 0 ? userPreferences.getMainSplitterPosition() : (int) (window.getSize().width * 0.75));

    JToolBar toolbar = initToolBar();
    contentPane.add(toolbar, BorderLayout.NORTH);

    EditorScreen.instance().setMapEditorPanel(mapEditorPanel);
    EditorScreen.instance().setMapSelectionPanel(mapSelectionPanel);

    Menu mnFile = initFileMenu();
    menuBar.add(mnFile);

    Menu mnView = initViewMenu();
    menuBar.add(mnView);

    Menu mnProject = initProjectMenu();
    menuBar.add(mnProject);

    Menu mnMap = initMapMenu();
    menuBar.add(mnMap);
  }

  private static Menu initFileMenu() {
    Menu mnFile = new Menu(Resources.get("menu_file"));

    MenuItem create = new MenuItem(Resources.get("menu_createProject"));
    create.setShortcut(new MenuShortcut(KeyEvent.VK_N));
    create.addActionListener(a -> EditorScreen.instance().create());

    MenuItem load = new MenuItem(Resources.get("menu_loadProject"));
    load.setShortcut(new MenuShortcut(KeyEvent.VK_O));
    load.addActionListener(a -> EditorScreen.instance().load());

    MenuItem save = new MenuItem(Resources.get("menu_save"));
    save.setShortcut(new MenuShortcut(KeyEvent.VK_S));
    save.addActionListener(a -> EditorScreen.instance().save(false));

    MenuItem saveAs = new MenuItem(Resources.get("menu_saveAs"));
    saveAs.addActionListener(a -> EditorScreen.instance().save(true));

    MenuItem exit = new MenuItem(Resources.get("menu_exit"));
    exit.setShortcut(new MenuShortcut(KeyEvent.VK_Q));
    exit.addActionListener(a -> Game.terminate());

    mnFile.add(load);
    mnFile.add(create);
    mnFile.add(save);
    mnFile.add(saveAs);
    mnFile.addSeparator();
    recentFiles = new Menu(Resources.get("menu_recentFiles"));
    loadRecentFiles();
    mnFile.add(recentFiles);
    mnFile.addSeparator();
    mnFile.add(exit);
    return mnFile;
  }

  private static Menu initViewMenu() {
    Menu mnView = new Menu(Resources.get("menu_view"));

    CheckboxMenuItem snapToGrid = new CheckboxMenuItem(Resources.get("menu_snapGrid"));
    snapToGrid.setState(userPreferences.isSnapGrid());
    EditorScreen.instance().getMapComponent().setSnapToGrid(snapToGrid.getState());
    snapToGrid.addItemListener(e -> {
      EditorScreen.instance().getMapComponent().setSnapToGrid(snapToGrid.getState());
      userPreferences.setSnapGrid(snapToGrid.getState());
    });

    CheckboxMenuItem renderGrid = new CheckboxMenuItem(Resources.get("menu_renderGrid"));
    renderGrid.setState(userPreferences.isShowGrid());
    EditorScreen.instance().getMapComponent().setRenderGrid(renderGrid.getState());
    renderGrid.setShortcut(new MenuShortcut(KeyEvent.VK_G));
    renderGrid.addItemListener(e -> {
      EditorScreen.instance().getMapComponent().setRenderGrid(renderGrid.getState());
      userPreferences.setShowGrid(renderGrid.getState());
    });

    CheckboxMenuItem renderCollision = new CheckboxMenuItem(Resources.get("menu_renderCollisionBoxes"));
    renderCollision.setState(userPreferences.isRenderBoundingBoxes());
    EditorScreen.instance().getMapComponent().setRenderCollisionBoxes(renderCollision.getState());
    renderCollision.setShortcut(new MenuShortcut(KeyEvent.VK_H));
    renderCollision.addItemListener(e -> {
      EditorScreen.instance().getMapComponent().setRenderCollisionBoxes(renderCollision.getState());
      userPreferences.setRenderBoundingBoxes(renderCollision.getState());
    });

    MenuItem setGrid = new MenuItem(Resources.get("menu_gridSize"));
    setGrid.addActionListener(a -> {
      GridEditPanel panel = new GridEditPanel(EditorScreen.instance().getMapComponent().getGridSize());
      int option = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), panel, Resources.get("menu_gridSettings"), JOptionPane.DEFAULT_OPTION);
      if (option == JOptionPane.OK_OPTION) {
        EditorScreen.instance().getMapComponent().setGridSize(panel.getGridSize());
      }
    });

    MenuItem zoomIn = new MenuItem(Resources.get("menu_zoomIn"));
    zoomIn.setShortcut(new MenuShortcut(KeyEvent.VK_PLUS));
    zoomIn.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomIn());

    MenuItem zoomOut = new MenuItem(Resources.get("menu_zoomOut"));
    zoomOut.setShortcut(new MenuShortcut(KeyEvent.VK_MINUS));
    zoomOut.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomOut());

    mnView.add(snapToGrid);
    mnView.add(renderGrid);
    mnView.add(renderCollision);
    mnView.add(setGrid);
    mnView.addSeparator();
    mnView.add(zoomIn);
    mnView.add(zoomOut);

    return mnView;
  }

  private static Menu initProjectMenu() {
    Menu mnProject = new Menu(Resources.get("menu_project"));

    CheckboxMenuItem compress = new CheckboxMenuItem(Resources.get("menu_compressProjectFile"));
    compress.setState(userPreferences.isCompressFile());
    compress.addItemListener(e -> userPreferences.setCompressFile(compress.getState()));

    CheckboxMenuItem sync = new CheckboxMenuItem(Resources.get("menu_syncMaps"));
    sync.setState(userPreferences.isSyncMaps());
    sync.addItemListener(e -> userPreferences.setSyncMaps(sync.getState()));

    MenuItem importSpriteFile = new MenuItem(Resources.get("menu_assets_importSpriteFile"));
    importSpriteFile.addActionListener(a -> EditorScreen.instance().importSpriteFile());

    MenuItem importSprite = new MenuItem(Resources.get("menu_assets_importSprite"));
    importSprite.addActionListener(a -> EditorScreen.instance().importSpritesheets());

    MenuItem importEmitters = new MenuItem(Resources.get("menu_assets_importEmitters"));
    importEmitters.addActionListener(a -> EditorScreen.instance().importEmitters());

    MenuItem importBlueprints = new MenuItem(Resources.get("menu_assets_importBlueprints"));
    importBlueprints.addActionListener(a -> EditorScreen.instance().importBlueprints());

    mnProject.add(importSprite);
    mnProject.add(importSpriteFile);
    mnProject.add(importEmitters);
    mnProject.add(importBlueprints);
    mnProject.addSeparator();
    mnProject.add(compress);
    mnProject.add(sync);

    return mnProject;
  }

  private static Menu initMapMenu() {
    Menu mnMap = new Menu(Resources.get("menu_map"));

    MenuItem imp = new MenuItem(Resources.get("menu_import"));
    imp.addActionListener(a -> EditorScreen.instance().getMapComponent().importMap());

    MenuItem exp = new MenuItem(Resources.get("menu_export"));
    exp.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    MenuItem del2 = new MenuItem(Resources.get("menu_removeMap"));
    del2.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());

    MenuItem mapProps = new MenuItem(Resources.get("menu_properties"));
    mapProps.setShortcut(new MenuShortcut(KeyEvent.VK_M));
    mapProps.addActionListener(a -> {
      if (EditorScreen.instance().getMapComponent().getMaps() == null || EditorScreen.instance().getMapComponent().getMaps().isEmpty()) {
        return;
      }

      MapPropertyPanel panel = new MapPropertyPanel();
      panel.bind(Game.getEnvironment().getMap());

      int option = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), panel, Resources.get("menu_mapProperties"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        panel.saveChanges();

        final String colorProp = Game.getEnvironment().getMap().getCustomProperty(MapProperty.AMBIENTCOLOR);
        try {
          if (Game.getEnvironment().getMap().getCustomProperty(MapProperty.AMBIENTALPHA) != null) {
            int alpha = Integer.parseInt(Game.getEnvironment().getMap().getCustomProperty(MapProperty.AMBIENTALPHA));
            Game.getEnvironment().getAmbientLight().setAlpha(alpha);
          }

          if (colorProp != null && !colorProp.isEmpty()) {
            Color ambientColor = Color.decode(colorProp);
            Game.getEnvironment().getAmbientLight().setColor(ambientColor);
          }
        } catch (final NumberFormatException nfe) {
          log.log(Level.SEVERE, nfe.getLocalizedMessage(), nfe);
        }

        EditorScreen.instance().getMapComponent().loadMaps(EditorScreen.instance().getGameFile().getMaps());
        EditorScreen.instance().getMapComponent().loadEnvironment((Map) Game.getEnvironment().getMap());
        EditorScreen.instance().mapChanged();
      }
    });

    mnMap.add(imp);
    mnMap.add(exp);
    mnMap.add(del2);
    mnMap.addSeparator();
    mnMap.add(mapProps);

    return mnMap;
  }

  private static Component initAssetsComponent() {
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    assetTree = new AssetTree();
    split.setLeftComponent(assetTree);
    assetPanel = new AssetPanel();

    JScrollPane scrollPane = new JScrollPane(assetPanel);

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> userPreferences.setAssetsSplitter(split.getDividerLocation()));
    split.setDividerLocation(userPreferences.getMainSplitterPosition() != 0 ? userPreferences.getAssetsSplitter() : 200);

    split.setRightComponent(scrollPane);
    return split;
  }

  private static Component initConsole() {
    // TODO: implement possibility to configure the desired log level
    //// Logger root = Logger.getLogger("de.gurkenlabs");
    //// root.setLevel(Level.FINE);
    Logger root = Logger.getLogger("");
    JTextPane consoleTextArea = new JTextPane();
    JScrollPane consoleScrollPane = new JScrollPane();
    consoleScrollPane.setViewportBorder(null);
    consoleScrollPane.setViewportView(consoleTextArea);

    consoleTextArea.setEditable(false);
    consoleTextArea.setBackground(Color.DARK_GRAY);
    consoleTextArea.setAutoscrolls(true);
    root.addHandler(new ConsoleLogHandler(consoleTextArea));
    return consoleScrollPane;
  }

  private static JToolBar initToolBar() {
    // create basic icon toolbar
    JToolBar basicMenu = new JToolBar();

    JButton cr = new JButton();
    cr.setIcon(new ImageIcon(Resources.getImage("button-create.png")));
    basicMenu.add(cr);
    cr.addActionListener(a -> EditorScreen.instance().create());

    JButton op = new JButton();
    op.setIcon(new ImageIcon(Resources.getImage("button-load.png")));
    basicMenu.add(op);
    op.addActionListener(a -> EditorScreen.instance().load());

    JButton sv = new JButton();
    sv.setIcon(new ImageIcon(Resources.getImage("button-save.png")));
    basicMenu.add(sv);
    sv.addActionListener(a -> EditorScreen.instance().save(false));

    basicMenu.addSeparator();

    JButton undo = new JButton();
    undo.setIcon(new ImageIcon(Resources.getImage("button-undo.png")));
    basicMenu.add(undo);
    undo.addActionListener(a -> UndoManager.instance().undo());

    JButton redo = new JButton();
    redo.setIcon(new ImageIcon(Resources.getImage("button-redo.png")));
    basicMenu.add(redo);
    redo.addActionListener(a -> UndoManager.instance().redo());

    undo.setEnabled(false);
    redo.setEnabled(false);

    basicMenu.addSeparator();

    JToggleButton place = new JToggleButton();
    place.setIcon(new ImageIcon(Resources.getImage("button-placeobject.png")));
    basicMenu.add(place);

    JToggleButton ed = new JToggleButton();
    ed.setIcon(new ImageIcon(Resources.getImage("button-edit.png")));
    ed.setSelected(true);

    JToggleButton mv = new JToggleButton();
    mv.setIcon(new ImageIcon(Resources.getImage("button-move.png")));
    mv.setEnabled(false);

    ed.addActionListener(a -> {
      ed.setSelected(true);
      place.setSelected(false);
      mv.setSelected(false);
      isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_EDIT);
      isChanging = false;

      Game.getScreenManager().getRenderComponent().setCursor(CURSOR, 0, 0);
    });
    basicMenu.add(ed);
    basicMenu.add(mv);

    place.addActionListener(a -> {

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

      Game.getScreenManager().getRenderComponent().setCursor(CURSOR_MOVE, 0, 0);
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(i -> {
      if (isChanging) {
        return;
      }

      if (i == MapComponent.EDITMODE_CREATE) {
        ed.setSelected(false);
        mv.setSelected(false);
        place.setSelected(true);
      }

      if (i == MapComponent.EDITMODE_EDIT) {
        place.setSelected(false);
        mv.setSelected(false);
        ed.setSelected(true);
        Game.getScreenManager().getRenderComponent().setCursor(CURSOR, 0, 0);
      }

      if (i == MapComponent.EDITMODE_MOVE) {
        ed.setSelected(false);
        place.setSelected(false);
        mv.setSelected(true);
        Game.getScreenManager().getRenderComponent().setCursor(CURSOR_MOVE, 0, 0);
      }
    });

    JButton del = new JButton();
    del.setIcon(new ImageIcon(Resources.getImage("button-delete.png")));
    basicMenu.add(del);
    del.setEnabled(false);
    del.addActionListener(a -> EditorScreen.instance().getMapComponent().delete());

    // copy
    JButton cop = new JButton();
    cop.setIcon(new ImageIcon(Resources.getImage("button-copy.png")));
    basicMenu.add(cop);
    cop.setEnabled(false);
    ActionListener copyAction = a -> EditorScreen.instance().getMapComponent().copy();
    cop.addActionListener(copyAction);
    cop.getModel().setMnemonic('C');
    KeyStroke keyStroke = KeyStroke.getKeyStroke('C', Event.CTRL_MASK, false);
    cop.registerKeyboardAction(copyAction, keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // paste
    JButton paste = new JButton();
    paste.setIcon(new ImageIcon(Resources.getImage("button-paste.png")));
    basicMenu.add(paste);
    ActionListener pasteAction = a -> EditorScreen.instance().getMapComponent().paste();
    paste.addActionListener(pasteAction);
    paste.getModel().setMnemonic('V');
    KeyStroke keyStrokePaste = KeyStroke.getKeyStroke('V', Event.CTRL_MASK, false);
    paste.registerKeyboardAction(pasteAction, keyStrokePaste, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // cut
    JButton cut = new JButton();
    cut.setIcon(new ImageIcon(Resources.getImage("button-cut.png")));
    basicMenu.add(cut);
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

    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> {
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
    });

    UndoManager.onUndoStackChanged(manager -> {
      EditorScreen.instance().getMapComponent().updateTransformControls();
      EditorScreen.instance().mapChanged();
      undo.setEnabled(manager.canUndo());
      redo.setEnabled(manager.canRedo());
    });

    basicMenu.addSeparator();

    JButton colorButton = new JButton();
    colorButton.setIcon(new ImageIcon(Resources.getImage("button-color.png")));
    colorButton.setEnabled(false);

    JTextField colorText = new JTextField();
    colorText.setEnabled(false);
    colorText.setMaximumSize(new Dimension(50, 50));

    JSpinner spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.setFont(Program.TEXT_FONT);
    spinnerAmbientAlpha.setMaximumSize(new Dimension(50, 50));
    spinnerAmbientAlpha.setEnabled(false);
    spinnerAmbientAlpha.addChangeListener(e -> {
      if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null || isChanging) {
        return;
      }

      Game.getEnvironment().getMap().setCustomProperty(MapProperty.AMBIENTALPHA, spinnerAmbientAlpha.getValue().toString());
      Game.getEnvironment().getAmbientLight().setAlpha((int) spinnerAmbientAlpha.getValue());
    });

    colorButton.addActionListener(a -> {
      if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null || isChanging) {
        return;
      }

      Color color = null;
      if (colorText.getText() != null && !colorText.getText().isEmpty()) {
        Color solid = Color.decode(colorText.getText());
        color = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), (int) spinnerAmbientAlpha.getValue());
      }

      Color result = ColorChooser.showRgbDialog("Select an ambient color.", color);
      if (result == null) {
        return;
      }

      String h = "#" + Integer.toHexString(result.getRGB()).substring(2);
      colorText.setText(h);

      spinnerAmbientAlpha.setValue(result.getAlpha());

      Game.getEnvironment().getMap().setCustomProperty(MapProperty.AMBIENTCOLOR, colorText.getText());
      Game.getEnvironment().getAmbientLight().setColor(result);
    });

    EditorScreen.instance().getMapComponent().onMapLoaded(map -> {
      isChanging = true;
      colorButton.setEnabled(map != null);
      spinnerAmbientAlpha.setEnabled(map != null);
      colorText.setText(map.getCustomProperty(MapProperty.AMBIENTCOLOR));

      String alpha = map.getCustomProperty(MapProperty.AMBIENTALPHA);
      if (alpha != null && !alpha.isEmpty()) {
        spinnerAmbientAlpha.setValue((int) Double.parseDouble(alpha));
      }
      isChanging = false;
    });

    basicMenu.add(colorButton);
    basicMenu.add(Box.createHorizontalStrut(5));
    basicMenu.add(colorText);
    basicMenu.add(Box.createHorizontalStrut(5));
    basicMenu.add(spinnerAmbientAlpha);

    return basicMenu;
  }

  private static void initCanvasPopupMenu(Canvas canvas) {
    canvasPopup = new JPopupMenu();
    JMenuItem delete = new JMenuItem("Delete Entity", new ImageIcon(Resources.getImage("button-deletex16.png")));
    delete.addActionListener(e -> EditorScreen.instance().getMapComponent().delete());
    delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    delete.setEnabled(false);

    JMenuItem copy = new JMenuItem("Copy Entity", new ImageIcon(Resources.getImage("button-copyx16.png")));
    copy.addActionListener(e -> EditorScreen.instance().getMapComponent().copy());
    copy.setEnabled(false);
    copy.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.Event.CTRL_MASK));

    JMenuItem cut = new JMenuItem("Cut Entity", new ImageIcon(Resources.getImage("button-cutx16.png")));
    cut.addActionListener(e -> EditorScreen.instance().getMapComponent().cut());
    cut.setEnabled(false);
    cut.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK));

    JMenuItem paste = new JMenuItem("Paste Entity", new ImageIcon(Resources.getImage("button-pastex16.png")));
    paste.addActionListener(e -> EditorScreen.instance().getMapComponent().paste());
    paste.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.Event.CTRL_MASK));
    paste.setEnabled(false);

    JMenuItem blueprint = new JMenuItem("Define Blueprint", new ImageIcon(Resources.getImage("blueprint.png")));
    blueprint.addActionListener(e -> EditorScreen.instance().getMapComponent().defineBlueprint());
    blueprint.setEnabled(false);

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

    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> {
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
    });

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
      MenuItem exitItem = new MenuItem(Resources.get("menu_exit"));
      exitItem.addActionListener(a -> Game.terminate());
      menu.add(exitItem);

      trayIcon = new TrayIcon(Resources.getImage("pixel-icon-utility.png"), Game.getInfo().toString(), menu);
      trayIcon.setImageAutoSize(true);
      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
  }
}
