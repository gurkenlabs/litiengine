package de.gurkenlabs.utiliti.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public class MapSelectionPanel extends JSplitPane {
  private final JList<String> mapList;
  private final DefaultListModel<String> model;
  private final JScrollPane mapScrollPane;

  public MapSelectionPanel() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setMapPanelSplitter(this.getDividerLocation()));
    if (Program.preferences().getMapPanelSplitter() != 0) {
      this.setDividerLocation(Program.preferences().getMapPanelSplitter());
    }

    this.setMaximumSize(new Dimension(0, 250));
    setContinuousLayout(true);
    mapScrollPane = new JScrollPane();
    mapScrollPane.setMinimumSize(new Dimension(80, 0));
    mapScrollPane.setMaximumSize(new Dimension(0, 250));
    this.setLeftComponent(mapScrollPane);

    this.model = new DefaultListModel<>();

    this.mapList = new JList<>();
    this.mapList.setModel(this.model);
    this.mapList.setVisibleRowCount(8);
    this.mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.mapList.setMaximumSize(new Dimension(0, 250));

    this.mapList.getSelectionModel().addListSelectionListener(e -> {
      if (EditorScreen.instance().isLoading() || EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }

      if (this.mapList.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && this.mapList.getSelectedIndex() >= 0) {
        TmxMap map = EditorScreen.instance().getMapComponent().getMaps().get(this.mapList.getSelectedIndex());
        if (Game.world().environment() != null && Game.world().environment().getMap().equals(map)) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(map);
      }
    });

    mapScrollPane.setViewportView(this.mapList);
    mapScrollPane.setViewportBorder(null);

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setFont(tabPane.getFont().deriveFont(Font.BOLD));
    tabPane.add(UI.getEntityList());
    tabPane.add(UI.getMapLayerList());
    tabPane.setMaximumSize(new Dimension(0, 150));

    this.setRightComponent(tabPane);

    UndoManager.onMapObjectAdded(manager -> {
      this.updateComponents();

    });

    UndoManager.onMapObjectRemoved(manager -> {
      this.updateComponents();
    });

    UndoManager.onUndoStackChanged(manager -> this.bind(EditorScreen.instance().getMapComponent().getMaps()));
  }

  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, false);
  }

  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.model.clear();
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

    mapList.revalidate();
    this.updateComponents();
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
    } else {
      if (model.contains(mapName)) {
        mapList.setSelectedValue(mapName, true);
      }
    }

    this.updateComponents();
  }

  public IMap getCurrentMap() {
    if (this.mapList.getSelectedIndex() == -1) {
      return null;
    }
    return EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
  }

  public void updateComponents() {
    if (mapList.getSelectedIndex() == -1 && this.model.size() > 0) {
      this.mapList.setSelectedIndex(0);
    }

    UI.getEntityList().update();
    UI.getMapLayerList().update();
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
