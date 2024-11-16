package de.gurkenlabs.litiengine.configuration;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 * A custom implementation of the Properties class that sorts the keys and strips the first line of comments when storing.
 */
class CleanProperties extends Properties {
  @Serial private static final long serialVersionUID = 7567765340218227372L;

  /**
   * Returns an enumeration of the keys in this property list, sorted in ascending order.
   *
   * @return an enumeration of the keys in this property list.
   */
  @Override
  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(new TreeSet<>(super.keySet()));
  }

  /**
   * Writes this property list (key and element pairs) in this Properties table to the output stream in a format suitable for loading into a
   * Properties table using the load method. This implementation strips the first line of comments.
   *
   * @param out      an output stream.
   * @param comments a description of the property list.
   * @throws IOException if an I/O error occurs.
   */
  @Override
  public void store(final OutputStream out, final String comments) throws IOException {
    super.store(new StripFirstLineStream(out), null);
  }

  /**
   * A custom FilterOutputStream that strips the first line of output.
   */
  private static class StripFirstLineStream extends FilterOutputStream {
    private boolean firstlineseen = false;

    /**
     * Constructs a new StripFirstLineStream.
     *
     * @param out the underlying output stream.
     */
    public StripFirstLineStream(final OutputStream out) {
      super(out);
    }

    /**
     * Writes the specified byte to this output stream. This implementation skips the first line.
     *
     * @param b the byte to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
      if (firstlineseen) {
        out.write(b);
      } else if (b == '\n') {
        firstlineseen = true;
      }
    }

    /**
     * Writes len bytes from the specified byte array starting at offset off to this output stream. This implementation skips the first line.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      while (!firstlineseen) {
        if (b[off++] == '\n') {
          firstlineseen = true;
        }
        if (--len == 0) {
          return;
        }
      }
      out.write(b, off, len);
    }
  }
}
