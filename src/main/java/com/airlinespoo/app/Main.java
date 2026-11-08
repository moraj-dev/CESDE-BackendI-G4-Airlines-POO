package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.AirlineMenu;
import com.airlinespoo.core.util.IdGenerator;

/**
 * Punto de entrada de AirlinesPOO.
 * Ensambla los componentes del sistema y ejecuta la interfaz de usuario.
 */
public final class Main {

private Main() {
    // Clase de arranque: no se instancia.
}

public static void main(String[] args) {
    System.out.println("=== Airlines-poo ===");
    System.out.println("Iniciando sistema con persistencia en SQL Server...");

    // 1. Instanciar el repositorio
    AirlineRepository repository = new AirlineRepository();

    // 2. Generador con configuración del módulo (arranque 100, paso 10)
    IdGenerator idGenerator = new IdGenerator(100, 10);

    // 3. Inyección de dependencias en el servicio
    AirlineService service = new AirlineService(repository, idGenerator);

    // 4. Iniciar menú interactivo
    AirlineMenu menu = new AirlineMenu(service);
    menu.run();
}
}
