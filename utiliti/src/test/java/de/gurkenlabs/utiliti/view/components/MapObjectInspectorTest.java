package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.SpinnerNumberModel;
import org.junit.jupiter.api.Test;

class MapObjectInspectorTest {

  @Test
  void coordinateSpinnerModelAllowsNegativeValues() {
    SpinnerNumberModel model = MapObjectInspector.createCoordinateSpinnerModel();

    model.setValue(-42.0);

    assertEquals(-42.0, model.getNumber().doubleValue());
  }
}
