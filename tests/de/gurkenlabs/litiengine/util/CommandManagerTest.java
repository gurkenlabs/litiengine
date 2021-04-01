package de.gurkenlabs.litiengine.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandManagerTest {

    @Test
    public void testExecuteCommandNull(){
        CommandManager commandManager = new CommandManager();
        assertFalse(commandManager.executeCommand(null));
    }

    @Test
    public void testExecuteCommandEmpty(){
        CommandManager commandManager = new CommandManager();
        assertFalse(commandManager.executeCommand(""));
    }

    @Test
    public void testExecuteCommandBlankSpace(){
        CommandManager commandManager = new CommandManager();
        assertFalse(commandManager.executeCommand(" "));
    }

    @Test
    public void testExecuteCommand(){
        CommandManager commandManager = new CommandManager();
        assertFalse(commandManager.executeCommand("test"));
    }
}


