📱 Red Social Empresarial - Backend (TPO)
Materia: Algoritmos y Estructuras de Datos II (UADE)

- Iteración: 1 (Gestión de Clientes y Estructuras Lineales)
- Lenguaje: Java 17+ | Build: Maven

📄 Descripción del Proyecto:
Este proyecto implementa el backend de una red social corporativa, priorizando la eficiencia algorítmica y el uso de Tipos Abstractos de Datos (TADs) adecuados para cada requerimiento.
El sistema permite la gestión de usuarios, procesamiento de solicitudes de seguimiento y un historial de acciones reversible.
El diseño se centra en evitar búsquedas secuenciales $O(n)$ mediante el uso de índices basados en Hashing.

🚀 Funcionalidades(Iteración 1)
- Gestión de Clientes: Alta de usuarios con validación de unicidad y rangos de scoring.
- Búsqueda Eficiente:
              - Por Nombre: Acceso inmediato $O(1)$.
              - Por Scoring: Acceso inmediato al grupo de puntaje $O(1)$.
- Sistema de Solicitudes: Procesamiento de solicitudes de seguimiento en estricto orden de llegada (FIFO).
- Historial de Acciones: Capacidad de "Deshacer" (Undo) la última operación realizada (LIFO).
- Persistencia: Carga inicial automática de datos desde archivo datos.json.

Módulo,Estructura de Datos,Complejidad (Big O),Justificación de Diseño
Búsqueda por Nombre,"HashMap<String, Cliente>",O(1),Permite acceso directo por clave única sin recorrer colecciones. Reemplaza la búsqueda lineal.
Búsqueda por Scoring,"HashMap<Integer, List<Cliente>>",O(1) (al grupo),Actúa como índice invertido. Agrupa clientes por puntaje para evitar recorrer toda la base de datos.
Historial (Deshacer),Stack<Accion> (Pila),O(1),"La naturaleza del ""Undo"" es LIFO (Último en entrar, primero en salir)."
Solicitudes,Queue<Solicitud> (LinkedList),O(1),"Implementa FIFO (Primero en entrar, primero en salir) para respetar el orden de llegada."

src/main/java/
├── models/       # Clientes, Acciones y Solicitudes (POJOs)
├── services/     # Lógica de negocio (SocialNetwork, ActionHistory)
├── utils/        # Carga de datos (JsonLoader con Gson)
├── exceptions/   # Manejo de errores de dominio (ClienteYaExiste, etc.)
└── Main.java     # Punto de entrada y Menú Interactivo

⚙️ Requisitos y Ejecución
Prerrequisitos
- JDK 17 o superior.
- Maven 3.6+ (para gestión de dependencias).

Cómo ejecutar
- Asegúrese de que el archivo datos.json se encuentre en la raíz del proyecto.
- Compilar y ejecutar:Bashmvn clean compile exec:java -Dexec.mainClass="Main" O simplemente ejecutar la clase Main desde su IDE favorito (IntelliJ/Eclipse).

Cómo ejecutar los Tests
- El proyecto incluye pruebas unitarias con JUnit 5 para validar lógica de negocio y excepciones.
- mvn test

🛡️ Manejo de Errores
El sistema implementa Excepciones Personalizadas para separar la lógica de negocio de la interfaz de usuario:
- ClienteYaExisteException: Evita duplicados.
- ClienteNoEncontradoException: Valida integridad referencial.
- OperacionInvalidaException: Reglas de negocio (ej. auto-seguimiento).
  
Trabajo Práctico realizado para la cursada de Verano 2026.
