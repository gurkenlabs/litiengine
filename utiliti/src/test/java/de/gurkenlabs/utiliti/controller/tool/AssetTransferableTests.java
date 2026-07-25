package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AssetTransferableTests {

  @Test
  void testGetTransferDataFlavors() {
    Object asset = "test-asset";
    AssetTransferable transferable = new AssetTransferable(asset);
    DataFlavor[] flavors = transferable.getTransferDataFlavors();
    assertEquals(1, flavors.length);
    assertEquals(AssetTransferable.ASSET_FLAVOR, flavors[0]);
  }

  @Test
  void testIsDataFlavorSupported() {
    Object asset = "test-asset";
    AssetTransferable transferable = new AssetTransferable(asset);
    assertTrue(transferable.isDataFlavorSupported(AssetTransferable.ASSET_FLAVOR));
    assertFalse(transferable.isDataFlavorSupported(DataFlavor.stringFlavor));
    assertFalse(transferable.isDataFlavorSupported(DataFlavor.imageFlavor));
  }

  @Test
  void testGetTransferDataReturnsAsset() throws Exception {
    Object asset = "test-asset";
    AssetTransferable transferable = new AssetTransferable(asset);
    Object result = transferable.getTransferData(AssetTransferable.ASSET_FLAVOR);
    assertEquals(List.of(asset), result);
  }

  @Test
  void testGetTransferDataUnsupportedFlavor() {
    Object asset = "test-asset";
    AssetTransferable transferable = new AssetTransferable(asset);
    assertThrows(UnsupportedFlavorException.class,
        () -> transferable.getTransferData(DataFlavor.stringFlavor));
  }

  @Test
  void testAssetEquals() {
    Object asset1 = new Object();
    Object asset2 = asset1;
    AssetTransferable transferable = new AssetTransferable(asset1);
    assertNotNull(transferable);
    assertEquals(asset2, asset1);
  }

  @Test
  void spritesheetProvidesImageAndFileFlavors() throws Exception {
    BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(1, 1, Color.MAGENTA.getRGB());
    SpritesheetResource asset = new SpritesheetResource(source, "transfer", 2, 2);
    AssetTransferable transferable = new AssetTransferable(asset);

    assertTrue(transferable.isDataFlavorSupported(DataFlavor.imageFlavor));
    assertTrue(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
    BufferedImage image = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
    assertEquals(Color.MAGENTA.getRGB(), image.getRGB(1, 1));

    @SuppressWarnings("unchecked")
    List<File> first = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    @SuppressWarnings("unchecked")
    List<File> second = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    assertSame(first, second);
    assertEquals(1, first.size());
    assertTrue(first.getFirst().isFile());
    assertTrue(first.getFirst().getName().endsWith(".png"));
  }

  @Test
  void multipleAssetsProduceOneOrderedPayloadAndFileList() throws Exception {
    SpritesheetResource first = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "first", 1, 1);
    SpritesheetResource second = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "second", 1, 1);
    AssetTransferable transferable = new AssetTransferable(List.of(first, second));

    assertEquals(List.of(first, second),
        transferable.getTransferData(AssetTransferable.ASSET_FLAVOR));
    assertFalse(transferable.isDataFlavorSupported(DataFlavor.imageFlavor));
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    assertEquals(2, files.size());
    assertTrue(files.stream().allMatch(File::isFile));
  }

  @Test
  void cleanupRemovesOnlyOwnedTransferDirectory() throws Exception {
    SpritesheetResource asset = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "cleanup", 1, 1);
    AssetTransferable transferable = new AssetTransferable(asset);
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    java.nio.file.Path root = files.getFirst().toPath().getParent().getParent();

    transferable.cleanupNow();

    assertFalse(java.nio.file.Files.exists(root));
  }

  @Test
  void clipboardOwnershipIgnoresDragCompletionCleanup() throws Exception {
    SpritesheetResource asset = new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "clipboard", 1, 1);
    AssetTransferable transferable = new AssetTransferable(asset);
    transferable.ownClipboard();
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

    transferable.close();

    assertTrue(files.getFirst().isFile());
    transferable.cleanupNow();
  }

  @Test
  void clipboardCleanupIsScheduledOnlyAfterOwnershipIsLost() throws Exception {
    TestScheduler scheduler = new TestScheduler();
    AssetTransferable transferable = new AssetTransferable(
        List.of(asset("clipboard-owned")), AssetFileExporter::deleteTree, scheduler);
    transferable.ownClipboard();
    transferable.getTransferData(DataFlavor.javaFileListFlavor);

    transferable.close();
    assertFalse(transferable.hasScheduledCleanup());
    assertTrue(scheduler.tasks.isEmpty());

    transferable.lostOwnership(null, transferable);
    assertTrue(transferable.hasScheduledCleanup());
    assertEquals(30, scheduler.tasks.getFirst().delay);
    assertEquals(TimeUnit.SECONDS, scheduler.tasks.getFirst().unit);
    transferable.cleanupNow();
  }

  @Test
  void staleAbandonedCleanupCannotDeleteCurrentClipboardExport() throws Exception {
    TestScheduler scheduler = new TestScheduler();
    AtomicInteger cleanups = new AtomicInteger();
    AssetTransferable transferable = new AssetTransferable(
        List.of(asset("stale")), directory -> {
          cleanups.incrementAndGet();
          return AssetFileExporter.deleteTree(directory);
        }, scheduler);
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    TestTask staleTask = scheduler.tasks.getFirst();

    transferable.ownClipboard();
    staleTask.runnable.run();

    assertTrue(staleTask.cancelled);
    assertEquals(0, cleanups.get());
    assertTrue(files.getFirst().isFile());
    transferable.cleanupNow();
  }

  @Test
  void ownershipWaitsForCleanupAndCancelsItsRetry() throws Exception {
    TestScheduler scheduler = new TestScheduler();
    CountDownLatch cleanupEntered = new CountDownLatch(1);
    CountDownLatch finishCleanup = new CountDownLatch(1);
    CountDownLatch ownershipAttempted = new CountDownLatch(1);
    CountDownLatch ownershipFinished = new CountDownLatch(1);
    AtomicInteger cleanups = new AtomicInteger();
    AssetTransferable transferable = new AssetTransferable(
        List.of(asset("race")), directory -> {
          cleanups.incrementAndGet();
          cleanupEntered.countDown();
          try {
            if (!finishCleanup.await(5, TimeUnit.SECONDS)) {
              throw new AssertionError("cleanup test barrier timed out");
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
          }
          return false;
        }, scheduler);
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    java.nio.file.Path root = files.getFirst().toPath().getParent().getParent();
    transferable.close();
    TestTask cleanup = scheduler.tasks.getLast();

    Thread cleanupThread = new Thread(cleanup.runnable);
    cleanupThread.start();
    assertTrue(cleanupEntered.await(5, TimeUnit.SECONDS));
    Thread ownershipThread = new Thread(() -> {
      ownershipAttempted.countDown();
      transferable.ownClipboard();
      ownershipFinished.countDown();
    });
    ownershipThread.start();
    assertTrue(ownershipAttempted.await(5, TimeUnit.SECONDS));
    assertEquals(1, ownershipFinished.getCount());

    finishCleanup.countDown();
    cleanupThread.join(5000);
    ownershipThread.join(5000);
    assertEquals(0, ownershipFinished.getCount());
    TestTask retry = scheduler.tasks.getLast();
    assertTrue(retry.cancelled);

    retry.runnable.run();
    assertEquals(1, cleanups.get());
    assertTrue(files.getFirst().isFile());
    assertTrue(AssetFileExporter.deleteTree(root));
  }

  @Test
  void failedCleanupIsRetriedWithBoundedBackoff() throws Exception {
    TestScheduler scheduler = new TestScheduler();
    AtomicInteger attempts = new AtomicInteger();
    AssetTransferable transferable = new AssetTransferable(
        List.of(asset("retry")), directory -> {
          attempts.incrementAndGet();
          return false;
        }, scheduler);
    @SuppressWarnings("unchecked")
    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    java.nio.file.Path root = files.getFirst().toPath().getParent().getParent();
    transferable.close();

    for (int i = 0; i < 5; i++) {
      scheduler.tasks.getLast().runnable.run();
    }

    assertEquals(5, attempts.get());
    assertEquals(6, scheduler.tasks.size()); // abandoned cleanup, close, then four retries
    assertEquals(List.of(1L, 2L, 4L, 8L), scheduler.tasks.subList(2, 6).stream()
        .map(task -> task.delay).toList());
    assertFalse(transferable.hasScheduledCleanup());
    assertTrue(AssetFileExporter.deleteTree(root));
  }

  private static SpritesheetResource asset(String name) {
    return new SpritesheetResource(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), name, 1, 1);
  }

  private static final class TestScheduler implements AssetTransferable.CleanupScheduler {
    private final List<TestTask> tasks = new ArrayList<>();

    @Override
    public AssetTransferable.CleanupTask schedule(Runnable task, long delay, TimeUnit unit) {
      TestTask scheduled = new TestTask(task, delay, unit);
      this.tasks.add(scheduled);
      return scheduled;
    }
  }

  private static final class TestTask implements AssetTransferable.CleanupTask {
    private final Runnable runnable;
    private final long delay;
    private final TimeUnit unit;
    private boolean cancelled;

    private TestTask(Runnable runnable, long delay, TimeUnit unit) {
      this.runnable = runnable;
      this.delay = delay;
      this.unit = unit;
    }

    @Override
    public void cancel() {
      this.cancelled = true;
    }
  }
}
