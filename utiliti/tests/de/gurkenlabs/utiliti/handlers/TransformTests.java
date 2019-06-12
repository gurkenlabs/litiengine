package de.gurkenlabs.utiliti.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TransformTests {
  @Test
  public void testAnchorUpdate() {
    Transform.updateAnchors();
    
    assertEquals(0, Transform.getAnchors().size());
  }
}
