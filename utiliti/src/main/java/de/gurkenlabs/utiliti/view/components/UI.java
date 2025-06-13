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
import de.gurkenlabs.utiliti.controller.MapComponent;
import de.gurkenlabs.utiliti.controller.MapController;
import de.gurkenlabs.utiliti.controller.PropertyInspector;
import de.gurkenlabs.utiliti.controller.Scroll;
import de.gurkenlabs.utiliti.model.Cursors;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Style.Theme;
import de.gurkenlabs.utiliti.view.menus.CanvasPopupMenu;
import de.gurkenlabs.utiliti.view.menus.MainMenuBar;
import java.awt.BorderLayout;
import java.awt.Canvas;
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
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public final class UI {
  private static final List<JComponent> orphanComponents = new CopyOnWriteArrayList<>();
  private static JPopupMenu canvasPopup;
  private static AssetList assetComponent;

  private static MapObjectInspector mapObjectPanel;
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

    int n =
      JOptionPane.showConfirmDialog(
        Game.window().getRenderComponent(),
        Resources.strings().get("hud_saveProjectMessage") + "\n" + resourceFile,
        Resources.strings().get("hud_saveProject"),
        JOptionPane.YES_NO_CANCEL_OPTION);

    if (n == JOptionPane.YES_OPTION) {
      Editor.instance().save(false);
    }

    return n != JOptionPane.CANCEL_OPTION && n != JOptionPane.CLOSED_OPTION;
  }

  public static boolean showRevertWarning() {
    int n =
      JOptionPane.showConfirmDialog(
        Game.window().getRenderComponent(),
        Resources.strings().get("hud_revertChangesMessage"),
        Resources.strings().get("hud_revertChanges"),
        JOptionPane.YES_NO_OPTION);
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

    UIManager.addPropertyChangeListener(
      e -> {
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

  public static LayerController getLayerController() {
    return mapLayerList;
  }

  public static EntityController getEntityController() {
    return entityList;
  }

  public static Controller getAssetController() {
    return assetComponent;
  }

  public static MapController getMapController() {
    return mapSelectionPanel;
  }

  private static void initScrollBars(JPanel renderPane) {
    ScrollHandlerBar horizontalScroll = new ScrollHandlerBar(java.awt.Adjustable.HORIZONTAL);
    ScrollHandlerBar verticalScroll = new ScrollHandlerBar(java.awt.Adjustable.VERTICAL);

    EnvironmentListener environmentListener = new EnvironmentListener() {
      @Override
      public void loaded(Environment environment) {
        boolean hasMap = environment != null && environment.getMap() != null;
        horizontalScroll.setVisible(hasMap);
        verticalScroll.setVisible(hasMap);
      }

      @Override
      public void unloaded(Environment environment) {
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

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (window.getSize().width * 0.75), window.getSize().height);

    // remove canvas because we want to add a wrapping panel
    window.remove(canvas);
    JPanel renderPanel;

    renderPanel = new JPanel(new BorderLayout());
    renderPanel.add(canvas);
    renderPanel.setMinimumSize(new Dimension(300, 100));
    initScrollBars(renderPanel);

    JSplitPane split =
      new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        initRenderSplitPanel(renderPanel, window),
        initRightSplitPanel());
    split.setContinuousLayout(true);
    split.addComponentListener(
      new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          Editor.preferences().setWidth(window.getWidth());
          Editor.preferences().setHeight(window.getHeight());
        }
      });

    split.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setMainSplitter(split.getDividerLocation()));

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);

    rootPanel.add(split, BorderLayout.CENTER);
    split.setDividerLocation(
      Editor.preferences().getMainSplitterPosition() != 0
        ? Editor.preferences().getMainSplitterPosition()
        : (int) (window.getSize().width * 0.75));

    initPopupMenu(canvas);
    window.setJMenuBar(new MainMenuBar());
  }

  private static JFrame initWindow() {
    JFrame window = ((JFrame) Game.window().getHostControl());
    window.setResizable(true);

    Game.addGameListener(
      new GameListener() {
        @Override
        public boolean terminating() {
          boolean terminate = notifyPendingChanges();
          if (terminate) {
            Editor.preferences().setFrameState(window.getExtendedState());
          }

          return terminate;
        }
      });

    window.setLocationRelativeTo(null);
    if (Editor.preferences().getFrameState() != java.awt.Frame.ICONIFIED
      && Editor.preferences().getFrameState() != java.awt.Frame.NORMAL) {
      window.setExtendedState(Editor.preferences().getFrameState());
    } else if (Editor.preferences().getWidth() != 0 && Editor.preferences().getHeight() != 0) {
      window.setSize(Editor.preferences().getWidth(), Editor.preferences().getHeight());
    }

    return window;
  }

  private static Component initRenderSplitPanel(JPanel renderPanel, JFrame window) {
    JSplitPane renderSplitPanel =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, initBottomPanel());
    if (Editor.preferences().getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(Editor.preferences().getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (window.getSize().height * 0.75));
    }

    renderSplitPanel.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setBottomSplitter(renderSplitPanel.getDividerLocation()));
    renderSplitPanel.setContinuousLayout(true);
    return renderSplitPanel;
  }

  private static Component initRightSplitPanel() {
    mapSelectionPanel = new MapList();

    mapLayerList = new LayerList();
    entityList = new EntityList();
    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setFont(Style.getHeaderFont());
    tabPane.add(entityList);
    tabPane.add(mapLayerList);
    tabPane.setMaximumSize(new Dimension(0, 150));

    JSplitPane topRightSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    topRightSplitPanel.setContinuousLayout(true);
    topRightSplitPanel.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setMapPanelSplitter(topRightSplitPanel.getDividerLocation()));
    if (Editor.preferences().getMapPanelSplitter() != 0) {
      topRightSplitPanel.setDividerLocation(Editor.preferences().getMapPanelSplitter());
    }

    topRightSplitPanel.setLeftComponent(mapSelectionPanel);
    topRightSplitPanel.setRightComponent(tabPane);

    mapObjectPanel = new MapObjectInspector();

    JSplitPane rightSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    rightSplitPanel.setMinimumSize(new Dimension(320, 0));
    rightSplitPanel.setBottomComponent(mapObjectPanel);
    rightSplitPanel.setTopComponent(topRightSplitPanel);
    rightSplitPanel.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setSelectionEditSplitter(rightSplitPanel.getDividerLocation()));
    if (Editor.preferences().getSelectionEditSplitter() != 0) {
      rightSplitPanel.setDividerLocation(Editor.preferences().getSelectionEditSplitter());
    }

    return rightSplitPanel;
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();
    bottomTab.setFont(Style.getHeaderFont());

    assetComponent = new AssetList();
    bottomTab.addTab(Resources.strings().get("assettree_assets"), Icons.ASSET, assetComponent);
    bottomTab.addTab(
      Resources.strings().get("assettree_console"), Icons.CONSOLE, new ConsoleComponent());

    bottomPanel.add(StatusBar.create(), BorderLayout.NORTH);
    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    return bottomPanel;
  }

  private static void initPopupMenu(Canvas canvas) {
    canvasPopup = new CanvasPopupMenu();
    addOrphanComponent(canvasPopup);

    canvas.addMouseListener(
      new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          if (SwingUtilities.isRightMouseButton(e)) {
            Editor.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_EDIT);
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

    loadingTheme = true;

    switch (theme) {
      case DARK -> LafManager.install(new OneDarkTheme());
      case LIGHT -> LafManager.install(new IntelliJTheme());
    }

    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setBackground(UIManager.getColor("Panel.background"));
    }
    Editor.preferences().setTheme(theme);
    loadingTheme = false;
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
