package de.gurkenlabs.litiengine.scripting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/// Reusable script bindings that are automatically applied to entities of a project type.
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntityScriptBinding {
  @XmlAttribute(required = true) private String targetType;
  @XmlAttribute private boolean inherited = true;
  @XmlElementWrapper(name = "scripts") @XmlElement(name = "binding")
  private final List<ScriptBinding> scripts = new ArrayList<>();

  public EntityScriptBinding() {}

  public EntityScriptBinding(String targetType) {
    this.targetType = targetType;
  }

  public EntityScriptBinding(Class<?> targetType) {
    this(targetType == null ? null : targetType.getName());
  }

  public EntityScriptBinding(EntityScriptBinding binding) {
    this.targetType = binding.targetType;
    this.inherited = binding.inherited;
    binding.scripts.forEach(script -> this.scripts.add(new ScriptBinding(script)));
  }

  public String getTargetType() { return this.targetType; }
  public boolean isInherited() { return this.inherited; }
  public List<ScriptBinding> getScripts() { return this.scripts; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
  public void setInherited(boolean inherited) { this.inherited = inherited; }
}
