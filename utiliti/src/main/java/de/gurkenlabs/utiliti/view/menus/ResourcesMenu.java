package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
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
    this.setMnemonic(this.getText().charAt(0));

    JCheckBoxMenuItem compress =
        new JCheckBoxMenuItem(Resources.strings().get("menu_compressResourceFile"));
    compress.setIcon(Icons.ASSET_16);
    compress.setState(Editor.preferences().compressFile());
    compress.addItemListener(e -> Editor.preferences().setCompressFile(compress.getState()));

    JMenu importMenu = new JMenu(Resources.strings().get("menu_assets_import"));
    importMenu.setIcon(Icons.IMPORT_16);

    JMenuItem importSpriteFile = new JMenuItem(Resources.strings().get("menu_assets_importSpriteFile"), Icons.SPRITESHEET_16);
    importSpriteFile.addActionListener(a -> Editor.instance().importSpriteFile());
    importSpriteFile.setEnabled(false);

    JMenuItem importSprite = new JMenuItem(Resources.strings().get("menu_assets_importSprite"), Icons.SPRITESHEET_16);
    importSprite.addActionListener(a -> Editor.instance().importSpriteSheets());
    importSprite.setEnabled(false);

    JMenuItem importTextureAtlas = new JMenuItem(Resources.strings().get("menu_assets_importTextureAtlas"), Icons.ASSET_16);
    importTextureAtlas.addActionListener(a -> Editor.instance().importTextureAtlas());
    importTextureAtlas.setEnabled(false);

    JMenuItem importEmitters = new JMenuItem(Resources.strings().get("menu_assets_importEmitters"), Icons.EMITTER_16);
    importEmitters.addActionListener(a -> Editor.instance().importEmitters());
    importEmitters.setEnabled(false);

    JMenuItem importBlueprints = new JMenuItem(Resources.strings().get("menu_assets_importBlueprints"), Icons.BLUEPRINT_16);
    importBlueprints.addActionListener(a -> Editor.instance().importBlueprints());
    importBlueprints.setEnabled(false);

    JMenuItem importTilesets = new JMenuItem(Resources.strings().get("menu_assets_importTilesets"), Icons.TILESET_16);
    importTilesets.addActionListener(a -> Editor.instance().importTilesets());
    importTilesets.setEnabled(false);

    JMenuItem importSounds = new JMenuItem(Resources.strings().get("menu_assets_importSounds"), Icons.SOUND_16);
    importSounds.addActionListener(a -> Editor.instance().importSounds());
    importSounds.setEnabled(false);

    JMenuItem importAnimations = new JMenuItem(Resources.strings().get("menu_assets_importAnimations"), Icons.ANIMATION_16);
    importAnimations.addActionListener(a -> Editor.instance().importAnimations());
    // animations live in an in-memory resource container and can be imported without a loaded project
    importAnimations.setEnabled(true);

    importMenu.add(importSprite);
    importMenu.add(importTextureAtlas);
    importMenu.add(importSpriteFile);
    importMenu.addSeparator();
    importMenu.add(importSounds);
    importMenu.add(importAnimations);
    importMenu.addSeparator();
    importMenu.add(importEmitters);
    importMenu.add(importBlueprints);
    importMenu.add(importTilesets);

    JMenu exportMenu = new JMenu(Resources.strings().get("menu_assets_export"));
    exportMenu.setIcon(Icons.EXPORT_16);

    JMenuItem exportSpriteSheets = new JMenuItem(Resources.strings().get("menu_export_spriteSheets"), Icons.SPRITESHEET_16);
    KeyBindings.bind(exportSpriteSheets, Command.EXPORT_SPRITES);
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
