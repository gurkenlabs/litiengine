package de.gurkenlabs.utiliti.controller.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
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
    assertEquals(asset, result);
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
}
