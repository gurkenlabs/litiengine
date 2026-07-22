package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssetImportTransferHandlerTest {
  @TempDir
  Path tempDir;

  @Test
  void importsClipboardFileList() throws Exception {
    Path file = java.nio.file.Files.createFile(this.tempDir.resolve("asset.png"));
    AtomicReference<Path[]> imported = new AtomicReference<>();
    AssetImportTransferHandler handler = new AssetImportTransferHandler(
        imported::set, image -> {});
    Transferable transferable = transferable(
        DataFlavor.javaFileListFlavor, List.of(file.toFile()));
    TransferHandler.TransferSupport support =
        new TransferHandler.TransferSupport(new JPanel(), transferable);

    assertTrue(handler.canImport(support));
    assertTrue(handler.importData(support));
    assertEquals(file, imported.get()[0]);
  }

  @Test
  void importsClipboardImage() {
    BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(1, 0, Color.GREEN.getRGB());
    AtomicReference<BufferedImage> imported = new AtomicReference<>();
    AssetImportTransferHandler handler = new AssetImportTransferHandler(
        paths -> {}, imported::set);
    TransferHandler.TransferSupport support = new TransferHandler.TransferSupport(
        new JPanel(), transferable(DataFlavor.imageFlavor, source));

    assertTrue(handler.importData(support));
    assertSame(source, imported.get());
  }

  @Test
  void importsLinuxUriList() throws Exception {
    Path first = java.nio.file.Files.createFile(this.tempDir.resolve("first asset.png"));
    Path second = java.nio.file.Files.createFile(this.tempDir.resolve("second.png"));
    DataFlavor flavor = new DataFlavor("text/uri-list;class=java.io.Reader");
    String payload = "# copied by file manager\r\n"
        + first.toUri().toASCIIString() + "\r\n"
        + "https://example.com/not-a-file.png\r\n"
        + "not a uri\r\n"
        + second.toUri().toASCIIString() + "\0\r\n";
    AtomicReference<Path[]> imported = new AtomicReference<>();
    AssetImportTransferHandler handler = new AssetImportTransferHandler(imported::set, image -> {});
    TransferHandler.TransferSupport support = new TransferHandler.TransferSupport(
        new JPanel(), transferable(flavor, new StringReader(payload)));

    assertTrue(handler.canImport(support));
    assertTrue(handler.importData(support));
    assertEquals(List.of(first, second), List.of(imported.get()));
  }

  @Test
  void importsStringUriListFlavor() throws Exception {
    Path file = java.nio.file.Files.createFile(this.tempDir.resolve("asset.png"));
    DataFlavor flavor = new DataFlavor("text/uri-list;class=java.lang.String");
    AtomicReference<Path[]> imported = new AtomicReference<>();
    AssetImportTransferHandler handler = new AssetImportTransferHandler(imported::set, image -> {});
    TransferHandler.TransferSupport support = new TransferHandler.TransferSupport(
        new JPanel(), transferable(flavor, file.toUri().toASCIIString()));

    assertTrue(handler.canImport(support));
    assertTrue(handler.importData(support));
    assertEquals(List.of(file), List.of(imported.get()));
  }

  private static Transferable transferable(DataFlavor flavor, Object value) {
    return new Transferable() {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {flavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor requested) {
        return flavor.equals(requested);
      }

      @Override
      public Object getTransferData(DataFlavor requested) {
        return value;
      }
    };
  }
}
