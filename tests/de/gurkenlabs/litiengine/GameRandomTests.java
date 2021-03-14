package de.gurkenlabs.litiengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameRandomTests {

    private GameRandom gameRandom;
    @BeforeEach
    public void initSeed(){
        gameRandom = new GameRandom();
        gameRandom.setSeed(1337);
    }

    @Test
    public void testNextIntMinEqualsBound(){
        double result = this.gameRandom.nextInt(42, 42);

        assertEquals(42, result);
    }

    @Test
    public void testNextIntMinGreaterThanBound(){
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextInt(42, 40));
    }

    @Test
    public void testNextIntMinSmallerThanBound(){
        double result = this.gameRandom.nextInt(40, 42);

        assertEquals(41, result);
    }
}
