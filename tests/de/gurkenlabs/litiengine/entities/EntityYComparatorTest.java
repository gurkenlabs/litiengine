package de.gurkenlabs.litiengine.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityYComparatorTest {

    @Test
    void compareIsEqual() {
        TestCollisionEntity entityA = new TestCollisionEntity();
        TestCollisionEntity entityB = new TestCollisionEntity();

        EntityYComparator entityYComparator = new EntityYComparator();

        assertEquals(0, entityYComparator.compare(entityA, entityB));
    }

    @Test
    void compareIsGreater() {
        TestCollisionEntity entityA = new TestCollisionEntity();
        entityA.setHeight(32.0);
        TestCollisionEntity entityB = new TestCollisionEntity();
        entityB.setHeight(10.0);

        EntityYComparator entityYComparator = new EntityYComparator();

        assertTrue(0 < entityYComparator.compare(entityA, entityB));
    }

    @Test
    void compareIsSmaller() {
        TestCollisionEntity entityA = new TestCollisionEntity();
        entityA.setHeight(10.0);
        TestCollisionEntity entityB = new TestCollisionEntity();
        entityB.setHeight(32.0);

        EntityYComparator entityYComparator = new EntityYComparator();

        assertTrue(0 > entityYComparator.compare(entityA, entityB));
    }

    @Test
    void compareWithNoICollisionEntityIsEquals() {
        TestCollisionEntity entityA = new TestCollisionEntity();
        TestEntity entityB = new TestEntity();

        EntityYComparator entityYComparator = new EntityYComparator();

        assertEquals(0, entityYComparator.compare(entityA, entityB));
    }

    @Test
    void compareWithNoICollisionEntity() {
        TestCollisionEntity entityA = new TestCollisionEntity();
        entityA.setHeight(10.0);
        TestEntity entityB = new TestEntity();
        entityB.setHeight(32.0);

        EntityYComparator entityYComparator = new EntityYComparator();

        assertTrue(0 > entityYComparator.compare(entityA, entityB));
        assertTrue(0 < entityYComparator.compare(entityB, entityA));
    }

    @CollisionInfo(collision = true, collisionBoxHeight = 10)
    private class TestCollisionEntity extends CollisionEntity {
        protected TestCollisionEntity() {
            super();
        }
    }

    private class TestEntity extends Entity {
        protected TestEntity() {
            super();
        }
    }
}