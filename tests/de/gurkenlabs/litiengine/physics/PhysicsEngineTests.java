package de.gurkenlabs.litiengine.physics;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.geom.Line2D;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PhysicsEngineTests {

    @BeforeEach
    void setUp(){
        Game.init(Game.COMMANDLINE_ARG_NOGUI);
    }

    @AfterEach
    void tearDown(){
        Game.physics().clear();
    }

    @ParameterizedTest
    @MethodSource("getRaycastCollisionArguments")
    void testRaycastCollision(Line2D rayLine, double expectedHitX, double expectedHitY){
        // arrange
        ICollisionEntity collisionBox1 = new CollisionBox(1, 3, 5, 5);
        ICollisionEntity collisionBox2 = new CollisionBox(7, 5, 1, 4);

        Game.physics().add(collisionBox1);
        Game.physics().add(collisionBox2);

        // act
        RaycastHit raycast = Game.physics().raycast(rayLine, Collision.ANY, collisionBox1);

        // assert
        assertEquals(expectedHitX, raycast.getPoint().getX());
        assertEquals(expectedHitY, raycast.getPoint().getY());
    }

    @Test
    void testRaycastNoCollisionNoOtherEntity(){
        // arrange
        ICollisionEntity collisionBox1 = new CollisionBox(1, 3, 5, 5);
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Game.physics().add(collisionBox1);

        // act
        RaycastHit raycast = Game.physics().raycast(line, Collision.ANY, collisionBox1);

        // assert
        assertNull(raycast);
    }

    @Test
    void testRaycastNoCollisionOtherEntity(){
        // arrange
        ICollisionEntity collisionBox1 = new CollisionBox(1, 3, 5, 5);
        ICollisionEntity collisionBox2 = new CollisionBox(10, 30, 5, 5);
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Game.physics().add(collisionBox1);
        Game.physics().add(collisionBox2);

        // act
        RaycastHit raycast = Game.physics().raycast(line, Collision.ANY, collisionBox1);

        // assert
        assertNull(raycast);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> getRaycastCollisionArguments(){
        return Stream.of(
                Arguments.of(new Line2D.Double(0, 0, 10, 10), 7, 7),
                Arguments.of(new Line2D.Double(10, 10, 0, 0), 8, 8)
        );
    }
}
