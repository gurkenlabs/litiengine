package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import java.util.List;

/** Resolved entity binding layers without flattening persisted defaults and instance overrides. */
public record EntityBindingState(List<ResolvedBinding> inherited, List<ResolvedBinding> overrides,
                                 List<ResolvedBinding> effective) {
  public EntityBindingState {
    inherited = copy(inherited);
    overrides = copy(overrides);
    effective = copy(effective);
  }

  private static List<ResolvedBinding> copy(List<ResolvedBinding> bindings) {
    return bindings == null ? List.of() : bindings.stream().map(ResolvedBinding::new).toList();
  }

  public enum BindingOrigin {
    INHERITED,
    OVERRIDE,
    INSTANCE_ONLY
  }

  public record ResolvedBinding(ScriptBinding binding, BindingOrigin origin) {
    public ResolvedBinding {
      binding = binding == null ? null : new ScriptBinding(binding);
    }

    public ResolvedBinding(ResolvedBinding binding) {
      this(binding.binding, binding.origin);
    }
  }
}
