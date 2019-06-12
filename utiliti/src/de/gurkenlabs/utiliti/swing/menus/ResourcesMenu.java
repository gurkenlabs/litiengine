package de.gurkenlabs.utiliti.swing.menus;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;

@SuppressWarnings("serial")
public final class ResourcesMenu extends JMenu {

  public ResourcesMenu() {
    super(Resources.strings().get("menu_resources"));
    this.setMnemonic('P');

    JCheckBoxMenuItem compress = new JCheckBoxMenuItem(Resources.strings().get("menu_compressResourceFile"));
    compress.setState(Editor.preferences().compressFile());
    compress.addItemListener(e -> Editor.preferences().setCompressFile(compress.getState()));

    JMenuItem importSpriteFile = new JMenuItem(Resources.strings().get("menu_assets_importSpriteFile"));
    importSpriteFile.addActionListener(a -> Editor.instance().importSpriteFile());
    importSpriteFile.setEnabled(false);

    JMenuItem importSprite = new JMenuItem(Resources.strings().get("menu_assets_importSprite"));
    importSprite.addActionListener(a -> Editor.instance().importSpriteSheets());
    importSprite.setEnabled(false);
    
    JMenuItem importTextureAtlas = new JMenuItem(Resources.strings().get("menu_assets_importTextureAtlas"));
    importTextureAtlas.addActionListener(a -> Editor.instance().importTextureAtlas());
    importTextureAtlas.setEnabled(false);
    
    JMenuItem importEmitters = new JMenuItem(Resources.strings().get("menu_assets_importEmitters"));
    importEmitters.addActionListener(a -> Editor.instance().importEmitters());
    importEmitters.setEnabled(false);
    
    JMenuItem importBlueprints = new JMenuItem(Resources.strings().get("menu_assets_importBlueprints"));
    importBlueprints.addActionListener(a -> Editor.instance().importBlueprints());
    importBlueprints.setEnabled(false);
    
    JMenuItem importTilesets = new JMenuItem(Resources.strings().get("menu_assets_importTilesets"));
    importTilesets.addActionListener(a -> Editor.instance().importTilesets());
    importTilesets.setEnabled(false);
    
    JMenuItem importSounds = new JMenuItem(Resources.strings().get("menu_assets_importSounds"));
    importSounds.addActionListener(a -> Editor.instance().importSounds());
    importSounds.setEnabled(false);
    
    Editor.instance().onLoaded(() -> {
      importSpriteFile.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importSprite.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importTextureAtlas.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importEmitters.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importBlueprints.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importTilesets.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importSounds.setEnabled(Editor.instance().getCurrentResourceFile() != null);
    });

    this.add(importSprite);
    this.add(importTextureAtlas);
    this.add(importSpriteFile);
    this.add(importEmitters);
    this.add(importBlueprints);
    this.add(importTilesets);
    this.add(importSounds);
    this.addSeparator();
    this.add(compress);
  }
}
