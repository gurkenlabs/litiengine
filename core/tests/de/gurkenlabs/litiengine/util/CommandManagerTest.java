package de.gurkenlabs.litiengine.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class CommandManagerTest {

  @Test
  void bindAddNewCommand() {
    // arrange
    CommandManager commandManager = new CommandManager();
    assertFalse(commandManager.executeCommand("command predicate"));
    Predicate<String[]> predicate = arr -> arr.length == 2;

    // act
    commandManager.bind("command", predicate);

    // assert
    assertTrue(
        commandManager.executeCommand(
            "command predicate")); // only means to access private commandConsumers
  }

  @Test
  void bindThrowsIfContained() {
    // arrange
    CommandManager commandManager = new CommandManager();

    Predicate<String[]> predicate = arr -> arr.length == 2;
    commandManager.bind("command", predicate);
    assertTrue(commandManager.executeCommand("command predicate"));

    // act, assert
    assertThrows(
        IllegalArgumentException.class,
        () -> commandManager.bind("command", predicate)); // add again
  }

  @Test
  void executeCommandNull() {
    // arrange
    CommandManager commandManager = new CommandManager();

    // act, assert
    assertFalse(commandManager.executeCommand(null));
  }

  @Test
  void executeCommandEmpty() {
    // arrange
    CommandManager commandManager = new CommandManager();

    // act, assert
    assertFalse(commandManager.executeCommand(""));
  }

  @Test
  void executeCommandBlankSpace() {
    // arrange
    CommandManager commandManager = new CommandManager();

    // act, assert
    assertFalse(commandManager.executeCommand(" "));
  }

  @Test
  void executeCommandSingleWord() {
    // arrange
    CommandManager commandManager = new CommandManager();

    // act, assert
    assertFalse(commandManager.executeCommand("test"));
  }

  @Test
  void executeCommandMultiWordNotContained() {
    // arrange
    CommandManager commandManager = new CommandManager();

    // act, assert
    assertFalse(commandManager.executeCommand("banana testing command"));
  }

  @Test
  void executeCommandKeywordContainedNotMatching() {
    // arrange
    CommandManager commandManager = new CommandManager();
    Predicate<String[]> predicate = arr -> arr.length > 5;
    commandManager.bind("apple", predicate);

    // act, assert
    assertFalse(commandManager.executeCommand("apple predicate mismatch"));
  }

  @Test
  void executeCommandKeywordContainedMatching() {
    // arrange
    CommandManager commandManager = new CommandManager();
    Predicate<String[]> predicate = arr -> arr.length < 5;
    commandManager.bind("pear", predicate);

    // act, assert
    assertTrue(commandManager.executeCommand("pear matching predicate"));
  }
}
