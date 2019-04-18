package de.gurkenlabs.utiliti.swing.menus;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public final class ResourcesMenu extends JMenu {

  public ResourcesMenu() {
    super(Resources.strings().get("menu_resources"));
    this.setMnemonic('P');

    JCheckBoxMenuItem compress = new JCheckBoxMenuItem(Resources.strings().get("menu_compressResourceFile"));
    compress.setState(Program.preferences().isCompressFile());
    compress.addItemListener(e -> Program.preferences().setCompressFile(compress.getState()));

    JMenuItem importSpriteFile = new JMenuItem(Resources.strings().get("menu_assets_importSpriteFile"));
    importSpriteFile.addActionListener(a -> EditorScreen.instance().importSpriteFile());
    importSpriteFile.setEnabled(false);

    JMenuItem importSprite = new JMenuItem(Resources.strings().get("menu_assets_importSprite"));
    importSprite.addActionListener(a -> EditorScreen.instance().importSpriteSheets());
    importSprite.setEnabled(false);
    
    JMenuItem importTextureAtlas = new JMenuItem(Resources.strings().get("menu_assets_importTextureAtlas"));
    importTextureAtlas.addActionListener(a -> EditorScreen.instance().importTextureAtlas());
    importTextureAtlas.setEnabled(false);
    
    JMenuItem importEmitters = new JMenuItem(Resources.strings().get("menu_assets_importEmitters"));
    importEmitters.addActionListener(a -> EditorScreen.instance().importEmitters());
    importEmitters.setEnabled(false);
    
    JMenuItem importBlueprints = new JMenuItem(Resources.strings().get("menu_assets_importBlueprints"));
    importBlueprints.addActionListener(a -> EditorScreen.instance().importBlueprints());
    importBlueprints.setEnabled(false);
    
    JMenuItem importTilesets = new JMenuItem(Resources.strings().get("menu_assets_importTilesets"));
    importTilesets.addActionListener(a -> EditorScreen.instance().importTilesets());
    importTilesets.setEnabled(false);
    
    JMenuItem importSounds = new JMenuItem(Resources.strings().get("menu_assets_importSounds"));
    importSounds.addActionListener(a -> EditorScreen.instance().importSounds());
    importSounds.setEnabled(false);
    
    EditorScreen.instance().onLoaded(() -> {
      importSpriteFile.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importSprite.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importTextureAtlas.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importEmitters.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importBlueprints.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importTilesets.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
      importSounds.setEnabled(EditorScreen.instance().getCurrentResourceFile() != null);
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
