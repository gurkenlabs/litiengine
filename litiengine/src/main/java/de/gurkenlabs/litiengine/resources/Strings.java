package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.Game;
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

/**
 * This class provides methods to manage and retrieve localized strings from resource bundles. It supports different encodings and allows fetching
 * strings with or without formatting arguments.
 */
public final class Strings {
  public static final String DEFAULT_BUNDLE = "strings";

  private static final Logger log = Logger.getLogger(Strings.class.getName());

  // default encoding for properties is ISO_8859_1 see:
  // https://docs.oracle.com/javase/7/docs/api/java/util/Properties.html
  private Charset charset = StandardCharsets.ISO_8859_1;

  Strings() {
    Locale.setDefault(Locale.of("en", "US"));
  }

  /**
   * Sets the character encoding to be used for reading resource bundles.
   *
   * @param charset The character encoding to set.
   */
  public void setEncoding(Charset charset) {
    this.charset = charset;
  }

  /**
   * Retrieves the localized string for the specified key from the default resource bundle.
   *
   * @param key The key for the desired string.
   * @return The localized string corresponding to the specified key, or null if the key is null.
   */
  public String get(final String key) {
    if (key == null) {
      return null;
    }

    return this.getFrom(DEFAULT_BUNDLE, key);
  }

  /**
   * Retrieves the localized string for the specified key from the default resource bundle, with optional formatting arguments.
   *
   * @param key  The key for the desired string.
   * @param args The arguments to format the string with.
   * @return The localized string corresponding to the specified key, formatted with the provided arguments, or null if the key is null.
   */
  public String get(final String key, Object... args) {
    if (key == null) {
      return null;
    }

    return this.getFrom(DEFAULT_BUNDLE, key, args);
  }

  /**
   * Retrieves the localized string for the specified key from the specified resource bundle, with optional formatting arguments.
   *
   * @param bundleName The name of the resource bundle to retrieve the string from.
   * @param key        The key for the desired string.
   * @param args       The arguments to format the string with.
   * @return The localized string corresponding to the specified key, formatted with the provided arguments, or the key itself if the resource is not
   * found.
   */
  public String getFrom(final String bundleName, final String key, Object... args) {
    if (bundleName == null || key == null) {
      return null;
    }

    try {
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(bundleName, Game.config().client().getLocale());

      String value = defaultBundle.getString(key);

      String decodedValue =
        this.charset.equals(StandardCharsets.ISO_8859_1) ? value : new String(value.getBytes(StandardCharsets.ISO_8859_1), this.charset);
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
   * Get a list of strings from the specified raw text files. Strings are separated by a new line. <br>
   * <b>This method is not cached. Ever call will open up a new {@link InputStream} to read the strings from the text
   * file.</b>
   *
   * @param textFile The text file that will be retrieved.
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

  /**
   * Checks if the default resource bundle contains the specified key.
   *
   * @param key The key to check for.
   * @return True if the key is found in the default resource bundle, false otherwise.
   */
  public boolean contains(final String key) {
    return contains(DEFAULT_BUNDLE, key);
  }

  /**
   * Checks if the specified resource bundle contains the specified key.
   *
   * @param bundleName The name of the resource bundle to check.
   * @param key        The key to check for.
   * @return True if the key is found in the specified resource bundle, false otherwise.
   */
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
