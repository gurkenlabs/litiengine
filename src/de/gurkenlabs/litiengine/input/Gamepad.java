package de.gurkenlabs.litiengine.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Event;

public class Gamepad implements IGamepad, IUpdateable {
  private final Map<String, List<Consumer<Float>>> onPollConsumer;
  private final int index;
  private final Controller controller;

  protected Gamepad(final int index, Controller controller) {
    this.onPollConsumer = new ConcurrentHashMap<>();
    this.index = index;
    this.controller = controller;
    Game.getLoop().registerForUpdate(this);
  }

  public int getIndex() {
    return this.index;
  }

  public String getName() {
    return this.controller.getName();
  }

  @Override
  public void update(IGameLoop loop) {
    boolean couldPoll = this.controller.poll();
    if (!couldPoll) {
      this.dispose();
    }

    Event event = new Event();
    while (this.controller.getEventQueue().getNextEvent(event)) {
      List<Consumer<Float>> consumers = this.onPollConsumer.get(event.getComponent().getIdentifier().getName());
      System.out.println(event.getComponent().getName() + " - " + event.getValue());
      if (consumers != null) {
        for (Consumer<Float> cons : consumers) {
          cons.accept(event.getValue());
        }
      }
    }
  }

  @Override
  public float getPollData(Identifier identifier) {
    Component comp = this.controller.getComponent(identifier);
    if (comp == null) {
      return 0;
    }

    return comp.getPollData();
  }

  private void dispose() {
    Game.getLoop().unregisterFromUpdate(this);
    this.onPollConsumer.clear();
    Input.GAMEPADMANAGER.remove(this);
  }

  @Override
  public void onPoll(String identifier, Consumer<Float> consumer) {
    if (!this.onPollConsumer.containsKey(identifier)) {
      this.onPollConsumer.put(identifier, new ArrayList<>());
    }

    this.onPollConsumer.get(identifier).add(consumer);
  }
  public static class Xbox{
    public static final String A =  Identifier.Button._0.getName();
    public static final String B =  Identifier.Button._1.getName();
    public static final String X =  Identifier.Button._2.getName();
    public static final String Y =  Identifier.Button._3.getName();
    public static final String RB =  Identifier.Button._5.getName();
    public static final String LB =  Identifier.Button._4.getName();
    // range -1 - 0
    public static final String RT =  Identifier.Axis.Z.getName();
    
    // range 0 - 1
    public static final String LT =  Identifier.Axis.Z.getName();
    public static final String START =  Identifier.Button._7.getName();
    public static final String SELECT =  Identifier.Button._6.getName();
    public static final String LEFT_STICK_X =  Identifier.Axis.X.getName();
    public static final String LEFT_STICK_Y =  Identifier.Axis.Y.getName();
    public static final String LEFT_STICK_PRESS =  Identifier.Button._8.getName();
    public static final String RIGHT_STICK_X =  Identifier.Axis.RX.getName();
    public static final String RIGHT_STICK_Y =  Identifier.Axis.RY.getName();
    public static final String RIGHT_STICK_PRESS =  Identifier.Button._9.getName();
  }
  
  public static class Buttons {
    public static final String _0 = Identifier.Button._0.getName();
    public static final String _1 = Identifier.Button._1.getName();
    public static final String _2 = Identifier.Button._2.getName();
    public static final String _3 = Identifier.Button._3.getName();
    public static final String _4 = Identifier.Button._4.getName();
    public static final String _5 = Identifier.Button._5.getName();
    public static final String _6 = Identifier.Button._6.getName();
    public static final String _7 = Identifier.Button._7.getName();
    public static final String _8 = Identifier.Button._8.getName();
    public static final String _9 = Identifier.Button._9.getName();
    public static final String _10 = Identifier.Button._10.getName();
    public static final String _11 = Identifier.Button._11.getName();
    public static final String _12 = Identifier.Button._12.getName();
    public static final String _13 = Identifier.Button._13.getName();
    public static final String _14 = Identifier.Button._14.getName();
    public static final String _15 = Identifier.Button._15.getName();
    public static final String _16 = Identifier.Button._16.getName();
    public static final String _17 = Identifier.Button._17.getName();
    public static final String _18 = Identifier.Button._18.getName();
    public static final String _19 = Identifier.Button._19.getName();
    public static final String _20 = Identifier.Button._20.getName();
    public static final String _21 = Identifier.Button._21.getName();
    public static final String _22 = Identifier.Button._22.getName();
    public static final String _23 = Identifier.Button._23.getName();
    public static final String _24 = Identifier.Button._24.getName();
    public static final String _25 = Identifier.Button._25.getName();
    public static final String _26 = Identifier.Button._26.getName();
    public static final String _27 = Identifier.Button._27.getName();
    public static final String _28 = Identifier.Button._28.getName();
    public static final String _29 = Identifier.Button._29.getName();
    public static final String _30 = Identifier.Button._30.getName();
    public static final String _31 = Identifier.Button._31.getName();
    
    // TODO: incomplete list... implement before shipping the engine :)
  }
}
