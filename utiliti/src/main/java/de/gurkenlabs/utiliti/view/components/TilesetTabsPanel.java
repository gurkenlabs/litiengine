package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.resources.Resources;
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
    super(new BorderLayout(0, 0));
    setOpaque(true);
    setBackground(Style.background());
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    this.commands = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    this.commands.setOpaque(false);
    JButton add = Style.iconButton(Icons.ADD_16);
    add.setToolTipText(Resources.strings().get("mapTilesets_add"));
    add.addActionListener(e -> UI.showMapTilesetMenu(add));
    JButton addAll = Style.iconButton(Icons.COPY_16);
    addAll.setToolTipText(Resources.strings().get("mapTilesets_addAll"));
    addAll.addActionListener(e -> UI.addAllMapTilesets());
    JButton create = Style.iconButton(Icons.ASSET_16);
    create.setToolTipText(Resources.strings().get("mapTilesets_create"));
    create.addActionListener(e -> UI.createMapTileset());
    JButton remove = Style.iconButton(Icons.DELETE_16);
    Style.styleButton(remove, Style.ButtonVariant.DESTRUCTIVE);
    remove.setToolTipText(Resources.strings().get("mapTilesets_remove"));
    remove.addActionListener(e -> UI.removeSelectedMapTileset(this));
    this.commands.add(add);
    this.commands.add(addAll);
    this.commands.add(create);
    this.commands.add(remove);
    this.tabs.putClientProperty("JTabbedPane.noContentBorder", Boolean.TRUE);
    this.tabs.putClientProperty("JTabbedPane.hasFullBorder", Boolean.FALSE);
    this.tabs.putClientProperty("JTabbedPane.contentInsets", new java.awt.Insets(0, 0, 0, 0));
    this.tabs.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(0, 0, 0, 0));
    this.tabs.putClientProperty("JTabbedPane.tabType", "underlined");
    this.tabs.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
    this.tabs.putClientProperty("JTabbedPane.tabHeight", 28);
    this.tabs.putClientProperty("JTabbedPane.tabInsets", new java.awt.Insets(2, 10, 2, 10));
    this.tabs.putClientProperty("JTabbedPane.underlineColor", Style.accent());
    this.tabs.putClientProperty("JTabbedPane.underlineHeight", 2);
    this.tabs.putClientProperty("JTabbedPane.selectedBackground", Style.surface());
    this.tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    this.tabs.setBackground(Style.background());
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
    return tileset.getName() == null || tileset.getName().isBlank()
      ? Resources.strings().get("tilesetEditor_tileset") : tileset.getName();
  }
}
