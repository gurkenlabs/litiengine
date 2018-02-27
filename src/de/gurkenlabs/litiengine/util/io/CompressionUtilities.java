package de.gurkenlabs.litiengine.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class CompressionUtilities {
  private static final Logger log = Logger.getLogger(CompressionUtilities.class.getName());

  private CompressionUtilities() {
  }

  public static byte[] compress(final byte[] data) {
    final Deflater deflater = new Deflater();
    deflater.setInput(data);
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    final byte[] buffer = new byte[1024];
    try {
      while (!deflater.finished()) {
        final int count = deflater.deflate(buffer); // returns the generated
                                                    // code...
        // index
        outputStream.write(buffer, 0, count);
      }
      outputStream.close();
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return outputStream.toByteArray();
  }

  public static byte[] decompress(final byte[] data) {
    final Inflater inflater = new Inflater();
    inflater.setInput(data);
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    final byte[] buffer = new byte[1024];
    try {
      while (!inflater.finished()) {
        int count;

        count = inflater.inflate(buffer);

        outputStream.write(buffer, 0, count);
      }

      outputStream.close();
    } catch (final DataFormatException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return outputStream.toByteArray();
  }

  public static void unzip(final InputStream zipfile, final File directory) throws IOException {
    final ZipInputStream zfile = new ZipInputStream(zipfile);
    ZipEntry entry;
    while ((entry = zfile.getNextEntry()) != null) {
      final File file = new File(directory, entry.getName());
      if (entry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try {
          StreamUtilities.copy(zfile, file);
        } catch (IOException e) {
          log.log(Level.SEVERE, e.getMessage(), e);
        }
      }
    }

    zfile.close();
  }

  public static void zip(File directory, final File zipfile) throws IOException {
    final URI base = directory.toURI();
    final Deque<File> queue = new LinkedList<>();
    queue.push(directory);
    try (final OutputStream out = new FileOutputStream(zipfile)) {
      final ZipOutputStream zout = new ZipOutputStream(out);
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (final File kid : directory.listFiles()) {
          String name = base.relativize(kid.toURI()).getPath();
          if (kid.isDirectory()) {
            queue.push(kid);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            StreamUtilities.copy(kid, zout);
            zout.closeEntry();
          }
        }
      }
    }
  }
}
