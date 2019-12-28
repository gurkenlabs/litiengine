package de.gurkenlabs.utiliti.swing.controllers;

import java.awt.Dimension;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapController;
import de.gurkenlabs.utiliti.swing.MapListCellRenderer;
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public class MapList extends JScrollPane implements MapController {
  private final JList<IMap> list;
  private final DefaultListModel<IMap> model;

  public MapList() {
    super();
    this.setMinimumSize(new Dimension(80, 0));
    this.setMaximumSize(new Dimension(0, 250));

    this.model = new DefaultListModel<>();

    this.list = new JList<>();
    this.list.setVisibleRowCount(8);
    this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.setCellRenderer(new MapListCellRenderer());
    this.list.setMaximumSize(new Dimension(0, 250));
    this.list.setSelectedIndex(0);
    this.list.getSelectionModel().addListSelectionListener(e -> {
      if (Editor.instance().isLoading() || Editor.instance().getMapComponent().isLoading()) {
        return;
      }
      Optional<TmxMap> map = Editor.instance().getMapComponent().getMaps().stream().filter(m -> m == this.list.getSelectedValue()).findFirst();
      if ((map.isPresent() && Game.world().environment() != null && Game.world().environment().getMap() == map.get()) || !map.isPresent()) {
        return;
      }
      Editor.instance().getMapComponent().loadEnvironment(map.get());
    });

    this.setViewportView(this.list);
    this.setViewportBorder(null);

    UndoManager.onMapObjectAdded(manager -> this.refresh());
    UndoManager.onMapObjectRemoved(manager -> this.refresh());

    UndoManager.onUndoStackChanged(manager -> this.bind(Editor.instance().getMapComponent().getMaps(), false));
  }

  @Override
  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, true);
  }

  @Override
  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.model.clear();
      this.list.setSelectedIndex(0);
    }
    int selectedIndex = this.list.getSelectedIndex();
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

    this.list.setModel(this.model);
    this.list.setSelectedIndex(selectedIndex);

    this.refresh();
  }

  @Override
  public void setSelection(TmxMap map) {
    if (map == null) {
      this.list.clearSelection();
    } else {
      if (this.model.contains(map)) {
        this.list.setSelectedValue(map, true);
      }
    }
    this.refresh();
  }

  @Override
  public TmxMap getCurrentMap() {
    if (this.list.getSelectedIndex() == -1) {
      return null;
    }
    Optional<TmxMap> map = Editor.instance().getMapComponent().getMaps().stream().filter(m -> m.getName().equals(this.list.getSelectedValue())).findFirst();
    return map.isPresent() ? map.get() : null;
  }

  @Override
  public void refresh() {
    this.list.revalidate();
    UI.getEntityController().refresh();
    UI.getLayerController().refresh();
  }
}
