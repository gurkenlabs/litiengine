package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AssetTransferable implements Transferable, ClipboardOwner, AutoCloseable {
  public static final DataFlavor ASSET_FLAVOR = new DataFlavor(Object.class, "application/x-utiliti-asset");
  private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "utiliti-asset-transfer-cleaner");
    thread.setDaemon(true);
    return thread;
  });
  private static final int MAX_CLEANUP_ATTEMPTS = 5;

  private final List<Object> assetOrigins;
  private final CleanupScheduler cleanupScheduler;
  private final DirectoryCleanup directoryCleanup;
  private List<File> exportedFiles;
  private Path exportDirectory;
  private boolean clipboardOwned;
  private CleanupTask scheduledCleanup;
  private long cleanupGeneration;

  public AssetTransferable(Object assetOrigin) {
    this(List.of(assetOrigin));
  }

  public AssetTransferable(Collection<?> assetOrigins) {
    this(assetOrigins, AssetFileExporter::deleteTree,
        (task, delay, unit) -> {
          var future = CLEANER.schedule(task, delay, unit);
          return () -> future.cancel(false);
        });
  }

  AssetTransferable(
      Collection<?> assetOrigins, DirectoryCleanup directoryCleanup,
      CleanupScheduler cleanupScheduler) {
    this.assetOrigins = assetOrigins.stream()
        .filter(java.util.Objects::nonNull)
        .map(Object.class::cast)
        .toList();
    this.directoryCleanup = directoryCleanup;
    this.cleanupScheduler = cleanupScheduler;
  }

  public static List<Object> getAssets(Object payload) {
    if (payload instanceof Collection<?> collection) {
      return collection.stream().filter(java.util.Objects::nonNull).map(Object.class::cast).toList();
    }
    return payload == null ? List.of() : List.of(payload);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    List<DataFlavor> flavors = new ArrayList<>();
    flavors.add(ASSET_FLAVOR);
    if (this.assetOrigins.stream().anyMatch(AssetFileExporter::supports)) {
      flavors.add(DataFlavor.javaFileListFlavor);
    }
    if (this.assetOrigins.size() == 1 && this.assetOrigins.getFirst() instanceof SpritesheetResource) {
      flavors.add(DataFlavor.imageFlavor);
    }
    return flavors.toArray(DataFlavor[]::new);
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    if (ASSET_FLAVOR.equals(flavor)) {
      return true;
    }
    if (DataFlavor.javaFileListFlavor.equals(flavor)) {
      return this.assetOrigins.stream().anyMatch(AssetFileExporter::supports);
    }
    return DataFlavor.imageFlavor.equals(flavor)
        && this.assetOrigins.size() == 1
        && this.assetOrigins.getFirst() instanceof SpritesheetResource;
  }

  @Override
  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    if (ASSET_FLAVOR.equals(flavor)) {
      return this.assetOrigins;
    }
    if (DataFlavor.imageFlavor.equals(flavor)
        && this.assetOrigins.getFirst() instanceof SpritesheetResource resource) {
      Image image = AssetFileExporter.getSpritesheetImage(resource);
      if (image == null) {
        throw new IOException("Spritesheet has no image data");
      }
      return image;
    }
    return exportedFiles();
  }

  private synchronized List<File> exportedFiles() throws IOException {
    if (this.exportedFiles != null) {
      return this.exportedFiles;
    }
    Path directory = Files.createTempDirectory("utiliti-resource-");
    this.exportDirectory = directory;
    if (!this.clipboardOwned) {
      scheduleCleanup(directory, 10, TimeUnit.MINUTES, 1);
    }
    List<Path> paths = new ArrayList<>();
    for (int i = 0; i < this.assetOrigins.size(); i++) {
      Object asset = this.assetOrigins.get(i);
      if (!AssetFileExporter.supports(asset)) {
        continue;
      }
      Path assetDirectory = Files.createDirectory(directory.resolve(Integer.toString(i)));
      paths.addAll(AssetFileExporter.export(asset, assetDirectory));
    }
    List<File> files = paths.stream().map(Path::toFile).toList();
    this.exportedFiles = List.copyOf(files);
    return this.exportedFiles;
  }

  @Override
  public synchronized void close() {
    if (this.clipboardOwned) {
      return;
    }
    scheduleCleanup(30, TimeUnit.SECONDS);
  }

  public synchronized void ownClipboard() {
    this.clipboardOwned = true;
    cancelScheduledCleanup();
  }

  @Override
  public synchronized void lostOwnership(Clipboard clipboard, Transferable contents) {
    this.clipboardOwned = false;
    scheduleCleanup(30, TimeUnit.SECONDS);
  }

  private void scheduleCleanup(long delay, TimeUnit unit) {
    Path directory = this.exportDirectory;
    if (directory != null) {
      scheduleCleanup(directory, delay, unit, 1);
    }
  }

  private synchronized void scheduleCleanup(
      Path directory, long delay, TimeUnit unit, int attempt) {
    cancelScheduledCleanup();
    long generation = this.cleanupGeneration;
    this.scheduledCleanup = this.cleanupScheduler.schedule(
        () -> runCleanup(directory, attempt, generation), delay, unit);
  }

  private synchronized void runCleanup(Path directory, int attempt, long generation) {
    if (generation != this.cleanupGeneration
        || directory == null
        || this.exportDirectory == null
        || !directory.equals(this.exportDirectory)) {
      return;
    }
    this.scheduledCleanup = null;
    if (this.clipboardOwned) {
      return;
    }
    if (this.directoryCleanup.delete(directory)) {
      this.exportDirectory = null;
      this.exportedFiles = null;
      this.cleanupGeneration++;
    } else if (attempt < MAX_CLEANUP_ATTEMPTS) {
      scheduleCleanup(directory, 1L << (attempt - 1), TimeUnit.SECONDS, attempt + 1);
    }
  }

  private void cancelScheduledCleanup() {
    this.cleanupGeneration++;
    if (this.scheduledCleanup != null) {
      this.scheduledCleanup.cancel();
      this.scheduledCleanup = null;
    }
  }

  synchronized void cleanupNow() {
    Path directory = this.exportDirectory;
    cancelScheduledCleanup();
    this.clipboardOwned = false;
    runCleanup(directory, MAX_CLEANUP_ATTEMPTS, this.cleanupGeneration);
  }

  synchronized boolean hasScheduledCleanup() {
    return this.scheduledCleanup != null;
  }

  @FunctionalInterface
  interface DirectoryCleanup {
    boolean delete(Path directory);
  }

  @FunctionalInterface
  interface CleanupScheduler {
    CleanupTask schedule(Runnable task, long delay, TimeUnit unit);
  }

  @FunctionalInterface
  interface CleanupTask {
    void cancel();
  }
}
