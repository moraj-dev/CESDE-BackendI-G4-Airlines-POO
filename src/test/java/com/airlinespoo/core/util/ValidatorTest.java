package com.airlinespoo.core.util;

import com.airlinespoo.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    @Test
    void requireNotNullRejectsNullAndNamesTheField() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> Validator.requireNotNull(null, "país"));
        assertTrue(e.getMessage().contains("país"));
    }

    @Test
    void requireNotNullReturnsTheSameValue() {
        Object value = new Object();
        assertSame(value, Validator.requireNotNull(value, "campo"));
    }

    @Test
    void requireTextRejectsNullEmptyAndBlank() {
        assertThrows(ValidationException.class, () -> Validator.requireText(null, "nombre"));
        assertThrows(ValidationException.class, () -> Validator.requireText("", "nombre"));
        ValidationException e = assertThrows(ValidationException.class,
                () -> Validator.requireText("   ", "nombre del país"));
        assertTrue(e.getMessage().contains("nombre del país"));
    }

    @Test
    void requireTextReturnsNormalizedText() {
        assertEquals("Medellín Antioquia", Validator.requireText("  Medellín   Antioquia ", "ciudad"));
    }

    @Test
    void requireTextWithMaxLengthRejectsLongText() {
        String twentyOneChars = "a".repeat(21);
        assertThrows(ValidationException.class, () -> Validator.requireText(twentyOneChars, "nombre", 20));
        assertEquals("a".repeat(20), Validator.requireText("a".repeat(20), "nombre", 20));
    }

    @Test
    void requireMaxLengthAcceptsNullForOptionalFields() {
        assertNull(Validator.requireMaxLength(null, 10, "dirección"));
    }

    @Test
    void requirePositiveRejectsZeroAndNegatives() {
        assertThrows(ValidationException.class, () -> Validator.requirePositive(0, "id"));
        assertThrows(ValidationException.class, () -> Validator.requirePositive(-3, "id"));
        assertEquals(1, Validator.requirePositive(1, "id"));
    }

    @Test
    void requireInRangeIsInclusiveOnBothEnds() {
        assertEquals(1, Validator.requireInRange(1, 1, 12, "mes"));
        assertEquals(12, Validator.requireInRange(12, 1, 12, "mes"));
        assertThrows(ValidationException.class, () -> Validator.requireInRange(13, 1, 12, "mes"));
        assertThrows(ValidationException.class, () -> Validator.requireInRange(0, 1, 12, "mes"));
    }

    @Test
    void normalizeHandlesNullAndRepeatedSpaces() {
        assertNull(Validator.normalize(null));
        assertEquals("", Validator.normalize("   "));
        assertEquals("a b c", Validator.normalize("  a   b \t c  "));
    }
}
