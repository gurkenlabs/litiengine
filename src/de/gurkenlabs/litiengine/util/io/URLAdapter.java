package de.gurkenlabs.litiengine.util.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

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
    if (v == null) {
      return null;
    }

    if (this.base != null) {
      return new URL(base, v);
    }
    return new URL(v);
  }

  // TODO make a unit test for this
  @Override
  public String marshal(URL v) {
    if (v == null) {
      return null;
    }

    if (this.base == null || !this.base.getProtocol().equals(v.getProtocol()) || !Objects.equals(this.base.getAuthority(), v.getAuthority())) {
      return v.toExternalForm();
    }
    if (this.base.equals(v)) {
      return "#";
    }
    if (this.base.getFile().equals(v.getFile())) {
      return '#' + v.getRef();
    }

    String[] path1 = v.getFile().split("/");
    String[] path2 = this.base.getFile().split("/");
    int firstDiff = 0;
    while (firstDiff < path1.length && firstDiff < path2.length && path1[firstDiff].equals(path2[firstDiff]))
      firstDiff++;
    if (firstDiff == 0) {
      return v.getFile();
    }
    StringBuilder builder = new StringBuilder();
    for (int i = path2.length - 1; i > firstDiff; i--)
      builder.append("/..");
    for (int i = firstDiff; i < path1.length; i++) {
      builder.append('/');
      builder.append(path1[i]);
    }
    return builder.substring(1);
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
