package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

/**
 * The Interface IPhysicsEngine.
 */
public interface IPhysicsEngine {

  public void add(ICollisionEntity entity);

  public void add(Rectangle2D staticCollisionBox);

  public void clear();

  public boolean collides(double x, double y);

  public Point2D collides(Line2D rayCast);

  public boolean collides(Point2D point);

  public boolean collides(Rectangle2D rect);

  /**
   * Checks whether the specified rectangle collides with anything.
   * 
   * @param rect
   *          The rectangle to check the collision for.
   * @param collisionType
   *          use the following flags
   *          <ul>
   *          <li>COLLTYPE_ENTITY</li>
   *          <li>COLLTYPE_STATIC</li>
   *          <li>COLLTYPE_ALL</li>
   *          </ul>
   * @return
   */
  public boolean collides(Rectangle2D rect, int collisionType);

  public List<ICollisionEntity> collidesWithEntites(Rectangle2D rect);

  public List<Rectangle2D> getAllCollisionBoxes();

  public List<ICollisionEntity> getCollisionEntities();

  public List<Rectangle2D> getStaticCollisionBoxes();

  /**
   * Moves the specified entity by the delta in the direction of the angle.
   *
   * @param entity
   *          the entity
   * @param angle
   *          the angle
   * @param delta
   *          the delta
   * @return true, if successful, false if the physics engine detected a
   *         collision.
   */
  public boolean move(IMovableEntity entity, double angle, double delta);

  public boolean move(IMovableEntity entity, double x, double y, float delta);

  public boolean move(IMovableEntity entity, float delta);

  public boolean move(final IMovableEntity entity, Point2D newPosition);

  /**
   * Moves the specified entity towards target by the delta.
   *
   * @param entity
   * @param target
   * @param delta
   * @return
   */
  public boolean move(IMovableEntity entity, Point2D target, float delta);

  public void remove(ICollisionEntity entity);

  public void remove(Rectangle2D staticCollisionBox);

  public void setBounds(Rectangle2D environmentBounds);
}
