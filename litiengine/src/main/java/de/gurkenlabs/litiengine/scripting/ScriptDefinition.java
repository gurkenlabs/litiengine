package de.gurkenlabs.litiengine.scripting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Describes a reusable script source and its implementation class.
///
/// Definitions are registered with [ScriptManager#setDefinitions(Collection)] and referenced by
/// [ScriptBinding#getScript()]. Source resolution and compilation are delegated to the selected
/// [ScriptProvider].
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScriptDefinition {
  @XmlAttribute(required = true) private String id;
  @XmlAttribute private String name;
  @XmlAttribute(required = true) private String language = "java";
  @XmlAttribute private String source;
  @XmlAttribute(required = true) private String implementation;
  @XmlAttribute private ScriptHostType host = ScriptHostType.ENTITY;
  @XmlAttribute private String targetType;

  /// Creates an empty JAXB definition.
  public ScriptDefinition() {}

  /// Creates a definition.
  ///
  /// @param id The unique definition identifier.
  /// @param language The provider language identifier.
  /// @param source The source path or resource name.
  /// @param implementation The fully qualified implementation class name.
  /// @param host The required host category.
  public ScriptDefinition(String id, String language, String source, String implementation, ScriptHostType host) {
    this.id = id;
    this.language = language;
    this.source = source;
    this.implementation = implementation;
    this.host = host;
  }

  /// Creates an independent snapshot of another definition.
  ///
  /// @param definition The definition to copy.
  public ScriptDefinition(ScriptDefinition definition) {
    this(
      definition.id,
      definition.language,
      definition.source,
      definition.implementation,
      definition.host);
    this.name = definition.name;
    this.targetType = definition.targetType;
  }

  boolean hasSameConfiguration(ScriptDefinition definition) {
    return definition != null
      && Objects.equals(this.id, definition.id)
      && Objects.equals(this.name, definition.name)
      && Objects.equals(this.language, definition.language)
      && Objects.equals(this.source, definition.source)
      && Objects.equals(this.implementation, definition.implementation)
      && this.host == definition.host
      && Objects.equals(this.targetType, definition.targetType);
  }

  /// Validates fields required for registration.
  ///
  /// @return Human-readable validation errors; empty when valid.
  public List<String> validate() {
    List<String> errors = new ArrayList<>();
    if (this.id == null || this.id.isBlank()) errors.add("Script id must not be blank.");
    if (this.language == null || this.language.isBlank()) errors.add("Script language must not be blank.");
    if (this.implementation == null || this.implementation.isBlank()) errors.add("Script implementation must not be blank.");
    if (this.host == null) errors.add("Script host must be configured.");
    return List.copyOf(errors);
  }

  /// Returns the unique identifier.
  /// @return The identifier.
  public String getId() { return this.id; }
  /// Returns the display name.
  /// @return The name, or `null`.
  public String getName() { return this.name; }
  /// Returns the provider language identifier.
  /// @return The language identifier.
  public String getLanguage() { return this.language; }
  /// Returns the source path or resource name.
  /// @return The source, or `null` for precompiled code.
  public String getSource() { return this.source; }
  /// Returns the implementation class name.
  /// @return The fully qualified class name.
  public String getImplementation() { return this.implementation; }
  /// Returns the required host category.
  /// @return The host category.
  public ScriptHostType getHost() { return this.host; }
  /// Returns an optional required host class.
  /// @return The fully qualified type name, or `null`.
  public String getTargetType() { return this.targetType; }
  /// Sets the unique identifier.
  /// @param id The identifier.
  public void setId(String id) { this.id = id; }
  /// Sets the display name.
  /// @param name The name.
  public void setName(String name) { this.name = name; }
  /// Sets the provider language identifier.
  /// @param language The language identifier.
  public void setLanguage(String language) { this.language = language; }
  /// Sets the source path or resource name.
  /// @param source The source.
  public void setSource(String source) { this.source = source; }
  /// Sets the implementation class name.
  /// @param implementation The fully qualified class name.
  public void setImplementation(String implementation) { this.implementation = implementation; }
  /// Sets the required host category.
  /// @param host The host category.
  public void setHost(ScriptHostType host) { this.host = host; }
  /// Sets an optional required host class.
  /// @param targetType The fully qualified type name.
  public void setTargetType(String targetType) { this.targetType = targetType; }
}
