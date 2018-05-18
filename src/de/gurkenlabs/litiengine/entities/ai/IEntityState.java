package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityProvider;
import de.gurkenlabs.litiengine.states.IState;

public interface IEntityState<T extends IEntity> extends IState, IEntityProvider {
}
