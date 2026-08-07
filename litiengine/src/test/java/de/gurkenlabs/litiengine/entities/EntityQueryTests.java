package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityQueryTests {
  @Test
  void combinesGameplayFiltersAndDeterministicDistanceOrdering() {
    Creature source = creature("source", 1, 0, 0);
    Creature nearEnemy = creature("near", 2, 20, 0);
    nearEnemy.addTag("guard");
    Creature farEnemy = creature("far", 2, 80, 0);
    farEnemy.addTag("guard");
    Creature ally = creature("ally", 1, 10, 0);
    ally.addTag("guard");

    List<Creature> result = new EntityQuery<>(List.of(farEnemy, ally, nearEnemy))
      .tagged("guard")
      .alive()
      .enemyOf(source)
      .within(source.getCenter(), 100)
      .nearestTo(source.getCenter())
      .list();

    assertEquals(List.of("near", "far"), result.stream().map(Creature::getName).toList());
  }

  private static Creature creature(String name, int team, double x, double y) {
    Creature creature = new Creature("test");
    creature.setName(name);
    creature.setTeam(team);
    creature.setLocation(new Point2D.Double(x, y));
    return creature;
  }
}
