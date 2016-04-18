package de.gurkenlabs.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressionUtilities {
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
      e.printStackTrace();
    }

    final byte[] output = outputStream.toByteArray();
    return output;
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
    } catch (final DataFormatException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    final byte[] output = outputStream.toByteArray();
    return output;
  }

  public static void zip(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<File>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zout = new ZipOutputStream(out);
      res = zout;
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (File kid : directory.listFiles()) {
          String name = base.relativize(kid.toURI()).getPath();
          if (kid.isDirectory()) {
            queue.push(kid);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            copy(kid, zout);
            zout.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }

  public static void unzip(InputStream zipfile, File directory) throws IOException {
    ZipInputStream zfile = new ZipInputStream(zipfile);
    ZipEntry entry;
    while ((entry = zfile.getNextEntry()) != null) {
      File file = new File(directory, entry.getName());
      if (entry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try {
          copy(zfile, file);
        } finally {
        }
      }
    }

    zfile.close();
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  private static void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  private static void copy(InputStream in, File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try {
      copy(in, out);
    } finally {
      out.close();
    }
  }
}
