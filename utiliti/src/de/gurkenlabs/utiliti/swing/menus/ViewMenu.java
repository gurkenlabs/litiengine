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
import de.gurkenlabs.utiliti.swing.dialogs.GridEditPanel;

@SuppressWarnings("serial")
public final class ViewMenu extends JMenu {
  public ViewMenu() {
    super(Resources.strings().get("menu_view"));
    this.setMnemonic('V');
    
    JCheckBoxMenuItem snapToPixels = new JCheckBoxMenuItem(Resources.strings().get("menu_snapPixels"));
    snapToPixels.setState(Program.getUserPreferences().isSnapPixels());
    snapToPixels.addItemListener(e -> {
      Program.getUserPreferences().setSnapPixels(snapToPixels.getState());
      EditorScreen.instance().getMapObjectPanel().updateSpinnerModels();
      EditorScreen.instance().getMapObjectPanel().bind(EditorScreen.instance().getMapComponent().getFocusedMapObject());
    });

    JCheckBoxMenuItem snapToGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_snapGrid"));
    snapToGrid.setState(Program.getUserPreferences().isSnapGrid());
    snapToGrid.addItemListener(e -> Program.getUserPreferences().setSnapGrid(snapToGrid.getState()));

    JCheckBoxMenuItem renderGrid = new JCheckBoxMenuItem(Resources.strings().get("menu_renderGrid"));
    renderGrid.setState(Program.getUserPreferences().isShowGrid());
    renderGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK));
    renderGrid.addItemListener(e -> Program.getUserPreferences().setShowGrid(renderGrid.getState()));

    JCheckBoxMenuItem renderCollision = new JCheckBoxMenuItem(Resources.strings().get("menu_renderCollisionBoxes"));
    renderCollision.setState(Program.getUserPreferences().isRenderBoundingBoxes());
    renderCollision.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK));
    renderCollision.addItemListener(e -> Program.getUserPreferences().setRenderBoundingBoxes(renderCollision.getState()));

    JCheckBoxMenuItem renderCustomMapObjects = new JCheckBoxMenuItem(Resources.strings().get("menu_renderCustomMapObjects"));
    renderCustomMapObjects.setState(Program.getUserPreferences().isRenderCustomMapObjects());
    renderCustomMapObjects.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK));
    renderCustomMapObjects.addItemListener(e -> Program.getUserPreferences().setRenderCustomMapObjects(renderCustomMapObjects.getState()));

    JCheckBoxMenuItem renderMapIds = new JCheckBoxMenuItem(Resources.strings().get("menu_renderMapIds"));
    renderMapIds.setState(Program.getUserPreferences().isRenderMapIds());
    renderMapIds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
    renderMapIds.addItemListener(e -> Program.getUserPreferences().setRenderMapIds(renderMapIds.getState()));

    JMenuItem setGrid = new JMenuItem(Resources.strings().get("menu_gridSettings"));
    setGrid.addActionListener(a -> {
      GridEditPanel panel = new GridEditPanel(Program.getUserPreferences().getGridLineWidth(), Program.getUserPreferences().getGridColor());
      int option = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), panel, Resources.strings().get("menu_gridSettings"), JOptionPane.PLAIN_MESSAGE);
      if (option == JOptionPane.OK_OPTION) {
        Program.getUserPreferences().setGridColor(ColorHelper.encode(panel.getGridColor()));
        Program.getUserPreferences().setGridLineWidth(panel.getStrokeWidth());
      }
    });

    JMenuItem zoomIn = new JMenuItem(Resources.strings().get("menu_zoomIn"));
    zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Event.CTRL_MASK));
    zoomIn.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomIn());

    JMenuItem zoomOut = new JMenuItem(Resources.strings().get("menu_zoomOut"));
    zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.CTRL_MASK));
    zoomOut.addActionListener(a -> EditorScreen.instance().getMapComponent().zoomOut());

    this.add(snapToPixels);
    this.add(snapToGrid);
    this.add(renderGrid);
    this.add(renderCollision);
    this.add(renderCustomMapObjects);
    this.add(renderMapIds);
    this.add(setGrid);
    this.addSeparator();
    this.add(zoomIn);
    this.add(zoomOut);
  }
}
