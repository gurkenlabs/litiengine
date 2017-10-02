package de.gurkenlabs.litiengine.environment;

import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.awt.Dimension;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;

public class EnvironmentTests {

  @Test
  public void testInitialization() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(100, 100));

    Environment env = new Environment(map);

    Assert.assertNotNull(env);
  }
}
