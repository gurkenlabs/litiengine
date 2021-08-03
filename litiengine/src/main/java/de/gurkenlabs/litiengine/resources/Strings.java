package de.gurkenlabs.litiengine.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;

public final class Strings {
  public static final String DEFAULT_BUNDLE = "strings";
  
  private static final Logger log = Logger.getLogger(Strings.class.getName());

  // default encoding for properties is ISO_8859_1 see: https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html
  private Charset charset = StandardCharsets.ISO_8859_1;

  Strings() {
    Locale.setDefault(new Locale("en", "US"));
  }

  public void setEncoding(Charset charset) {
    this.charset = charset;
  }

  public String get(final String key) {
    if (key == null) {
      return null;
    }

    return this.getFrom(DEFAULT_BUNDLE, key);
  }

  public String get(final String key, Object... args) {
    if (key == null) {
      return null;
    }

    return this.getFrom(DEFAULT_BUNDLE, key, args);
  }

  public String getFrom(final String bundleName, final String key, Object... args) {
    if (bundleName == null || key == null) {
      return null;
    }

    try {
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName, Game.config().client().getLocale());

      String value = defaultBundle.getString(key);

      String decodedValue = this.charset.equals(StandardCharsets.ISO_8859_1) ? value : new String(value.getBytes(StandardCharsets.ISO_8859_1), this.charset);
      if (args.length > 0) {
        return MessageFormat.format(decodedValue, args);
      }

      return decodedValue;
    } catch (final MissingResourceException e) {
      final StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
    }

    return key;
  }

  /**
   * Get a list of strings from the specified raw text files. Strings are
   * separated by a new line. <br>
   * <b>This method is not cached. Ever call will open up a new
   * {@link InputStream} to read the strings from the text file.</b>
   * 
   * @param textFile
   *          The text file that will be retrieved.
   * @return A list with all strings that are contained by the text file.
   */
  public String[] getList(String textFile) {
    if (textFile == null || textFile.isEmpty()) {
      return new String[0];
    }

    try (InputStream is = Resources.get(textFile)) {
      if (is == null) {
        return new String[0];
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      final List<String> strings = new ArrayList<>();
      String str = "";
      while ((str = reader.readLine()) != null) {
        if (str.startsWith("//")) {
          continue;
        }
        strings.add(str);
      }

      String[] stockArr = new String[strings.size()];
      return strings.toArray(stockArr);
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return new String[0];
  }

  public boolean contains(final String key) {
    return contains(DEFAULT_BUNDLE, key);
  }

  public boolean contains(final String bundleName, final String key) {
    try {
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName, Game.config().client().getLocale());
      return defaultBundle.containsKey(key);
    } catch (final MissingResourceException me) {
      final StringWriter sw = new StringWriter();
      me.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
    }

    return false;
  }
}
