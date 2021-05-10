package de.gurkenlabs.litiengine.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityActionMapTests {

    private EntityAction entityAction;
    private EntityActionMap actionMap;
    private final String ACTION_NAME = "Action1";

    @BeforeEach
    public void setUp(){
        entityAction = new EntityAction(ACTION_NAME, () -> {});
        actionMap = new EntityActionMap();
        actionMap.register(entityAction);
    }

    @ParameterizedTest(name = "testExists actionName={0}, expectedResult={1}")
    @CsvSource({
            "Action1, true",
            "Action2, false"
    })
    public void testExists(String actionName, boolean expectedResult){
        // act
        boolean exists = actionMap.exists(actionName);

        // assert
        assertEquals(expectedResult, exists);
    }

    @Test
    public void testGet(){
        // act
        EntityAction action = actionMap.get(ACTION_NAME);

        // assert
        assertEquals(entityAction, action);
    }

    @Test
    public void testGetActions(){
        // arrange
        actionMap.register(new EntityAction("Action2", () -> {}));

        // act
        Collection<EntityAction> actions = actionMap.getActions();

        // assert
        assertEquals(2, actions.size());
    }

    @Test
    public void testRegister(){
        // arrange
        EntityAction action = new EntityAction("Action2", () -> {});

        // act
        actionMap.register(action);

        // assert
        assertTrue(actionMap.getActions().contains(action));
    }

    @Test
    public void testUnregister(){
        // act
        actionMap.unregister(ACTION_NAME);

        // assert
        assertFalse(actionMap.getActions().contains(entityAction));
    }
}
