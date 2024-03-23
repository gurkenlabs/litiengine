package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValignTests {
  private Valign valignObject;

  @BeforeEach
  public void initAlign() {
    valignObject = Valign.get("down"); // portion = 1
  }

  @Test
  void getLocation_OnPoint() {
    // act
    double onPointLocation = valignObject.getLocation(1.0, 1.0);

    // assert
    assertEquals(0.0, onPointLocation, 0.001); // clamp(0.5, 0, 0)
  }

  @Test
  void getLocation_OffPoint() {
    // act
    double offPointLocation = valignObject.getLocation(1.0, 1.1);

    // assert
    assertEquals(0.45, offPointLocation, 0.001); // 1.0 - 1.1 / 2.0
  }

  @Test
  void getLocation_InPoint() {
    // act
    double inPointLocation = valignObject.getLocation(1.0, 5.0);

    // assert
    assertEquals(-1.5, inPointLocation, 0.001); // 1.0 - 5.0 / 2.0
  }

  @Test
  void getLocation_OutPoint() {
    // act
    double outPointLocation = valignObject.getLocation(1.0, 0.5);

    // assert
    assertEquals(0.5, outPointLocation, 0.001); // clamp(0.75, 0, 0.5)
  }

  @Test
  void getClampedLocation_OnPoint() {
    // act
    double onPointLocation = valignObject.getLocation(1.0, 1.0, true);

    // assert
    assertEquals(0.0, onPointLocation, 0.001); // clamp(0.5, 0, 0)
  }

  @Test
  void getClampedLocation_OffPoint() {
    // act
    double offPointLocation = valignObject.getLocation(1.0, 1.1, true);

    // assert
    assertEquals(0.0, offPointLocation, 0.001); // clamp(0.45, 0, -0.1)
  }

  @Test
  void getClampedLocation_InPoint() {
    // act
    double inPointLocation = valignObject.getLocation(1.0, 5.0, true);

    // assert
    assertEquals(0.0, inPointLocation, 0.001); // clamp(-1.5, 0, -4.0)
  }

  @Test
  void getClampedLocation_OutPoint() {
    // act
    double outPointLocation = valignObject.getLocation(1.0, 0.5, true);

    // assert
    assertEquals(0.5, outPointLocation, 0.001); // clamp(0.75, 0, 0.5)
  }
}
