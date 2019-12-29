package de.gurkenlabs.litiengine.graphics.emitters.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.EllipseParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RectangleOutlineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ShimmerParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.SpriteParticle;
import de.gurkenlabs.litiengine.graphics.emitters.particles.TextParticle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@EmitterInfo(maxParticles = 0, spawnAmount = 0, activateOnInit = true)
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
      loaded = XmlUtilities.readFromFile(EmitterData.class, emitterXml);
    } catch (FileNotFoundException | JAXBException | URISyntaxException e) {
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

  public EmitterData getEmitterData() {
    return this.emitterData;
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

    x = (float) this.getEmitterData().getParticleX().get();
    y = (float) this.getEmitterData().getParticleY().get();
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
    case LEFTLINE:
      particle = new LeftLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case DISC:
      particle = new EllipseParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RECTANGLE:
      particle = new RectangleFillParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RECTANGLE_OUTLINE:
      particle = new RectangleOutlineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case RIGHTLINE:
      particle = new RightLineParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case SHIMMER:
      particle = new ShimmerParticle(new Rectangle2D.Float(x, y, (float) this.getWidth(), (float) this.getHeight()), width, height, this.getRandomParticleColor()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth)
          .setDeltaHeight(deltaHeight);
      break;
    case TEXT:
      particle = new TextParticle(this.getEmitterData().getParticleText(), this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    case SPRITE:
      Spritesheet sprite = Resources.spritesheets().get(this.getEmitterData().getSpritesheet());
      if (sprite == null) {
        return null;
      }

      particle = new SpriteParticle(sprite.getSprite(MathUtilities.randomInRange(0, sprite.getTotalNumberOfSprites() - 1)), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth)
          .setDeltaHeight(deltaHeight);
      break;
    default:
      particle = new RectangleFillParticle(width, height, this.getRandomParticleColor(), this.getRandomParticleTTL()).setX(x).setY(y).setDeltaIncX(gravityX).setDeltaIncY(gravityY).setDeltaX(deltaX).setDeltaY(deltaY).setDeltaWidth(deltaWidth).setDeltaHeight(deltaHeight);
      break;
    }

    particle.setDeltaWidth(deltaWidth);
    particle.setDeltaHeight(deltaHeight);
    particle.setCollisionType(this.getEmitterData().getCollisionType());
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

    for (final ParticleColor color : this.getEmitterData().getColors()) {
      this.addParticleColor(color.toColor());
    }
  }
}
