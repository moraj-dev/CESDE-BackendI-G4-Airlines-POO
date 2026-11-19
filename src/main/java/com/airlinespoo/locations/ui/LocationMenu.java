package com.airlinespoo.locations.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.service.LocationService;

import java.util.List;

public class LocationMenu implements Menu {

    private final ConsoleInput console;
    private final LocationService locationService;

    public LocationMenu(ConsoleInput console, LocationService locationService) {
        this.console = console;
        this.locationService = locationService;
    }

    @Override
    public String title() {
        return "Módulo 2: Ubicaciones (Ciudades y Aeropuertos)";
    }

    @Override
    public void run() {
        int option;
        do {
            System.out.println("\n=== " + title() + " ===");
            System.out.println("1. Registrar ciudad");
            System.out.println("2. Listar ciudades");
            System.out.println("3. Buscar ciudad por nombre");
            System.out.println("4. Renombrar ciudad");
            System.out.println("5. Eliminar ciudad");
            System.out.println("6. Registrar aeropuerto");
            System.out.println("7. Listar aeropuertos por ciudad");
            System.out.println("8. Buscar aeropuerto por nombre");
            System.out.println("0. Volver al menú principal");

            option = console.readInt("Seleccione una opción", 0, 8);

            try {
                switch (option) {
                    case 1 -> registerCity();
                    case 2 -> listCities();
                    case 3 -> searchCity();
                    case 4 -> renameCity();
                    case 5 -> deleteCity();
                    case 6 -> registerAirport();
                    case 7 -> listAirportsByCity();
                    case 8 -> searchAirport();
                    case 0 -> System.out.println("Volviendo al menú principal...");
                }
            } catch (AirlinesException e) {
                System.out.println("⚠️ " + e.getMessage());
            } catch (Exception e) {
                System.out.println("⚠️ Ocurrió un error inesperado: " + e.getMessage());
            }
        } while (option != 0);
    }

    private void registerCity() {
        String name = console.readRequiredText("Nombre de la ciudad");
        City city = locationService.createCity(name);
        System.out.println("✅ Ciudad registrada: [" + city.getId() + "] " + city.getName());
    }

    private void listCities() {
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }
        System.out.println("\n--- Ciudades registradas ---");
        for (City city : cities) {
            System.out.println("ID: " + city.getId() + " | Nombre: " + city.getName());
        }
    }

    private void searchCity() {
        String text = console.readRequiredText("Texto a buscar en el nombre de la ciudad");
        List<City> matches = locationService.searchCitiesByName(text);
        if (matches.isEmpty()) {
            System.out.println("No se encontraron ciudades con '" + text + "'.");
            return;
        }
        System.out.println("\n--- Resultados de búsqueda ---");
        for (City city : matches) {
            System.out.println("ID: " + city.getId() + " | Nombre: " + city.getName());
        }
    }

    private void renameCity() {
        City city = chooseCity();
        if (city == null) {
            return;
        }
        String newName = console.readRequiredText("Nuevo nombre para la ciudad");
        City updated = locationService.updateCity(city.getId(), newName);
        System.out.println("✅ Ciudad actualizada: [" + updated.getId() + "] " + updated.getName());
    }

    private void deleteCity() {
        City city = chooseCity();
        if (city == null) {
            return;
        }
        boolean confirm = console.confirm("¿Seguro que desea eliminar la ciudad '" + city.getName() + "'?");
        if (!confirm) {
            System.out.println("Operación cancelada.");
            return;
        }
        locationService.deleteCity(city.getId());
        System.out.println("✅ Ciudad eliminada correctamente.");
    }

    private void registerAirport() {
        City city = chooseCity();
        if (city == null) {
            return;
        }
        String airportName = console.readRequiredText("Nombre del aeropuerto");
        Airport airport = locationService.createAirport(airportName, city.getId());
        System.out.println("✅ Aeropuerto registrado: " + airport.getName() + " (Ciudad: " + city.getName() + ")");
    }

    private void listAirportsByCity() {
        City city = chooseCity();
        if (city == null) {
            return;
        }
        List<Airport> airports = locationService.listAirportsByCity(city.getId());
        if (airports.isEmpty()) {
            System.out.println("La ciudad '" + city.getName() + "' no tiene aeropuertos registrados.");
            return;
        }
        System.out.println("\n--- Aeropuertos en " + city.getName() + " ---");
        for (Airport airport : airports) {
            System.out.println("- " + airport.getName() + " (ID: " + airport.getId() + ")");
        }
    }

    private void searchAirport() {
        String text = console.readRequiredText("Texto a buscar en el nombre del aeropuerto");
        List<Airport> matches = locationService.searchAirportsByName(text);
        if (matches.isEmpty()) {
            System.out.println("No se encontraron aeropuertos con '" + text + "'.");
            return;
        }
        System.out.println("\n--- Aeropuertos encontrados ---");
        for (Airport airport : matches) {
            System.out.println("- " + airport.getName() + " | Ciudad: " + airport.getCity().getName() + " | ID: " + airport.getId());
        }
    }

    private City chooseCity() {
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("⚠️ Primero debe registrar al menos una ciudad.");
            return null;
        }
        System.out.println("\nSeleccione una ciudad:");
        for (int i = 0; i < cities.size(); i++) {
            System.out.println((i + 1) + ". " + cities.get(i).getName() + " (ID: " + cities.get(i).getId() + ")");
        }
        int choice = console.readInt("Número de ciudad", 1, cities.size());
        return cities.get(choice - 1);
    }
}
