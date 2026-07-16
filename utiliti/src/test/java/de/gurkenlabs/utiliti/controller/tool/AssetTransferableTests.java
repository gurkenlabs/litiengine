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
import java.util.List;
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
}
