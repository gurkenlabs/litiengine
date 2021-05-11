package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityActionTests {

  private EntityAction entityAction;
  private boolean hasBeenPerformed;
  private final String ACTION_NAME = "actionSetHasBeenPerformed";

  @BeforeEach
  public void setUp() {
    hasBeenPerformed = false;
    entityAction = new EntityAction(ACTION_NAME, () -> hasBeenPerformed = true);
  }

  @Test
  public void testGetDescription() {
    // arrange
    String description = "Sets hasBeenPerformed to true if it has executed successfully.";
    entityAction.setDescription(description);

    // act
    String actualDescription = entityAction.getDescription();

    // assert
    assertEquals(description, actualDescription);
  }

  @Test
  public void testGetName() {
    // act
    String actualName = entityAction.getName();

    // assert
    assertEquals(ACTION_NAME, actualName);
  }

  @Test
  public void testPerform() {
    // act
    entityAction.perform();

    // assert
    assertTrue(hasBeenPerformed);
  }
}
