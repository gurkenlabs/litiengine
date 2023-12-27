package de.gurkenlabs.litiengine.environment.tilemap.xml;

class CustomPropertyType {
  static final String STRING = "string";
  static final String FLOAT = "float";
  static final String INT = "int";
  static final String BOOL = "bool";
  static final String FILE = "file";
  static final String COLOR = "color";
  static final String OBJECT = "object";

  static boolean isValid(String type) {
    for (String valid : values()) {
      if (valid.equalsIgnoreCase(type)) {
        return true;
      }
    }

    return false;
  }

  private static String[] values() {
    return new String[] {STRING, FLOAT, INT, BOOL, FILE, COLOR, OBJECT};
  }
}
