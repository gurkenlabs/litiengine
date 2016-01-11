package de.gurkenlabs.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
}
