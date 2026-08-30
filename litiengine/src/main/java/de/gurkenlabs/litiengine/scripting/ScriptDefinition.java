package de.gurkenlabs.litiengine.scripting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Describes a reusable script source and its implementation class.
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScriptDefinition {
  @XmlAttribute(required = true) private String id;
  @XmlAttribute private String name;
  @XmlAttribute(required = true) private String language = "java";
  @XmlAttribute private String source;
  @XmlAttribute(required = true) private String implementation;
  @XmlAttribute private ScriptHostType host = ScriptHostType.ENTITY;
  @XmlAttribute private String targetType;

  public ScriptDefinition() {}

  public ScriptDefinition(String id, String language, String source, String implementation, ScriptHostType host) {
    this.id = id;
    this.language = language;
    this.source = source;
    this.implementation = implementation;
    this.host = host;
  }

  /// Creates an independent snapshot of another definition.
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

  public List<String> validate() {
    List<String> errors = new ArrayList<>();
    if (this.id == null || this.id.isBlank()) errors.add("Script id must not be blank.");
    if (this.language == null || this.language.isBlank()) errors.add("Script language must not be blank.");
    if (this.implementation == null || this.implementation.isBlank()) errors.add("Script implementation must not be blank.");
    if (this.host == null) errors.add("Script host must be configured.");
    return List.copyOf(errors);
  }

  public String getId() { return this.id; }
  public String getName() { return this.name; }
  public String getLanguage() { return this.language; }
  public String getSource() { return this.source; }
  public String getImplementation() { return this.implementation; }
  public ScriptHostType getHost() { return this.host; }
  public String getTargetType() { return this.targetType; }
  public void setId(String id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setLanguage(String language) { this.language = language; }
  public void setSource(String source) { this.source = source; }
  public void setImplementation(String implementation) { this.implementation = implementation; }
  public void setHost(ScriptHostType host) { this.host = host; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
}
