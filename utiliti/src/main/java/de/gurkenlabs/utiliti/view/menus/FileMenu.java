package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.view.dialogs.SettingsDialog;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class FileMenu extends JMenu {
  private static final Logger log = Logger.getLogger(FileMenu.class.getName());
  private final JMenu recentFiles;

  public FileMenu() {
    super(Resources.strings().get("menu_file"));
    this.setMnemonic(this.getText().charAt(0));

    JMenuItem create = new JMenuItem(Resources.strings().get("menu_file_new"), Icons.FILE_NEW_16);
    KeyBindings.bind(create, Command.NEW_PROJECT);
    create.addActionListener(a -> Editor.instance().create());

    JMenuItem load = new JMenuItem(Resources.strings().get("menu_file_open"), Icons.FOLDER_OPEN_16);
    KeyBindings.bind(load, Command.OPEN_PROJECT);
    load.addActionListener(a -> Editor.instance().load());

    JMenuItem close = new JMenuItem(Resources.strings().get("menu_file_close"), Icons.CROSS_16);
    close.addActionListener(a -> Editor.instance().close(false));
    close.setEnabled(false);
    Editor.instance()
      .onLoaded(() -> close.setEnabled(Editor.instance().getCurrentResourceFile() != null));

    this.recentFiles = new JMenu(Resources.strings().get("menu_file_recentFiles"));
    this.recentFiles.setIcon(Icons.HISTORY_16);
    loadRecentFiles();
    Editor.instance().onLoaded(this::loadRecentFiles);

    JMenuItem save = new JMenuItem(Resources.strings().get("menu_file_save"), Icons.SAVE_16);
    KeyBindings.bind(save, Command.SAVE_PROJECT);
    save.addActionListener(a -> Editor.instance().save(false));

    JMenuItem saveAs = new JMenuItem(Resources.strings().get("menu_file_saveAs"), Icons.EXPORT_16);
    saveAs.addActionListener(a -> Editor.instance().save(true));

    JMenuItem revert = new JMenuItem(Resources.strings().get("menu_file_revert"), Icons.UNDO_16);
    revert.addActionListener(a -> Editor.instance().revert());

    JMenuItem exit = new JMenuItem(Resources.strings().get("menu_exit"), Icons.POWER_16);
    KeyBindings.bind(exit, Command.EXIT);
    exit.addActionListener(a -> Game.exit());

    JMenuItem settings = new JMenuItem(Resources.strings().get("menu_file_settings"), Icons.SETTINGS_16);
    settings.addActionListener(event -> SettingsDialog.show(this));

    this.add(create);
    this.add(load);
    this.add(close);
    this.add(recentFiles);
    this.addSeparator();
    this.add(save);
    this.add(saveAs);
    this.add(revert);
    this.addSeparator();
    this.add(settings);
    this.addSeparator();
    this.add(exit);
  }

  public void loadRecentFiles() {
    recentFiles.removeAll();
    int added = 0;
    for (Path recent : Editor.preferences().getLastOpenedFiles()) {
      if (recent != null && Files.exists(recent)) {
        JMenuItem fileButton = new JMenuItem(recent.toString(), Icons.ASSET_16);
        fileButton.addActionListener(
          a -> {
            log.log(Level.INFO, "load {0}", fileButton.getText());
            Editor.instance().load(Path.of(fileButton.getText()), false);
          });

        recentFiles.add(fileButton);
        added++;
      }
    }

    if (added == 0) {
      recentFiles.setEnabled(false);
      return;
    }

    JMenuItem clear = new JMenuItem(Resources.strings().get("menu_file_clear_recent"), Icons.DELETE_16);
    clear.addActionListener(
      a -> {
        recentFiles.removeAll();
        Editor.preferences().clearOpenedFiles();
        recentFiles.setEnabled(false);
      });

    recentFiles.addSeparator();
    recentFiles.add(clear);
    recentFiles.setEnabled(true);
  }
}
