package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public final class ResourcesMenu extends JMenu {

  public ResourcesMenu() {
    super(Resources.strings().get("menu_resources"));
    this.setMnemonic('P');

    JCheckBoxMenuItem compress =
        new JCheckBoxMenuItem(Resources.strings().get("menu_compressResourceFile"));
    compress.setState(Editor.preferences().compressFile());
    compress.addItemListener(e -> Editor.preferences().setCompressFile(compress.getState()));

    JMenu importMenu = new JMenu(Resources.strings().get("menu_assets_import"));

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

    importMenu.add(importSprite);
    importMenu.add(importTextureAtlas);
    importMenu.add(importSpriteFile);
    importMenu.addSeparator();
    importMenu.add(importSounds);
    importMenu.addSeparator();
    importMenu.add(importEmitters);
    importMenu.add(importBlueprints);
    importMenu.add(importTilesets);

    JMenu exportMenu = new JMenu(Resources.strings().get("menu_assets_export"));

    JMenuItem exportSpriteSheets = new JMenuItem(Resources.strings().get("menu_export_spriteSheets"));
    exportSpriteSheets.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
    exportSpriteSheets.addActionListener(a -> Editor.instance().exportSpriteFile());
    exportMenu.add(exportSpriteSheets);

    Editor.instance().onLoaded(() -> {
      importSpriteFile.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importSprite.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importTextureAtlas.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importEmitters.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importBlueprints.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importTilesets.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      importSounds.setEnabled(Editor.instance().getCurrentResourceFile() != null);
      exportSpriteSheets.setEnabled(Editor.instance().getCurrentResourceFile() != null);
    });

    this.add(importMenu);
    this.add(exportMenu);
    this.add(compress);
  }
}
