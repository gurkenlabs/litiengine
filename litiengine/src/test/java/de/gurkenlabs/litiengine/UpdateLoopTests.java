package de.gurkenlabs.litiengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateLoopTests {
  @Test
  void updatePriorityIsDeterministicAndRetainsRegistrationOrderForTies() {
    TestLoop loop = new TestLoop();
    List<String> updates = new ArrayList<>();
    IUpdateable defaultFirst = updateable(0, () -> updates.add("default-first"));
    IUpdateable late = updateable(100, () -> updates.add("late"));
    IUpdateable early = updateable(-100, () -> updates.add("early"));
    IUpdateable defaultSecond = updateable(0, () -> updates.add("default-second"));

    loop.attach(defaultFirst);
    loop.attach(late);
    loop.attach(early);
    loop.attach(defaultSecond);
    loop.attach(defaultFirst);
    loop.tick();

    assertEquals(List.of("early", "default-first", "default-second", "late"), updates);
    assertEquals(4, loop.getUpdatableCount());
  }

  @Test
  void updateablesCanAttachMoreWorkForTheNextTick() {
    TestLoop loop = new TestLoop();
    List<String> updates = new ArrayList<>();
    IUpdateable added = () -> updates.add("added");
    loop.attach(() -> {
      updates.add("first");
      loop.attach(added);
    });

    loop.tick();
    loop.tick();

    assertEquals(List.of("first", "first", "added"), updates);
  }

  private static IUpdateable updateable(int priority, Runnable update) {
    return new IUpdateable() {
      @Override public void update() { update.run(); }
      @Override public int getUpdatePriority() { return priority; }
    };
  }

  private static final class TestLoop extends UpdateLoop {
    private TestLoop() { super("test", 60); }
    private void tick() { super.update(); }
  }
}
