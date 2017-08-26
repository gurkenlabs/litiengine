package de.gurkenlabs.litiengine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Resources {
  public static final String LOCALIZATION_RESOURCE_FOLDER = "localization/";
  public static final String DEFAULT_BUNDLE = "strings";
  private static final Logger log = Logger.getLogger(Resources.class.getName());
  static {
    Locale.setDefault(new Locale("en", "US"));
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
      final ResourceBundle defaultBundle = ResourceBundle.getBundle(LOCALIZATION_RESOURCE_FOLDER + bundleName, Game.getConfiguration().CLIENT.getLocale());
      final String text = defaultBundle.getString(key);
      return text;
    } catch (final MissingResourceException me) {
      final StringWriter sw = new StringWriter();
      me.printStackTrace(new PrintWriter(sw));
      final String stacktrace = sw.toString();
      log.severe(stacktrace);
    }

    return key;
  }
}
