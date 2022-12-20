package de.gurkenlabs.litiengine.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class GameTestSuite extends SwingTestSuite implements BeforeAllCallback, AfterAllCallback {

  private static final Lock GameLock = new ReentrantLock();

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    GameLock.lock();
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    GameLock.unlock();
  }

}
