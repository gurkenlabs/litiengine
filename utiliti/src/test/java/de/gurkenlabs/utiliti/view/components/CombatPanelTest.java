package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class CombatPanelTest {

  @Test
  void hitpointsAllowValuesAboveDefault() {
    assertTrue(SwingUtilities.isEventDispatchThread());

    SpinnerNumberModel model = new CombatPanel().getHitpointsModel();

    assertEquals(100, model.getValue());
    assertEquals(0, model.getMinimum());
    assertNull(model.getMaximum());

    model.setValue(250);

    assertEquals(250, model.getValue());
  }
}
