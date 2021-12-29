package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EntityDistanceComparatorTests {
  @ParameterizedTest(name = "{0}: (x1={1}, y1={2}, x2={3}, y2={4}) = {5}")
  @CsvSource({
      "'equal-distance', 1.0d, 1.0d, -1.0d, -1.0d, 0",
      "'first-entity-closer', 1.0d, 1.0d, 2.0d, 2.0d, -1",
      "'second-entity-closer', 2.0d, 2.0d, 1.0d, 1.0d, 1"
  })
  void testDistanceComparison(
      String partition, double x1, double y1, double x2, double y2, int expectedResult) {
    // arrange
    EntityDistanceComparator comparator = new EntityDistanceComparator(new TestEntity());

    TestEntity entity1 = new TestEntity();
    TestEntity entity2 = new TestEntity();

    entity1.setLocation(new Point2D.Double(x1, y1));
    entity2.setLocation(new Point2D.Double(x2, y2));

    // act, assert
    assertEquals(expectedResult, comparator.compare(entity1, entity2));
  }

  private static class TestEntity extends Entity {
    protected TestEntity() {
      super();
    }
  }
}
