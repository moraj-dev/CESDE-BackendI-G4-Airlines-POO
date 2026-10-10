package com.airlinespoo.core.util;

import com.airlinespoo.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdGeneratorTest {

    @Test
    void airlinesStartAt100AndStepBy10() {
        IdGenerator generator = new IdGenerator(100, 10);
        assertEquals(100, generator.next());
        assertEquals(110, generator.next());
        assertEquals(120, generator.next());
    }

    @Test
    void citiesStartAt1AndStepBy1() {
        IdGenerator generator = new IdGenerator(1, 1);
        assertEquals(1, generator.next());
        assertEquals(2, generator.next());
        assertEquals(3, generator.next());
    }

    @Test
    void stepZeroIsRejected() {
        assertThrows(ValidationException.class, () -> new IdGenerator(1, 0));
    }

    @Test
    void startZeroIsRejected() {
        assertThrows(ValidationException.class, () -> new IdGenerator(0, 1));
    }

    @Test
    void syncWithSkipsPastExistingIds() {
        IdGenerator generator = new IdGenerator(100, 10);
        generator.syncWith(130);
        assertEquals(140, generator.next());
    }

    @Test
    void syncWithLowerIdDoesNotGoBackwards() {
        IdGenerator generator = new IdGenerator(100, 10);
        generator.next();
        generator.next();
        generator.syncWith(100);
        assertEquals(120, generator.next());
    }

    @Test
    void peekNextDoesNotAdvance() {
        IdGenerator generator = new IdGenerator(5, 1);
        assertEquals(5, generator.peekNext());
        assertEquals(5, generator.peekNext());
        assertEquals(5, generator.next());
    }
}
