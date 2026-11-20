
package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.service.DocumentTypeService;

import java.io.PrintStream;

public class DocumentTypeMenu implements Menu {

    private final DocumentTypeService service;
    private final ConsoleInput input;
    private final PrintStream out;

    public DocumentTypeMenu(DocumentTypeService service, ConsoleInput input) {
        this(service, input, System.out);
    }

    public DocumentTypeMenu(
            DocumentTypeService service,
            ConsoleInput input,
            PrintStream out
    ) {
        this.service = service;
        this.input = input;
        this.out = out;
    }

    @Override
    public String title() {
        return "Administrar tipos de documento";
    }

    @Override
    public void run() {
        boolean back = false;

        while (!back) {
            out.println("\n=== TIPOS DE DOCUMENTO ===");
            out.println("1. Crear  2. Consultar  3. Listar  4. Actualizar  5. Eliminar  0. Volver");

            int option = input.readInt("Opción", 0, 5);

            try {
                switch (option) {
                    case 1 -> out.println("Creado: " + service.createDocumentType(
                            input.readRequiredText("Código (2 caracteres)"),
                            input.readRequiredText("Descripción")
                    ));
                    case 2 -> out.println(service.getDocumentType(
                            input.readRequiredText("Código")
                    ));
                    case 3 -> service.listDocumentTypes().forEach(out::println);
                    case 4 -> {
                        String id = input.readRequiredText("Código");

                        out.println("Actualizado: " + service.updateDocumentType(
                                id,
                                input.readRequiredText("Nueva descripción")
                        ));
                    }
                    case 5 -> {
                        String id = input.readRequiredText("Código");

                        if (input.confirm("¿Eliminar tipo de documento?")) {
                            service.deleteDocumentType(id);
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
