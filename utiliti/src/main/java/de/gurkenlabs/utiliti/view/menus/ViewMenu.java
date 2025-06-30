package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style.Theme;
import de.gurkenlabs.utiliti.view.components.UI;
import de.gurkenlabs.utiliti.view.dialogs.GridEditPanel;
import de.gurkenlabs.utiliti.view.renderers.GridRenderer;
import de.gurkenlabs.utiliti.view.renderers.Renderers;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial") public final class ViewMenu extends JMenu {
  public ViewMenu() {
    super(Resources.strings().get("menu_view"));
    this.setMnemonic('V');

    JMenu themeMenu = new JMenu(Resources.strings().get("menu_view_theme"));
    ButtonGroup themegroup = new ButtonGroup();
    for (Theme theme : Theme.values()) {
      JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(Resources.strings().get("menu_view_theme_" + theme.name().toLowerCase()));
      menuItem.setSelected(Editor.preferences().getTheme() == theme);
      menuItem.addActionListener(e -> UI.setTheme(theme));
      themegroup.add(menuItem);
      themeMenu.add(menuItem);
    }

    JCheckBoxMenuItem clampToMap = new JCheckBoxMenuItem(Resources.strings().get("menu_view_clampMap"));
    clampToMap.setState(Editor.preferences().clampToMap());
    clampToMap.addItemListener(e -> Editor.preferences().setClampToMap(clampToMap.getState()));

    JCheckBoxMenuItem snapToPixels = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapPixels"));
    snapToPixels.setState(Editor.preferences().snapToPixels());
    snapToPixels.addItemListener(e -> {
      Editor.preferences().setSnapToPixels(snapToPixels.getState());
      UI.getInspector().refresh();
      UI.getInspector().bind(Editor.instance().getMapComponent().getFocusedMapObject());
    });

    JCheckBoxMenuItem snapToGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapGrid"));
    snapToGrid.setState(Editor.preferences().snapToGrid());
    snapToGrid.addItemListener(e -> Editor.preferences().setSnapToGrid(snapToGrid.getState()));

    JCheckBoxMenuItem renderGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showGrid"));
    configureCustomCheckBoxMenuItem(renderGrid, Editor.preferences().showGrid());
    renderGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
    renderGrid.addItemListener(e -> Editor.preferences().setShowGrid(renderGrid.getState()));

    JCheckBoxMenuItem renderCollision = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showCollisionBoxes"));
    configureCustomCheckBoxMenuItem(renderCollision, Editor.preferences().renderBoundingBoxes());
    renderCollision.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
    renderCollision.addItemListener(e -> Editor.preferences().setRenderBoundingBoxes(renderCollision.getState()));

    JCheckBoxMenuItem renderCustomMapObjects = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showCustomMapObjects"));
    configureCustomCheckBoxMenuItem(renderCustomMapObjects, Editor.preferences().renderCustomMapObjects());
    renderCustomMapObjects.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
    renderCustomMapObjects.addItemListener(e -> Editor.preferences().setRenderCustomMapObjects(renderCustomMapObjects.getState()));

    JCheckBoxMenuItem renderNames = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showNames"));
    configureCustomCheckBoxMenuItem(renderNames, Editor.preferences().renderNames());
    renderNames.addItemListener(e -> Editor.preferences().setRenderNames(renderNames.getState()));

    JCheckBoxMenuItem renderMapIds = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showMapIds"));
    configureCustomCheckBoxMenuItem(renderMapIds, Editor.preferences().renderMapIds());
    renderMapIds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
    renderMapIds.addItemListener(e -> Editor.preferences().setRenderMapIds(renderMapIds.getState()));

    JMenuItem setGrid = new JMenuItem(Resources.strings().get("menu_view_gridSettings"));
    setGrid.addActionListener(a -> {
      GridEditPanel panel =
        new GridEditPanel(Editor.preferences().getGridLineWidth(), Editor.preferences().getGridColor(), Editor.preferences().getSnapDivision());
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_view_gridSettings"),
        JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        Editor.preferences().setGridColor(ColorHelper.encode(panel.getGridColor()));
        Editor.preferences().setGridLineWidth(panel.getStrokeWidth());
        Editor.preferences().setSnapDivision(panel.getSnapDivision());
        Renderers.get(GridRenderer.class).clearCache();
      }
    });

    JMenuItem zoomIn = new JMenuItem(Resources.strings().get("menu_view_zoomIn"));
    zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
    zoomIn.addActionListener(a -> Zoom.in());

    JMenuItem zoomOut = new JMenuItem(Resources.strings().get("menu_view_zoomOut"));
    zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
    zoomOut.addActionListener(a -> Zoom.out());

    JMenuItem centerFocus = new JMenuItem(Resources.strings().get("menu_view_center"));
    centerFocus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
    centerFocus.addActionListener(a -> Editor.instance().getMapComponent().centerCameraOnFocus());
    centerFocus.setEnabled(false);

    JMenuItem centerMap = new JMenuItem(Resources.strings().get("menu_view_centermap"));
    centerMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));
    centerMap.addActionListener(a -> Editor.instance().getMapComponent().centerCameraOnMap());

    Editor.instance().getMapComponent().onFocusChanged(mo -> centerFocus.setEnabled(mo != null));

    this.add(themeMenu);
    this.addSeparator();
    this.add(renderGrid);
    this.add(renderCollision);
    this.add(renderCustomMapObjects);
    this.add(renderMapIds);
    this.add(renderNames);
    this.addSeparator();
    this.add(zoomIn);
    this.add(zoomOut);
    this.add(centerFocus);
    this.add(centerMap);
    this.addSeparator();
    this.add(clampToMap);
    this.add(snapToPixels);
    this.add(snapToGrid);
    this.addSeparator();
    this.add(setGrid);
  }

  private void configureCustomCheckBoxMenuItem(JCheckBoxMenuItem menuItem, boolean initialState) {
    menuItem.setState(initialState);
    menuItem.setIcon(initialState ? Icons.SHOW_24 : Icons.HIDE_24);
    menuItem.addItemListener(e -> menuItem.setIcon(menuItem.getState() ? Icons.SHOW_24 : Icons.HIDE_24));
  }
}
