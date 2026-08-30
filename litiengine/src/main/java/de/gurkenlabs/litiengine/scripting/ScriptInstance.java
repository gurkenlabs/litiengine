package de.gurkenlabs.litiengine.scripting;

import java.awt.Graphics2D;

/// One configured runtime instance of a script.
///
/// Instances are created by a [CompiledScript] and owned by [ScriptManager]. Implementations should
/// acquire host-specific resources in [#attach(ScriptContext)] and release them in [#detach()].
public interface ScriptInstance {
  /// Attaches this instance to a host context.
  ///
  /// @param context The context for this attachment.
  /// @throws Exception if initialization fails.
  void attach(ScriptContext<?> context) throws Exception;

  /// Processes one game-loop update.
  ///
  /// @throws Exception if script execution fails.
  default void update() throws Exception {}

  /// Renders script-owned content.
  ///
  /// @param g The current graphics context.
  /// @throws Exception if script rendering fails.
  default void render(Graphics2D g) throws Exception {}

  /// Releases resources acquired by this attachment.
  ///
  /// @throws Exception if cleanup fails.
  void detach() throws Exception;
}

