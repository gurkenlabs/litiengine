package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/** Shared inspector section for the tilesets assigned to a map. */
final class TilesetTabsPanel extends JPanel {
  private final JTabbedPane tabs = new JTabbedPane();
  private final JPanel commands;

  TilesetTabsPanel() {
    super(new BorderLayout(0, 6));
    setOpaque(true);
    setBackground(Style.COLOR_BG);
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
    remove.setToolTipText("Remove selected tileset from this map only");
    remove.addActionListener(e -> UI.removeSelectedMapTileset(this));
    this.commands.add(add);
    this.commands.add(addAll);
    this.commands.add(create);
    this.commands.add(remove);
    add(this.tabs, BorderLayout.CENTER);
  }

  void bind(IMap map) {
    this.tabs.removeAll();
    if (map == null) {
      return;
    }
    for (ITileset tileset : map.getTilesets()) {
      if (tileset instanceof Tileset editableTileset) {
        TilesetEditorPanel editor = new TilesetEditorPanel();
        editor.bind(editableTileset);
        this.tabs.addTab(tabName(editableTileset), new JScrollPane(editor));
      }
    }
  }

  void select(Tileset tileset) {
    for (int i = 0; i < this.tabs.getTabCount(); i++) {
      if (this.tabs.getComponentAt(i) instanceof JScrollPane scroll
          && scroll.getViewport().getView() instanceof TilesetEditorPanel editor
          && editor.getTileset() == tileset) {
        this.tabs.setSelectedIndex(i);
        return;
      }
    }
  }

  Tileset getSelectedTileset() {
    if (this.tabs.getSelectedComponent() instanceof JScrollPane scroll
        && scroll.getViewport().getView() instanceof TilesetEditorPanel editor) {
      return editor.getTileset();
    }
    return null;
  }

  JPanel getCommands() {
    return this.commands;
  }

  private static String tabName(Tileset tileset) {
    return tileset.getName() == null || tileset.getName().isBlank() ? "Tileset" : tileset.getName();
  }
}
