package com.litiengine.configuration;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

class CleanProperties extends Properties {
  private static final long serialVersionUID = 7567765340218227372L;

  @Override
  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(new TreeSet<>(super.keySet()));
  }

  @Override
  public void store(final OutputStream out, final String comments) throws IOException {
    super.store(new StripFirstLineStream(out), null);
  }

  private static class StripFirstLineStream extends FilterOutputStream {
    private boolean firstlineseen = false;

    public StripFirstLineStream(final OutputStream out) {
      super(out);
    }

    @Override
    public void write(final int b) throws IOException {
      if (firstlineseen) {
        out.write(b);
      } else if (b == '\n') {
        firstlineseen = true;
      }
    }

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
