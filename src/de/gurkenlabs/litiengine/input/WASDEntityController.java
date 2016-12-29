package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.util.MathUtilities;

public class WASDEntityController extends ClientEntityMovementController implements IKeyObserver {
  private double velocityX, velocityY;

  private boolean movedX, movedY;
  private float dx;
  private float dy;
  
  int deceleration = 1000;
  public WASDEntityController(final IMovableEntity entity) {
    super(entity);

    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final KeyEvent keyCode) {   

    switch (keyCode.getKeyCode()) {
    case KeyEvent.VK_W:
      this.dy--;
      this.movedY = true;
      break;
    case KeyEvent.VK_A:
      this.dx--;
      this.movedX = true;
      break;
    case KeyEvent.VK_S:
      this.movedY = true;
      this.dy++;
      break;
    case KeyEvent.VK_D:
      this.dx++;
      this.movedX = true;
      break;
    }
  }

  @Override
  public void handleReleasedKey(final KeyEvent keyCode) {

  }

  @Override
  public void handleTypedKey(final KeyEvent keyCode) {

  }
  
  @Override
  public void update(final IGameLoop loop) {
    super.update(loop);
    double maxPixelsPerTick = this.getControlledEntity().getVelocity() * 0.001 * Game.getConfiguration().CLIENT.getUpdaterate();
    double inc = this.getControlledEntity().getAcceleration() == 0 ? maxPixelsPerTick : Game.getConfiguration().CLIENT.getUpdaterate() * 1/this.getControlledEntity().getAcceleration() * maxPixelsPerTick;
    double dec = deceleration == 0 ? maxPixelsPerTick : Game.getConfiguration().CLIENT.getUpdaterate() * 1.0/deceleration * maxPixelsPerTick;
    double relativDec = dec / maxPixelsPerTick;
    final double STOP_THRESHOLD = 0.1;
    
    if (this.movedX) {
      this.velocityX += this.dx * inc;
      this.velocityX = MathUtilities.clamp(this.velocityX, -maxPixelsPerTick, maxPixelsPerTick);
      this.dx = 0;
      this.movedX = false;
    }else{
      // TODO: depends on friction
      this.velocityX *= 1 - relativDec;
      if(Math.abs(this.velocityX) < STOP_THRESHOLD){
        this.velocityX = 0;
      }
    }
    
    if (this.movedY) {
      this.velocityY += this.dy * inc;
      this.velocityY = MathUtilities.clamp(this.velocityY, -maxPixelsPerTick, maxPixelsPerTick);
      this.dy = 0;
      this.movedY = false;
    }else{
      // TODO: depends on friction
      this.velocityY *= 1 - relativDec;
      if(Math.abs(this.velocityY) < STOP_THRESHOLD){
        this.velocityY = 0;
      }
    }
    
    System.out.println("dec: " + dec + "; relDec: " + relativDec + "; velx: " + velocityX + "; velY: " + velocityY);
    
    final Point2D newLocation = new Point2D.Double(this.getControlledEntity().getLocation().getX() + this.velocityX, this.getControlledEntity().getLocation().getY() + this.velocityY);
    Game.getPhysicsEngine().move(this.getControlledEntity(), newLocation);
  }
}
