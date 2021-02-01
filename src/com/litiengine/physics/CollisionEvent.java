package com.litiengine.physics;

import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import com.litiengine.entities.ICollisionEntity;

public class CollisionEvent extends EventObject {
  private static final long serialVersionUID = 1916709290207855154L;
  
  private final transient List<ICollisionEntity> involved;

  public CollisionEvent(ICollisionEntity source, ICollisionEntity... involved) {
    super(source);
    this.involved = Collections.unmodifiableList(Arrays.asList(involved));
  }

  public List<ICollisionEntity> getInvolvedEntities() {
    return this.involved;
  }
}
