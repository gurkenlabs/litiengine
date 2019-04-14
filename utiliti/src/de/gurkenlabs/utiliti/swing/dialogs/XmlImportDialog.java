package de.gurkenlabs.utiliti.swing.dialogs;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JFileChooser;

import de.gurkenlabs.utiliti.Program;

public final class XmlImportDialog {
  private XmlImportDialog() {
  }

  public static void importXml(String name, Consumer<File> consumer) {
    importXml(name, consumer, "xml");
  }

  public static void importXml(String name, Consumer<File> consumer, String... extensions) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < extensions.length; i++) {
      String extension = extensions[i];
      if (i > 0) {
        sb.append(" / ");
      }
      if (extension != null && !extension.isEmpty()) {
        sb.append("." + extension);
      }
    }

    sb.append(" - " + name + " XML");
    if (EditorFileChooser.showFileDialog(sb.toString(), "Import " + name + " XML", true, extensions) == JFileChooser.APPROVE_OPTION) {
      for (File file : EditorFileChooser.instance().getSelectedFiles()) {
        consumer.accept(file);
      }

      Program.updateAssets();
    }
  }
}
