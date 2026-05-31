package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.view.components.AssetPanel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

@SuppressWarnings("serial")
public final class AssetPanelPopupMenu extends JPopupMenu {

  public AssetPanelPopupMenu(AssetPanel.AssetType currentType) {
    boolean projectLoaded = Editor.instance().getCurrentResourceFile() != null;

    if (currentType != null) {
      switch (currentType) {
        case SPRITESHEET -> {
          addImportItem("menu_assets_importSprite", Editor.instance()::importSpriteSheets, projectLoaded);
          addImportItem("menu_assets_importTextureAtlas", Editor.instance()::importTextureAtlas, projectLoaded);
          addImportItem("menu_assets_importSpriteFile", Editor.instance()::importSpriteFile, projectLoaded);
        }
        case TILESET -> addImportItem("menu_assets_importTilesets", Editor.instance()::importTilesets, projectLoaded);
        case EMITTER -> addImportItem("menu_assets_importEmitters", Editor.instance()::importEmitters, projectLoaded);
        case BLUEPRINT -> addImportItem("menu_assets_importBlueprints", Editor.instance()::importBlueprints, projectLoaded);
        case SOUND -> addImportItem("menu_assets_importSounds", Editor.instance()::importSounds, projectLoaded);
        case ANIMATION -> addImportItem("menu_assets_importAnimations", Editor.instance()::importAnimations, true);
      }
      addSeparator();
    }

    JMenu importAll = new JMenu(Resources.strings().get("menu_assets_import"));
    addToMenu(importAll, "menu_assets_importSprite", Editor.instance()::importSpriteSheets, projectLoaded);
    addToMenu(importAll, "menu_assets_importTextureAtlas", Editor.instance()::importTextureAtlas, projectLoaded);
    addToMenu(importAll, "menu_assets_importSpriteFile", Editor.instance()::importSpriteFile, projectLoaded);
    importAll.addSeparator();
    addToMenu(importAll, "menu_assets_importSounds", Editor.instance()::importSounds, projectLoaded);
    addToMenu(importAll, "menu_assets_importAnimations", Editor.instance()::importAnimations, true);
    importAll.addSeparator();
    addToMenu(importAll, "menu_assets_importEmitters", Editor.instance()::importEmitters, projectLoaded);
    addToMenu(importAll, "menu_assets_importBlueprints", Editor.instance()::importBlueprints, projectLoaded);
    addToMenu(importAll, "menu_assets_importTilesets", Editor.instance()::importTilesets, projectLoaded);
    add(importAll);
  }

  private void addImportItem(String key, Runnable action, boolean enabled) {
    JMenuItem item = new JMenuItem(Resources.strings().get(key));
    item.addActionListener(e -> action.run());
    item.setEnabled(enabled);
    add(item);
  }

  private static void addToMenu(JMenu menu, String key, Runnable action, boolean enabled) {
    JMenuItem item = new JMenuItem(Resources.strings().get(key));
    item.addActionListener(e -> action.run());
    item.setEnabled(enabled);
    menu.add(item);
  }
}
