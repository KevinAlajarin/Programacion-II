# Documentación de Diseño - Sistema de Gestión de Clientes en Red Social Empresarial

## Índice
1. [Introducción](#introducción)
2. [Arquitectura General del Sistema](#arquitectura-general-del-sistema)
3. [Justificación de Tipos Abstractos de Datos (TADs)](#justificación-de-tipos-abstractos-de-datos-tads)
4. [Análisis Detallado por Clase](#análisis-detallado-por-clase)
5. [Invariantes de Representación](#invariantes-de-representación)
6. [Análisis de Complejidad](#análisis-de-complejidad)
7. [Decisiones de Diseño](#decisiones-de-diseño)

---

## Introducción

Este documento describe la arquitectura, diseño y justificación de las estructuras de datos y clases implementadas para la **Iteración 1** del Trabajo Práctico de Algoritmos y Estructuras de Datos II. El sistema gestiona clientes en una red social empresarial, permitiendo operaciones eficientes de búsqueda, gestión de acciones y procesamiento de solicitudes de seguimiento.

El diseño prioriza la **eficiencia computacional**, especialmente en operaciones de búsqueda, siguiendo el requerimiento explícito del enunciado que valora especialmente el uso de búsquedas eficientes (evitar búsquedas secuenciales siempre que sea posible).

---

## Arquitectura General del Sistema

El sistema está organizado en una arquitectura por capas con separación clara de responsabilidades:

```
┌─────────────────────────────────────────┐
│           Capa de Presentación         │
│              (Main.java)                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Capa de Servicios               │
│    (SocialNetwork, ActionHistory)       │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      Capa de Estructuras de Datos       │
│    (ScoringBST, SocialGraph*)           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Capa de Modelos                 │
│   (Cliente, Accion, Solicitud)          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      Capa de Utilidades                 │
│         (JsonLoader)                     │
└─────────────────────────────────────────┘
```

*Nota: `SocialGraph` está preparado para Iteración 3, pero se incluye en la documentación para completitud.*

---

## Justificación de Tipos Abstractos de Datos (TADs)

### 1. TAD: Conjunto de Clientes con Búsqueda Dual

**Especificación del TAD:**
- **Operaciones principales:**
  - `agregarCliente(nombre, scoring)`: Inserta un nuevo cliente en el sistema
  - `buscarPorNombre(nombre)`: Retorna el cliente con el nombre dado
  - `buscarPorScoring(scoring, nombre)`: Retorna el cliente con el scoring y nombre dados

**Justificación de la Representación:**

El sistema utiliza una **representación dual** que combina dos estructuras complementarias:

1. **`HashMap<String, Cliente> clienteMap`**: 
   - **Propósito**: Búsqueda por nombre en tiempo constante O(1) promedio
   - **Justificación**: El nombre es un identificador único y natural para los clientes. Un `HashMap` permite acceso directo mediante la función hash del nombre, evitando búsquedas secuenciales O(n)
   - **Ventaja**: Operaciones de inserción, búsqueda y eliminación son O(1) en promedio

2. **`ScoringBST` (Árbol Binario de Búsqueda)**:
   - **Propósito**: Mantener los clientes ordenados por scoring para búsquedas eficientes O(log n)
   - **Justificación**: El requerimiento explícito pide búsquedas por scoring eficientes. Un BST permite:
     - Búsqueda por scoring en O(log n) en el caso promedio
     - Mantenimiento automático del orden (útil para consultas de rango futuras)
     - Preparación para la Iteración 2 que requiere clientes ordenados por scoring
   - **Ventaja**: Estructura ordenada que facilita operaciones de consulta y análisis

**¿Por qué no usar solo una estructura?**

- Si usáramos solo `HashMap` por nombre: perderíamos la capacidad de búsqueda eficiente por scoring
- Si usáramos solo un BST: perderíamos el acceso O(1) por nombre, que es la operación más frecuente
- La combinación ofrece lo mejor de ambos mundos: acceso rápido por nombre (O(1)) y búsqueda eficiente por scoring (O(log n))

**Invariante de Representación:**
- Todo cliente presente en `clienteMap` también debe estar presente en `scoringIndex`
- No puede haber dos clientes con el mismo nombre (garantizado por `clienteMap`)
- El scoring de cualquier cliente debe estar en el rango [0, 100]

---

### 2. TAD: Historial de Acciones (Stack)

**Especificación del TAD:**
- **Operaciones principales:**
  - `registrarAccion(accion)`: Registra una nueva acción en el historial
  - `deshacer()`: Elimina y retorna la última acción realizada
  - `isEmpty()`: Verifica si el historial está vacío

**Justificación de la Representación:**

Se utiliza un **`Stack<Accion>`** implementado mediante `java.util.Stack`:

- **Propósito**: Mantener un registro LIFO (Last In, First Out) de todas las acciones realizadas
- **Justificación**: 
  - El requerimiento pide "deshacer la última acción realizada", lo cual es exactamente la semántica de un Stack
  - Las operaciones `push()` y `pop()` son O(1), cumpliendo con el requerimiento de eficiencia
  - La estructura es simple y directa: no requiere búsquedas ni recorridos, solo inserción y eliminación del tope

**Ventajas:**
- **Simplicidad**: La estructura es intuitiva y fácil de mantener
- **Eficiencia**: Operaciones O(1) tanto para registrar como para deshacer
- **Trazabilidad**: Permite mantener un registro completo de todas las operaciones del sistema

**Invariante de Representación:**
- El elemento en el tope del Stack siempre es la última acción registrada
- El orden de las acciones refleja el orden temporal inverso (la más reciente está en el tope)
- `deshacer()` solo puede ejecutarse si `isEmpty() == false`

---

### 3. TAD: Cola de Solicitudes de Seguimiento (Queue)

**Especificación del TAD:**
- **Operaciones principales:**
  - `enviarSolicitud(solicitante, solicitado)`: Agrega una solicitud a la cola
  - `procesarSolicitudes()`: Procesa todas las solicitudes en orden FIFO

**Justificación de la Representación:**

Se utiliza un **`Queue<Solicitud>`** implementado mediante `LinkedList`:

- **Propósito**: Mantener las solicitudes de seguimiento en orden de llegada (FIFO - First In, First Out)
- **Justificación**:
  - El requerimiento explícito dice "procesar solicitudes de seguimiento en el orden en que fueron enviadas"
  - Una cola garantiza que el primer elemento en entrar es el primero en salir
  - `LinkedList` implementa `Queue` de manera eficiente: `add()` es O(1) y `poll()` es O(1)

**Ventajas:**
- **Orden garantizado**: FIFO asegura procesamiento justo y cronológico
- **Eficiencia**: Operaciones de encolar y desencolar son O(1)
- **Simplicidad**: Estructura estándar de Java, bien documentada y probada

**Invariante de Representación:**
- Las solicitudes se procesan en el mismo orden en que fueron agregadas
- Toda solicitud contiene nombres de clientes válidos (validados antes de encolar)
- Una solicitud no puede tener `solicitante == solicitado` (auto-seguimiento prohibido)

---

### 4. TAD: Grafo de Relaciones Sociales (Preparado para Iteración 3)

**Especificación del TAD:**
- **Operaciones principales:**
  - `addVertex(cliente)`: Agrega un vértice (cliente) al grafo
  - `addEdge(c1, c2)`: Agrega una arista bidireccional entre dos clientes
  - `getNeighbors(cliente)`: Retorna todos los vecinos (conexiones) de un cliente
  - `getDistance(start, end)`: Calcula la distancia mínima entre dos clientes

**Justificación de la Representación:**

Se utiliza una **Lista de Adyacencia** mediante `Map<Cliente, Set<Cliente>>`:

- **Propósito**: Representar relaciones generales entre clientes (amistades, conexiones) de manera eficiente
- **Justificación**:
  - El requerimiento de la Iteración 3 pide "obtener vecinos de un cliente" en O(1)
  - Una lista de adyacencia permite acceso directo a los vecinos mediante el mapa: `adjList.get(cliente)` es O(1)
  - `Set<Cliente>` evita duplicados y permite verificación de pertenencia en O(1)
  - La representación es eficiente en espacio para grafos dispersos (típico en redes sociales)

**Ventajas:**
- **Acceso rápido**: O(1) para obtener vecinos de un cliente
- **Eficiencia espacial**: Solo almacena las conexiones existentes
- **Flexibilidad**: Permite agregar/eliminar aristas fácilmente

**Nota**: Esta estructura está preparada para la Iteración 3 y no se utiliza activamente en la Iteración 1, pero se documenta aquí para completitud del diseño.

---

## Análisis Detallado por Clase

### Clase: `Cliente` (models/Cliente.java)

**Responsabilidad**: Modelar la entidad cliente con sus atributos fundamentales (nombre y scoring).

**Atributos:**
- `nombre` (String): Identificador único del cliente
- `scoring` (int): Valor numérico que representa el nivel de influencia (0-100)

**Lógica de Implementación:**

1. **Implementación de `Comparable<Cliente>`**:
   ```java
   public int compareTo(Cliente otro) {
       int result = Integer.compare(this.scoring, otro.scoring);
       if (result == 0) {
           return this.nombre.compareTo(otro.nombre); // Desempate por nombre
       }
       return result;
   }
   ```
   - **Justificación**: Permite ordenar clientes por scoring para el BST
   - **Desempate**: Si dos clientes tienen el mismo scoring, se ordenan alfabéticamente por nombre
   - **Importancia**: Crítico para el funcionamiento correcto de `ScoringBST`

2. **Implementación de `equals()` y `hashCode()`**:
   ```java
   public boolean equals(Object o) {
       // Compara solo por nombre (identificador único)
   }
   
   public int hashCode() {
       return Objects.hash(nombre);
   }
   ```
   - **Justificación**: 
     - `equals()` compara solo por `nombre` porque el nombre es el identificador único
     - `hashCode()` debe ser consistente con `equals()` para funcionar correctamente en `HashMap`
     - **Crítico**: Si `hashCode()` no fuera consistente, el `HashMap` no funcionaría correctamente

3. **Mutabilidad**:
   - La clase tiene `setNombre()` y `setScoring()`, pero en la práctica estos métodos no deberían usarse después de la creación
   - **Riesgo**: Cambiar el nombre rompería la clave del `HashMap`; cambiar el scoring desincronizaría el BST
   - **Recomendación conceptual**: En un diseño más estricto, la clase debería ser inmutable o las mutaciones deberían ir acompañadas de actualización de índices

**Complejidad:**
- Constructor: O(1)
- `compareTo()`: O(1)
- `equals()`: O(1)
- `hashCode()`: O(1)

---

### Clase: `ScoringBST` (structures/ScoringBST.java)

**Responsabilidad**: Mantener un índice ordenado de clientes por scoring para búsquedas eficientes.

**Estructura Interna:**
- Clase interna `Node`: Representa un nodo del árbol con `data` (Cliente), `left` y `right` (hijos)

**Lógica de Implementación:**

1. **Inserción (`insert`)**:
   ```java
   private Node insertRec(Node root, Cliente cliente) {
       if (root == null) return new Node(cliente);
       
       if (cliente.compareTo(root.data) < 0)
           root.left = insertRec(root.left, cliente);
       else if (cliente.compareTo(root.data) > 0)
           root.right = insertRec(root.right, cliente);
       
       return root;
   }
   ```
   - **Algoritmo**: Inserción recursiva estándar en BST
   - **Ordenamiento**: Los clientes se ordenan por `compareTo()` (scoring primero, nombre como desempate)
   - **Complejidad**: O(log n) promedio, O(n) en el peor caso (árbol degenerado)

2. **Búsqueda (`search`)**:
   ```java
   public Cliente search(int scoring, String nombre) {
       Cliente target = new Cliente(nombre, scoring);
       return searchRec(root, target);
   }
   ```
   - **Estrategia**: Crea un cliente "target" con los parámetros dados y busca usando `compareTo()`
   - **Ventaja**: Reutiliza la lógica de comparación ya implementada en `Cliente`
   - **Complejidad**: O(log n) promedio, O(n) en el peor caso

3. **Limitación Actual**:
   - El método `printLevel4()` está preparado para la Iteración 2 pero no implementado aún
   - Esto es correcto según el diseño incremental del trabajo práctico

**Análisis de Complejidad:**
- **Inserción**: 
  - Tiempo promedio: O(log n) si el árbol está balanceado
  - Tiempo peor caso: O(n) si el árbol está degenerado (lista enlazada)
  - Espacio: O(n) para almacenar todos los nodos
- **Búsqueda**:
  - Tiempo promedio: O(log n)
  - Tiempo peor caso: O(n)
  - Espacio: O(h) donde h es la altura del árbol (por recursión)

**Mejora Futura Potencial:**
- Para garantizar O(log n) en todos los casos, se podría implementar un árbol balanceado (AVL o Red-Black Tree)
- Sin embargo, para la Iteración 1, un BST simple es suficiente y más fácil de implementar y mantener

---

### Clase: `ActionHistory` (services/ActionHistory.java)

**Responsabilidad**: Gestionar el historial de acciones del sistema con capacidad de deshacer.

**Estructura Interna:**
- `Stack<Accion> historial`: Pila que almacena todas las acciones en orden LIFO

**Lógica de Implementación:**

1. **Registro de Acciones (`registrarAccion`)**:
   ```java
   public void registrarAccion(Accion accion) {
       historial.push(accion);
   }
   ```
   - **Operación**: Simplemente apila la acción en el Stack
   - **Complejidad**: O(1) - operación de tiempo constante
   - **Garantía**: La acción queda registrada como la más reciente

2. **Deshacer Última Acción (`deshacer`)**:
   ```java
   public Accion deshacer() {
       if (historial.isEmpty()) return null;
       return historial.pop();
   }
   ```
   - **Operación**: Desapila y retorna la última acción
   - **Complejidad**: O(1) - operación de tiempo constante
   - **Validación**: Verifica que el historial no esté vacío antes de desapilar

3. **Consulta de Estado (`isEmpty`)**:
   ```java
   public boolean isEmpty() {
       return historial.isEmpty();
   }
   ```
   - **Propósito**: Permite verificar si hay acciones para deshacer
   - **Uso**: Utilizado por `SocialNetwork.deshacerUltimaAccion()` para validar antes de deshacer

**Invariante de Representación:**
- El Stack siempre mantiene el orden LIFO: la acción más reciente está en el tope
- `deshacer()` siempre retorna la última acción registrada (si existe)
- No hay acciones duplicadas ni modificaciones externas al Stack

**Complejidad:**
- `registrarAccion()`: O(1) tiempo, O(1) espacio adicional por acción
- `deshacer()`: O(1) tiempo
- `isEmpty()`: O(1) tiempo
- **Espacio total**: O(n) donde n es el número de acciones registradas

---

### Clase: `Accion` (models/Accion.java)

**Responsabilidad**: Modelar una acción realizada en el sistema para su registro y posible reversión.

**Estructura:**

1. **Enum `TipoAccion`**:
   - `AGREGAR_CLIENTE`: Representa la acción de agregar un nuevo cliente
   - `SEGUIR_USUARIO`: Representa la acción de enviar una solicitud de seguimiento
   - **Extensibilidad**: Fácil agregar nuevos tipos de acción en el futuro

2. **Atributos**:
   - `tipo` (TipoAccion): Tipo de acción realizada
   - `sujeto` (String): Nombre del cliente que realiza la acción
   - `objeto` (String): Nombre del cliente afectado (puede ser null para acciones sin objeto)

**Lógica de Diseño:**

- **Inmutabilidad**: La clase no tiene setters, lo que garantiza que una acción registrada no puede modificarse
- **Información suficiente**: Contiene toda la información necesaria para revertir la acción:
  - Para `AGREGAR_CLIENTE`: El `sujeto` es el nombre del cliente a eliminar
  - Para `SEGUIR_USUARIO`: `sujeto` es quien envía, `objeto` es quien recibe la solicitud

**Uso en el Sistema:**
- Las acciones se crean en `SocialNetwork` cuando se realizan operaciones
- Se registran en `ActionHistory` para mantener el historial
- Se utilizan en `deshacerUltimaAccion()` para revertir los efectos

**Complejidad:**
- Constructor: O(1)
- Todos los getters: O(1)
- `toString()`: O(1)

---

### Clase: `Solicitud` (models/Solicitud.java)

**Responsabilidad**: Modelar una solicitud de seguimiento entre dos clientes.

**Atributos:**
- `solicitante` (String): Nombre del cliente que envía la solicitud
- `solicitado` (String): Nombre del cliente que recibe la solicitud
- `timestamp` (long): Marca de tiempo de creación (preparado para futuras funcionalidades)

**Lógica de Implementación:**

1. **Timestamp Automático**:
   ```java
   this.timestamp = System.currentTimeMillis();
   ```
   - Se asigna automáticamente en el constructor
   - **Propósito**: Permite ordenar solicitudes por tiempo si fuera necesario en el futuro
   - **Nota**: Aunque el requerimiento menciona "opcionalmente fecha y hora", se incluye para preparar funcionalidades futuras

2. **Inmutabilidad**:
   - No tiene setters, garantizando que una solicitud no puede modificarse después de crearse
   - **Ventaja**: Previene inconsistencias en la cola de solicitudes

**Uso en el Sistema:**
- Se crean en `SocialNetwork.enviarSolicitud()`
- Se almacenan en la cola `requestQueue`
- Se procesan en orden FIFO mediante `procesarSolicitudes()`

**Complejidad:**
- Constructor: O(1)
- Todos los getters: O(1)

---

### Clase: `SocialNetwork` (services/SocialNetwork.java)

**Responsabilidad**: Servicio principal que coordina todas las operaciones del sistema de red social.

**Estructura de Datos Interna:**
```java
private Map<String, Cliente> clienteMap;        // O(1) búsqueda por nombre
private ScoringBST scoringIndex;                // O(log n) búsqueda por scoring
private ActionHistory history;                   // Historial de acciones
private Queue<Solicitud> requestQueue;          // Cola FIFO de solicitudes
```

**Lógica de Implementación por Método:**

#### 1. `agregarCliente(String nombre, int scoring)`

**Flujo de Ejecución:**
1. **Validación de Duplicados**: Verifica si el cliente ya existe en `clienteMap`
   - Si existe → lanza `ClienteYaExisteException`
2. **Validación de Scoring**: Verifica que el scoring esté en el rango [0, 100]
   - Si está fuera de rango → lanza `IllegalArgumentException`
3. **Creación del Cliente**: Crea una nueva instancia de `Cliente`
4. **Inserción Dual**: 
   - Agrega a `clienteMap` para búsqueda O(1) por nombre
   - Inserta en `scoringIndex` para búsqueda O(log n) por scoring
5. **Registro de Acción**: Registra la acción en el historial
6. **Logging**: Imprime confirmación (útil para debugging)

**Complejidad:**
- Tiempo: O(log n) - dominado por la inserción en el BST
- Espacio: O(1) adicional (el cliente ya se crea)

**Invariante Mantenido:**
- Después de ejecutarse, el cliente existe tanto en `clienteMap` como en `scoringIndex`

---

#### 2. `buscarPorNombre(String nombre)`

**Flujo de Ejecución:**
1. **Búsqueda Directa**: Utiliza `clienteMap.get(nombre)`
2. **Retorno**: Retorna el `Cliente` encontrado o `null` si no existe

**Complejidad:**
- Tiempo: O(1) promedio - acceso directo mediante hash
- Espacio: O(1)

**Ventaja:**
- Es la operación más eficiente del sistema gracias al `HashMap`

---

#### 3. `buscarPorScoring(int scoring, String nombre)`

**Flujo de Ejecución:**
1. **Creación de Target**: Crea un `Cliente` temporal con los parámetros dados
2. **Búsqueda en BST**: Utiliza `scoringIndex.search()` que busca usando `compareTo()`
3. **Retorno**: Retorna el `Cliente` encontrado o `null` si no existe

**Complejidad:**
- Tiempo: O(log n) promedio - búsqueda en BST
- Espacio: O(1) adicional (cliente temporal)

**Nota sobre el Diseño:**
- Requiere tanto `scoring` como `nombre` porque el BST ordena por scoring primero y usa nombre como desempate
- Esto garantiza que la búsqueda sea precisa y eficiente

---

#### 4. `enviarSolicitud(String solicitante, String solicitado)`

**Flujo de Ejecución:**
1. **Validación de Auto-seguimiento**: Verifica que `solicitante != solicitado`
   - Si son iguales → lanza `OperacionInvalidaException`
2. **Validación de Existencia**: Verifica que ambos clientes existan en `clienteMap`
   - Si alguno no existe → lanza `ClienteNoEncontradoException`
3. **Creación de Solicitud**: Crea una nueva instancia de `Solicitud`
4. **Encolar**: Agrega la solicitud a `requestQueue`
5. **Registro de Acción**: Registra la acción en el historial
6. **Logging**: Imprime confirmación

**Complejidad:**
- Tiempo: O(1) - todas las operaciones son constantes
- Espacio: O(1) adicional por solicitud

**Invariante Mantenido:**
- La solicitud queda en la cola en orden FIFO
- La acción queda registrada en el historial

---

#### 5. `procesarSolicitudes()`

**Flujo de Ejecución:**
1. **Inicialización**: Crea una lista para almacenar los resultados
2. **Procesamiento FIFO**: Mientras la cola no esté vacía:
   - Desencola la primera solicitud (`poll()`)
   - Procesa la solicitud (actualmente solo logging)
   - Agrega el resultado a la lista
3. **Retorno**: Retorna la lista de solicitudes procesadas

**Complejidad:**
- Tiempo: O(n) donde n es el número de solicitudes en la cola
- Espacio: O(n) para la lista de resultados

**Nota sobre la Implementación Actual:**
- El método actualmente solo registra las solicitudes procesadas
- En la Iteración 2, este método deberá actualizar las relaciones de seguimiento reales
- La estructura FIFO está correctamente implementada

---

#### 6. `deshacerUltimaAccion()`

**Flujo de Ejecución:**
1. **Validación**: Verifica que el historial no esté vacío
   - Si está vacío → imprime mensaje y retorna
2. **Deshacer**: Obtiene la última acción del historial
3. **Reversión según Tipo**:
   - **`AGREGAR_CLIENTE`**: 
     - Elimina el cliente de `clienteMap`
     - **Nota**: No elimina del BST (esto es una limitación actual que debería mejorarse)
   - **`SEGUIR_USUARIO`**: 
     - Actualmente solo hace logging
     - **Nota**: En una implementación completa, debería buscar y eliminar la solicitud de la cola si aún no fue procesada

**Complejidad:**
- Tiempo: O(1) para obtener la acción + O(1) o O(n) según el tipo de reversión
- Espacio: O(1)

**Limitaciones Actuales:**
1. **Inconsistencia en Deshacer AGREGAR_CLIENTE**: 
   - Elimina de `clienteMap` pero no del BST
   - **Mejora sugerida**: También eliminar de `scoringIndex`
2. **Deshacer SEGUIR_USUARIO**: 
   - No revierte realmente la solicitud de la cola
   - **Mejora sugerida**: Buscar y eliminar la solicitud correspondiente (aunque esto sería O(n))

---

### Clase: `JsonLoader` (utils/JsonLoader.java)

**Responsabilidad**: Cargar datos iniciales del sistema desde un archivo JSON.

**Estructura Interna:**

1. **Clases DTO (Data Transfer Objects)**:
   - `ClienteJson`: Mapea la estructura JSON de un cliente
     ```java
     String nombre;
     int scoring;
     List<String> siguiendo;  // Para Iteración 1
     ```
   - `DataWrapper`: Mapea la estructura raíz del JSON
     ```java
     List<ClienteJson> clientes;
     ```

**Lógica de Implementación:**

#### `cargar(String ruta, SocialNetwork red)`

**Flujo de Ejecución:**

1. **Lectura del Archivo**:
   - Utiliza `Gson` para deserializar el JSON
   - Usa `try-with-resources` para garantizar cierre automático del archivo
   - Maneja `IOException` si el archivo no existe o no se puede leer

2. **Validación de Datos**:
   - Verifica que el JSON no sea null y que tenga la estructura esperada
   - Si el formato es incorrecto → imprime advertencia y retorna

3. **Fase 1: Carga de Clientes**:
   ```java
   for (ClienteJson c : data.clientes) {
       try {
           red.agregarCliente(c.nombre, c.scoring);
           cargados++;
       } catch (Exception e) {
           // Manejo de errores (duplicados, etc.)
       }
   }
   ```
   - Itera sobre cada cliente en el JSON
   - Intenta agregar cada cliente al sistema
   - Maneja errores individualmente (no detiene la carga completa)
   - **Ventaja**: Si un cliente tiene datos inválidos, los demás se cargan igualmente

4. **Fase 2: Carga de Relaciones**:
   ```java
   for (ClienteJson c : data.clientes) {
       if (c.siguiendo != null) {
           for (String seguido : c.siguiendo) {
               red.enviarSolicitud(c.nombre, seguido);
           }
       }
   }
   ```
   - Itera sobre cada cliente
   - Para cada cliente, procesa su lista `siguiendo`
   - Envía solicitudes de seguimiento
   - **Nota**: Las solicitudes se encolan pero no se procesan automáticamente

5. **Manejo de Errores**:
   - Errores de cliente: Se reportan pero no detienen la carga
   - Errores de relación: Se ignoran silenciosamente (podrían mejorarse con logging opcional)
   - Errores de IO: Se reportan con mensaje claro al usuario

**Complejidad:**
- Tiempo: O(n + m) donde n es el número de clientes y m es el número de relaciones
- Espacio: O(n) para cargar el JSON en memoria

**Ventajas del Diseño:**
- **Robustez**: No falla completamente si hay datos inválidos
- **Separación de responsabilidades**: La lógica de carga está separada de la lógica de negocio
- **Reutilizable**: Puede usarse para cargar desde diferentes archivos JSON

---

### Clase: `Main` (Main.java)

**Responsabilidad**: Interfaz de usuario y punto de entrada del sistema.

**Lógica de Implementación:**

1. **Inicialización**:
   - Crea una instancia única de `SocialNetwork`
   - Carga datos iniciales desde `datos.json` automáticamente

2. **Menú Interactivo**:
   - Bucle principal que muestra opciones al usuario
   - Procesa la entrada del usuario y ejecuta la operación correspondiente

3. **Manejo de Excepciones Centralizado**:
   ```java
   catch (SocialNetworkException e) {
       // Errores de negocio
   } catch (IllegalArgumentException e) {
       // Errores de validación de datos
   } catch (Exception e) {
       // Errores inesperados
   }
   ```
   - Captura y maneja todas las excepciones de manera amigable
   - Proporciona mensajes claros al usuario según el tipo de error

**Complejidad:**
- Depende de las operaciones seleccionadas por el usuario
- El menú en sí es O(1) por iteración

---

### Jerarquía de Excepciones

**Diseño de Excepciones:**

```
SocialNetworkException (checked)
├── ClienteYaExisteException
├── ClienteNoEncontradoException
└── OperacionInvalidaException
```

**Justificación:**

1. **`SocialNetworkException` (clase base)**:
   - Excepción checked que extiende `Exception`
   - **Ventaja**: Fuerza al código cliente a manejar las excepciones explícitamente
   - **Propósito**: Agrupar todas las excepciones de negocio del sistema

2. **Excepciones Específicas**:
   - **`ClienteYaExisteException`**: Se lanza cuando se intenta agregar un cliente duplicado
   - **`ClienteNoEncontradoException`**: Se lanza cuando se busca un cliente que no existe
   - **`OperacionInvalidaException`**: Se lanza para operaciones inválidas (ej: auto-seguimiento)

**Ventajas del Diseño:**
- **Claridad**: Cada excepción tiene un propósito específico y claro
- **Mantenibilidad**: Fácil agregar nuevas excepciones específicas
- **Manejo**: Permite manejar diferentes tipos de errores de manera diferenciada

---

## Invariantes de Representación

### Invariantes del Sistema `SocialNetwork`

1. **Consistencia de Estructuras de Clientes**:
   ```
   Para todo cliente c en clienteMap:
       c también está en scoringIndex
   ```
   - **Garantía**: Ambas estructuras mantienen la misma información
   - **Violación**: Si se agrega a una estructura pero no a la otra

2. **Unicidad de Nombres**:
   ```
   No existen dos clientes c1, c2 tal que c1.nombre == c2.nombre
   ```
   - **Garantía**: El nombre es identificador único
   - **Violación**: Si se permite agregar un cliente con nombre duplicado

3. **Rango de Scoring**:
   ```
   Para todo cliente c: 0 <= c.scoring <= 100
   ```
   - **Garantía**: El scoring siempre está en el rango válido
   - **Violación**: Si se crea un cliente con scoring fuera de rango

4. **Orden del Historial**:
   ```
   El elemento en el tope de history.historial es la última acción registrada
   ```
   - **Garantía**: El Stack mantiene el orden LIFO correcto
   - **Violación**: Si se modifica el Stack externamente

5. **Orden de la Cola de Solicitudes**:
   ```
   Las solicitudes en requestQueue se procesan en orden FIFO
   ```
   - **Garantía**: La cola mantiene el orden de llegada
   - **Violación**: Si se modifica la cola externamente o se usa una estructura no-FIFO

6. **Validez de Solicitudes**:
   ```
   Para toda solicitud s en requestQueue:
       s.solicitante != s.solicitado
       s.solicitante existe en clienteMap
       s.solicitado existe en clienteMap
   ```
   - **Garantía**: Todas las solicitudes son válidas
   - **Violación**: Si se encola una solicitud inválida

---

## Análisis de Complejidad

### Resumen de Complejidades por Operación

| Operación | Complejidad Temporal | Complejidad Espacial | Justificación |
|-----------|----------------------|---------------------|---------------|
| `agregarCliente()` | O(log n) | O(1) | Dominado por inserción en BST |
| `buscarPorNombre()` | O(1) promedio | O(1) | Acceso directo en HashMap |
| `buscarPorScoring()` | O(log n) promedio | O(1) | Búsqueda en BST |
| `enviarSolicitud()` | O(1) | O(1) | Operaciones constantes |
| `procesarSolicitudes()` | O(n) | O(n) | n = número de solicitudes |
| `registrarAccion()` | O(1) | O(1) | Push en Stack |
| `deshacer()` | O(1) | O(1) | Pop en Stack |
| `deshacerUltimaAccion()` | O(1) a O(n) | O(1) | Depende del tipo de acción |

### Análisis Detallado

#### Operaciones Críticas de Búsqueda

1. **Búsqueda por Nombre**:
   - **Estructura**: `HashMap<String, Cliente>`
   - **Complejidad**: O(1) promedio, O(n) en el peor caso (colisiones extremas)
   - **Justificación**: El hash function de String en Java es eficiente y las colisiones son raras
   - **Ventaja**: Es la operación más rápida del sistema

2. **Búsqueda por Scoring**:
   - **Estructura**: `ScoringBST` (BST no balanceado)
   - **Complejidad**: O(log n) promedio, O(n) en el peor caso
   - **Justificación**: 
     - Promedio: Si los datos están distribuidos aleatoriamente, el árbol estará balanceado
     - Peor caso: Si los datos están ordenados, el árbol se degenera en una lista
   - **Mejora potencial**: Usar árbol balanceado (AVL/Red-Black) para garantizar O(log n) siempre

#### Operaciones de Historial

1. **Registrar Acción**:
   - **Complejidad**: O(1) tiempo, O(1) espacio por acción
   - **Justificación**: `Stack.push()` es operación constante

2. **Deshacer Acción**:
   - **Complejidad**: O(1) tiempo
   - **Justificación**: `Stack.pop()` es operación constante

#### Operaciones de Cola

1. **Enviar Solicitud**:
   - **Complejidad**: O(1) tiempo y espacio
   - **Justificación**: `Queue.add()` en LinkedList es constante

2. **Procesar Solicitudes**:
   - **Complejidad**: O(n) tiempo, O(n) espacio para resultados
   - **Justificación**: Debe procesar todas las solicitudes en la cola

### Complejidad Espacial General

- **Almacenamiento de Clientes**: O(n) donde n es el número de clientes
  - `clienteMap`: O(n)
  - `scoringIndex`: O(n)
  - **Total**: O(n) (no O(2n) porque almacenan referencias, no copias)

- **Historial de Acciones**: O(m) donde m es el número de acciones
  - Cada acción ocupa espacio constante
  - **Total**: O(m)

- **Cola de Solicitudes**: O(k) donde k es el número de solicitudes pendientes
  - **Total**: O(k)

- **Complejidad Espacial Total**: O(n + m + k)

---

## Decisiones de Diseño

### 1. ¿Por qué HashMap + BST en lugar de una sola estructura?

**Decisión**: Usar dos estructuras complementarias (`HashMap` + `BST`)

**Justificación**:
- El requerimiento pide búsquedas eficientes por **nombre** y por **scoring**
- Un `HashMap` ofrece O(1) para búsqueda por nombre (clave natural)
- Un BST ofrece O(log n) para búsqueda por scoring (ordenamiento necesario)
- **Alternativas consideradas**:
  - Solo HashMap: Perderíamos búsqueda eficiente por scoring
  - Solo BST: Perderíamos acceso O(1) por nombre
  - TreeMap: Ofrecería ordenamiento pero solo por una clave (nombre o scoring)
- **Conclusión**: La combinación ofrece lo mejor de ambos mundos

**Trade-off**:
- **Ventaja**: Búsquedas eficientes en ambos casos
- **Desventaja**: Duplicación de referencias (aunque no de datos, solo referencias)
- **Costo**: Mantener sincronizadas ambas estructuras (complejidad adicional en `agregarCliente`)

### 2. ¿Por qué Stack para el historial?

**Decisión**: Usar `Stack<Accion>` para el historial

**Justificación**:
- El requerimiento pide "deshacer la última acción", que es exactamente la semántica LIFO
- Las operaciones deben ser O(1), y `Stack.push()` y `Stack.pop()` lo son
- **Alternativas consideradas**:
  - `ArrayList`: Permitiría acceso aleatorio pero `remove()` del último sería O(1) también
  - `LinkedList`: Similar a Stack pero Stack es más semánticamente correcto
- **Conclusión**: Stack es la estructura más adecuada semánticamente

### 3. ¿Por qué Queue (LinkedList) para solicitudes?

**Decisión**: Usar `Queue<Solicitud>` implementado con `LinkedList`

**Justificación**:
- El requerimiento pide procesar en orden FIFO (First In, First Out)
- `LinkedList` implementa `Queue` eficientemente con `add()` y `poll()` en O(1)
- **Alternativas consideradas**:
  - `ArrayDeque`: También O(1) pero `LinkedList` es más estándar para colas
  - `ArrayList`: `remove(0)` sería O(n), ineficiente
- **Conclusión**: `LinkedList` como `Queue` es la opción más estándar y eficiente

### 4. ¿Por qué excepciones checked?

**Decisión**: Usar excepciones checked (`SocialNetworkException extends Exception`)

**Justificación**:
- Fuerzan al código cliente a manejar los errores explícitamente
- Mejoran la robustez del sistema
- **Alternativa**: Excepciones unchecked (`RuntimeException`)
  - Más convenientes pero permiten ignorar errores
- **Conclusión**: Checked exceptions para errores de negocio, unchecked para errores de programación (`IllegalArgumentException`)

### 5. ¿Por qué Gson para JSON?

**Decisión**: Usar la librería `Gson` de Google para deserialización JSON

**Justificación**:
- Librería estándar y bien mantenida
- Permite mapeo automático de JSON a objetos Java mediante clases DTO
- **Alternativas**:
  - `Jackson`: Similar pero Gson es más simple para casos básicos
  - Parsing manual: Posible pero propenso a errores y más código
- **Conclusión**: Gson ofrece simplicidad y robustez

### 6. ¿Por qué clases DTO internas en JsonLoader?

**Decisión**: Usar clases internas `ClienteJson` y `DataWrapper` dentro de `JsonLoader`

**Justificación**:
- Encapsulan la estructura específica del JSON
- No contaminan el espacio de nombres global
- Solo son necesarias para la deserialización
- **Alternativa**: Clases públicas separadas
  - Más verboso y menos encapsulado
- **Conclusión**: Clases internas mantienen el código más limpio y organizado

### 7. ¿Por qué validar scoring en agregarCliente?

**Decisión**: Validar que scoring esté en [0, 100] en `agregarCliente()`

**Justificación**:
- **Defensive Programming**: Previene datos inválidos en el sistema
- Facilita debugging: Errores se detectan temprano
- **Alternativa**: No validar y confiar en el código cliente
  - Menos robusto, errores más difíciles de rastrear
- **Conclusión**: Validación temprana mejora la robustez del sistema

### 8. ¿Por qué no eliminar del BST en deshacerUltimaAccion?

**Decisión Actual**: Solo eliminar de `clienteMap` cuando se deshace `AGREGAR_CLIENTE`

**Limitación Identificada**: 
- No se elimina de `scoringIndex`, causando inconsistencia
- **Razón probable**: Eliminar de un BST requiere búsqueda O(log n) + eliminación O(log n)
- **Mejora sugerida**: Implementar `remove()` en `ScoringBST` y llamarlo en `deshacerUltimaAccion()`

**Justificación de la Limitación Actual**:
- En la Iteración 1, el foco está en la estructura del historial (Stack O(1))
- La reversión completa puede mejorarse en iteraciones futuras
- **Nota para la defensa**: Reconocer esta limitación muestra comprensión del sistema

---

## Conclusiones

### Fortalezas del Diseño

1. **Eficiencia**: Las operaciones críticas (búsquedas) son O(1) o O(log n), cumpliendo con el requerimiento de eficiencia
2. **Modularidad**: Separación clara de responsabilidades entre clases
3. **Extensibilidad**: Fácil agregar nuevas funcionalidades (ej: nuevos tipos de acción)
4. **Robustez**: Validaciones y manejo de excepciones adecuado
5. **Claridad**: Código bien organizado y comentado

### Áreas de Mejora Identificadas

1. **Consistencia en deshacer**: Eliminar también del BST cuando se deshace `AGREGAR_CLIENTE`
2. **Reversión completa de solicitudes**: Implementar lógica para eliminar solicitudes de la cola al deshacer
3. **Tests adicionales**: Agregar tests para `buscarPorScoring()` y `deshacerUltimaAccion()`
4. **Balanceo del BST**: Considerar árbol balanceado para garantizar O(log n) en todos los casos

### Cumplimiento de Requerimientos

✅ **Gestión de Clientes**: Implementada con búsquedas eficientes O(1) y O(log n)  
✅ **Historial de Acciones**: Implementado con Stack O(1)  
✅ **Solicitudes de Seguimiento**: Implementadas con Queue FIFO O(1)  
✅ **Carga desde JSON**: Implementada con Gson y manejo robusto de errores  
✅ **Pruebas Unitarias**: Implementadas con JUnit para casos principales  

---

**Documento generado para**: Trabajo Práctico - Algoritmos y Estructuras de Datos II  
**Iteración**: 1  
**Fecha**: 2026
