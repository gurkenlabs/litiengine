package de.gurkenlabs.litiengine.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StreamUtilities {
  private static final Logger log = Logger.getLogger(StreamUtilities.class.getName());

  private StreamUtilities() {
    throw new UnsupportedOperationException();
  }

  public static void copy(final Path file, final OutputStream out) throws IOException {
    Files.copy(file, out);
  }

  public static void copy(final InputStream in, final Path file) throws IOException {
    Files.copy(in, file);
  }

  public static void copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[1024];

    if (in.markSupported()) {
      in.mark(Integer.MAX_VALUE);
    }

    while (true) {
      final int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }

    if (in.markSupported()) {
      in.reset();
    }
  }

  public static byte[] getBytes(final InputStream in) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    try {
      StreamUtilities.copy(in, buffer);
      return buffer.toByteArray();
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return new byte[0];
  }
}
