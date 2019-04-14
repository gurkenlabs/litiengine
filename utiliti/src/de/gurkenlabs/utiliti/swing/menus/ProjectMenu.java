package de.gurkenlabs.utiliti.swing.menus;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public final class ProjectMenu extends JMenu {

  public ProjectMenu() {
    super(Resources.strings().get("menu_project"));
    this.setMnemonic('P');

    JCheckBoxMenuItem compress = new JCheckBoxMenuItem(Resources.strings().get("menu_compressProjectFile"));
    compress.setState(Program.preferences().isCompressFile());
    compress.addItemListener(e -> Program.preferences().setCompressFile(compress.getState()));

    JCheckBoxMenuItem sync = new JCheckBoxMenuItem(Resources.strings().get("menu_syncMaps"));
    sync.setState(Program.preferences().isSyncMaps());
    sync.addItemListener(e -> Program.preferences().setSyncMaps(sync.getState()));

    JMenuItem importSpriteFile = new JMenuItem(Resources.strings().get("menu_assets_importSpriteFile"));
    importSpriteFile.addActionListener(a -> EditorScreen.instance().importSpriteFile());

    JMenuItem importSprite = new JMenuItem(Resources.strings().get("menu_assets_importSprite"));
    importSprite.addActionListener(a -> EditorScreen.instance().importSpriteSheets());

    JMenuItem importTextureAtlas = new JMenuItem(Resources.strings().get("menu_assets_importTextureAtlas"));
    importTextureAtlas.addActionListener(a -> EditorScreen.instance().importTextureAtlas());

    JMenuItem importEmitters = new JMenuItem(Resources.strings().get("menu_assets_importEmitters"));
    importEmitters.addActionListener(a -> EditorScreen.instance().importEmitters());

    JMenuItem importBlueprints = new JMenuItem(Resources.strings().get("menu_assets_importBlueprints"));
    importBlueprints.addActionListener(a -> EditorScreen.instance().importBlueprints());

    JMenuItem importTilesets = new JMenuItem(Resources.strings().get("menu_assets_importTilesets"));
    importTilesets.addActionListener(a -> EditorScreen.instance().importTilesets());

    JMenuItem importSounds = new JMenuItem(Resources.strings().get("menu_assets_importSounds"));
    importSounds.addActionListener(a -> EditorScreen.instance().importSounds());

    this.add(importSprite);
    this.add(importTextureAtlas);
    this.add(importSpriteFile);
    this.add(importEmitters);
    this.add(importBlueprints);
    this.add(importTilesets);
    this.add(importSounds);
    this.addSeparator();
    this.add(compress);
    this.add(sync);
  }
}
