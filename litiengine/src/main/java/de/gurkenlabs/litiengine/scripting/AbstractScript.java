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
public abstract class AbstractScript<T> implements ScriptInstance {
  private ScriptContext<T> context;
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
  protected void onRender(Graphics2D g) throws Exception {}

  /// Called when a key is pressed.
  protected void onKeyPressed(KeyEvent event) throws Exception {}

  /// Called when a key is released.
  protected void onKeyReleased(KeyEvent event) throws Exception {}

  /// Called when a key is typed.
  protected void onKeyTyped(KeyEvent event) throws Exception {}

  /// Called when a mouse button is clicked (pressed and released).
  protected void onMouseClicked(MouseEvent event) throws Exception {}

  /// Called when a mouse button is pressed.
  protected void onMousePressed(MouseEvent event) throws Exception {}

  /// Called when a mouse button is released.
  protected void onMouseReleased(MouseEvent event) throws Exception {}

  /// Called when the mouse is moved.
  protected void onMouseMoved(MouseEvent event) throws Exception {}

  /// Called when the mouse wheel is rotated.
  protected void onMouseWheel(MouseWheelEvent event) throws Exception {}

  protected final ScriptContext<T> context() {
    if (this.context == null) throw new IllegalStateException("The script is not attached.");
    return this.context;
  }

  protected final T host() {
    return this.context().host();
  }

  /// Returns the host's current environment, or `null` for game scripts without one.
  protected final Environment environment() {
    return this.context().environment();
  }

  protected final ScriptGlobals globals() {
    return this.globals;
  }

  /// Returns the managed input helper for key/mouse bindings and state queries.
  protected final ScriptInput input() {
    return this.context().input();
  }

  /// Returns the scripted UI overlay service owned by this context.
  protected final ScriptUiOverlay ui() {
    return this.context().ui();
  }

  /// Returns the active camera from the game world.
  protected final ICamera camera() {
    return this.context().camera();
  }

  /// Returns a fluent spawner for creating entities in the current environment.
  protected final ScriptedSpawner spawner() {
    return this.context().spawner();
  }

  /// Spawns a creature with the given sprite prefix at the specified coordinates.
  protected final Creature spawnCreature(String spritePrefix, double x, double y) {
    return this.context().spawnCreature(spritePrefix, x, y);
  }

  /// Spawns a creature with the given sprite prefix at the specified location.
  protected final Creature spawnCreature(String spritePrefix, Point2D location) {
    return this.context().spawnCreature(spritePrefix, location);
  }

  /// Spawns a prop with the given spritesheet at the specified coordinates.
  protected final Prop spawnProp(String spriteSheet, double x, double y) {
    return this.context().spawnProp(spriteSheet, x, y);
  }

  /// Spawns a prop with the given spritesheet at the specified location.
  protected final Prop spawnProp(String spriteSheet, Point2D location) {
    return this.context().spawnProp(spriteSheet, location);
  }

  /// Spawns an entity of the given type at the specified coordinates.
  protected final <E extends IEntity> E spawn(Class<E> entityType, double x, double y) {
    return this.context().spawn(entityType, x, y);
  }

  /// Spawns an entity of the given type at the specified location.
  protected final <E extends IEntity> E spawn(Class<E> entityType, Point2D location) {
    return this.context().spawn(entityType, location);
  }

  /// Spawns the given entity at the specified coordinates.
  protected final <E extends IEntity> E spawn(E entity, double x, double y) {
    return this.context().spawn(entity, x, y);
  }

  /// Spawns the given entity at the specified location.
  protected final <E extends IEntity> E spawn(E entity, Point2D location) {
    return this.context().spawn(entity, location);
  }

  protected void attached() throws Exception {}

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

