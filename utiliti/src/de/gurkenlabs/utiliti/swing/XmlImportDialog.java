package de.gurkenlabs.utiliti.swing;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;

public final class XmlImportDialog {
  private static final Logger log = Logger.getLogger(XmlImportDialog.class.getName());

  private XmlImportDialog() {
  }

  public static void importXml(String name, Consumer<File[]> consumer) {
    importXml(name, "xml", consumer);
  }

  public static void importXml(String name, String extension, Consumer<File[]> consumer) {
    JFileChooser chooser;

    try {
      String source = EditorScreen.instance().getProjectPath();
      chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());

      FileFilter filter = new FileNameExtensionFilter("." + extension + " - " + name + " XML", extension);
      chooser.setFileFilter(filter);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.addChoosableFileFilter(filter);
      chooser.setMultiSelectionEnabled(true);
      if (chooser.showOpenDialog(Game.getScreenManager().getRenderComponent()) == JFileChooser.APPROVE_OPTION) {
        consumer.accept(chooser.getSelectedFiles());
        Program.getAssetTree().forceUpdate();
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

}
