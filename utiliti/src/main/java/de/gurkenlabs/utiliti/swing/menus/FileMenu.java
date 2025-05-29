package de.gurkenlabs.utiliti.swing.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
    this.setMnemonic('F');

    JMenuItem create = new JMenuItem(Resources.strings().get("menu_file_new"));
    create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
    create.addActionListener(a -> Editor.instance().create());

    JMenuItem load = new JMenuItem(Resources.strings().get("menu_file_open"));
    load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    load.addActionListener(a -> Editor.instance().load());

    JMenuItem close = new JMenuItem(Resources.strings().get("menu_file_close"));
    close.addActionListener(a -> Editor.instance().close(false));
    close.setEnabled(false);
    Editor.instance()
      .onLoaded(() -> close.setEnabled(Editor.instance().getCurrentResourceFile() != null));

    this.recentFiles = new JMenu(Resources.strings().get("menu_file_recentFiles"));
    loadRecentFiles();
    Editor.instance().onLoaded(this::loadRecentFiles);

    JMenuItem save = new JMenuItem(Resources.strings().get("menu_file_save"));
    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    save.addActionListener(a -> Editor.instance().save(false));

    JMenuItem saveAs = new JMenuItem(Resources.strings().get("menu_file_saveAs"));
    saveAs.addActionListener(a -> Editor.instance().save(true));

    JMenuItem revert = new JMenuItem(Resources.strings().get("menu_file_revert"));
    revert.addActionListener(a -> Editor.instance().revert());

    JMenuItem exit = new JMenuItem(Resources.strings().get("menu_exit"));
    exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    exit.addActionListener(a -> Game.exit());

    this.add(create);
    this.add(load);
    this.add(close);
    this.add(recentFiles);
    this.addSeparator();
    this.add(save);
    this.add(saveAs);
    this.add(revert);
    this.addSeparator();
    this.add(exit);
  }

  public void loadRecentFiles() {
    recentFiles.removeAll();
    int added = 0;
    for (Path recent : Editor.preferences().getLastOpenedFiles()) {
      if (recent != null && recent.toFile().exists()) {
        JMenuItem fileButton = new JMenuItem(recent.toString());
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

    JMenuItem clear = new JMenuItem(Resources.strings().get("menu_file_clear_recent"));
    clear.addActionListener(
      a -> {
        recentFiles.removeAll();
        Editor.preferences().clearOpenedFiles();
        Editor.preferences().setLastGameFile(null);
        recentFiles.setEnabled(false);
      });

    recentFiles.addSeparator();
    recentFiles.add(clear);
    recentFiles.setEnabled(true);
  }
}
