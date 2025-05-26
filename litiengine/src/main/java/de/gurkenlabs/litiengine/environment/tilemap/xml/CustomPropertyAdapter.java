package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.util.io.URLAdapter;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code CustomPropertyAdapter} class is an implementation of the {@link XmlAdapter} that facilitates the conversion between a
 * {@link PropertyList} and a {@link Map} of custom properties.
 *
 * <p>This adapter is used for XML serialization and deserialization of custom properties,
 * ensuring proper handling of property types, values, and locations.
 */
public class CustomPropertyAdapter extends XmlAdapter<CustomPropertyAdapter.PropertyList, Map<String, ICustomProperty>> {
  /**
   * Represents a custom property with attributes for name, type, value, and location.
   *
   * <p>This class is used for XML serialization and deserialization of individual properties.
   * It implements the {@link Comparable} interface to allow sorting by property name.
   */
  @XmlAccessorType(XmlAccessType.FIELD) static class Property implements Comparable<Property> {
    @XmlAttribute String name;
    @XmlAttribute String type;
    @XmlAttribute String value;
    @XmlValue String contents;
    @XmlTransient URL location;

    Property() {
      // keep for serialization
    }

    /**
     * Constructs a new {@code Property} instance with the specified name and type.
     *
     * <p>If the provided type is {@code null} or invalid, it defaults to {@code CustomPropertyType.STRING}.
     *
     * @param name the name of the property
     * @param type the type of the property, which must be a valid {@code CustomPropertyType}
     */
    Property(String name, String type) {
      this.name = name;
      this.type = type == null || !CustomPropertyType.isValid(type) ? CustomPropertyType.STRING : type;
    }

    @SuppressWarnings("unused") private void afterUnmarshal(Unmarshaller u, Object parent) throws MalformedURLException {
      if (this.type == null) {
        this.type = CustomPropertyType.STRING;
      }
      if (this.type.equals(CustomPropertyType.FILE)) {
        this.location = u.getAdapter(URLAdapter.class).unmarshal(this.value);
      }
    }

    @SuppressWarnings("unused") private void beforeMarshal(Marshaller m) throws URISyntaxException {
      if (this.type.equals(CustomPropertyType.STRING)) {
        this.type = null;
      }
      if (this.location != null) {
        this.value = m.getAdapter(URLAdapter.class).marshal(this.location);
      }
    }

    @Override public int compareTo(Property o) {
      if (o == null) {
        return 1;
      }

      if (o.name == null && this.name == null) {
        return 0;
      }

      if (this.name == null) {
        return -1;
      }

      return this.name.compareTo(o.name);
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD) static class PropertyList {
    @XmlElement(name = "property") List<Property> properties;

    PropertyList() {
      // keep for serialization
    }

    PropertyList(List<Property> properties) {
      this.properties = properties;
    }
  }

  @Override public Map<String, ICustomProperty> unmarshal(PropertyList v) {
    Map<String, ICustomProperty> map = HashMap.newHashMap(v.properties.size()); // use hashtable to reject null keys/values
    for (Property property : v.properties) {
      CustomProperty prop = new CustomProperty(property.type, property.value != null ? property.value : property.contents);
      if (property.location != null) {
        prop.setValue(property.location);
      }
      map.put(property.name, prop);
    }
    return map;
  }

  @Override public PropertyList marshal(Map<String, ICustomProperty> v) {
    if (v.isEmpty()) {
      return null;
    }
    List<Property> list = new ArrayList<>(v.size());
    for (Map.Entry<String, ICustomProperty> entry : v.entrySet()) {
      ICustomProperty property = entry.getValue();
      String value = property.getAsString();
      if (value == null || value.isEmpty()) {
        continue;
      }

      Property saved = new Property(entry.getKey(), property.getType());
      if (value.contains("\n")) {
        saved.contents = value;
      } else {
        saved.value = value;
      }
      saved.location = property.getAsFile();
      list.add(saved);
    }

    list.sort(null);
    return new PropertyList(list);
  }
}
