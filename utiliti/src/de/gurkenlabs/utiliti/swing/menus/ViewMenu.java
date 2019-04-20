package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.dialogs.GridEditPanel;

@SuppressWarnings("serial")
public final class ViewMenu extends JMenu {
  public ViewMenu() {
    super(Resources.strings().get("menu_view"));
    this.setMnemonic('V');

    JCheckBoxMenuItem snapToPixels = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapPixels"));
    snapToPixels.setState(Program.preferences().isSnapPixels());
    snapToPixels.addItemListener(e -> {
      Program.preferences().setSnapPixels(snapToPixels.getState());
      UI.getMapObjectPanel().updateSpinnerModels();
      UI.getMapObjectPanel().bind(EditorScreen.instance().getMapComponent().getFocusedMapObject());
    });

    JCheckBoxMenuItem snapToGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_snapGrid"));
    snapToGrid.setState(Program.preferences().isSnapGrid());
    snapToGrid.addItemListener(e -> Program.preferences().setSnapGrid(snapToGrid.getState()));

    JCheckBoxMenuItem renderGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_view_renderGrid"));
    renderGrid.setState(Program.preferences().isShowGrid());
    renderGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
    renderGrid.addItemListener(e -> Program.preferences().setShowGrid(renderGrid.getState()));

    JCheckBoxMenuItem renderCollision = new JCheckBoxMenuItem(Resources.strings().get("menu_view_renderCollisionBoxes"));
    renderCollision.setState(Program.preferences().isRenderBoundingBoxes());
    renderCollision.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK));
    renderCollision.addItemListener(e -> Program.preferences().setRenderBoundingBoxes(renderCollision.getState()));

    JCheckBoxMenuItem renderCustomMapObjects = new JCheckBoxMenuItem(Resources.strings().get("menu_view_renderCustomMapObjects"));
    renderCustomMapObjects.setState(Program.preferences().isRenderCustomMapObjects());
    renderCustomMapObjects.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK));
    renderCustomMapObjects.addItemListener(e -> Program.preferences().setRenderCustomMapObjects(renderCustomMapObjects.getState()));

    JCheckBoxMenuItem renderNames = new JCheckBoxMenuItem(Resources.strings().get("menu_view_renderNames"));
    renderNames.setState(Program.preferences().isRenderNames());
    renderNames.addItemListener(e -> Program.preferences().setRenderNames(renderNames.getState()));

    JCheckBoxMenuItem renderMapIds = new JCheckBoxMenuItem(Resources.strings().get("menu_view_renderMapIds"));
    renderMapIds.setState(Program.preferences().isRenderMapIds());
    renderMapIds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
    renderMapIds.addItemListener(e -> Program.preferences().setRenderMapIds(renderMapIds.getState()));

    JMenuItem setGrid = new JMenuItem(Resources.strings().get("menu_view_gridSettings"));
    setGrid.addActionListener(a -> {
      GridEditPanel panel = new GridEditPanel(Program.preferences().getGridLineWidth(), Program.preferences().getGridColor());
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_view_gridSettings"), JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        Program.preferences().setGridColor(ColorHelper.encode(panel.getGridColor()));
        Program.preferences().setGridLineWidth(panel.getStrokeWidth());
      }
    });

    JMenuItem zoomIn = new JMenuItem(Resources.strings().get("menu_view_zoomIn"));
    zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Event.CTRL_MASK));
    zoomIn.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomIn());

    JMenuItem zoomOut = new JMenuItem(Resources.strings().get("menu_view_zoomOut"));
    zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.CTRL_MASK));
    zoomOut.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomOut());

    JMenuItem centerFocus = new JMenuItem(Resources.strings().get("menu_view_center"));
    centerFocus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
    centerFocus.addActionListener(a -> EditorScreen.instance().getMapComponent().centerCameraOnFocus());
    centerFocus.setEnabled(false);

    JMenuItem centerMap = new JMenuItem(Resources.strings().get("menu_view_centermap"));
    centerMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, Event.CTRL_MASK));
    centerMap.addActionListener(a -> EditorScreen.instance().getMapComponent().centerCameraOnMap());

    EditorScreen.instance().getMapComponent().onFocusChanged(mo -> {
      centerFocus.setEnabled(mo != null);
    });

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
    this.add(snapToPixels);
    this.add(snapToGrid);
    this.addSeparator();
    this.add(setGrid);
  }
}
