package de.gurkenlabs.utiliti.view.dialogs;

import static de.gurkenlabs.utiliti.model.constants.EditorConstants.DEFAULT_GAME_NAME;
import static de.gurkenlabs.utiliti.model.constants.EditorConstants.GAME_FILE_NAME;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class EditorFileSaver extends JFileChooser {
  public EditorFileSaver(Path projectFile) {
    super(projectFile.toFile());
    setFileSelectionMode(JFileChooser.FILES_ONLY);
    setDialogType(JFileChooser.SAVE_DIALOG);
    FileFilter filter = new FileNameExtensionFilter(GAME_FILE_NAME, ResourceBundle.FILE_EXTENSION);
    setFileFilter(filter);
    addChoosableFileFilter(filter);
    setSelectedFile(new File(DEFAULT_GAME_NAME + "." + ResourceBundle.FILE_EXTENSION));
  }

  @Override public int showSaveDialog(Component parent) throws HeadlessException {
    return super.showSaveDialog(parent);
  }
}
