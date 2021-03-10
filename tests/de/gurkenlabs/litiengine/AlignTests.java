package de.gurkenlabs.litiengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlignTests {
    private Align alignObject;

    @BeforeEach
    public void initAlign() {
        alignObject = Align.get("right"); // portion = 1
    }

    @Test
    public void getLocation_OnPoint() {
        // act
        double onPointLocation = alignObject.getLocation(1.0, 1.0);

        // assert
        assertEquals(0.0, onPointLocation); // clamp(0.5, 0, 0)
    }

    @Test
    public void getLocation_OffPoint() {
        // act
        double offPointLocation = alignObject.getLocation(1.0, 1.1);

        // assert
        assertEquals(0.45, offPointLocation); // 1.0 - 1.1 / 2.0
    }

    @Test
    public void getLocation_InPoint() {
        // act
        double inPointLocation = alignObject.getLocation(1.0, 5.0);

        // assert
        assertEquals(-1.5, inPointLocation); // 1.0 - 5.0 / 2.0
    }

    @Test
    public void getLocation_OutPoint() {
        // act
        double outPointLocation = alignObject.getLocation(1.0, 0.5);

        // assert
        assertEquals(0.5, outPointLocation); // clamp(0.75, 0, 0.5)
    }
}
