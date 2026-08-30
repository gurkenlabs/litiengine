package de.gurkenlabs.utiliti.controller.debug;

public final class JdiLateLoadedDebuggeeFixture {
  public static void main(String[] arguments) throws InterruptedException {
    Thread.sleep(1_000);
    new Worker().run();
  }

  private static final class Worker {
    private void run() {
      int value = 41;
      value++;
      System.out.println(value);
    }
  }
}
