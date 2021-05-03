package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandManagerTest {

    @Test
    public void bindAddNewCommand(){
        // arrange
        CommandManager commandManager = new CommandManager();
        assertFalse(commandManager.executeCommand("command predicate"));
        Predicate<String []> predicate = arr -> arr.length == 2;

        // act
        commandManager.bind("command", predicate);

        // assert
        assertTrue(commandManager.executeCommand("command predicate")); // only means to access private commandConsumers
    }

    @Test
    public void bindThrowsIfContained(){
        // arrange
        CommandManager commandManager = new CommandManager();

        Predicate<String []> predicate = arr -> arr.length == 2;
        commandManager.bind("command", predicate);
        assertTrue(commandManager.executeCommand("command predicate"));

        // act, assert
        assertThrows(IllegalArgumentException.class, () -> commandManager.bind("command", predicate)); // add again
    }

    @Test
    public void executeCommandNull(){
        // arrange
        CommandManager commandManager = new CommandManager();

        // act, assert
        assertFalse(commandManager.executeCommand(null));
    }

    @Test
    public void executeCommandEmpty(){
        // arrange
        CommandManager commandManager = new CommandManager();

        // act, assert
        assertFalse(commandManager.executeCommand(""));
    }

    @Test
    public void executeCommandBlankSpace(){
        // arrange
        CommandManager commandManager = new CommandManager();

        // act, assert
        assertFalse(commandManager.executeCommand(" "));
    }

    @Test
    public void executeCommandSingleWord(){
        // arrange
        CommandManager commandManager = new CommandManager();

        // act, assert
        assertFalse(commandManager.executeCommand("test"));
    }

    @Test
    public void executeCommandMultiWordNotContained(){
        // arrange
        CommandManager commandManager = new CommandManager();

        // act, assert
        assertFalse(commandManager.executeCommand("banana testing command"));
    }

    @Test
    public void executeCommandKeywordContainedNotMatching(){
        // arrange
        CommandManager commandManager = new CommandManager();
        Predicate<String []> predicate = arr -> arr.length > 5;
        commandManager.bind("apple", predicate);

        // act, assert
        assertFalse(commandManager.executeCommand("apple predicate mismatch"));
    }

    @Test
    public void executeCommandKeywordContainedMatching(){
        // arrange
        CommandManager commandManager = new CommandManager();
        Predicate<String []> predicate = arr -> arr.length < 5;
        commandManager.bind("pear", predicate);

        // act, assert
        assertTrue(commandManager.executeCommand("pear matching predicate"));
    }
}
