package de.gurkenlabs.litiengine.util.io;

import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public final class ImageSerializer {
  private static final Logger log = Logger.getLogger(ImageSerializer.class.getName());

  private ImageSerializer() {
    throw new UnsupportedOperationException();
  }

  public static BufferedImage loadImage(final String fileName) {
    final Path file = Paths.get(fileName);
    if (!Files.exists(file)) {
      return null;
    }

    BufferedImage img;
    try(InputStream is = Files.newInputStream(file)) {
      img = ImageIO.read(is);
      if (img == null) {
        return null;
      }

      final BufferedImage compatibleImg =
          Imaging.getCompatibleImage(img.getWidth(), img.getHeight());
      compatibleImg.createGraphics().drawImage(img, 0, 0, null);
      compatibleImg.createGraphics().dispose();

      return compatibleImg;
    } catch (final Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  public static void saveImage(final String fileName, final BufferedImage image) {
    saveImage(fileName, image, ImageFormat.PNG);
  }

  public static void saveImage(
      final String fileName, final BufferedImage image, ImageFormat imageFormat) {
    try {
      final Path file = Paths.get(fileName);
      final String extension = FileUtilities.getExtension(fileName);
      Iterator<ImageWriter> iter = null;
      if (canWriteFormat(extension)) {
        iter = ImageIO.getImageWritersByFormatName(extension);
      } else {
        iter = ImageIO.getImageWritersByFormatName(imageFormat.toString());
      }

      final ImageWriter writer = iter.next();
      final ImageWriteParam iwp = writer.getDefaultWriteParam();

      Files.createDirectories(file.getParent());
      try (final FileImageOutputStream output = new FileImageOutputStream(file.toAbsolutePath().toFile())) {
        writer.setOutput(output);
        final IIOImage outimage = new IIOImage(image, null, null);
        writer.write(null, outimage, iwp);
        writer.dispose();
      }
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static boolean canWriteFormat(final String formatName) {
    final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
    return iter.hasNext();
  }
}
