package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.Controller;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.EntityController;
import de.gurkenlabs.utiliti.components.LayerController;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.components.MapController;
import de.gurkenlabs.utiliti.components.PropertyInspector;
import de.gurkenlabs.utiliti.swing.controllers.AssetList;
import de.gurkenlabs.utiliti.swing.controllers.EntityList;
import de.gurkenlabs.utiliti.swing.controllers.LayerList;
import de.gurkenlabs.utiliti.swing.controllers.MapList;
import de.gurkenlabs.utiliti.swing.menus.CanvasPopupMenu;
import de.gurkenlabs.utiliti.swing.menus.MainMenuBar;
import de.gurkenlabs.utiliti.swing.panels.MapObjectInspector;

public final class UI {
  private static final Logger log = Logger.getLogger(UI.class.getName());
  private static JScrollBar horizontalScroll;
  private static JScrollBar verticalScroll;
  private static JPopupMenu canvasPopup;
  private static AssetList assetComponent;

  private static MapObjectInspector mapObjectPanel;
  private static MapList mapSelectionPanel;
  private static LayerList mapLayerList;
  private static EntityList entityList;

  private static boolean initialized;

  private UI() {
  }

  public static JScrollBar getHorizontalScrollBar() {
    return horizontalScroll;
  }

  public static JScrollBar getVerticalcrollBar() {
    return verticalScroll;
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
    String resourceFile = Editor.instance().getCurrentResourceFile() != null ? Editor.instance().getCurrentResourceFile() : "";
    if (Editor.instance().getChangedMaps().isEmpty() && !Editor.instance().isUnsavedProject()) {
      return true;
    }

    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_saveProjectMessage") + "\n" + resourceFile, Resources.strings().get("hud_saveProject"), JOptionPane.YES_NO_CANCEL_OPTION);

    if (n == JOptionPane.YES_OPTION) {
      Editor.instance().save(false);
    }

    return n != JOptionPane.CANCEL_OPTION && n != JOptionPane.CLOSED_OPTION;
  }

  public static boolean showRevertWarning() {
    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_revertChangesMessage"), Resources.strings().get("hud_revertChanges"), JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    Game.screens().display(Editor.instance());

    initSwingComponentStyle();
    Tray.init();
    Game.window().cursor().set(Cursors.DEFAULT, 0, 0);
    Game.window().cursor().setOffsetX(0);
    Game.window().cursor().setOffsetY(0);
    setupInterface();
    Game.window().getHostControl().revalidate();

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

  private static void setupInterface() {
    JFrame window = initWindow();

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (window.getSize().width * 0.75), window.getSize().height);

    // remove canvas because we want to add a wrapping panel
    window.remove(canvas);

    JPanel renderPanel = new JPanel(new BorderLayout());
    renderPanel.add(canvas);
    renderPanel.setMinimumSize(new Dimension(300, 0));
    initScrollBars(renderPanel);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initRenderSplitPanel(renderPanel, window), initRightSplitPanel());
    split.setContinuousLayout(true);
    split.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        Editor.preferences().setWidth(window.getWidth());
        Editor.preferences().setHeight(window.getHeight());
      }
    });

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Editor.preferences().setMainSplitter(split.getDividerLocation()));

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);

    rootPanel.add(split, BorderLayout.CENTER);
    split.setDividerLocation(Editor.preferences().getMainSplitterPosition() != 0 ? Editor.preferences().getMainSplitterPosition() : (int) (window.getSize().width * 0.75));

    initPopupMenu(canvas);
    window.setJMenuBar(new MainMenuBar());
  }

  private static JFrame initWindow() {
    JFrame window = ((JFrame) Game.window().getHostControl());
    window.setResizable(true);

    Game.addGameListener(new GameListener() {
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
    if (Editor.preferences().getFrameState() != java.awt.Frame.ICONIFIED && Editor.preferences().getFrameState() != java.awt.Frame.NORMAL) {
      window.setExtendedState(Editor.preferences().getFrameState());
    } else if (Editor.preferences().getWidth() != 0 && Editor.preferences().getHeight() != 0) {
      window.setSize(Editor.preferences().getWidth(), Editor.preferences().getHeight());
    }

    return window;
  }

  private static Component initRenderSplitPanel(JPanel renderPanel, JFrame window) {
    JSplitPane renderSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, initBottomPanel());
    if (Editor.preferences().getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(Editor.preferences().getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (window.getSize().height * 0.75));
    }

    renderSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Editor.preferences().setBottomSplitter(renderSplitPanel.getDividerLocation()));
    renderSplitPanel.setContinuousLayout(true);
    return renderSplitPanel;
  }

  private static Component initRightSplitPanel() {
    mapSelectionPanel = new MapList();

    mapLayerList = new LayerList();
    entityList = new EntityList();
    JTabbedPane tabPane = new JTabbedPane();
    tabPane.add(entityList);
    tabPane.add(mapLayerList);
    tabPane.setMaximumSize(new Dimension(0, 150));

    JSplitPane topRightSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    topRightSplitPanel.setContinuousLayout(true);
    topRightSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Editor.preferences().setMapPanelSplitter(topRightSplitPanel.getDividerLocation()));
    if (Editor.preferences().getMapPanelSplitter() != 0) {
      topRightSplitPanel.setDividerLocation(Editor.preferences().getMapPanelSplitter());
    }

    topRightSplitPanel.setLeftComponent(mapSelectionPanel);
    topRightSplitPanel.setRightComponent(tabPane);

    mapObjectPanel = new MapObjectInspector();

    JSplitPane rightSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    rightSplitPanel.setMinimumSize(new Dimension(300, 0));
    rightSplitPanel.setBottomComponent(mapObjectPanel);
    rightSplitPanel.setTopComponent(topRightSplitPanel);
    rightSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Editor.preferences().setSelectionEditSplitter(rightSplitPanel.getDividerLocation()));
    if (Editor.preferences().getSelectionEditSplitter() != 0) {
      rightSplitPanel.setDividerLocation(Editor.preferences().getSelectionEditSplitter());
    }

    return rightSplitPanel;
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();

    assetComponent = new AssetList();
    bottomTab.addTab(Resources.strings().get("assettree_assets"), assetComponent);
    bottomTab.addTab(Resources.strings().get("assettree_console"), new ConsoleComponent());
    bottomTab.setIconAt(0, Icons.ASSET);
    bottomTab.setIconAt(1, Icons.CONSOLE);

    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    bottomPanel.add(StatusBar.create(), BorderLayout.SOUTH);
    return bottomPanel;
  }

  private static void initScrollBars(JPanel renderPane) {
    horizontalScroll = new JScrollBar(java.awt.Adjustable.HORIZONTAL);
    renderPane.add(horizontalScroll, BorderLayout.SOUTH);
    verticalScroll = new JScrollBar(java.awt.Adjustable.VERTICAL);
    renderPane.add(verticalScroll, BorderLayout.EAST);

    horizontalScroll.addAdjustmentListener(e -> {
      if (Editor.instance().getMapComponent().isLoading()) {
        return;
      }

      Point2D newFocus = new Point2D.Double(horizontalScroll.getValue(), Game.world().camera().getFocus().getY());
      Game.world().camera().setFocus(newFocus);
    });

    verticalScroll.addAdjustmentListener(e -> {
      if (Editor.instance().getMapComponent().isLoading()) {
        return;
      }
      Point2D newFocus = new Point2D.Double(Game.world().camera().getFocus().getX(), verticalScroll.getValue());
      Game.world().camera().setFocus(newFocus);
    });
  }

  private static void initPopupMenu(Canvas canvas) {
    canvasPopup = new CanvasPopupMenu();

    canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          Editor.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_EDIT);
          canvasPopup.show(canvas, e.getX(), e.getY());
        }
      }
    });
  }

  private static void initSwingComponentStyle() {
    try {
      javax.swing.JComponent.setDefaultLocale(Locale.getDefault());
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
      setDefaultSwingFont(new FontUIResource(Style.getDefaultFont()));
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  private static void setDefaultSwingFont(FontUIResource font) {
    Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();

      Object value = UIManager.get(key);

      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put(key, font);
      }
    }
  }
}
