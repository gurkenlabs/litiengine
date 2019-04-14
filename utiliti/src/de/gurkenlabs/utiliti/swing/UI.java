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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.menus.AddMenu;
import de.gurkenlabs.utiliti.swing.menus.CanvasPopupMenu;
import de.gurkenlabs.utiliti.swing.menus.MainMenuBar;
import de.gurkenlabs.utiliti.swing.panels.MapObjectPanel;

public final class UI {
  private static JScrollBar horizontalScroll;
  private static JScrollBar verticalScroll;
  private static JPopupMenu canvasPopup;
  private static AssetComponent assetComponent;

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

  public static void updateAssets() {
    assetComponent.update();
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

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    Game.screens().display(EditorScreen.instance());

    Style.initSwingComponentStyle();
    Tray.init();
    Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
    Game.window().getRenderComponent().setCursorOffsetX(0);
    Game.window().getRenderComponent().setCursorOffsetY(0);
    setupInterface();
    Game.window().getHostControl().revalidate();

    initialized = true;
  }

  private static void setupInterface() {
    JFrame window = initWindow();

    Canvas canvas = Game.window().getRenderComponent();
    canvas.setFocusable(true);
    canvas.setSize((int) (window.getSize().width * 0.75), window.getSize().height);

    // remove canvas because we want to add a wrapping panel
    window.remove(canvas);

    initPopupMenu(canvas);

    JPanel renderPanel = new JPanel(new BorderLayout());
    renderPanel.add(canvas);
    renderPanel.setMinimumSize(new Dimension(300, 0));
    initScrollBars(renderPanel);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initRenderSplitPanel(renderPanel, window), initRightSplitPanel());
    split.setContinuousLayout(true);
    split.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        Program.preferences().setWidth(window.getWidth());
        Program.preferences().setHeight(window.getHeight());
      }
    });

    split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setMainSplitter(split.getDividerLocation()));

    JPanel rootPanel = new JPanel(new BorderLayout());
    window.setContentPane(rootPanel);

    rootPanel.add(split, BorderLayout.CENTER);
    split.setDividerLocation(Program.preferences().getMainSplitterPosition() != 0 ? Program.preferences().getMainSplitterPosition() : (int) (window.getSize().width * 0.75));

    rootPanel.add(new ToolBar(), BorderLayout.NORTH);

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
          Program.preferences().setFrameState(window.getExtendedState());
        }

        return terminate;
      }
    });

    window.setLocationRelativeTo(null);
    if (Program.preferences().getFrameState() != JFrame.ICONIFIED && Program.preferences().getFrameState() != JFrame.NORMAL) {
      window.setExtendedState(Program.preferences().getFrameState());
    } else if (Program.preferences().getWidth() != 0 && Program.preferences().getHeight() != 0) {
      window.setSize(Program.preferences().getWidth(), Program.preferences().getHeight());
    }

    return window;
  }

  private static Component initRenderSplitPanel(JPanel renderPanel, JFrame window) {
    JSplitPane renderSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, initBottomPanel());
    if (Program.preferences().getBottomSplitter() != 0) {
      renderSplitPanel.setDividerLocation(Program.preferences().getBottomSplitter());
    } else {
      renderSplitPanel.setDividerLocation((int) (window.getSize().height * 0.75));
    }

    renderSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setBottomSplitter(renderSplitPanel.getDividerLocation()));
    renderSplitPanel.setContinuousLayout(true);
    return renderSplitPanel;
  }

  private static Component initRightSplitPanel() {
    final MapObjectPanel mapObjectPanel = new MapObjectPanel();
    final MapSelectionPanel mapSelectionPanel = new MapSelectionPanel();
    EditorScreen.instance().setMapEditorPanel(mapObjectPanel);
    EditorScreen.instance().setMapSelectionPanel(mapSelectionPanel);

    JSplitPane rightSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    rightSplitPanel.setMinimumSize(new Dimension(300, 0));
    rightSplitPanel.setBottomComponent(mapObjectPanel);
    rightSplitPanel.setTopComponent(mapSelectionPanel);
    rightSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setSelectionEditSplitter(rightSplitPanel.getDividerLocation()));
    if (Program.preferences().getSelectionEditSplitter() != 0) {
      rightSplitPanel.setDividerLocation(Program.preferences().getSelectionEditSplitter());
    }

    return rightSplitPanel;
  }

  private static JPanel initBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    JTabbedPane bottomTab = new JTabbedPane();

    assetComponent = new AssetComponent();
    bottomTab.addTab(Resources.strings().get("assettree_assets"), assetComponent);
    bottomTab.addTab(Resources.strings().get("assettree_console"), new ConsoleComponent());
    bottomTab.setIconAt(0, Icons.ASSET);
    bottomTab.setIconAt(1, Icons.CONSOLE);

    bottomPanel.add(bottomTab, BorderLayout.CENTER);

    bottomPanel.add(StatusBar.create(), BorderLayout.SOUTH);
    return bottomPanel;
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

  private static void initPopupMenu(Canvas canvas) {
    AddMenu.initPopup();
    canvasPopup = new CanvasPopupMenu();

    canvas.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          canvasPopup.show(canvas, e.getX(), e.getY());
        }
      }
    });
  }
}
