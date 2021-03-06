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
    public void testNextDoubleMinEqualsBound(){
        double result = this.gameRandom.nextDouble(42, 42);

        assertEquals(42, result);
    }

    @Test
    public void testNextDoubleMinGreaterThanBound(){
        assertThrows(IllegalArgumentException.class, () -> this.gameRandom.nextDouble(42, 40));
    }

    @Test
    public void testNextDoubleMinSmallerThanBound(){
        double result = this.gameRandom.nextDouble(40, 42);

        assertEquals(41.319859569489644d, result);
    }
}
