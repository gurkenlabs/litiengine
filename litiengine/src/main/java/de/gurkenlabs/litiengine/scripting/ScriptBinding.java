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

/** Configures one script attachment to a game, environment, or entity. */
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScriptBinding {
  @XmlAttribute(required = true) private String script;
  @XmlAttribute private boolean enabled = true;
  @XmlAttribute private int order;
  @XmlElementWrapper(name = "parameters") @XmlElement(name = "parameter")
  private final List<ScriptParameterValue> parameters = new ArrayList<>();

  public ScriptBinding() {}

  public ScriptBinding(String script) {
    this.script = script;
  }

  public ScriptBinding(String script, boolean enabled) {
    this.script = script;
    this.enabled = enabled;
  }

  /** Creates an independent copy suitable for controller ownership. */
  public ScriptBinding(ScriptBinding binding) {
    this.script = binding.script;
    this.enabled = binding.enabled;
    this.order = binding.order;
    binding.parameters.forEach(parameter -> this.parameters.add(new ScriptParameterValue(parameter.getName(), parameter.getValue())));
  }

  public String getScript() { return this.script; }
  public boolean isEnabled() { return this.enabled; }
  public int getOrder() { return this.order; }
  public List<ScriptParameterValue> getParameterValues() { return this.parameters; }
  public void setScript(String script) { this.script = script; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public void setOrder(int order) { this.order = order; }

  public Map<String, String> getParameters() {
    Map<String, String> values = new LinkedHashMap<>();
    for (ScriptParameterValue parameter : this.parameters) {
      if (parameter.getName() != null) values.put(parameter.getName(), parameter.getValue());
    }
    return values;
  }

  public void setParameter(String name, String value) {
    this.parameters.stream().filter(parameter -> name.equals(parameter.getName())).findFirst()
      .ifPresentOrElse(parameter -> parameter.setValue(value), () -> this.parameters.add(new ScriptParameterValue(name, value)));
  }
}
