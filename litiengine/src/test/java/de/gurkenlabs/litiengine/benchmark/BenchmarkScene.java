package de.gurkenlabs.litiengine.benchmark;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleParticle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BenchmarkScene {

  private BenchmarkScene() { throw new UnsupportedOperationException(); }

  private static final int WARMUP = 100;
  private static final int SAMPLES = 500;

  public static BenchmarkResult measure(String name, Consumer<Graphics2D> renderFn) {
    Graphics2D g = mock(Graphics2D.class);
    when(g.getTransform()).thenReturn(new AffineTransform());
    when(g.getClip()).thenReturn(new Rectangle(0, 0, 1920, 1080));
    when(g.getPaint()).thenReturn(Color.BLACK);

    for (int i = 0; i < WARMUP; i++) {
      renderFn.accept(g);
    }

    long gcBefore = gcCount();
    double[] samples = new double[SAMPLES];
    for (int i = 0; i < SAMPLES; i++) {
      long start = System.nanoTime();
      renderFn.accept(g);
      samples[i] = (System.nanoTime() - start) / 1_000_000.0;
    }
    long gcAfter = gcCount();

    return new BenchmarkResult(name, samples, gcBefore, gcAfter);
  }

  public static BenchmarkResult measureEntityRender(int entityCount, boolean withEffects) {
    Environment env = createBaseEnvironment();

    for (int i = 0; i < entityCount; i++) {
      Creature c = new Creature();
      c.setName("bench" + i);
      c.setX(i * 30 % 1800);
      c.setY(i * 20 % 1800);
      c.setWidth(16);
      c.setHeight(16);
      c.setRenderType(RenderType.NORMAL);
      env.add(c);
    }

    return measure(entityCount + " entities", g -> env.render(g));
  }

  public static BenchmarkResult measureLightRender(int lightCount, int shadowCount, LightSource.Type type) {
    Environment env = createBaseEnvironment();

    for (int i = 0; i < shadowCount; i++) {
      StaticShadow s = new StaticShadow(i * 40, i * 20, 30, 30, StaticShadowType.NONE);
      env.add(s);
    }

    for (int i = 0; i < lightCount; i++) {
      LightSource light = new LightSource(
        100 + i * 100, new Color(255, 255, 200, 80), type, true);
      light.setX(i * 150);
      light.setY(i * 100);
      env.add(light);
    }

    env.updateLighting();

    return measure(lightCount + " lights(" + type + ") + " + shadowCount + " shadows",
      g -> env.render(g));
  }

  public static BenchmarkResult measureParticleRender(int emitterCount, int particlesPerEmitter) {
    List<Emitter> emitters = new ArrayList<>();
    for (int e = 0; e < emitterCount; e++) {
      var emp = new Emitter() {
        @Override
        protected Particle createNewParticle() {
          Particle p = new RectangleParticle(8, 8);
          p.setTimeToLive(5000);
          return p;
        }
      };
      emp.data().setMaxParticles(particlesPerEmitter);
      emp.data().setSpawnAmount(particlesPerEmitter);
      emp.setX(400);
      emp.setY(300);
      emitters.add(emp);
    }

    return measure(emitterCount + " emitters x " + particlesPerEmitter + " particles",
      g -> { for (Emitter em : emitters) em.render(g); });
  }

  private static Environment createBaseEnvironment() {
    IMap map = mock(IMap.class);
    when(map.getSizeInPixels()).thenReturn(new Dimension(2000, 2000));
    when(map.getSizeInTiles()).thenReturn(new Dimension(50, 50));
    when(map.getBounds()).thenReturn(new java.awt.geom.Rectangle2D.Double(0, 0, 2000, 2000));
    when(map.getColorValue(any(), any())).thenReturn(new Color(0, 0, 0, 0));
    when(map.getIntValue(any(), anyInt())).thenReturn(0);

    Environment env = new Environment(map);
    env.load();
    return env;
  }

  private static long gcCount() {
    return ManagementFactory.getGarbageCollectorMXBeans().stream()
      .mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
  }
}
