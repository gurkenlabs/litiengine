package de.gurkenlabs.litiengine.util;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for URI-related operations.
 */
public final class UriUtilities {
  private static final Logger log = Logger.getLogger(UriUtilities.class.getName());

  /**
   * Private constructor to prevent instantiation.
   * Throws UnsupportedOperationException if called.
   */
  private UriUtilities() {
    throw new UnsupportedOperationException();
  }

  /**
   * Opens a webpage in the default browser.
   *
   * @param uri the URI of the webpage to open
   * @return true if the webpage was successfully opened, false otherwise
   */
  public static boolean openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
        return true;
      } catch (Exception e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return false;
  }

  /**
   * Opens a webpage in the default browser.
   *
   * @param url the URL of the webpage to open
   * @return true if the webpage was successfully opened, false otherwise
   */
  public static boolean openWebpage(URL url) {
    try {
      return openWebpage(url.toURI());
    } catch (URISyntaxException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return false;
  }
}
