package de.gurkenlabs.utiliti.swing.controllers;

import java.awt.Dimension;
import java.util.List;

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
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public class MapList extends JScrollPane implements MapController {
  private final JList<String> list;
  private final DefaultListModel<String> model;
  private int index = -1;

  public MapList() {
    super();
    this.setMinimumSize(new Dimension(80, 0));
    this.setMaximumSize(new Dimension(0, 250));

    this.model = new DefaultListModel<>();

    this.list = new JList<>();
    this.list.setVisibleRowCount(8);
    this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.list.setMaximumSize(new Dimension(0, 250));

    this.list.getSelectionModel().addListSelectionListener(e -> {
      if (Editor.instance().isLoading() || Editor.instance().getMapComponent().isLoading()) {
        return;
      }

      if (this.list.getSelectedIndex() < Editor.instance().getMapComponent().getMaps().size() && this.list.getSelectedIndex() >= 0) {
        this.index = this.list.getSelectedIndex();
        TmxMap map = Editor.instance().getMapComponent().getMaps().get(this.list.getSelectedIndex());
        if (Game.world().environment() != null && Game.world().environment().getMap().equals(map)) {
          return;
        }

        Editor.instance().getMapComponent().loadEnvironment(map);
      }
    });

    this.setViewportView(this.list);
    this.setViewportBorder(null);

    UndoManager.onMapObjectAdded(manager -> this.refresh());
    UndoManager.onMapObjectRemoved(manager -> this.refresh());

    UndoManager.onUndoStackChanged(manager -> this.bind(Editor.instance().getMapComponent().getMaps()));
  }

  @Override
  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, false);
  }

  @Override
  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.model.clear();
      this.index = -1;
    }

    for (TmxMap map : maps) {
      String name = map.getName();
      if (UndoManager.hasChanges(map)) {
        name += " *";
      }

      // update existing strings
      int indexToReplace = getIndexToReplace(map.getName());
      if (indexToReplace != -1) {
        this.model.set(indexToReplace, name);
      } else {
        // add new maps
        this.model.addElement(name);
      }
    }

    // remove maps that are no longer present
    for (int i = 0; i < this.model.getSize(); i++) {
      final String current = this.model.get(i);
      if (current == null || maps.stream().noneMatch(x -> current.startsWith(x.getName()))) {
        this.model.remove(i);
      }
    }

    this.list.setModel(this.model);
    this.list.revalidate();
    this.refresh();
  }

  @Override
  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      list.clearSelection();
    } else {
      if (model.contains(mapName)) {
        list.setSelectedValue(mapName, true);
      }
    }

    this.refresh();
  }

  @Override
  public IMap getCurrentMap() {
    if (this.list.getSelectedIndex() == -1) {
      return null;
    }
    return Editor.instance().getMapComponent().getMaps().get(list.getSelectedIndex());
  }

  @Override
  public void refresh() {
    if (this.index == -1 && this.model.size() > 0) {
      this.list.setSelectedIndex(0);
    } else {
      this.list.setSelectedIndex(this.index);
    }

    UI.getEntityController().refresh();
    UI.getLayerController().refresh();
  }

  private int getIndexToReplace(String mapName) {
    for (int i = 0; i < this.model.getSize(); i++) {
      final String currentName = this.model.get(i);
      if (currentName != null && (currentName.equals(mapName) || currentName.equals(mapName + " *"))) {
        return i;
      }
    }

    return -1;
  }
}
