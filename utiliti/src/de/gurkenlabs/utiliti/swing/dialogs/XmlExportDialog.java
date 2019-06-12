package de.gurkenlabs.utiliti.swing.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.components.Editor;

public final class XmlExportDialog {
  private static final Logger log = Logger.getLogger(XmlExportDialog.class.getName());

  private XmlExportDialog() {
  }

  public static <T> void export(T object, String name, String filename) {
    export(object, name, filename, "xml");
  }

  public static <T> void export(T object, String name, String filename, String extension) {
    export(object, name, filename, extension, d -> {
    });
  }

  public static <T> void export(T object, String name, String filename, String extension, Consumer<String> consumer) {
    JFileChooser chooser;
    try {
      String source = Editor.instance().getProjectPath();
      chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setDialogTitle("Export " + name);
      FileFilter filter = new FileNameExtensionFilter("." + extension + " - " + name + " XML", extension);
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      chooser.setSelectedFile(new File(filename + "." + extension));

      int result = chooser.showSaveDialog(Game.window().getRenderComponent());
      if (result == JFileChooser.APPROVE_OPTION) {
        File newFile = XmlUtilities.save(object, chooser.getSelectedFile().toString(), extension);
        String dir = FileUtilities.getParentDirPath(newFile.getAbsolutePath());
        consumer.accept(dir);
        log.log(Level.INFO, "exported {0} {1} to {2}", new Object[] { name, filename, newFile });
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
