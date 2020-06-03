package de.gurkenlabs.litiengine.graphics.emitters.xml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.EllipseParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TextParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TriangleParticle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@EmitterInfo
public class CustomEmitter extends Emitter {
  private static final Map<String, EmitterData> loadedCustomEmitters;

  private static final Logger log = Logger.getLogger(CustomEmitter.class.getName());

  static {
    loadedCustomEmitters = new ConcurrentHashMap<>();
  }

  private final EmitterData emitterData;

  public CustomEmitter(EmitterData emitterData) {
    super();
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(Point2D location, EmitterData emitterData) {
    super(location);
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(Point2D location, final String emitterXml) {
    super(location);

    this.emitterData = load(emitterXml);
    if (this.emitterData == null) {
      this.delete();
      return;
    }

    this.init();

  }

  public CustomEmitter(final double x, final double y, EmitterData emitterData) {
    super(x, y);
    this.emitterData = emitterData;
    this.init();
  }

  public CustomEmitter(final double x, final double y, final String emitterXml) {
    super(x, y);

    this.emitterData = load(emitterXml);
    if (this.emitterData == null) {
      this.delete();
      return;
    }

    this.init();

  }

  public static EmitterData load(String emitterXml) {
    return load(Resources.getLocation(emitterXml));
  }

  public static EmitterData load(URL emitterXml) {
    final String name = emitterXml.getFile();
    if (loadedCustomEmitters.containsKey(name)) {
      return loadedCustomEmitters.get(name);
    }

    EmitterData loaded;
    try {
      loaded = XmlUtilities.read(EmitterData.class, emitterXml);
    } catch (JAXBException e) {
      log.log(Level.SEVERE, "failed to load emmiter data for " + emitterXml, e);
      return null;
    }

    return load(loaded);
  }

  public static EmitterData load(EmitterData emitterData) {
    if (loadedCustomEmitters.containsKey(emitterData.getName())) {
      return loadedCustomEmitters.get(emitterData.getName());
    }

    loadedCustomEmitters.put(emitterData.getName(), emitterData);
    return emitterData;
  }

  public static CustomEmitter get(String name) {
    if (loadedCustomEmitters.containsKey(name)) {
      return new CustomEmitter(loadedCustomEmitters.get(name));
    }

    return null;
  }

  public EmitterData getEmitterData() {
    return this.emitterData;
  }

  @Override
  protected Color getRandomParticleColor() {
    if (this.getColors().isEmpty()) {
      return EmitterData.DEFAULT_COLOR;
    }

    return ColorHelper.getRandomVariant(this.getColors().get(ThreadLocalRandom.current().nextInt(this.getColors().size())), this.getEmitterData().getColorVariance(), this.getEmitterData().getAlphaVariance());
  }

  @Override
  protected Particle createNewParticle() {
    float x;
    float y;
    float deltaX;
    float deltaY;
    float gravityX;
    float gravityY;
    float width;
    float height;
    float deltaWidth;
    float deltaHeight;

    x = (float) this.getEmitterData().getParticleOffsetX().get();
    y = (float) this.getEmitterData().getParticleOffsetY().get();
    deltaX = (float) this.getEmitterData().getDeltaX().get();
    deltaY = (float) this.getEmitterData().getDeltaY().get();
    gravityX = (float) this.getEmitterData().getGravityX().get();
    gravityY = (float) this.getEmitterData().getGravityY().get();
    width = (float) this.getEmitterData().getParticleWidth().get();
    height = (float) this.getEmitterData().getParticleHeight().get();
    deltaWidth = (float) this.getEmitterData().getDeltaWidth().get();
    deltaHeight = (float) this.getEmitterData().getDeltaHeight().get();

    Particle particle;
    switch (this.getEmitterData().getParticleType()) {

    case ELLIPSE:
      particle = new EllipseParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case RECTANGLE:
      particle = new RectangleParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case TRIANGLE:
      particle = new TriangleParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case LEFTLINE:
      particle = new LeftLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case RIGHTLINE:
      particle = new RightLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case TEXT:
      particle = new TextParticle(this.getEmitterData().getParticleText(), this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    case SPRITE:
      Spritesheet sprite = Resources.spritesheets().get(this.getEmitterData().getSpritesheet());
      if (sprite == null) {
        return null;
      }

      particle = new SpriteParticle(sprite.getSprite(ThreadLocalRandom.current().nextInt(0, sprite.getTotalNumberOfSprites() - 1)), this.getRandomParticleTTL());
      break;
    default:
      particle = new RectangleParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL());
      break;
    }
    particle.setX(x);
    particle.setY(y);

    particle.setDeltaIncX(gravityX);
    particle.setDeltaIncY(gravityY);

    particle.setDeltaX(deltaX);
    particle.setDeltaY(deltaY);

    particle.setDeltaWidth(deltaWidth);
    particle.setDeltaHeight(deltaHeight);

    particle.setCollisionType(this.getEmitterData().getCollisionType());
    particle.setOutlineOnly(this.getEmitterData().isOutlineOnly());
    particle.setFade(this.getEmitterData().isFading());
    return particle;
  }

  private void init() {
    // set emitter parameters
    this.setMaxParticles(this.getEmitterData().getMaxParticles());
    this.setParticleMinTTL(this.getEmitterData().getParticleMinTTL());
    this.setParticleMaxTTL(this.getEmitterData().getParticleMaxTTL());
    this.setTimeToLive(this.getEmitterData().getEmitterTTL());
    this.setSpawnAmount(this.getEmitterData().getSpawnAmount());
    this.setSpawnRate(this.getEmitterData().getSpawnRate());
    this.setParticleUpdateRate(this.getEmitterData().getUpdateRate());
    this.setSize(this.getEmitterData().getWidth(), this.getEmitterData().getHeight());
    this.setOriginAlign(this.getEmitterData().getOriginAlign());
    this.setOriginValign(this.getEmitterData().getOriginValign());

    for (final String color : this.getEmitterData().getColors()) {
      this.addParticleColor(ColorHelper.decode(color));
    }
  }
}
