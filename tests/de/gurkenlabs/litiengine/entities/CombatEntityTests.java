package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

public class CombatEntityTests {

    private CombatEntity combatEntity;
    private CombatEntityListener entityListener;
    private Ability ability;

    @BeforeEach
    public void setUp(){
        Game.init(Game.COMMADLINE_ARG_NOGUI);

        combatEntity = new CombatEntity();

        entityListener = mock(CombatEntityListener.class);

        combatEntity.addCombatEntityListener(entityListener);

        ability = mock(Ability.class);
    }

    @Test
    public void testHit_notDead(){
        // arrange
        int hitPoints = 100;

        // act
        combatEntity.hit(hitPoints, ability);

        // assert
        verify(entityListener, times(2)).hit(any());
    }

    @Test
    public void testHit_dead(){
        // arrange
        int hitPoints = 100;
        combatEntity.die();

        // act
        combatEntity.hit(hitPoints, ability);

        // assert
        verify(entityListener, times(0)).hit(any());
    }

    @Test
    public void testHit_indestructible(){
        // arrange
        int hitPoints = 100;
        combatEntity.setIndestructible(true);

        // act
        combatEntity.hit(hitPoints, ability);

        // assert
        assertEquals(100, combatEntity.getHitPoints().get());
    }

    @Test
    public void testHit_getsKilledWithThisHit(){
        // arrange
        int hitPoints = 250;

        // act
        combatEntity.hit(hitPoints, ability);

        // assert
        verify(entityListener, times(2)).death(combatEntity);
    }
}
