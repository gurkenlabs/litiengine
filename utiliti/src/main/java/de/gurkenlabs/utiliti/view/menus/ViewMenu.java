package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Zoom;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.view.components.UI;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

@SuppressWarnings("serial") public final class ViewMenu extends JMenu {
  public ViewMenu() {
    super(Resources.strings().get("menu_view"));
    this.setMnemonic(this.getText().charAt(0));

    JCheckBoxMenuItem clampToMap = new JCheckBoxMenuItem(Resources.strings().get("menu_view_clampMap"));
    clampToMap.setIcon(Icons.CLAMP_MAP_16);
    clampToMap.setState(Editor.preferences().clampToMap());
    clampToMap.addItemListener(e -> Editor.preferences().setClampToMap(clampToMap.getState()));

    JCheckBoxMenuItem snapToPixels = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapPixels"));
    snapToPixels.setIcon(Icons.SNAP_PIXELS_16);
    snapToPixels.setState(Editor.preferences().snapToPixels());
    snapToPixels.addItemListener(e -> {
      Editor.preferences().setSnapToPixels(snapToPixels.getState());
      UI.getInspector().refresh();
      Editor.instance().getMapComponent().refreshInspector();
    });

    JCheckBoxMenuItem snapToGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapGrid"));
    snapToGrid.setIcon(Icons.SNAP_GRID_16);
    snapToGrid.setState(Editor.preferences().snapToGrid());
    snapToGrid.addItemListener(e -> {
      Editor.preferences().setSnapToGrid(snapToGrid.getState());
      syncViewportToolbar();
    });

    JCheckBoxMenuItem renderGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showGrid"));
    configureCustomCheckBoxMenuItem(renderGrid, Editor.preferences().showGrid(), Icons.GRID_16);
    KeyBindings.bind(renderGrid, Command.SHOW_GRID);
    renderGrid.addItemListener(e -> {
      Editor.preferences().setShowGrid(renderGrid.getState());
      syncViewportToolbar();
    });

    JCheckBoxMenuItem renderCollision = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showCollisionBoxes"));
    configureCustomCheckBoxMenuItem(renderCollision, Editor.preferences().renderBoundingBoxes(), Icons.COLLISIONBOX_16);
    KeyBindings.bind(renderCollision, Command.SHOW_COLLISION);
    renderCollision.addItemListener(e -> {
      Editor.preferences().setRenderBoundingBoxes(renderCollision.getState());
      syncViewportToolbar();
    });

    JCheckBoxMenuItem renderCustomMapObjects = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showCustomMapObjects"));
    configureCustomCheckBoxMenuItem(renderCustomMapObjects, Editor.preferences().renderCustomMapObjects(), Icons.ENTITY_16);
    KeyBindings.bind(renderCustomMapObjects, Command.SHOW_CUSTOM_OBJECTS);
    renderCustomMapObjects.addItemListener(e -> Editor.preferences().setRenderCustomMapObjects(renderCustomMapObjects.getState()));

    JCheckBoxMenuItem renderNames = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showNames"));
    configureCustomCheckBoxMenuItem(renderNames, Editor.preferences().renderNames(), Icons.SHOW_NAMES_16);
    renderNames.addItemListener(e -> Editor.preferences().setRenderNames(renderNames.getState()));

    JCheckBoxMenuItem renderMapIds = new JCheckBoxMenuItem(Resources.strings().get("menu_view_showMapIds"));
    configureCustomCheckBoxMenuItem(renderMapIds, Editor.preferences().renderMapIds(), Icons.MAP_IDS_16);
    KeyBindings.bind(renderMapIds, Command.SHOW_MAP_IDS);
    renderMapIds.addItemListener(e -> Editor.preferences().setRenderMapIds(renderMapIds.getState()));

    JMenuItem zoomIn = new JMenuItem(Resources.strings().get("menu_view_zoomIn"), Icons.ZOOM_IN_16);
    KeyBindings.bind(zoomIn, Command.ZOOM_IN);
    zoomIn.addActionListener(a -> Zoom.in());

    JMenuItem zoomOut = new JMenuItem(Resources.strings().get("menu_view_zoomOut"), Icons.ZOOM_OUT_16);
    KeyBindings.bind(zoomOut, Command.ZOOM_OUT);
    zoomOut.addActionListener(a -> Zoom.out());

    JMenuItem centerFocus = new JMenuItem(Resources.strings().get("menu_view_center"), Icons.SPAWNPOINT_16);
    KeyBindings.bind(centerFocus, Command.CENTER_FOCUS);
    centerFocus.addActionListener(a -> Editor.instance().getMapComponent().centerCameraOnFocus());
    centerFocus.setEnabled(false);

    JMenuItem centerMap = new JMenuItem(Resources.strings().get("menu_view_centermap"), Icons.MAP_16);
    KeyBindings.bind(centerMap, Command.CENTER_MAP);
    centerMap.addActionListener(a -> Editor.instance().getMapComponent().centerCameraOnMap());

    Editor.instance().getMapComponent().onFocusChanged(mo -> centerFocus.setEnabled(mo != null));

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
  }

  private void configureCustomCheckBoxMenuItem(
      JCheckBoxMenuItem menuItem, boolean initialState, javax.swing.Icon icon) {
    menuItem.setState(initialState);
    menuItem.setIcon(icon);
  }

  private static void syncViewportToolbar() {
    if (UI.getViewportToolbar() != null) {
      UI.getViewportToolbar().syncPreferenceButtons();
    }
  }
}
