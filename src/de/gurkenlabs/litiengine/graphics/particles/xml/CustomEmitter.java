package de.gurkenlabs.litiengine.graphics.particles.xml;

import java.awt.geom.Rectangle2D;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.LeftLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.OvalParticle;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleFillParticle;
import de.gurkenlabs.litiengine.graphics.particles.RectangleOutlineParticle;
import de.gurkenlabs.litiengine.graphics.particles.RightLineParticle;
import de.gurkenlabs.litiengine.graphics.particles.ShimmerParticle;
import de.gurkenlabs.litiengine.graphics.particles.TextParticle;

@EmitterInfo(maxParticles = 0, spawnAmount = 0, activateOnInit = true)
public class CustomEmitter extends Emitter {
  private CustomEmitterData emitterData;

  public CustomEmitter(double originX, double originY, URL emitterXml) {
    super(originX, originY);
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(CustomEmitterData.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      this.emitterData = (CustomEmitterData) jaxbUnmarshaller.unmarshal(emitterXml);
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    // set emitter parameters
    this.setMaxParticles(this.emitterData.getMaxParticles());
    this.setParticleMinTTL(this.emitterData.getParticleMinTTL());
    this.setParticleMaxTTL(this.emitterData.getParticleMaxTTL());
    this.setTimeToLive(this.emitterData.getEmitterTTL());
    this.setSpawnAmount(this.emitterData.getSpawnAmount());
    this.setSpawnRate(this.emitterData.getSpawnRate());
    this.setParticleUpdateRate(this.emitterData.getUpdateRate());
    this.setSize(this.emitterData.getWidth(), this.emitterData.getHeight());

    for (ParticleColor color : this.emitterData.getColors()) {
      this.addParticleColor(color.toColor());
    }
  }

  @Override
  protected Particle createNewParticle() {
    float x, y, deltaX, deltaY, gravityX, gravityY, width, height, deltaWidth, deltaHeight;

    x = this.emitterData.getX().get();
    y = this.emitterData.getY().get();
    deltaX = this.emitterData.getDeltaX().get();
    deltaY = this.emitterData.getDeltaY().get();
    gravityX = this.emitterData.getGravityX().get();
    gravityY = this.emitterData.getGravityY().get();
    width = this.emitterData.getParticleWidth().get();
    height = this.emitterData.getParticleHeight().get();
    deltaWidth = this.emitterData.getDeltaWidth().get();
    deltaHeight = this.emitterData.getDeltaHeight().get();

    Particle particle;
    switch (this.emitterData.getParticleType()) {
    case LeftLineParticle:
      particle = new LeftLineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case OvalParticle:
      particle = new OvalParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case RectangleFillParticle:
      particle = new RectangleFillParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case RectangleOutlineParticle:
      particle = new RectangleOutlineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case RightLineParticle:
      particle = new RightLineParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case ShimmerParticle:
      particle = new ShimmerParticle(new Rectangle2D.Float(x, y, this.getWidth(), this.getHeight()), x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    case TextParticle:
      particle = new TextParticle(this.emitterData.getParticleText(), x, y, deltaX, deltaY, gravityX, gravityY, getRandomParticleTTL(), getRandomParticleColor());
      break;
    default:
      particle = new RectangleFillParticle(x, y, deltaX, deltaY, gravityX, gravityY, width, height, getRandomParticleTTL(), getRandomParticleColor());
      break;
    }

    particle.setDeltaWidth(deltaWidth);
    particle.setDeltaHeight(deltaHeight);
    particle.setApplyStaticPhysics(this.emitterData.isApplyingStaticPhysics());
    return particle;
  }

}
