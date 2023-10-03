package de.gurkenlabs.litiengine.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.configuration.GameConfiguration;
import de.gurkenlabs.litiengine.configuration.GraphicConfiguration;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LightSourceTests {
  private LightSource lightSourceInactiveSpy;
  private LightSource lightSourceActiveSpy;

  @BeforeEach
  public void setup() {
    lightSourceInactiveSpy = spy(new LightSource(10, Color.WHITE, LightSource.Type.ELLIPSE, false));
    lightSourceActiveSpy = spy(new LightSource(10, Color.WHITE, LightSource.Type.ELLIPSE, true));
  }

  // BEHAVIOR

  @Test
  void initializeLightSource() {
    // arrange
    int intensity = 42;
    Color lightColor = Color.GREEN;
    LightSource.Type shapeType = LightSource.Type.RECTANGLE;
    boolean isActivated = true;

    // act
    LightSource lightSource = new LightSource(intensity, lightColor, shapeType, isActivated);

    // assert
    assertEquals(intensity, lightSource.getIntensity());
    assertEquals(lightColor, lightSource.getColor());
    assertEquals(shapeType, lightSource.getLightShapeType());
    assertTrue(lightSource.isActive());
  }

  @Test
  void activate_inactive() {
    // arrange
    assertFalse(lightSourceInactiveSpy.isActive());

    // act
    lightSourceInactiveSpy.activate();

    // assert
    assertTrue(lightSourceInactiveSpy.isActive());
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void activate_alreadyActive() {
    // arrange
    assertTrue(lightSourceActiveSpy.isActive());

    // act
    lightSourceActiveSpy.activate();

    // assert
    assertTrue(lightSourceActiveSpy.isActive());
    verify(lightSourceActiveSpy, times(0))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void deactivate_active() {
    // arrange
    assertTrue(lightSourceActiveSpy.isActive());

    // act
    lightSourceActiveSpy.deactivate();

    // assert
    assertFalse(lightSourceActiveSpy.isActive());
    verify(lightSourceActiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void deactivate_alreadyInactive() {
    // arrange
    assertFalse(lightSourceInactiveSpy.isActive());

    // act
    lightSourceInactiveSpy.deactivate();

    // assert
    assertFalse(lightSourceInactiveSpy.isActive());
    verify(lightSourceInactiveSpy, times(0))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void render_withDynamicShadows() {
    // arrange
    GameConfiguration actualGameConfigSpy = spy(Game.config());
    GraphicConfiguration actualGraphicsConfigSpy = spy(actualGameConfigSpy.graphics());
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic
        .when(Game::config)
        .thenReturn(actualGameConfigSpy); // otherwise it is null because of the mock
      when(actualGameConfigSpy.graphics()).thenReturn(actualGraphicsConfigSpy);
      when(actualGraphicsConfigSpy.renderDynamicShadows()).thenReturn(true);

      Environment mockedEnv = mock(Environment.class);
      GameWorld mockedWorld = mock(GameWorld.class);
      when(mockedWorld.environment()).thenReturn(mockedEnv);
      gameMockedStatic.when(Game::world).thenReturn(mockedWorld);

      Graphics2D graphicMock = mock(Graphics2D.class);

      assertTrue(Game.config().graphics().renderDynamicShadows());

      // act
      lightSourceInactiveSpy.render(graphicMock);

      // assert
      verify(mockedWorld, times(1))
        .environment(); // world.environment() is entry point of private method renderShadows()
    }
  }

  @Test
  void render_noDynamicShadows() {
    // arrange
    GameWorld mockedWorld = mock(GameWorld.class);
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic.when(Game::world).thenReturn(mockedWorld);
      gameMockedStatic
        .when(Game::config)
        .thenCallRealMethod(); // otherwise it is null because of the mock

      Graphics2D graphicMock = mock(Graphics2D.class);

      assertFalse(Game.config().graphics().renderDynamicShadows()); // is default

      // act
      lightSourceInactiveSpy.render(graphicMock);

      // assert
      verify(mockedWorld, times(0))
        .environment(); // world.environment() is entry point of private method renderShadows()
    }
  }

  @Test
  void updateAmbientLayers_delegatesWhenLoaded() {
    // arrange
    when(lightSourceInactiveSpy.isLoaded()).thenReturn(true);

    GameWorld actualWorld = spy(Game.world());
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic
        .when(Game::world)
        .thenReturn(actualWorld); // otherwise it is null because of the mock
      Environment environmentMock = mock(Environment.class);
      when(actualWorld.environment()).thenReturn(environmentMock);

      AmbientLight ambientLightMock = mock(AmbientLight.class);
      when(environmentMock.getAmbientLight()).thenReturn(ambientLightMock);
      StaticShadowLayer staticShadowLayerMock = mock(StaticShadowLayer.class);
      when(environmentMock.getStaticShadowLayer()).thenReturn(staticShadowLayerMock);

      // act
      lightSourceInactiveSpy.setColor(Color.GREEN); // means to trigger private method within

      // assert
      verify(lightSourceInactiveSpy, times(1))
        .isLoaded(); // entry point of private method updateAmbientLayers()
      verify(ambientLightMock, times(1)).updateSection(any(Rectangle2D.class));
      verify(staticShadowLayerMock, times(1)).updateSection(any(Rectangle2D.class));
    }
  }

  @Test
  void updateAmbientLayers_doesNothingWhenNotLoaded() {
    // arrange
    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // should be default, just making sure

    GameWorld actualWorld = spy(Game.world());
    try (var gameMockedStatic = mockStatic(Game.class)) {
      gameMockedStatic
        .when(Game::world)
        .thenReturn(actualWorld); // otherwise it is null because of the mock
      Environment environmentMock = mock(Environment.class);
      when(actualWorld.environment()).thenReturn(environmentMock);

      AmbientLight ambientLightMock = mock(AmbientLight.class);
      when(environmentMock.getAmbientLight()).thenReturn(ambientLightMock);
      StaticShadowLayer staticShadowLayerMock = mock(StaticShadowLayer.class);
      when(environmentMock.getStaticShadowLayer()).thenReturn(staticShadowLayerMock);

      // act
      lightSourceInactiveSpy.setColor(Color.GREEN); // means to trigger private method within

      // assert
      verify(lightSourceInactiveSpy, times(1))
        .isLoaded(); // entry point of private method updateAmbientLayers()
      verify(ambientLightMock, times(0)).updateSection(any(Rectangle2D.class));
      verify(staticShadowLayerMock, times(0)).updateSection(any(Rectangle2D.class));
    }
  }

  @Test
  void updateAmbientLayers_doesNothingWithoutGameEnvironment() {
    // arrange
    when(lightSourceInactiveSpy.isLoaded()).thenReturn(true);

    GameWorld actualWorld = spy(Game.world());
    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic
      .when(Game::world)
      .thenReturn(actualWorld); // otherwise it is null because of the mock
    Environment environmentMock = mock(Environment.class);
    when(actualWorld.environment()).thenReturn(null);

    AmbientLight ambientLightMock = mock(AmbientLight.class);
    when(environmentMock.getAmbientLight()).thenReturn(ambientLightMock);
    StaticShadowLayer staticShadowLayerMock = mock(StaticShadowLayer.class);
    when(environmentMock.getStaticShadowLayer()).thenReturn(staticShadowLayerMock);

    // act
    lightSourceInactiveSpy.setColor(Color.GREEN); // means to trigger private method within

    // assert
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
    verify(actualWorld, times(2)).environment();
    verify(ambientLightMock, times(0)).updateSection(any(Rectangle2D.class));
    verify(staticShadowLayerMock, times(0)).updateSection(any(Rectangle2D.class));

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  void updateAmbientLayers_doesNothingWithoutLayers() {
    // arrange
    when(lightSourceInactiveSpy.isLoaded()).thenReturn(true);

    GameWorld actualWorld = spy(Game.world());
    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    gameMockedStatic
      .when(Game::world)
      .thenReturn(actualWorld); // otherwise it is null because of the mock
    Environment environmentMock = mock(Environment.class);
    when(actualWorld.environment()).thenReturn(environmentMock);

    AmbientLight ambientLightMock = mock(AmbientLight.class);
    when(environmentMock.getAmbientLight()).thenReturn(null);
    StaticShadowLayer staticShadowLayerMock = mock(StaticShadowLayer.class);
    when(environmentMock.getStaticShadowLayer()).thenReturn(null);

    // act
    lightSourceInactiveSpy.setColor(Color.GREEN); // means to trigger private method within

    // assert
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
    verify(ambientLightMock, times(0)).updateSection(any(Rectangle2D.class));
    verify(staticShadowLayerMock, times(0)).updateSection(any(Rectangle2D.class));

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  void toggle_togglesActivatedAndUpdates() {
    // arrange
    assertFalse(lightSourceInactiveSpy.isActive());
    assertTrue(lightSourceActiveSpy.isActive());

    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods

    // act
    lightSourceInactiveSpy.toggle();
    lightSourceActiveSpy.toggle();

    // assert
    assertTrue(lightSourceInactiveSpy.isActive());
    assertFalse(lightSourceActiveSpy.isActive());
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
    verify(lightSourceActiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void sendMessage_returnsNullOnEmptyMessage() {
    // act, assert
    assertNull(lightSourceInactiveSpy.sendMessage(lightSourceInactiveSpy, null));
    assertNull(lightSourceInactiveSpy.sendMessage(lightSourceInactiveSpy, ""));
  }

  @Test
  void sendMessage_returnsNullOnInvalidMessage() {
    // act, assert
    assertNull(lightSourceInactiveSpy.sendMessage(lightSourceInactiveSpy, "random gibberish"));
  }

  @Test
  void sendMessage_togglesOnValidMessage() {
    // arrange
    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods
    assertFalse(lightSourceInactiveSpy.isActive());

    // act, assert
    assertEquals(
      "true",
      lightSourceInactiveSpy.sendMessage(lightSourceInactiveSpy, LightSource.TOGGLE_MESSAGE));
    verify(lightSourceInactiveSpy, times(1)).toggle();
  }

  @Test
  void updateShape_updatesRectangularShape() {
    // arrange
    Point2D newShapeLocation = new Point2D.Double(42, 42);
    Rectangle2D newShape =
      new Rectangle2D.Double(
        newShapeLocation.getX(), newShapeLocation.getY(), 32, 32); // 32 is default

    lightSourceInactiveSpy.setLightShapeType(LightSource.Type.RECTANGLE);
    assertEquals(LightSource.Type.RECTANGLE, lightSourceInactiveSpy.getLightShapeType());

    // act
    lightSourceInactiveSpy.setLocation(newShapeLocation); // means to trigger private method within

    // assert
    assertEquals(newShape, lightSourceInactiveSpy.getLightShape());
  }

  @Test
  void updateShape_updatesEllipticalShape() {
    // arrange
    Point2D newShapeLocation = new Point2D.Double(42, 42);
    Ellipse2D newShape =
      new Ellipse2D.Double(
        newShapeLocation.getX(), newShapeLocation.getY(), 32, 32); // 32 is default

    lightSourceInactiveSpy.setLightShapeType(LightSource.Type.ELLIPSE);
    assertEquals(LightSource.Type.ELLIPSE, lightSourceInactiveSpy.getLightShapeType());

    // act
    lightSourceInactiveSpy.setLocation(newShapeLocation); // means to trigger private method within

    // assert
    assertEquals(newShape, lightSourceInactiveSpy.getLightShape());
  }

  // ACCESSORS

  @Test
  void getIntensity_active() {
    // act, assert
    assertEquals(10, lightSourceActiveSpy.getIntensity());
  }

  @Test
  void getIntensity_inactive() {
    // act, assert
    assertEquals(0, lightSourceInactiveSpy.getIntensity());
  }

  @Test
  void setColor_setsColorAndUpdates() {
    // arrange
    Color newColor = Color.RED;
    assertEquals(Color.WHITE, lightSourceInactiveSpy.getColor());

    // act
    lightSourceInactiveSpy.setColor(newColor);

    // assert
    assertEquals(newColor, lightSourceInactiveSpy.getColor());
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setIntensity_setsIntensityAndUpdates() {
    // arrange
    int newIntensity = 42;
    assertEquals(10, lightSourceActiveSpy.getIntensity());

    // act
    lightSourceActiveSpy.setIntensity(newIntensity);

    // assert
    assertEquals(newIntensity, lightSourceActiveSpy.getIntensity());
    verify(lightSourceActiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setX_setsXAndUpdates() {
    // arrange
    double newX = 42d;
    assertEquals(0, lightSourceInactiveSpy.getX()); // default

    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods
    doNothing().when(lightSourceInactiveSpy).setLocation(anyDouble(), anyDouble());

    // act
    lightSourceInactiveSpy.setX(newX);

    // assert
    verify(lightSourceInactiveSpy, times(1)).setLocation(newX, 0);
    verify(lightSourceInactiveSpy, times(1))
      .getLightShapeType(); // entry point of private method updateShape()
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setY_setsYAndUpdates() {
    // arrange
    double newY = 42d;
    assertEquals(0, lightSourceInactiveSpy.getY()); // default

    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods
    doNothing().when(lightSourceInactiveSpy).setLocation(anyDouble(), anyDouble());

    // act
    lightSourceInactiveSpy.setY(newY);

    // assert
    verify(lightSourceInactiveSpy, times(1)).setLocation(0, newY);
    verify(lightSourceInactiveSpy, times(1))
      .getLightShapeType(); // entry point of private method updateShape()
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setHeight_setsHeightAndUpdates() {
    // arrange
    double newHeight = 42d;
    assertEquals(32, lightSourceInactiveSpy.getHeight()); // default

    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods

    // act
    lightSourceInactiveSpy.setHeight(newHeight);

    // assert
    assertEquals(newHeight, lightSourceInactiveSpy.getHeight());
    verify(lightSourceInactiveSpy, times(1))
      .getLightShapeType(); // entry point of private method updateShape()
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setWidth_setsWidthAndUpdates() {
    // arrange
    double newWidth = 42d;
    assertEquals(32, lightSourceInactiveSpy.getWidth()); // default

    when(lightSourceInactiveSpy.isLoaded())
      .thenReturn(false); // prevent further actions in private methods

    // act
    lightSourceInactiveSpy.setWidth(newWidth);

    // assert
    assertEquals(newWidth, lightSourceInactiveSpy.getWidth());
    verify(lightSourceInactiveSpy, times(1))
      .getLightShapeType(); // entry point of private method updateShape()
    verify(lightSourceInactiveSpy, times(1))
      .isLoaded(); // entry point of private method updateAmbientLayers()
  }

  @Test
  void setLocation_setsLocationAndUpdates() {
    // arrange
    Point2D newLocation = new Point2D.Double(5d, 5d);
    assertEquals(new Point2D.Double(0, 0), lightSourceInactiveSpy.getLocation()); // default

    // act
    lightSourceInactiveSpy.setLocation(newLocation);

    // assert
    assertEquals(newLocation, lightSourceInactiveSpy.getLocation());
    verify(lightSourceInactiveSpy, times(1))
      .getLightShapeType(); // entry point of private method updateShape()
  }

  @Test
  void setSize_setsSize() {
    // arrange
    double width = 10d;
    double height = 20d;

    // act
    lightSourceInactiveSpy.setSize(width, height);

    // assert
    assertEquals(width, lightSourceInactiveSpy.getWidth());
    assertEquals(height, lightSourceInactiveSpy.getHeight());
  }

  @Test
  void setSize_setsShorterSideAsRadius() {
    // arrange
    double firstWidth = 20d;
    double firstHeight = 10d;
    int firstRadius = 5;
    double secondWidth = 2d;
    double secondHeight = 10d;
    int secondRadius = 1;

    LightSource firstLightSourceSpy =
      spy(new LightSource(10, Color.WHITE, LightSource.Type.ELLIPSE, false));
    LightSource secondLightSourceSpy =
      spy(new LightSource(10, Color.WHITE, LightSource.Type.ELLIPSE, false));
    assertEquals(0, firstLightSourceSpy.getRadius());
    assertEquals(0, secondLightSourceSpy.getRadius());

    // act
    firstLightSourceSpy.setSize(firstWidth, firstHeight);
    secondLightSourceSpy.setSize(secondWidth, secondHeight);

    // assert
    assertEquals(firstRadius, firstLightSourceSpy.getRadius());
    assertEquals(secondRadius, secondLightSourceSpy.getRadius());
  }
}
