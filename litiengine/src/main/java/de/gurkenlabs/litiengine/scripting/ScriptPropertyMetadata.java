package de.gurkenlabs.litiengine.scripting;

/// Editor-facing description of one configurable script field discovered from its implementation.
///
/// @param name The Java field name used for binding.
/// @param displayName The label intended for editors and tools.
/// @param description Explanatory text for users configuring the field.
/// @param category The optional group under which an editor should display the field.
/// @param type The configured editor type identifier, or the Java field type name when no override is configured.
/// @param defaultValue The default value represented as text.
/// @param min The inclusive suggested minimum, or [Double#NEGATIVE_INFINITY] when unspecified.
/// @param max The inclusive suggested maximum, or [Double#POSITIVE_INFINITY] when unspecified.
/// @param unit The optional unit displayed with numeric values.
/// @param required Whether a binding must provide a value.
public record ScriptPropertyMetadata(String name, String displayName, String description, String category,
                                     String type, String defaultValue, double min, double max, String unit,
                                     boolean required) {}
