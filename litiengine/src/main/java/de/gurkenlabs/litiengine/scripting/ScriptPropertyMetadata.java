package de.gurkenlabs.litiengine.scripting;

/** Editor-facing description of one configurable script field discovered from its implementation. */
public record ScriptPropertyMetadata(String name, String displayName, String description, String category,
                                     String type, String defaultValue, double min, double max, String unit,
                                     boolean required) {}
