package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Shared inspector section for the tilesets assigned to a map. */
final class TilesetTabsPanel extends JPanel {
  private final JTabbedPane tabs = new JTabbedPane();
  private final JPanel commands;
  private IMap boundMap;
  private List<ITileset> boundTilesets = List.of();

  TilesetTabsPanel() {
    super(new BorderLayout(0, 6));
    setOpaque(true);
    setBackground(Style.background());
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    this.commands = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    this.commands.setOpaque(false);
    JButton add = Style.iconButton(Icons.ADD_16);
    add.setToolTipText("Add a project tileset to this map");
    add.addActionListener(e -> UI.showMapTilesetMenu(add));
    JButton addAll = Style.iconButton(Icons.COPY_16);
    addAll.setToolTipText("Add all project tilesets to this map");
    addAll.addActionListener(e -> UI.addAllMapTilesets());
    JButton create = Style.iconButton(Icons.ASSET_16);
    create.setToolTipText("Create a new tileset for this map");
    create.addActionListener(e -> UI.createMapTileset());
    JButton remove = Style.iconButton(Icons.DELETE_16);
    Style.styleButton(remove, Style.ButtonVariant.DESTRUCTIVE);
    remove.setToolTipText("Remove selected tileset from this map only");
    remove.addActionListener(e -> UI.removeSelectedMapTileset(this));
    this.commands.add(add);
    this.commands.add(addAll);
    this.commands.add(create);
    this.commands.add(remove);
    add(this.tabs, BorderLayout.CENTER);
    this.tabs.addChangeListener(_ -> publishSelectedEditor());
  }

  void bind(IMap map) {
    this.boundMap = map;
    this.boundTilesets = map != null ? List.copyOf(map.getTilesets()) : List.of();
    for (int i = 0; i < this.tabs.getTabCount(); i++) {
      if (this.tabs.getComponentAt(i) instanceof TilesetEditorPanel editor) {
        editor.dispose();
      }
    }
    this.tabs.removeAll();
    if (map == null) {
      return;
    }
    for (ITileset tileset : map.getTilesets()) {
      if (tileset instanceof Tileset editableTileset) {
        TilesetEditorPanel editor = new TilesetEditorPanel();
        editor.bind(editableTileset);
        this.tabs.addTab(tabName(editableTileset), editor);
        editor.onTilesetNameChanged(() -> {
          int index = this.tabs.indexOfComponent(editor);
          if (index >= 0) {
            this.tabs.setTitleAt(index, tabName(editableTileset));
          }
        });
      }
    }
    publishSelectedEditor();
  }

  void bindIfMapChanged(IMap map) {
    if (this.boundMap != map || map != null && !this.boundTilesets.equals(map.getTilesets())) {
      bind(map);
    }
  }

  void select(Tileset tileset) {
    for (int i = 0; i < this.tabs.getTabCount(); i++) {
      if (this.tabs.getComponentAt(i) instanceof TilesetEditorPanel editor && editor.getTileset() == tileset) {
        this.tabs.setSelectedIndex(i);
        return;
      }
    }
  }

  Tileset getSelectedTileset() {
    if (this.tabs.getSelectedComponent() instanceof TilesetEditorPanel editor) {
      return editor.getTileset();
    }
    return null;
  }

  JPanel getCommands() {
    return this.commands;
  }

  TilesetEditorPanel getSelectedEditorForTest() {
    return this.tabs.getSelectedComponent() instanceof TilesetEditorPanel editor ? editor : null;
  }

  int getTabCountForTest() {
    return this.tabs.getTabCount();
  }

  private void publishSelectedEditor() {
    if (this.tabs.getSelectedComponent() instanceof TilesetEditorPanel editor) {
      editor.publishToolSelection();
    }
  }

  private static String tabName(Tileset tileset) {
    return tileset.getName() == null || tileset.getName().isBlank() ? "Tileset" : tileset.getName();
  }
}
