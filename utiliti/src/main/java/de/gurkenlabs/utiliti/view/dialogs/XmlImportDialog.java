package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.view.components.UI;
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

    String filter = Resources.strings().get("file_filter_xml", sb, name);
    String title = Resources.strings().get("dialog_import_xml", name);
    if (EditorFileChooser.showFileDialog(filter, title, true, extensions) == JFileChooser.APPROVE_OPTION) {
      for (Path file : Stream.of(EditorFileChooser.instance().getSelectedFiles()).map(File::toPath).toList()) {
        consumer.accept(file);
      }

      UI.getAssetController().refresh();
    }
  }
}
