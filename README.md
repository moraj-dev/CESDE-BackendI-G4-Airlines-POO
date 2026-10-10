# Airlines-poo

Sistema de reservas aéreas en consola, hecho en **Java 21** con **Maven**, para el
[Curso abierto de POO con Java](https://moraj-dev.github.io/poo-java-course/).
Cada grupo de cinco estudiantes construye un módulo del sistema sobre este proyecto base.

En esta etapa los datos viven **en memoria**. Más adelante se reemplazará la capa de
repositorios por una base de datos con JDBC o JPA, sin cambiar servicios ni menús.

## Requisitos

- JDK 21 LTS
- IntelliJ IDEA (Community o Ultimate) con soporte Maven
- Git y una cuenta de GitHub

## Cómo abrir el proyecto en IntelliJ IDEA

1. Clona el repositorio y cámbiate a `develop`:
   ```bash
   git clone <url-del-repositorio>
   cd airlines-poo
   git checkout develop
   ```
2. En IntelliJ: **File → Open** y selecciona la carpeta `airlines-poo` (la que contiene `pom.xml`).
   Si IntelliJ pregunta, elige **Open as Project** y **Trust Project**.
3. Espera a que Maven importe el proyecto (barra de progreso inferior).
4. Verifica el JDK en **File → Project Structure → Project**: *SDK* y *Language level* en **21**.
5. Ejecuta `com.airlinespoo.app.Main` (clic derecho → **Run 'Main.main()'**). Debe imprimir un saludo.

Desde la terminal también puedes compilar y probar:

```bash
mvn clean test
mvn clean package
java -jar target/airlines-poo-0.1.0-SNAPSHOT.jar
```

## Estructura del proyecto

```text
airlines-poo/
├── pom.xml
├── README.md
├── .gitignore
├── .github/pull_request_template.md
└── src/
    ├── main/java/com/airlinespoo/
    │   ├── core/                  Kit base del docente (NO modificar)
    │   │   ├── exception/         AirlinesException y 4 excepciones especializadas
    │   │   ├── model/             Identifiable
    │   │   ├── repository/        Repository, InMemoryRepository
    │   │   ├── util/              IdGenerator, Validator
    │   │   └── ui/                Menu, ConsoleInput
    │   ├── customers/             Módulo 1: países, tipos de documento, clientes
    │   ├── locations/             Módulo 2: ciudades y aeropuertos
    │   ├── airlines/              Módulo 3: aerolíneas
    │   ├── flights/               Módulo 4: vuelos
    │   ├── reservations/          Módulo 5: reservas y pasajeros
    │   └── app/                   Módulo 3: Main y MainMenu
    └── test/java/com/airlinespoo/core/   Pruebas del kit base
```

Cada paquete de módulo trae los subpaquetes `model`, `repository`, `service` y `ui`
(cada uno con un `package-info.java` que describe qué va allí y permite que Git conserve
la carpeta vacía).

**Regla de dependencia:** `ui` → `service` → `repository` → `model`. Nunca al revés.

## Reparto de módulos

| Módulo | Tablas | Paquete | Rama |
| --- | --- | --- | --- |
| 1 · Clientes y catálogos | `tblCountries`, `tblDocumentsType`, `tblCustomers` | `customers` | `feature/modulo-1-clientes` |
| 2 · Ubicaciones | `tblCities`, `tblAirports` | `locations` | `feature/modulo-2-ubicaciones` |
| 3 · Aerolíneas y aplicación | `tblAirlines` | `airlines`, `app` | `feature/modulo-3-aerolineas`, `feature/modulo-3-aplicacion` |
| 4 · Vuelos | `tblFlights` | `flights` | `feature/modulo-4-vuelos` |
| 5 · Reservas | `tblReservations`, `tblDetailsReservation` | `reservations` | `feature/modulo-5-reservas` |

## Kit base (paquete `core`)

| Pieza | Para qué sirve |
| --- | --- |
| `AirlinesException` | Excepción base (no verificada) de todo el sistema |
| `ValidationException` | Un dato no cumple una regla (vacío, muy largo, fuera de rango) |
| `DuplicateException` | Id o valor único repetido |
| `NotFoundException` | No existe lo que se pidió (la lanza el **servicio**) |
| `BusinessRuleException` | Una regla entre varios objetos no se cumple |
| `Identifiable<ID>` | Contrato: toda entidad ofrece su id |
| `Repository<T, ID>` | Contrato CRUD: `create`, `findById`, `findAll`, `update`, `deleteById`, `existsById`, `count` |
| `InMemoryRepository<T, ID>` | Clase **abstracta** con mapa en memoria; cada módulo la extiende |
| `IdGenerator` | Ids consecutivos con arranque y paso (aerolíneas: 100, de 10 en 10) |
| `Validator` | Validaciones estáticas con el nombre del campo en el mensaje |
| `Menu` | Contrato de las pantallas de consola |
| `ConsoleInput` | Lectura del teclado con reintentos (texto, entero, fecha, hora, sí/no) |

Las clases del kit están documentadas con Javadoc: léelas antes de programar.
**No las modifiques.** Si crees que falta algo o hay un error, abre un *Issue* en GitHub.

## Flujo de trabajo con Git (Gitflow)

- `main`: versiones publicadas y etiquetadas. Solo el docente.
- `develop`: integración. Aquí llegan los módulos terminados, mediante Pull Request.
- `feature/*`: una rama por módulo. **Nace de `develop` y vuelve a `develop`.**

```bash
git checkout develop
git pull origin develop
git checkout -b feature/modulo-1-clientes
# ... programar, y luego:
git add <archivos de tu módulo>
git commit -m "feature: agrega clase Country con validaciones"
git push origin feature/modulo-1-clientes
```

Después abre un Pull Request hacia **`develop`** (no `main`) con la plantilla que ya trae
el repositorio. Formato de commits: `tipo: descripción` con `feature`, `fix`, `refactor`,
`docs`, `test` o `merge`.

---

## Para el docente: publicar este proyecto en GitHub (por grupo)

1. **Crear el repositorio** vacío en GitHub (uno por grupo, por ejemplo `airlines-poo-grupo-1`),
   sin README ni `.gitignore` propios.
2. **Inicializar y subir `main`** desde esta carpeta:
   ```bash
   git init -b main
   git add .
   git commit -m "feature: proyecto base Airlines-poo con kit base"
   git remote add origin <url-del-repositorio-del-grupo>
   git push -u origin main
   ```
3. **Crear `develop`** a partir de `main` y subirla:
   ```bash
   git checkout -b develop
   git push -u origin develop
   ```
4. **Marcar `develop` como rama por defecto** (Settings → Branches → Default branch) para que
   los Pull Requests apunten a ella automáticamente.
5. **Proteger `main` y `develop`** (Settings → Branches → Add rule): exigir Pull Request y al
   menos una aprobación; impedir *push* directo y *force push*.
6. **Agregar a los cinco estudiantes como colaboradores** (Settings → Collaborators).
7. **Crear un Issue por módulo** con su lista de tareas, asignado al estudiante, y opcionalmente
   un tablero de Projects (*Por hacer*, *En curso*, *En revisión*, *Integrado*).
8. Tras comprobar que `mvn clean test` pasa en limpio, etiqueta el punto de partida:
   ```bash
   git tag -a v0.1.0 -m "Proyecto base con kit base"
   git push origin v0.1.0
   ```
9. Al cerrar el ejercicio: `release/1.0.0` desde `develop`, ajustar la versión en `pom.xml`,
   integrar en `main` con `--no-ff`, etiquetar `v1.0.0` y volver a integrar en `develop`
   (ver [G0 · Gitflow](https://moraj-dev.github.io/poo-java-course/g0/03-gitflow.html)).

> Para reutilizar el mismo proyecto en varios grupos, copia la carpeta (sin `.git`, `target/` ni
> `.idea/`) y repite los pasos 1 a 8 con cada repositorio.
