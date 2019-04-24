package de.gurkenlabs.litiengine.util.io;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class allows for absolute and relative URLs to be unmarshalled as Java URL objects.
 * 
 * @see XmlJavaTypeAdapter
 */
public class URLAdapter extends XmlAdapter<String, URL> {
  private URL base;

  /**
   * Constructs a new {@code URLAdapter}, with no additional properties. This constructor is called if no configured instance is available to an
   * unmarshaller.
   */
  public URLAdapter() {
    this(null);
  }

  /**
   * Constructs a new {@code URLAdapter}, configured to use relative URLs using the supplied URL as a base.
   * 
   * @param base
   *          The base URL to use
   * 
   * @see Unmarshaller#setAdapter(XmlAdapter)
   * @see Marshaller#setAdapter(XmlAdapter)
   */
  public URLAdapter(URL base) {
    this.base = base;
  }

  @Override
  public URL unmarshal(String v) throws MalformedURLException {
    if (this.base != null) {
      return new URL(base, v);
    }
    return new URL(v);
  }

  @Override
  public String marshal(URL v) throws URISyntaxException {
    if (this.base != null) {
      return v.toURI().relativize(this.base.toURI()).toASCIIString();
    }
    return v.toExternalForm();
  }

  /**
   * Gets the base URL used by this {@code URLAdapter} instance.
   * 
   * @return The base URL used, or {@code null} if this instance has not been configured for relative URLs
   */
  public URL getBaseURL() {
    return this.base;
  }
}
