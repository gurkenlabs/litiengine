package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.view.components.UI;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class AutoSaveManager {
  private static final Logger log = Logger.getLogger(AutoSaveManager.class.getName());
  private static final long AUTO_SAVE_INTERVAL_MINUTES = 3;

  private static AutoSaveManager instance;
  private ScheduledExecutorService executor;

  private AutoSaveManager() {
  }

  public static synchronized AutoSaveManager instance() {
    if (instance == null) {
      instance = new AutoSaveManager();
    }
    return instance;
  }

  public synchronized void start() {
    if (executor != null && !executor.isShutdown()) {
      return;
    }

    executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "utiliti-AutoSave");
      t.setDaemon(true);
      return t;
    });

    executor.scheduleAtFixedRate(this::performAutoSave, AUTO_SAVE_INTERVAL_MINUTES, AUTO_SAVE_INTERVAL_MINUTES, TimeUnit.MINUTES);
  }

  public synchronized void stop() {
    if (executor != null) {
      executor.shutdown();
      executor = null;
    }
  }

  public static Path getBackupPath(Path gameFile) {
    if (gameFile == null) {
      return null;
    }
    return Path.of(gameFile.toString() + ".bak");
  }

  public static void deleteBackup(Path gameFile) {
    Path backup = getBackupPath(gameFile);
    if (backup != null && Files.exists(backup)) {
      try {
        Files.delete(backup);
        log.log(Level.INFO, "Deleted recovery backup {0}", backup);
      } catch (IOException e) {
        log.log(Level.WARNING, "Failed to delete backup file " + backup, e);
      }
    }
  }

  public static boolean checkForRecovery(Path gameFile) {
    Path backup = getBackupPath(gameFile);
    if (backup == null || !Files.exists(backup)) {
      return false;
    }

    try {
      if (Files.exists(gameFile) && Files.getLastModifiedTime(backup).compareTo(Files.getLastModifiedTime(gameFile)) <= 0) {
        return false;
      }
    } catch (IOException e) {
        log.log(Level.FINE, "Could not check file timestamps", e);
    }

    int choice = JOptionPane.showConfirmDialog(
      Game.window().getHostControl(),
      "An unsaved recovery file was found for this project:\n" + backup + "\n\nWould you like to restore this recovery file?",
      "Auto-Save Recovery Found",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE
    );

    if (choice == JOptionPane.YES_OPTION) {
      try {
        Files.copy(backup, gameFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        log.log(Level.INFO, "Restored project from recovery backup {0}", backup);
        return true;
      } catch (IOException e) {
        log.log(Level.SEVERE, "Failed to restore backup file " + backup, e);
      }
    }
    return false;
  }

  private void performAutoSave() {
    if (!Editor.instance().fileLoaded() || Editor.instance().getCurrentResourceFile() == null) {
      return;
    }

    if (Editor.instance().getChangedMaps().isEmpty() && !Editor.instance().isUnsavedProject()) {
      return;
    }

    Path gameFile = Editor.instance().getCurrentResourceFile();
    Path backupPath = getBackupPath(gameFile);
    if (backupPath == null) {
      return;
    }

    try {
      Editor.instance().getGameFile().save(backupPath.toString(), Editor.preferences().compressFile());
      log.log(Level.INFO, "Auto-saved project recovery backup to {0}", backupPath);
    } catch (Exception e) {
      log.log(Level.WARNING, "Auto-save failed for " + backupPath, e);
    }
  }
}
