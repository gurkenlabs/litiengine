package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import java.util.List;

/** Result of reading one script-binding scope without hiding malformed serialized data. */
public sealed interface BindingState {
  record Valid(List<ScriptBinding> bindings) implements BindingState {
    public Valid {
      bindings = bindings == null ? List.of() : bindings.stream().map(ScriptBinding::new).toList();
    }
  }

  record Invalid(String rawValue, String error) implements BindingState {
    public Invalid {
      rawValue = rawValue == null ? "" : rawValue;
      error = error == null || error.isBlank() ? "Invalid script assignment data." : error;
    }
  }
}
