
package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;

import java.io.PrintStream;
import java.util.List;

public class CustomerMenu implements Menu {

    private final CustomerService service;
    private final ConsoleInput input;
    private final PrintStream out;

    public CustomerMenu(CustomerService service, ConsoleInput input) {
        this(service, input, System.out);
    }

    public CustomerMenu(
            CustomerService service,
            ConsoleInput input,
            PrintStream out
    ) {
        this.service = service;
        this.input = input;
        this.out = out;
    }

    @Override
    public String title() {
        return "Clientes (CUSTOMERS)";
    }

    @Override
    public void run() {
        boolean back = false;

        while (!back) {
            out.println("\n=== GESTIÓN DE CLIENTES ===");
            out.println("1. Registrar cliente");
            out.println("2. Consultar cliente por documento");
            out.println("3. Actualizar contacto");
            out.println("4. Listar clientes");
            out.println("5. Buscar por apellido");
            out.println("6. Filtrar por país");
            out.println("7. Filtrar por tipo de documento");
            out.println("8. Eliminar cliente");
            out.println("9. Contar clientes");
            out.println("0. Volver");

            int option = input.readInt("Seleccione una opción", 0, 9);

            try {
                switch (option) {
                    case 1 -> register();
                    case 2 -> show(service.getCustomer(
                            input.readRequiredText("Documento")
                    ));
                    case 3 -> updateContact();
                    case 4 -> showAll(service.listCustomers());
                    case 5 -> showAll(service.searchCustomersByLastName(
                            input.readRequiredText("Apellido o fragmento")
                    ));
                    case 6 -> showAll(service.listCustomersByCountry(
                            input.readInt("Id del país", 1, Integer.MAX_VALUE)
                    ));
                    case 7 -> showAll(service.listCustomersByDocumentType(
                            input.readRequiredText("Código de tipo")
                    ));
                    case 8 -> delete();
                    case 9 -> out.println(
                            "Total de clientes: " + service.countCustomers()
                    );
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

    private void register() {
        String document = input.readRequiredText("Documento");
        String firstName = input.readRequiredText("Nombres");
        String lastName = input.readRequiredText("Apellidos");
        String type = input.readRequiredText("Tipo de documento (CC/CE/PA/TI)");
        int country = input.readInt("Id del país", 1, Integer.MAX_VALUE);
        String phone = input.readRequiredText("Teléfono");
        String email = input.readOptionalText("Correo");
        String address = input.readOptionalText("Dirección");

        Customer customer = service.createCustomer(
                document,
                firstName,
                lastName,
                type,
                country,
                phone,
                email,
                address
        );

        out.println("Cliente registrado: " + customer);
    }

    private void updateContact() {
        String document = input.readRequiredText("Documento del cliente");
        String phone = input.readRequiredText("Nuevo teléfono");
        String email = input.readOptionalText("Nuevo correo, vacío para quitarlo");
        String address = input.readOptionalText("Nueva dirección, vacía para quitarla");

        out.println("Contacto actualizado: " + service.updateCustomer(
                document,
                phone,
                email,
                address
        ));
    }

    private void delete() {
        String document = input.readRequiredText("Documento del cliente");

        if (input.confirm("¿Confirma la eliminación del cliente " + document + "?")) {
            service.deleteCustomer(document);
            out.println("Cliente eliminado.");
        } else {
            out.println("Operación cancelada.");
        }
    }

    private void showAll(List<Customer> customers) {
        if (customers.isEmpty()) {
            out.println("No se encontraron clientes.");
            return;
        }

        customers.forEach(this::show);
        out.println("Registros: " + customers.size());
    }

    private void show(Customer customer) {
        out.println("----------------------------------------");
        out.println("Documento: " + customer.getId());
        out.println("Nombre: " + customer.getFullName());
        out.println("Tipo: " + customer.getDocumentType().getId()
                + " - " + customer.getDocumentType().getDescription());
        out.println("País: " + customer.getCountry().getName());
        out.println("Teléfono: " + customer.getTelephoneNumber());
        out.println("Correo: " + (
                customer.getEmail() == null
                        ? "No registrado"
                        : customer.getEmail()
        ));
        out.println("Dirección: " + (
                customer.getAddress() == null
                        ? "No registrada"
                        : customer.getAddress()
        ));
    }
}
