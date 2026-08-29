package de.gurkenlabs.utiliti.controller.debug;

import de.gurkenlabs.litiengine.scripting.ScriptGlobals;

public final class JdiDebuggeeFixture {
  private static int counter = 7;
  private int health = 100;
  private final ScriptGlobals globals = new ScriptGlobals();

  public static void main(String[] arguments) {
    new JdiDebuggeeFixture().run();
  }

  private void run() {
    this.globals.put("score", 42);
    int value = 41;
    value++;
    System.out.println(value);
  }
}
