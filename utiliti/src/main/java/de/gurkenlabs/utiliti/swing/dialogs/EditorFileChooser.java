package de.gurkenlabs.utiliti.swing.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.utiliti.components.Editor;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class EditorFileChooser extends JFileChooser {
  private static EditorFileChooser instance;

  private EditorFileChooser() {
    String source = Editor.instance().getProjectPath();
    this.setCurrentDirectory(source != null ? new File(source) : new File("."));
    this.setDialogType(JFileChooser.OPEN_DIALOG);
  }

  public static int showFileDialog(
      String description, String title, boolean multiselect, String... extensions) {
    FileFilter filter = new FileNameExtensionFilter(description, extensions);
    instance().resetChoosableFileFilters();
    instance().setFileFilter(filter);
    instance().setFileSelectionMode(JFileChooser.FILES_ONLY);
    instance().setMultiSelectionEnabled(multiselect);
    instance().setDialogTitle(title);
    return instance().showOpenDialog(Game.window().getHostControl());
  }

  public static int showFileDialog(String description, boolean multiselect, String... extensions) {
    return showFileDialog(description, null, multiselect, extensions);
  }

  public static EditorFileChooser instance() {
    if (instance == null) {
      instance = new EditorFileChooser();
    }

    return instance;
  }
}
