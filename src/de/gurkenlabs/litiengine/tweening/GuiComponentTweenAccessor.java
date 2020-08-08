package de.gurkenlabs.litiengine.tweening;

import de.gurkenlabs.litiengine.gui.GuiComponent;
import dorkbox.tweenEngine.TweenAccessor;

public class GuiComponentTweenAccessor implements TweenAccessor<GuiComponent> {

  @Override
  public int getValues(GuiComponent target, int tweenType, float[] returnValues) {
    switch (TweenType.values()[tweenType]) {
    case POSITION_X:
      returnValues[0] = (float) target.getX();
      return 1;
    case POSITION_Y:
      returnValues[0] = (float) target.getY();
      return 1;
    case POSITION_XY:
      returnValues[0] = (float) target.getX();
      returnValues[1] = (float) target.getY();
      return 2;
    default:
      assert false;
      return -1;
    }
  }

  @Override
  public void setValues(GuiComponent target, int tweenType, float[] newValues) {
    switch (TweenType.values()[tweenType]) {
    case POSITION_X:
      target.setX(newValues[0]);
      break;
    case POSITION_Y:
      target.setY(newValues[0]);
      break;
    case POSITION_XY:
      target.setX(newValues[0]);
      target.setY(newValues[1]);
      break;
    default:
      assert false;
      break;
    }
  }

}
