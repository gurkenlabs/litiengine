package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.controller.Editor;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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

  public static <T> void export(
    T object, String name, String filename, String extension, Consumer<String> consumer) {
    JFileChooser chooser;
    Path source = Editor.instance().getProjectPath();
    chooser = new JFileChooser(source.toFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setDialogTitle("Export " + name);
    FileFilter filter =
      new FileNameExtensionFilter("." + extension + " - " + name + " XML", extension);
    chooser.setFileFilter(filter);
    chooser.addChoosableFileFilter(filter);
    chooser.setSelectedFile(new File(filename + "." + extension));

    int result = chooser.showSaveDialog(Game.window().getRenderComponent());
    if (result == JFileChooser.APPROVE_OPTION) {
      Path newFile = XmlUtilities.save(object, chooser.getSelectedFile().toPath(), extension);
      consumer.accept(newFile.getParent().toString());
      log.log(Level.INFO, "Exported {0} {1} to {2}", new Object[] {name, filename, newFile});
    }
  }
}
