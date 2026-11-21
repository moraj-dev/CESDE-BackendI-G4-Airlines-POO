package com.airlinespoo.app;

import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.Validator;

import java.util.List;
import java.util.Scanner;

/**
 * Menú principal de la aplicación AirlinesPOO.
 * Administra de forma polimórfica los submenús de cada módulo.
 */
public class MainMenu implements Menu {

private final List<Menu> menus;
private final Scanner scanner;

/**
 * Constructor del menú principal.
 *
 * @param menus Lista polimórfica de menús de los módulos registrados.
 */
public MainMenu(List<Menu> menus) {
    this.menus = List.copyOf(Validator.requireNotNull(menus, "lista de menús"));
    this.scanner = new Scanner(System.in);
}

@Override
public String title() {
    return "Menú Principal";
}

@Override
public void run() {
    boolean exit = false;
    while (!exit) {
        try {
            printMenu();
            int option = readInt("Seleccione una opción: ");
            if (option == 0) {
                System.out.println("\nCerrando AirlinesPOO. ¡Hasta pronto!");
                exit = true;
            } else if (option >= 1 && option <= menus.size()) {
                menus.get(option - 1).run();
            } else {
                System.out.println("\nOpción inválida. Ingrese un número entre 0 y " + menus.size() + ".");
            }
        } catch (Exception e) {
            System.err.println("\n[ERROR] Ocurrió un problema inesperado: " + e.getMessage());
        }
    }
}

private void printMenu() {
    System.out.println("\n=======================================================");
    System.out.println("            SISTEMA AIRLINES-POO :: " + title().toUpperCase());
    System.out.println("=======================================================");
    for (int i = 0; i < menus.size(); i++) {
        System.out.printf("  [%d] %s%n", (i + 1), menus.get(i).title());
    }
    System.out.println("  [0] Salir");
    System.out.println("-------------------------------------------------------");
}

private int readInt(String prompt) {
    while (true) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Debe ingresar un número entero.");
        }
    }
}
}
