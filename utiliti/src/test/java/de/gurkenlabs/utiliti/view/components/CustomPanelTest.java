package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class CustomPanelTest {

  @Test
  void bindToleratesPropertyMutationDuringTableRebuild() {
    CustomPanel panel = new CustomPanel() {
      @Override
      protected void clearControls() {
        super.clearControls();
        if (getDataSource() != null) {
          getDataSource().setValue("addedDuringClear", "2");
        }
      }
    };
    MapObject mapObject = new MapObject();
    mapObject.setValue("existing", "1");

    assertDoesNotThrow(() -> panel.bind(mapObject));
  }
}
