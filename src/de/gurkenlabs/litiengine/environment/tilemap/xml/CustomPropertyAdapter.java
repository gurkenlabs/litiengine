package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;

public class CustomPropertyAdapter extends XmlAdapter<CustomPropertyAdapter.PropertyList, Map<String, ICustomProperty>> {
  private static class PropertyType {
    private static final String STRING = "string";
    private static final String FLOAT = "float";
    private static final String INT = "int";
    private static final String BOOL = "bool";
    private static final String FILE = "file";
    private static final String COLOR = "color";

    private static String[] values() {
      return new String[] { STRING, FLOAT, INT, BOOL, FILE, COLOR };
    }

    private static boolean isValid(String type) {
      for (String valid : values()) {
        if (valid.equalsIgnoreCase(type)) {
          return true;
        }
      }

      return false;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  static class Property implements Comparable<Property> {
    @XmlAttribute
    String name;
    @XmlAttribute
    String type;
    @XmlAttribute
    String value;
    @XmlValue
    String contents;

    Property() {
    }

    Property(String name, String type) {
      this.name = name;
      this.type = type == null || !PropertyType.isValid(type) ? PropertyType.STRING : type;
    }

    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller u, Object parent) {
      if (this.type == null) {
        this.type = PropertyType.STRING;
      }
    }

    @SuppressWarnings("unused")
    private void beforeMarshal(Marshaller m) {
      if (this.type.equals(PropertyType.STRING)) {
        this.type = null;
      }
    }

    @Override
    public int compareTo(Property o) {
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

  @XmlAccessorType(XmlAccessType.FIELD)
  static class PropertyList {
    @XmlElement(name = "property")
    List<Property> properties;

    PropertyList() {
    }

    PropertyList(List<Property> properties) {
      this.properties = properties;
    }
  }

  @Override
  public Map<String, ICustomProperty> unmarshal(PropertyList v) {
    Map<String, ICustomProperty> map = new Hashtable<>(v.properties.size()); // use hashtable to reject null keys/values
    for (Property property : v.properties) {
      map.put(property.name, new CustomProperty(property.type, property.value != null ? property.value : property.contents));
    }
    return map;
  }

  @Override
  public PropertyList marshal(Map<String, ICustomProperty> v) {
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
      list.add(saved);
    }

    Collections.sort(list);
    return new PropertyList(list);
  }
}
