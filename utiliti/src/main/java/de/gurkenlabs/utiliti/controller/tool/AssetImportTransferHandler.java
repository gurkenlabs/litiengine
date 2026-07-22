package de.gurkenlabs.utiliti.controller.tool;

import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.TransferHandler;

public class AssetImportTransferHandler extends TransferHandler {
  private final Consumer<Path[]> fileImporter;
  private final Consumer<BufferedImage> imageImporter;

  public AssetImportTransferHandler(
      Consumer<Path[]> fileImporter, Consumer<BufferedImage> imageImporter) {
    this.fileImporter = fileImporter;
    this.imageImporter = imageImporter;
  }

  @Override
  public boolean canImport(TransferSupport support) {
    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        || uriListFlavor(support.getDataFlavors()) != null
        || support.isDataFlavorSupported(DataFlavor.imageFlavor);
  }

  @Override
  public boolean importData(TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }
    Transferable transferable = support.getTransferable();
    try {
      if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
        if (!(data instanceof List<?> values)) {
          return false;
        }
        Path[] paths = values.stream()
            .filter(File.class::isInstance)
            .map(File.class::cast)
            .map(File::toPath)
            .toArray(Path[]::new);
        if (paths.length == 0) {
          return false;
        }
        this.fileImporter.accept(paths);
        return true;
      }
      DataFlavor uriListFlavor = uriListFlavor(support.getDataFlavors());
      if (uriListFlavor != null) {
        Path[] paths = readUriList(uriListFlavor.getReaderForText(transferable));
        if (paths.length == 0) {
          return false;
        }
        this.fileImporter.accept(paths);
        return true;
      }
      Object data = transferable.getTransferData(DataFlavor.imageFlavor);
      if (!(data instanceof Image image)) {
        return false;
      }
      BufferedImage bufferedImage = Imaging.toBufferedImage(image);
      if (bufferedImage == null) {
        return false;
      }
      this.imageImporter.accept(bufferedImage);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private static DataFlavor uriListFlavor(DataFlavor[] flavors) {
    for (DataFlavor flavor : flavors) {
      if (flavor.isFlavorTextType()
          && "text".equalsIgnoreCase(flavor.getPrimaryType())
          && "uri-list".equalsIgnoreCase(flavor.getSubType())) {
        return flavor;
      }
    }
    return null;
  }

  private static Path[] readUriList(Reader reader) throws java.io.IOException {
    List<Path> paths = new ArrayList<>();
    try (BufferedReader lines = new BufferedReader(reader)) {
      String line;
      while ((line = lines.readLine()) != null) {
        line = line.replace("\0", "").trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        try {
          URI uri = URI.create(line);
          if ("file".equalsIgnoreCase(uri.getScheme())) {
            paths.add(Path.of(uri));
          }
        } catch (IllegalArgumentException ignored) {
          // Ignore malformed entries while retaining valid files from the same payload.
        }
      }
    }
    return paths.toArray(Path[]::new);
  }
}
