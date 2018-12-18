package de.gurkenlabs.litiengine.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;

public class TriggerTests {
  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMADLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    Game.terminate();
  }
  
  
  @Test
  public void testInteractTrigger() {
    Trigger trigger = new Trigger(TriggerActivation.INTERACT, "testrigger", "testmessage");
    IEntity entity = mock(IEntity.class);
    when(entity.getMapId()).thenReturn(123);

    IEntity target = mock(IEntity.class);
    when(target.getMapId()).thenReturn(456);
    when(target.sendMessage(any(Object.class), any(String.class))).thenReturn("answer");
    trigger.addTarget(456);

    IEnvironment env = mock(IEnvironment.class);
    when(env.identifier()).thenReturn("env: #123");
    Game.world().loadEnvironment(env);
    when(env.get(456)).thenReturn(target);

    assertFalse(trigger.isActivated());

    trigger.sendMessage(entity, Trigger.INTERACT_MESSAGE);

    assertTrue(trigger.isActivated());
    verify(target, times(1)).sendMessage(trigger, trigger.getMessage());
  }
}
