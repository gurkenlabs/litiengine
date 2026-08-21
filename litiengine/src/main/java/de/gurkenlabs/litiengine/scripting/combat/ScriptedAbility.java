package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.abilities.DynamicAbility;
import de.gurkenlabs.litiengine.entities.Creature;

/** An ability implementation configured fluently and executed via script callbacks. */
public class ScriptedAbility extends DynamicAbility {

  public ScriptedAbility(Creature executor, String name) {
    super(executor, name);
  }
}
