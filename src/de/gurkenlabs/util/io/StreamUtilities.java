package de.gurkenlabs.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtilities {

  public static void copy(final File file, final OutputStream out) throws IOException {
    final InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  public static void copy(final InputStream in, final File file) throws IOException {
    final OutputStream out = new FileOutputStream(file);
    try {
      copy(in, out);
    } finally {
      out.close();
    }
  }

  public static void copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[1024];

    while (true) {
      final int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  public static byte[] getByte(InputStream is) throws IOException {

    int len;
    int size = 1024;
    byte[] buf;

    if (is instanceof ByteArrayInputStream) {
      size = is.available();
      buf = new byte[size];
      len = is.read(buf, 0, size);
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      buf = new byte[size];
      while ((len = is.read(buf, 0, size)) != -1)
        bos.write(buf, 0, len);
      buf = bos.toByteArray();
    }
    return buf;
  }

  public static byte[] getBytes(final InputStream in) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    try {
      StreamUtilities.copy(in, buffer);
      return buffer.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new byte[0];
  }
}
