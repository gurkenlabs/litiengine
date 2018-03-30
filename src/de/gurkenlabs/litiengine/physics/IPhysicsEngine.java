package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;

/**
 * The Interface IPhysicsEngine.
 */
public interface IPhysicsEngine extends IUpdateable {

  public void add(ICollisionEntity entity);

  public void add(Rectangle2D staticCollisionBox);

  public void clear();

  public boolean collides(double x, double y);

  public boolean collides(double x, double y, ICollisionEntity collisionEntity);

  public boolean collides(double x, double y, CollisionType collisionType);

  public Point2D collides(Line2D rayCast);
  
  public Point2D collides(Line2D rayCast, CollisionType collisionType);

  public boolean collides(Point2D point);

  public boolean collides(Point2D point, CollisionType collisionType);

  public boolean collides(Point2D point, ICollisionEntity collisionEntity);

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
   * @return Returns true if the specified rectangle collides with any collision
   *         box of the specified type(s); otherwise false.
   */
  public boolean collides(Rectangle2D rect, CollisionType collisionType);

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
  public boolean move(IMobileEntity entity, double angle, double delta);

  public boolean move(IMobileEntity entity, double x, double y, float delta);

  public boolean move(IMobileEntity entity, float delta);

  public boolean move(final IMobileEntity entity, Point2D newPosition);

  public boolean move(IMobileEntity entity, Point2D target, float delta);

  public void remove(ICollisionEntity entity);

  public void remove(Rectangle2D staticCollisionBox);

  public void setBounds(Rectangle2D environmentBounds);

  public Rectangle2D getBounds();
}
