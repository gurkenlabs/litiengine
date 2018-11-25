package de.gurkenlabs.litiengine.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public final class Strings {
  public static final String DEFAULT_BUNDLE = "strings";
  public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";
  public static final String ENCODING_UTF_8 = "UTF-8";

  private static final Logger log = Logger.getLogger(ResourcesContainer.class.getName());

  private String encoding = ENCODING_ISO_8859_1;

  Strings() {
    Locale.setDefault(new Locale("en", "US"));
  }

  public void setEncoding(String newEncoding) {
    if (newEncoding == null || newEncoding.isEmpty()) {
      throw new IllegalArgumentException("The encoding must not be null or empty.");
    }

    encoding = newEncoding;
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
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName, Game.getConfiguration().client().getLocale());

      String value = defaultBundle.getString(key);

      String decodedValue = encoding.equals(ENCODING_ISO_8859_1) ? value : new String(value.getBytes(ENCODING_ISO_8859_1), encoding);
      if (args.length > 0) {
        return MessageFormat.format(decodedValue, args);
      }

      return decodedValue;
    } catch (final MissingResourceException | UnsupportedEncodingException me) {
      final StringWriter sw = new StringWriter();
      me.printStackTrace(new PrintWriter(sw));
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

    try (InputStream is = FileUtilities.getGameResource(textFile)) {
      if (is == null) {
        return new String[0];
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      final List<String> strings = new ArrayList<>();
      String str = "";
      while ((str = reader.readLine()) != null) {
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
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName, Game.getConfiguration().client().getLocale());
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