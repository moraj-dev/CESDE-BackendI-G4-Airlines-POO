package com.airlinespoo.core.ui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInputTest {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    private ConsoleInput inputWith(String typed) {
        ByteArrayInputStream in = new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8));
        return new ConsoleInput(in, new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    void readIntAsksAgainWhenTheTextIsNotANumber() {
        ConsoleInput console = inputWith("abc\n12\n3\n");
        assertEquals(3, console.readInt("Opción", 1, 5));
        assertTrue(output().contains("número entero"));
        assertTrue(output().contains("entre 1 y 5"));
    }

    @Test
    void readRequiredTextAsksAgainWhenEmpty() {
        ConsoleInput console = inputWith("\n   \n  Ana   María \n");
        assertEquals("Ana María", console.readRequiredText("Nombre"));
        assertTrue(output().contains("obligatorio"));
    }

    @Test
    void readOptionalTextReturnsEmptyWhenNothingTyped() {
        ConsoleInput console = inputWith("\n");
        assertEquals("", console.readOptionalText("Dirección"));
    }

    @Test
    void readDateParsesIsoFormatAndRepeatsOnError() {
        ConsoleInput console = inputWith("25/10/2026\n2026-10-25\n");
        assertEquals(LocalDate.of(2026, 10, 25), console.readDate("Fecha"));
        assertTrue(output().contains("AAAA-MM-DD"));
    }

    @Test
    void readTimeParsesTwentyFourHourFormat() {
        ConsoleInput console = inputWith("2:30 pm\n23:30\n");
        assertEquals(LocalTime.of(23, 30), console.readTime("Hora"));
    }

    @Test
    void confirmAcceptsYesNoVariantsAndRepeatsOnOtherInput() {
        assertTrue(inputWith("s\n").confirm("¿Seguro?"));
        assertTrue(inputWith("SÍ\n").confirm("¿Seguro?"));
        assertFalse(inputWith("n\n").confirm("¿Seguro?"));
        assertFalse(inputWith("tal vez\nno\n").confirm("¿Seguro?"));
    }

    @Test
    void sameInstanceReadsConsecutiveLinesWithoutPendingNewlineProblems() {
        ConsoleInput console = inputWith("7\nPedro\n");
        assertEquals(7, console.readInt("Número", 1, 10));
        assertEquals("Pedro", console.readRequiredText("Nombre"));
    }
}
