package com.airlinespoo.core.ui;

import com.airlinespoo.core.util.Validator;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Envuelve la lectura del teclado y se entrega, por el constructor, a todos los menús.
 *
 * <p>Reglas de diseño:</p>
 * <ul>
 *   <li><b>Una sola instancia</b> en toda la aplicación (se crea en {@code Main}): abrir dos
 *       lectores sobre la misma entrada causa errores difíciles de entender.</li>
 *   <li>Siempre lee una <b>línea completa</b> y convierte después, para evitar el problema del
 *       salto de línea pendiente que deja {@code nextInt()}.</li>
 *   <li>Si el dato no es válido, <b>repregunta</b> en un ciclo; no cierra el programa ni
 *       propaga la excepción.</li>
 * </ul>
 *
 * <p>Formatos fijos: fecha {@code AAAA-MM-DD} y hora {@code HH:mm} (24 horas).</p>
 */
public class ConsoleInput {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Scanner scanner;
    private final PrintStream out;

    /**
     * Crea un lector sobre el teclado ({@code System.in}) y la consola ({@code System.out}).
     */
    public ConsoleInput() {
        this(System.in, System.out);
    }

    /**
     * Crea un lector sobre una entrada y salida cualquiera (útil para pruebas).
     *
     * @param in  fuente de la que se lee
     * @param out destino de los mensajes y preguntas
     */
    public ConsoleInput(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
        this.out = out;
    }

    /**
     * Lee un texto opcional.
     *
     * @param prompt pregunta que se muestra al usuario
     * @return el texto normalizado, o una cadena vacía si el usuario no escribió nada
     */
    public String readOptionalText(String prompt) {
        out.print(prompt + " (opcional): ");
        return Validator.normalize(scanner.nextLine());
    }

    /**
     * Lee un texto obligatorio; repregunta mientras el usuario no escriba nada.
     *
     * @param prompt pregunta que se muestra al usuario
     * @return el texto normalizado, nunca vacío
     */
    public String readRequiredText(String prompt) {
        while (true) {
            out.print(prompt + ": ");
            String value = Validator.normalize(scanner.nextLine());
            if (!value.isEmpty()) {
                return value;
            }
            out.println("Este dato es obligatorio. Intenta de nuevo.");
        }
    }

    /**
     * Lee un entero dentro de un intervalo; repregunta si no es un número o está fuera de rango.
     *
     * @param prompt pregunta que se muestra al usuario
     * @param min    mínimo permitido (inclusive)
     * @param max    máximo permitido (inclusive)
     * @return el entero leído
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            out.print("%s [%d-%d]: ".formatted(prompt, min, max));
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.println("El número debe estar entre %d y %d.".formatted(min, max));
            } catch (NumberFormatException e) {
                out.println("Escribe un número entero válido.");
            }
        }
    }

    /**
     * Lee una fecha en formato {@code AAAA-MM-DD}; repregunta si el formato no es válido.
     *
     * @param prompt pregunta que se muestra al usuario
     * @return la fecha leída
     */
    public LocalDate readDate(String prompt) {
        while (true) {
            out.print(prompt + " (AAAA-MM-DD): ");
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                out.println("Fecha no válida. Usa el formato AAAA-MM-DD, por ejemplo 2026-10-25.");
            }
        }
    }

    /**
     * Lee una hora en formato {@code HH:mm} (24 horas); repregunta si el formato no es válido.
     *
     * @param prompt pregunta que se muestra al usuario
     * @return la hora leída
     */
    public LocalTime readTime(String prompt) {
        while (true) {
            out.print(prompt + " (HH:mm): ");
            try {
                return LocalTime.parse(scanner.nextLine().trim(), TIME_FORMAT);
            } catch (DateTimeParseException e) {
                out.println("Hora no válida. Usa el formato HH:mm de 24 horas, por ejemplo 14:30.");
            }
        }
    }

    /**
     * Pregunta de confirmación sí/no.
     *
     * @param prompt pregunta que se muestra al usuario
     * @return {@code true} si responde s, si o sí; {@code false} si responde n o no
     */
    public boolean confirm(String prompt) {
        while (true) {
            out.print(prompt + " (s/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            switch (answer) {
                case "s", "si", "sí" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> out.println("Responde s (sí) o n (no).");
            }
        }
    }

    /**
     * Espera a que el usuario presione Enter (útil para pausar antes de volver a un menú).
     */
    public void pause() {
        out.print("Presiona Enter para continuar...");
        scanner.nextLine();
    }
}
