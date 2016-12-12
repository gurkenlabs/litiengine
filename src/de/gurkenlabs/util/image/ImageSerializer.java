package de.gurkenlabs.util.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import de.gurkenlabs.util.io.FileUtilities;

public class ImageSerializer {
  public static final String FILE_FORMAT_PNG = "png";

  public static void saveImage(String fileName, BufferedImage image) {
    try {
      final File file = new File(fileName);
      final String extension = FileUtilities.getExtension(fileName);
      Iterator<ImageWriter> iter = null;
      if (canWriteFormat(extension)) {
        iter = ImageIO.getImageWritersByFormatName(extension);
      } else {
        iter = ImageIO.getImageWritersByFormatName(FILE_FORMAT_PNG);
      }
      
      final ImageWriter writer = iter.next();
      final ImageWriteParam iwp = writer.getDefaultWriteParam();

      file.getParentFile().mkdirs();
      try (final FileImageOutputStream output = new FileImageOutputStream(file.getAbsoluteFile())) {
        writer.setOutput(output);
        final IIOImage outimage = new IIOImage(image, null, null);
        writer.write(null, outimage, iwp);
        writer.dispose();
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static BufferedImage loadImage(String fileName) {
    final File file = new File(fileName);
    if (!file.exists()) {
      return null;
    }

    BufferedImage img;
    try {
      img = ImageIO.read(file);
      if (img == null) {
        return null;
      }

      final BufferedImage compatibleImg = ImageProcessing.getCompatibleImage(img.getWidth(), img.getHeight());
      compatibleImg.createGraphics().drawImage(img, 0, 0, null);
      compatibleImg.createGraphics().dispose();

      return compatibleImg;
    } catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static boolean canWriteFormat(String formatName) {
    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
    return iter.hasNext();
  }

}
