package de.gurkenlabs.utiliti.controller.tool;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class AssetTransferable implements Transferable {
  public static final DataFlavor ASSET_FLAVOR = new DataFlavor(Object.class, "application/x-utiliti-asset");

  private final Object assetOrigin;

  public AssetTransferable(Object assetOrigin) {
    this.assetOrigin = assetOrigin;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { ASSET_FLAVOR };
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return ASSET_FLAVOR.equals(flavor);
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return assetOrigin;
  }
}
