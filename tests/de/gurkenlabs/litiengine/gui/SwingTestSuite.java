package de.gurkenlabs.litiengine.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionException;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

public class SwingTestSuite implements InvocationInterceptor {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T interceptTestClassConstructor(
      Invocation<T> invocation,
      ReflectiveInvocationContext<Constructor<T>> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    final Object[] ret = new Object[1];
    SwingUtilities.invokeAndWait(
        () -> {
          ret[0] = proceed(invocation);
        });
    return (T) ret[0];
  }

  @Override
  public void interceptBeforeAllMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  public void interceptBeforeEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extendsionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T interceptTestFactoryMethod(
      Invocation<T> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    final Object[] ret = new Object[1];
    SwingUtilities.invokeAndWait(
        () -> {
          ret[0] = proceed(invocation);
        });
    return (T) ret[0];
  }

  @Override
  public void interceptTestTemplateMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  public void interceptAfterEachMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  @Override
  public void interceptAfterAllMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {
    proceedAndWait(invocation);
  }

  private void proceedAndWait(Invocation<Void> invocation) throws Throwable {
    SwingUtilities.invokeAndWait(
        () -> {
          proceed(invocation);
        });
  }

  private <T> T proceed(Invocation<T> invocation) {
    try {
      return invocation.proceed();
    } catch (Throwable e) {
      throw new CompletionException(e);
    }
  }
}
