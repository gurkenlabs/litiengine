package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class AStarGridTest {
    private Rectangle2D rectangle;
    private AStarGrid aStarGrid;
    private AStarNode node;

    @BeforeEach
    public void setup() {
        aStarGrid = new AStarGrid(10,20,5);
        rectangle = mock(Rectangle2D.class);
        node = mock(AStarNode.class);
        aStarGrid.getIntersectedNodes(rectangle);
        aStarGrid.updateWalkable(rectangle);
    }

    @Test
    public void testUpdateWalkable() {
        verify(rectangle, times(0)).intersects(rectangle);
    }


}
