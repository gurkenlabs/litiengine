package de.gurkenlabs.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtilities {

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
}
