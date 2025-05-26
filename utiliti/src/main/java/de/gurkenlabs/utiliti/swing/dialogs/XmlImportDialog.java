package de.gurkenlabs.utiliti.swing.dialogs;

import de.gurkenlabs.utiliti.swing.UI;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.JFileChooser;

public final class XmlImportDialog {
  private XmlImportDialog() {}

  public static void importXml(String name, Consumer<Path> consumer) {
    importXml(name, consumer, "xml");
  }

  public static void importXml(String name, Consumer<Path> consumer, String... extensions) {
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
      for (Path file : Stream.of(EditorFileChooser.instance().getSelectedFiles()).map(File::toPath).toList()) {
        consumer.accept(file);
      }

      UI.getAssetController().refresh();
    }
  }
}
