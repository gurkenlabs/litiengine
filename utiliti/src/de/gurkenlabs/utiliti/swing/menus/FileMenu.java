package de.gurkenlabs.utiliti.swing.menus;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;

@SuppressWarnings("serial")
public final class FileMenu extends JMenu {
  private static final Logger log = Logger.getLogger(FileMenu.class.getName());
  private final JMenu recentFiles;

  public FileMenu() {
    super(Resources.strings().get("menu_file"));
    this.setMnemonic('F');

    JMenuItem create = new JMenuItem(Resources.strings().get("menu_file_new"));
    create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
    create.addActionListener(a -> Editor.instance().create());

    JMenuItem load = new JMenuItem(Resources.strings().get("menu_file_open"));
    load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    load.addActionListener(a -> Editor.instance().load());

    JMenuItem close = new JMenuItem(Resources.strings().get("menu_file_close"));
    close.addActionListener(a -> Editor.instance().close(false));
    close.setEnabled(false);
    Editor.instance().onLoaded(() -> close.setEnabled(Editor.instance().getCurrentResourceFile() != null));

    this.recentFiles = new JMenu(Resources.strings().get("menu_file_recentFiles"));
    loadRecentFiles();
    Editor.instance().onLoaded(this::loadRecentFiles);

    JMenuItem save = new JMenuItem(Resources.strings().get("menu_file_save"));
    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    save.addActionListener(a -> Editor.instance().save(false));

    JMenuItem saveAs = new JMenuItem(Resources.strings().get("menu_file_saveAs"));
    saveAs.addActionListener(a -> Editor.instance().save(true));

    JMenuItem revert = new JMenuItem(Resources.strings().get("menu_file_revert"));
    revert.addActionListener(a -> Editor.instance().revert());

    JMenuItem exit = new JMenuItem(Resources.strings().get("menu_exit"));
    exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    exit.addActionListener(a -> System.exit(0));

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
    for (String recent : Editor.preferences().getLastOpenedFiles()) {
      if (recent != null && !recent.isEmpty() && new File(recent).exists()) {
        JMenuItem fileButton = new JMenuItem(recent);
        fileButton.addActionListener(a -> {
          log.log(Level.INFO, "load " + fileButton.getText());
          Editor.instance().load(new File(fileButton.getText()), false);
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
    clear.addActionListener(a -> {
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
