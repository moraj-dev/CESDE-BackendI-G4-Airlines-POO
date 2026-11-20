
package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.service.CountryService;

import java.io.PrintStream;

public class CountryMenu implements Menu {

    private final CountryService service;
    private final ConsoleInput input;
    private final PrintStream out;

    public CountryMenu(CountryService service, ConsoleInput input) {
        this(service, input, System.out);
    }

    public CountryMenu(CountryService service, ConsoleInput input, PrintStream out) {
        this.service = service;
        this.input = input;
        this.out = out;
    }

    @Override
    public String title() {
        return "Administrar países";
    }

    @Override
    public void run() {
        boolean back = false;

        while (!back) {
            out.println("\n=== PAÍSES ===");
            out.println("1. Crear  2. Consultar  3. Listar  4. Actualizar  5. Eliminar  0. Volver");

            int option = input.readInt("Opción", 0, 5);

            try {
                switch (option) {
                    case 1 -> {
                        int id = input.readInt("Id", 1, Integer.MAX_VALUE);
                        out.println("Creado: " + service.createCountry(
                                id,
                                input.readRequiredText("Nombre")
                        ));
                    }
                    case 2 -> out.println(service.getCountry(
                            input.readInt("Id", 1, Integer.MAX_VALUE)
                    ));
                    case 3 -> service.listCountries().forEach(out::println);
                    case 4 -> {
                        int id = input.readInt("Id", 1, Integer.MAX_VALUE);
                        out.println("Actualizado: " + service.updateCountry(
                                id,
                                input.readRequiredText("Nuevo nombre")
                        ));
                    }
                    case 5 -> {
                        int id = input.readInt("Id", 1, Integer.MAX_VALUE);

                        if (input.confirm("¿Eliminar país?")) {
                            service.deleteCountry(id);
                        }
                    }
                    case 0 -> back = true;
                    default -> {
                    }
                }
            } catch (AirlinesException e) {
                out.println("No fue posible completar la operación: " + e.getMessage());
            }

            if (!back) {
                input.pause();
            }
        }
    }
}
