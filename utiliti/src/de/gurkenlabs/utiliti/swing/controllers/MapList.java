package de.gurkenlabs.utiliti.swing.controllers;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapController;
import de.gurkenlabs.utiliti.swing.MapListCellRenderer;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.menus.MapPopupMenu;

@SuppressWarnings("serial")
public class MapList extends JScrollPane implements MapController {
  private final DefaultListModel<IMap> model;

  private static MapPopupMenu mapPopupMenu;
  private static JList<IMap> list = new JList<>();

  public MapList() {
    super();
    this.setMinimumSize(new Dimension(80, 0));
    this.setMaximumSize(new Dimension(0, 250));

    this.model = new DefaultListModel<>();
    list.setVisibleRowCount(8);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new MapListCellRenderer());
    list.setMaximumSize(new Dimension(0, 250));
    list.setSelectedIndex(0);
    list.getSelectionModel().addListSelectionListener(e -> {
      if (Editor.instance().isLoading() || Editor.instance().getMapComponent().isLoading()) {
        return;
      }
      Optional<TmxMap> map = Editor.instance().getMapComponent().getMaps().stream().filter(m -> m == list.getSelectedValue()).findFirst();
      if ((map.isPresent() && Game.world().environment() != null && Game.world().environment().getMap() == map.get()) || !map.isPresent()) {
        return;
      }
      Editor.instance().getMapComponent().loadEnvironment(map.get());
    });

    this.setViewportView(list);
    this.setViewportBorder(null);
    initPopupMenu();

    UndoManager.onMapObjectAdded(manager -> this.refresh());
    UndoManager.onMapObjectRemoved(manager -> this.refresh());

    UndoManager.onUndoStackChanged(manager -> this.bind(Editor.instance().getMapComponent().getMaps(), false));
  }

  private static void initPopupMenu() {
    mapPopupMenu = new MapPopupMenu();
    UI.addOrphanComponent(mapPopupMenu);

    list.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          list.setSelectedIndex(list.locationToIndex(e.getPoint()));
          mapPopupMenu.show(list, e.getX(), e.getY());
        }
      }
    });
  }

  @Override
  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, true);
  }

  @Override
  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.model.clear();
      list.setSelectedIndex(0);
    }
    int selectedIndex = list.getSelectedIndex();
    for (TmxMap map : maps) {
      if (!this.model.contains(map)) {
        this.model.addElement(map);
      }
    }

    // remove maps that are no longer present
    for (int i = 0; i < this.model.getSize(); i++) {
      final IMap current = this.model.get(i);
      if (this.model.get(i) == null || maps.stream().noneMatch(x -> x == current)) {
        this.model.remove(i);
      }
    }

    list.setModel(this.model);
    list.setSelectedIndex(selectedIndex);

    this.refresh();
  }

  @Override
  public void setSelection(TmxMap map) {
    if (map == null) {
      list.clearSelection();
    } else {
      if (this.model.contains(map)) {
        list.setSelectedValue(map, true);
      }
    }
    this.refresh();
  }

  @Override
  public TmxMap getCurrentMap() {
    if (list.getSelectedIndex() == -1) {
      return null;
    }
    Optional<TmxMap> map = Editor.instance().getMapComponent().getMaps().stream().filter(m -> m.equals(list.getSelectedValue())).findFirst();
    return map.isPresent() ? map.get() : null;
  }

  @Override
  public void refresh() {
    list.revalidate();
    list.repaint();

    UI.getEntityController().refresh();
    UI.getLayerController().refresh();
  }
}
