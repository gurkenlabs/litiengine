package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.scripting.ui.ScriptUiOverlay;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/// Base implementation that provides typed access to a script host, context, and global state.
///
/// Override the protected lifecycle and input callbacks needed by the script. Resources registered
/// through [#context()] are automatically released after [#detached()] completes.
///
/// @param <T> The required host type.
public abstract class AbstractScript<T> implements ScriptInstance {
  private ScriptContext<T> context;
  /// Game-wide values shared between scripts.
  protected final ScriptGlobals globals = Game.scripts().globals();

  @Override
  @SuppressWarnings("unchecked")
  public final void attach(ScriptContext<?> context) throws Exception {
    if (this.context != null) throw new IllegalStateException("The script is already attached.");
    this.context = (ScriptContext<T>) context;
    this.attached();
  }

  @Override
  public final void detach() throws Exception {
    if (this.context == null) return;
    try {
      this.detached();
    } finally {
      this.context.close();
      this.context = null;
    }
  }

  @Override
  public void render(Graphics2D g) throws Exception {
    this.onRender(g);
  }

  /// Called during the render pass for custom graphics rendering.
  ///
  /// @param g The current graphics context.
  /// @throws Exception if rendering fails.
  protected void onRender(Graphics2D g) throws Exception {}

  /// Called when a key is pressed.
  ///
  /// @param event The keyboard event.
  /// @throws Exception if handling fails.
  protected void onKeyPressed(KeyEvent event) throws Exception {}

  /// Called when a key is released.
  ///
  /// @param event The keyboard event.
  /// @throws Exception if handling fails.
  protected void onKeyReleased(KeyEvent event) throws Exception {}

  /// Called when a key is typed.
  ///
  /// @param event The keyboard event.
  /// @throws Exception if handling fails.
  protected void onKeyTyped(KeyEvent event) throws Exception {}

  /// Called when a mouse button is clicked (pressed and released).
  ///
  /// @param event The mouse event.
  /// @throws Exception if handling fails.
  protected void onMouseClicked(MouseEvent event) throws Exception {}

  /// Called when a mouse button is pressed.
  ///
  /// @param event The mouse event.
  /// @throws Exception if handling fails.
  protected void onMousePressed(MouseEvent event) throws Exception {}

  /// Called when a mouse button is released.
  ///
  /// @param event The mouse event.
  /// @throws Exception if handling fails.
  protected void onMouseReleased(MouseEvent event) throws Exception {}

  /// Called when the mouse is moved.
  ///
  /// @param event The mouse event.
  /// @throws Exception if handling fails.
  protected void onMouseMoved(MouseEvent event) throws Exception {}

  /// Called when the mouse wheel is rotated.
  ///
  /// @param event The wheel event.
  /// @throws Exception if handling fails.
  protected void onMouseWheel(MouseWheelEvent event) throws Exception {}

  /// Returns the current attachment context.
  ///
  /// @return The current context.
  /// @throws IllegalStateException if the script is not attached.
  protected final ScriptContext<T> context() {
    if (this.context == null) throw new IllegalStateException("The script is not attached.");
    return this.context;
  }

  /// Returns the current script host.
  ///
  /// @return The typed host.
  protected final T host() {
    return this.context().host();
  }

  /// Returns the host's current environment, or `null` for game scripts without one.
  ///
  /// @return The current environment, or `null` when unavailable.
  protected final Environment environment() {
    return this.context().environment();
  }

  /// Returns game-wide values shared between scripts.
  ///
  /// @return The shared global registry.
  protected final ScriptGlobals globals() {
    return this.globals;
  }

  /// Returns the managed input helper for key/mouse bindings and state queries.
  ///
  /// @return The input helper owned by this attachment.
  protected final ScriptInput input() {
    return this.context().input();
  }

  /// Returns the scripted UI overlay service owned by this context.
  ///
  /// @return The overlay owned by this attachment.
  protected final ScriptUiOverlay ui() {
    return this.context().ui();
  }

  /// Returns the active camera from the game world.
  ///
  /// @return The active camera, or `null` when unavailable.
  protected final ICamera camera() {
    return this.context().camera();
  }

  /// Returns a fluent spawner for creating entities in the current environment.
  ///
  /// @return A spawner bound to the current environment.
  protected final ScriptedSpawner spawner() {
    return this.context().spawner();
  }

  /// Spawns a creature with the given sprite prefix at the specified coordinates.
  ///
  /// @param spritePrefix The animation sprite prefix.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @return The spawned creature.
  protected final Creature spawnCreature(String spritePrefix, double x, double y) {
    return this.context().spawnCreature(spritePrefix, x, y);
  }

  /// Spawns a creature with the given sprite prefix at the specified location.
  ///
  /// @param spritePrefix The animation sprite prefix.
  /// @param location The location in map coordinates.
  /// @return The spawned creature.
  protected final Creature spawnCreature(String spritePrefix, Point2D location) {
    return this.context().spawnCreature(spritePrefix, location);
  }

  /// Spawns a prop with the given spritesheet at the specified coordinates.
  ///
  /// @param spriteSheet The spritesheet name.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @return The spawned prop.
  protected final Prop spawnProp(String spriteSheet, double x, double y) {
    return this.context().spawnProp(spriteSheet, x, y);
  }

  /// Spawns a prop with the given spritesheet at the specified location.
  ///
  /// @param spriteSheet The spritesheet name.
  /// @param location The location in map coordinates.
  /// @return The spawned prop.
  protected final Prop spawnProp(String spriteSheet, Point2D location) {
    return this.context().spawnProp(spriteSheet, location);
  }

  /// Spawns an entity of the given type at the specified coordinates.
  ///
  /// @param entityType The concrete entity type.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @param <E> The entity type.
  /// @return The spawned entity.
  protected final <E extends IEntity> E spawn(Class<E> entityType, double x, double y) {
    return this.context().spawn(entityType, x, y);
  }

  /// Spawns an entity of the given type at the specified location.
  ///
  /// @param entityType The concrete entity type.
  /// @param location The location in map coordinates.
  /// @param <E> The entity type.
  /// @return The spawned entity.
  protected final <E extends IEntity> E spawn(Class<E> entityType, Point2D location) {
    return this.context().spawn(entityType, location);
  }

  /// Spawns the given entity at the specified coordinates.
  ///
  /// @param entity The entity to add.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @param <E> The entity type.
  /// @return The supplied entity.
  protected final <E extends IEntity> E spawn(E entity, double x, double y) {
    return this.context().spawn(entity, x, y);
  }

  /// Spawns the given entity at the specified location.
  ///
  /// @param entity The entity to add.
  /// @param location The location in map coordinates.
  /// @param <E> The entity type.
  /// @return The supplied entity.
  protected final <E extends IEntity> E spawn(E entity, Point2D location) {
    return this.context().spawn(entity, location);
  }

  /// Called after the context is installed and the script becomes active.
  ///
  /// @throws Exception if initialization fails.
  protected void attached() throws Exception {}

  /// Called before context-owned resources are released.
  ///
  /// @throws Exception if cleanup fails.
  protected void detached() throws Exception {}

  final void dispatchKeyPressed(KeyEvent event) throws Exception {
    this.onKeyPressed(event);
  }

  final void dispatchKeyReleased(KeyEvent event) throws Exception {
    this.onKeyReleased(event);
  }

  final void dispatchKeyTyped(KeyEvent event) throws Exception {
    this.onKeyTyped(event);
  }

  final void dispatchMouseClicked(MouseEvent event) throws Exception {
    this.onMouseClicked(event);
  }

  final void dispatchMousePressed(MouseEvent event) throws Exception {
    this.onMousePressed(event);
  }

  final void dispatchMouseReleased(MouseEvent event) throws Exception {
    this.onMouseReleased(event);
  }

  final void dispatchMouseMoved(MouseEvent event) throws Exception {
    this.onMouseMoved(event);
  }

  final void dispatchMouseWheel(MouseWheelEvent event) throws Exception {
    this.onMouseWheel(event);
  }
}

