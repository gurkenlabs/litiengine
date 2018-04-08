package de.gurkenlabs.utiliti.swing;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JFileChooser;

import de.gurkenlabs.utiliti.Program;

public final class XmlImportDialog {
  private XmlImportDialog() {
  }

  public static void importXml(String name, Consumer<File[]> consumer) {
    importXml(name, "xml", consumer);
  }

  public static void importXml(String name, String extension, Consumer<File[]> consumer) {
    if (EditorFileChooser.showFileDialog("." + extension + " - " + name + " XML", "Import " + name + " XML", true, extension) == JFileChooser.APPROVE_OPTION) {
      consumer.accept(EditorFileChooser.instance().getSelectedFiles());
      Program.getAssetTree().forceUpdate();
    }
  }
}
