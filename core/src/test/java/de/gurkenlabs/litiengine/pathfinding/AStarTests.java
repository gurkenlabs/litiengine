package de.gurkenlabs.litiengine.pathfinding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameTest;
import de.gurkenlabs.litiengine.entities.behavior.AStarGrid;
import de.gurkenlabs.litiengine.entities.behavior.AStarNode;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AStarTests {
  @BeforeAll
  public static void initGame() {

    // necessary because the environment need access to the game loop and other
    // stuff
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @AfterAll
  public static void terminateGame() {
    GameTest.terminateGame();
  }

  @BeforeEach
  public void initEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));
    when(map.getSizeInTiles()).thenReturn(new Dimension(10, 10));
    when(map.getOrientation()).thenReturn(MapOrientations.ORTHOGONAL);
    when(map.getRenderLayers()).thenReturn(new ArrayList<>());

    Game.world().loadEnvironment(new Environment(map));
  }

  @ParameterizedTest(name = "testGetCostsSimpleDirections: {0}, expectedCost={2}")
  @MethodSource("getCostsSimpleDirectionsArguments")
  void testGetCostsSimpleDirections(String name, AStarNode remoteNode, double expectedCost) {
    // arrange
    AStarNode originNode = new AStarNode(false, new Rectangle(50, 50, 10, 10), 5, 5);

    // act
    double actualCost = originNode.getCosts(remoteNode);

    // assert
    assertEquals(expectedCost, actualCost);
  }

  /**
   * This method is used to fill in the arguments of the parametrized test
   * {@link #testGetCostsSimpleDirections(String, AStarNode, double)}
   *
   * @return Test arguments
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getCostsSimpleDirectionsArguments() {
    return Stream.of(
        Arguments.of("down", new AStarNode(false, new Rectangle(50, 100, 10, 10), 5, 10), 5.0),
        Arguments.of("up", new AStarNode(false, new Rectangle(50, 40, 10, 10), 5, 4), 1.0),
        Arguments.of("left", new AStarNode(false, new Rectangle(30, 50, 10, 10), 3, 5), 2.0),
        Arguments.of("right", new AStarNode(false, new Rectangle(60, 50, 10, 10), 6, 5), 1.0));
  }

  @ParameterizedTest(name = "testGetCostsAdvancedDirections: {0}")
  @MethodSource("getCostsAdvancedDirectionsArguments")
  void testGetCostsAdvancedDirections(String name, AStarNode remoteNode) {
    // arrange
    AStarNode originNode = new AStarNode(false, new Rectangle(50, 50, 10, 10), 5, 5);

    // act
    double actualCost = originNode.getCosts(remoteNode);

    // assert
    assertTrue(actualCost > 1);
  }

  /**
   * This method is used to fill in the arguments of the parametrized test
   * {@link #testGetCostsAdvancedDirections(String, AStarNode)}
   *
   * @return Test arguments
   */
  @SuppressWarnings("unused")
  private static Stream<Arguments> getCostsAdvancedDirectionsArguments() {
    return Stream.of(
        Arguments.of("downLeft", new AStarNode(false, new Rectangle(40, 60, 10, 10), 4, 6)),
        Arguments.of("downRight", new AStarNode(false, new Rectangle(60, 60, 10, 10), 6, 6)),
        Arguments.of("upLeft", new AStarNode(false, new Rectangle(40, 40, 10, 10), 4, 4)),
        Arguments.of("upRight", new AStarNode(false, new Rectangle(60, 40, 10, 10), 6, 4)));
  }

  @Test
  void testGridPopulation() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    AStarGrid grid = new AStarGrid(320, 240, 4);

    AStarNode node = grid.getNode(new Point2D.Double(10, 10));
    AStarNode nullNode = grid.getNode(Integer.MAX_VALUE, Integer.MAX_VALUE);

    List<AStarNode> nodes = grid.getIntersectedNodes(new Rectangle(2, 2, 12, 6));

    assertNotNull(grid.getGrid());
    assertNotNull(grid.getGrid()[0][0]);

    assertEquals(2, node.getGridX());
    assertEquals(2, node.getGridY());
    assertEquals(12, nodes.size());
    assertNull(nullNode);
  }

  @Test
  void testGetNeighbors() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    AStarGrid grid = new AStarGrid(320, 240, 4);

    AStarNode node = grid.getNode(new Point2D.Double(10, 10));
    List<AStarNode> neighbors = grid.getNeighbors(node);

    grid.setAllowDiagonalMovement(false);
    List<AStarNode> neighbors2 = grid.getNeighbors(node);

    assertEquals(8, neighbors.size());
    assertEquals(4, neighbors2.size());
  }
}
