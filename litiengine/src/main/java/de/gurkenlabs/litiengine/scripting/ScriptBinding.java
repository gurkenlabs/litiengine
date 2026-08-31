package de.gurkenlabs.litiengine.scripting;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Configures one script attachment to a game, environment, or entity.
///
/// Bindings reference a [ScriptDefinition] by identifier and supply instance-specific parameters.
/// Lower order values attach first. The parameter-value list is the live JAXB representation;
/// [#getParameters()] returns a convenient snapshot keyed by name.
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScriptBinding {
  @XmlAttribute(required = true) private String script;
  @XmlAttribute private boolean enabled = true;
  @XmlAttribute private int order;
  @XmlElementWrapper(name = "parameters") @XmlElement(name = "parameter")
  private final List<ScriptParameterValue> parameters = new ArrayList<>();

  /// Creates an enabled binding without a script identifier.
  public ScriptBinding() {}

  /// Creates an enabled binding for a script.
  ///
  /// @param script The definition identifier.
  public ScriptBinding(String script) {
    this.script = script;
  }

  /// Creates a binding for a script.
  ///
  /// @param script The definition identifier.
  /// @param enabled Whether the binding should attach.
  public ScriptBinding(String script, boolean enabled) {
    this.script = script;
    this.enabled = enabled;
  }

  /// Creates an independent copy suitable for controller ownership.
  ///
  /// @param binding The binding to copy.
  public ScriptBinding(ScriptBinding binding) {
    this.script = binding.script;
    this.enabled = binding.enabled;
    this.order = binding.order;
    binding.parameters.forEach(parameter -> this.parameters.add(new ScriptParameterValue(parameter.getName(), parameter.getValue())));
  }

  /// Returns the referenced definition identifier.
  /// @return The script identifier.
  public String getScript() { return this.script; }

  /// Returns whether this binding participates in attachment.
  /// @return `true` when enabled.
  public boolean isEnabled() { return this.enabled; }

  /// Returns the attachment order.
  /// @return The order; lower values attach first.
  public int getOrder() { return this.order; }

  /// Returns the live JAXB parameter list.
  /// @return The mutable parameter-value list.
  public List<ScriptParameterValue> getParameterValues() { return this.parameters; }

  /// Sets the referenced definition identifier.
  /// @param script The script identifier.
  public void setScript(String script) { this.script = script; }

  /// Enables or disables this binding.
  /// @param enabled Whether it should attach.
  public void setEnabled(boolean enabled) { this.enabled = enabled; }

  /// Sets the attachment order.
  /// @param order The order; lower values attach first.
  public void setOrder(int order) { this.order = order; }

  /// Returns the configured parameters keyed by name.
  ///
  /// Later duplicate names replace earlier values.
  ///
  /// @return A mutable snapshot of named parameter values.
  public Map<String, String> getParameters() {
    Map<String, String> values = new LinkedHashMap<>();
    for (ScriptParameterValue parameter : this.parameters) {
      if (parameter.getName() != null) values.put(parameter.getName(), parameter.getValue());
    }
    return values;
  }

  /// Adds or replaces a named parameter.
  ///
  /// @param name The non-null parameter name.
  /// @param value The parameter value; may be `null`.
  /// @throws NullPointerException if `name` is `null`.
  public void setParameter(String name, String value) {
    this.parameters.stream().filter(parameter -> name.equals(parameter.getName())).findFirst()
      .ifPresentOrElse(parameter -> parameter.setValue(value), () -> this.parameters.add(new ScriptParameterValue(name, value)));
  }
}
