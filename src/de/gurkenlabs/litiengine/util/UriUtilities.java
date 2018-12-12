package de.gurkenlabs.litiengine.util;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UriUtilities {
  private static final Logger log = Logger.getLogger(UriUtilities.class.getName());

  private UriUtilities() {
    throw new UnsupportedOperationException();
  }

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

  public static boolean openWebpage(URL url) {
    try {
      return openWebpage(url.toURI());
    } catch (URISyntaxException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return false;
  }
}
