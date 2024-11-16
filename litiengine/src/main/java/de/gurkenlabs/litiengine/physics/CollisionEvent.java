package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * This event is fired when a collision occurs between entities.
 */
public class CollisionEvent extends EventObject {
  @Serial private static final long serialVersionUID = 1916709290207855154L;

  private final transient List<ICollisionEntity> involved;

  /**
   * Constructs a new CollisionEvent.
   *
   * @param source   the source entity of the collision
   * @param involved the entities involved in the collision
   */
  public CollisionEvent(ICollisionEntity source, ICollisionEntity... involved) {
    super(source);
    this.involved = Collections.unmodifiableList(Arrays.asList(involved));
  }

  /**
   * Gets the list of entities involved in the collision.
   *
   * @return an unmodifiable list of involved entities
   */
  public List<ICollisionEntity> getInvolvedEntities() {
    return this.involved;
  }
}
