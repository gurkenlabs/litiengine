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
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public final class FileMenu extends JMenu {
  private static final Logger log = Logger.getLogger(FileMenu.class.getName());
  private final JMenu recentFiles;

  public FileMenu() {
    super(Resources.strings().get("menu_file"));
    this.setMnemonic('F');

    JMenuItem create = new JMenuItem(Resources.strings().get("menu_createProject"));
    create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
    create.addActionListener(a -> EditorScreen.instance().create());

    JMenuItem load = new JMenuItem(Resources.strings().get("menu_loadProject"));
    load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    load.addActionListener(a -> EditorScreen.instance().load());

    JMenuItem save = new JMenuItem(Resources.strings().get("menu_save"));
    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
    save.addActionListener(a -> EditorScreen.instance().save(false));

    JMenuItem saveAs = new JMenuItem(Resources.strings().get("menu_saveAs"));
    saveAs.addActionListener(a -> EditorScreen.instance().save(true));

    JMenuItem exit = new JMenuItem(Resources.strings().get("menu_exit"));
    exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
    exit.addActionListener(a -> System.exit(0));

    this.recentFiles = new JMenu(Resources.strings().get("menu_recentFiles"));
    loadRecentFiles();
    EditorScreen.instance().onLoaded(this::loadRecentFiles);

    this.add(create);
    this.add(load);
    this.add(save);
    this.add(saveAs);
    this.addSeparator();

    this.add(recentFiles);
    this.addSeparator();
    this.add(exit);
  }

  public void loadRecentFiles() {
    recentFiles.removeAll();
    for (String recent : Program.preferences().getLastOpenedFiles()) {
      if (recent != null && !recent.isEmpty() && new File(recent).exists()) {
        JMenuItem fileButton = new JMenuItem(recent);
        fileButton.addActionListener(a -> {
          log.log(Level.INFO, "load " + fileButton.getText());
          EditorScreen.instance().load(new File(fileButton.getText()));
        });

        recentFiles.add(fileButton);
      }
    }
  }
}
