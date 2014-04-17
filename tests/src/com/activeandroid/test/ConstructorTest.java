
package com.activeandroid.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;


public class ConstructorTest extends ActiveAndroidTestCase {

    /**
     * Should be able to instantiate a Model with a private default constructor.
     */
    public void testWithConstructor() {
        final int value = 1234;

        Entity entity = new Entity(value);
        entity.save();

        Entity entityFromDb = new Select()
                .from(Entity.class)
                .where("Value = ?", value)
                .executeSingle();

        assertNotNull(entityFromDb);

        assertEquals(entityFromDb, entity);
        assertEquals(entityFromDb.getValue(), value);
    }

    /**
     * Shouldn't be able to instantiate a Model without a default constructor.
     */
    public void testWithoutConstructor() {
        final int value = 1234;

        BaseEntity entity = new BaseEntity(value);
        entity.save();

        try {
            new Select()
                    .from(BaseEntity.class)
                    .where("Value = ?", value)
                    .executeSingle();

            fail("Should've thrown an exception, missing default constructor");

        } catch (java.lang.RuntimeException e) {
            String message = e.getMessage();

            assertNotNull(message);
            assertTrue(message.contains("does not define a default constructor"));
            assertTrue(message.contains("com.activeandroid.test.ConstructorTest$BaseEntity"));
        }
    }

    @Table(name = "BaseEntity")
    private static class BaseEntity extends Model {

        @Column(name = "Value")
        protected int value;

        protected BaseEntity(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    @Table(name = "Entity")
    private static class Entity extends BaseEntity {

        // Default constructor required by ActiveAndroid
        private Entity() {
            super(-1);
        }

        public Entity(int value) {
            super(value);
        }
    }
}
