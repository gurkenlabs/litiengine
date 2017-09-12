package de.gurkenlabs.litiengine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class Resources {
  public static final String LOCALIZATION_RESOURCE_FOLDER = "localization/";
  public static final String DEFAULT_BUNDLE = "strings";
  public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";
  public static final String ENCODING_UTF_8 = "UTF-8";

  private static String encoding = ENCODING_ISO_8859_1;
  private static final Logger log = Logger.getLogger(Resources.class.getName());
  static {
    Locale.setDefault(new Locale("en", "US"));
  }

  private Resources() {
  }

  public static void setEncoding(String newEncoding) {
    if (newEncoding == null || newEncoding.isEmpty()) {
      throw new IllegalArgumentException("The encoding must not be null or empty.");
    }

    encoding = newEncoding;
  }

  public static String get(final String key) {
    if (key == null) {
      return null;
    }
    return get(DEFAULT_BUNDLE, key);
  }

  public static String get(final String bundleName, final String key) {
    if (key == bundleName) {
      return null;
    }
    try {
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(LOCALIZATION_RESOURCE_FOLDER + bundleName, Game.getConfiguration().client().getLocale());

      String value = defaultBundle.getString(key);

      return encoding.equals(ENCODING_ISO_8859_1) ? value : new String(value.getBytes(ENCODING_ISO_8859_1), encoding);
    } catch (final MissingResourceException | UnsupportedEncodingException me) {
      final StringWriter sw = new StringWriter();
      me.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
    }

    return key;
  }
}
