package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.awt.Image;
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

public class AssetTransferable implements Transferable {
  public static final DataFlavor ASSET_FLAVOR = new DataFlavor(Object.class, "application/x-utiliti-asset");

  private final List<Object> assetOrigins;
  private List<File> exportedFiles;

  public AssetTransferable(Object assetOrigin) {
    this(List.of(assetOrigin));
  }

  public AssetTransferable(Collection<?> assetOrigins) {
    this.assetOrigins = assetOrigins.stream()
        .filter(java.util.Objects::nonNull)
        .map(Object.class::cast)
        .toList();
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
    directory.toFile().deleteOnExit();
    List<Path> paths = new ArrayList<>();
    for (int i = 0; i < this.assetOrigins.size(); i++) {
      Object asset = this.assetOrigins.get(i);
      if (!AssetFileExporter.supports(asset)) {
        continue;
      }
      Path assetDirectory = Files.createDirectory(directory.resolve(Integer.toString(i)));
      assetDirectory.toFile().deleteOnExit();
      paths.addAll(AssetFileExporter.export(asset, assetDirectory));
    }
    List<File> files = paths.stream().map(Path::toFile).toList();
    files.forEach(File::deleteOnExit);
    this.exportedFiles = List.copyOf(files);
    return this.exportedFiles;
  }
}
