package com.airlinespoo.airlines.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.Validator;

import java.util.List;
import java.util.Scanner;

/**
 * Menú de consola para la administración del módulo de aerolíneas.
 * Proporciona formato visual en tablas y respuestas estilizadas.
 */
public class AirlineMenu implements Menu {

private final AirlineService service;
private final Scanner scanner;

public AirlineMenu(AirlineService service) {
    this.service = Validator.requireNotNull(service, "servicio de aerolíneas");
    this.scanner = new Scanner(System.in);
}

@Override
public String title() {
    return "Gestión de Aerolíneas";
}

@Override
public void run() {
    boolean exit = false;
    while (!exit) {
        printMenuHeader();
        int option = readInt("Seleccione una opción: ");
        switch (option) {
            case 1 -> createAirline();
            case 2 -> findAirlineById();
            case 3 -> listAirlines();
            case 4 -> searchAirlines();
            case 5 -> renameAirline();
            case 6 -> deleteAirline();
            case 0 -> {
                printBanner("Saliendo del módulo de aerolíneas...");
                exit = true;
            }
            default -> printWarning("Opción inválida. Ingrese un número entre 0 y 6.");
        }
    }
}

private void printMenuHeader() {
    System.out.println("\n=======================================================");
    System.out.println("            SISTEMA AIRLINES-POO :: MÓDULO 3           ");
    System.out.println("                  " + title().toUpperCase() + "          ");
    System.out.println("=======================================================");
    System.out.println("  [1] Registrar nueva aerolínea");
    System.out.println("  [2] Consultar aerolínea por ID");
    System.out.println("  [3] Listar todas las aerolíneas");
    System.out.println("  [4] Buscar aerolíneas por texto");
    System.out.println("  [5] Renombrar aerolínea existente");
    System.out.println("  [6] Eliminar aerolínea");
    System.out.println("  [0] Volver al menú principal");
    System.out.println("-------------------------------------------------------");
}

private void createAirline() {
    printSubHeader("REGISTRAR AEROLÍNEA");
    try {
        String name = readLine("Nombre de la nueva aerolínea: ");
        Airline created = service.createAirline(name);
        printSuccess("Aerolínea registrada con éxito:");
        printTableSingle(created);
    } catch (Exception e) {
        printError("No se pudo registrar la aerolínea: " + e.getMessage());
    }
}

private void findAirlineById() {
    printSubHeader("CONSULTA POR ID");
    try {
        int id = readInt("Ingrese el ID a consultar: ");
        Airline airline = service.getAirline(id);
        printTableSingle(airline);
    } catch (Exception e) {
        printError(e.getMessage());
    }
}

private void listAirlines() {
    printSubHeader("CATÁLOGO DE AEROLÍNEAS (" + service.countAirlines() + " REGISTRADAS)");
    List<Airline> airlines = service.listAirlines();
    if (airlines.isEmpty()) {
        printInfo("No hay aerolíneas registradas en la base de datos.");
        return;
    }
    renderTable(airlines);
}

private void searchAirlines() {
    printSubHeader("BÚSQUEDA POR COINCIDENCIA");
    String term = readLine("Texto o criterio de búsqueda: ");
    List<Airline> results = service.searchAirlinesByName(term);
    if (results.isEmpty()) {
        printInfo("No se encontraron aerolíneas que coincidan con '" + term + "'.");
        return;
    }
    renderTable(results);
}

private void renameAirline() {
    printSubHeader("RENOMBRAR AEROLÍNEA");
    try {
        int id = readInt("ID de la aerolínea a modificar: ");
        String newName = readLine("Nuevo nombre comercial: ");
        Airline updated = service.renameAirline(id, newName);
        printSuccess("Aerolínea renombrada exitosamente:");
        printTableSingle(updated);
    } catch (Exception e) {
        printError("Error al modificar: " + e.getMessage());
    }
}

private void deleteAirline() {
    printSubHeader("ELIMINAR AEROLÍNEA");
    try {
        int id = readInt("ID de la aerolínea a eliminar: ");
        boolean deleted = service.deleteAirline(id);
        if (deleted) {
            printSuccess("Registro con ID [" + id + "] eliminado satisfactoriamente.");
        } else {
            printWarning("No se encontró ningún registro para el ID [" + id + "].");
        }
    } catch (Exception e) {
        printError("No se puede eliminar la aerolínea: " + e.getMessage());
    }
}

// --- RENDERIZADO VISUAL EN TABLAS ---

private void renderTable(List<Airline> list) {
    System.out.println("+------------+----------------------------------------------------+");
    System.out.println("| ID         | NOMBRE DE LA AEROLÍNEA                             |");
    System.out.println("+------------+----------------------------------------------------+");
    for (Airline a : list) {
        System.out.printf("| %-10d | %-50s |\n", a.getId(), a.getName());
    }
    System.out.println("+------------+----------------------------------------------------+");
}

private void printTableSingle(Airline a) {
    System.out.println("+------------+----------------------------------------------------+");
    System.out.println("| ID         | NOMBRE DE LA AEROLÍNEA                             |");
    System.out.println("+------------+----------------------------------------------------+");
    System.out.printf("| %-10d | %-50s |\n", a.getId(), a.getName());
    System.out.println("+------------+----------------------------------------------------+");
}

// --- UTILIDADES DE INTERFAZ Y MENSAJES ---

private void printSubHeader(String title) {
    System.out.println("\n>>> " + title);
    System.out.println("-------------------------------------------------------");
}

private void printSuccess(String msg) {
    System.out.println("\n[OK] " + msg);
}

private void printError(String msg) {
    System.out.println("\n[ERROR] " + msg);
}

private void printWarning(String msg) {
    System.out.println("\n[AVISO] " + msg);
}

private void printInfo(String msg) {
    System.out.println("\n[INFO] " + msg);
}

private void printBanner(String msg) {
    System.out.println("\n* " + msg + " *");
}

private String readLine(String prompt) {
    System.out.print(prompt);
    return scanner.nextLine().trim();
}

private int readInt(String prompt) {
    while (true) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            printWarning("Entrada no válida. Ingrese un valor numérico entero.");
        }
    }
}
}
