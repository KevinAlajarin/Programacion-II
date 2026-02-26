package services;

import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import exceptions.SocialNetworkException;
import models.Accion;
import models.Cliente;
import utils.TDA.ArbolAVL;

import java.util.*;

public class SocialNetwork implements ISocialNetwork {
    // Índice principal: Búsqueda O(1) por nombre
    private Map<String, Cliente> clienteMap;

    // Índice secundario: Búsqueda O(log n) ordenada por scoring (ITERACIÓN 2)
    private ArbolAVL scoringTree;

    // Historial de acciones (Pila LIFO)
    private ActionHistory history;

    public SocialNetwork() {
        this.clienteMap = new HashMap<>();
        this.scoringTree = new ArbolAVL();
        this.history = new ActionHistory();

        assert repOK() : "Error: La red social no se inicializó correctamente.";
    }

    // ==========================================
    // --- REQ 1: GESTIÓN BÁSICA DE CLIENTES ---
    // ==========================================

    @Override
    public void agregarCliente(String nombre, int scoring) throws ClienteYaExisteException {
        if (clienteMap.containsKey(nombre)) {
            throw new ClienteYaExisteException(nombre);
        }

        if (scoring < 0 || scoring > 100) {
            throw new IllegalArgumentException("El scoring debe estar entre 0 y 100.");
        }

        Cliente nuevo = new Cliente(nombre, scoring);

        // 1. Agregar al HashMap (O(1))
        clienteMap.put(nombre, nuevo);

        // 2. Agregar al Árbol AVL (O(log n))
        scoringTree.insertar(scoring, nuevo);

        history.registrarAccion(new Accion(Accion.TipoAccion.AGREGAR_CLIENTE, nombre, null));
        System.out.println("LOG: Cliente agregado -> " + nombre);

        assert repOK() : "IREP Roto: Desincronización tras agregar cliente.";
    }

    @Override
    public Cliente buscarPorNombre(String nombre) {
        return clienteMap.get(nombre);
    }

    @Override
    public List<Cliente> buscarPorScoring(int scoring) {
        return scoringTree.buscar(scoring);
    }

    // ==============================================================
    // --- ITERACIÓN 2: GESTIÓN DE SEGUIMIENTOS (DIRIGIDO, MAX 2) ---
    // ==============================================================

    @Override
    public void enviarSolicitud(String solicitante, String solicitado) throws SocialNetworkException {
        if (solicitante.equals(solicitado)) {
            throw new OperacionInvalidaException("Un usuario no puede enviarse solicitud a sí mismo.");
        }

        Cliente origen = clienteMap.get(solicitante);
        Cliente destino = clienteMap.get(solicitado);

        if (origen == null) throw new ClienteNoEncontradoException(solicitante);
        if (destino == null) throw new ClienteNoEncontradoException(solicitado);

        // Usamos el buzón personal del destino (Descentralización)
        destino.recibirSolicitud(solicitante);

        history.registrarAccion(new Accion(Accion.TipoAccion.SEGUIR_USUARIO, solicitante, solicitado));
        System.out.println("LOG: Solicitud enviada al buzón de " + solicitado);

        assert repOK();
    }

    @Override
    public List<String> procesarSolicitudes() {
        List<String> procesados = new ArrayList<>();
        System.out.println("\n--- Procesando Solicitudes (Buzones por Cliente) ---");

        for (Cliente destino : clienteMap.values()) {
            Queue<String> buzon = destino.getSolicitudesRecibidas();

            while (!buzon.isEmpty()) {
                String nombreSolicitante = buzon.poll(); // Extrae de la cola O(1)
                Cliente solicitante = clienteMap.get(nombreSolicitante);

                if (solicitante != null) {
                    try {
                        // Validación de Iteración 2 (Máx 2) está dentro de agregarSeguido
                        solicitante.agregarSeguido(destino);
                        String log = "✅ Aceptada: " + solicitante.getNombre() + " -> " + destino.getNombre();
                        System.out.println(log);
                        procesados.add(log);
                    } catch (IllegalStateException e) {
                        System.out.println("⚠️ Solicitud rechazada para " + solicitante.getNombre() + ": " + e.getMessage());
                    }
                }
            }
        }
        assert repOK();
        return procesados;
    }

    /**
     * Búsqueda en Anchura (BFS) para encontrar conexiones dirigidas de 4to nivel.
     */
    /**
     * Búsqueda en Anchura (BFS) Dinámica para encontrar conexiones dirigidas en cualquier nivel N.
     */
    @Override
    public void analizarNivel(String nombreOrigen, int nivelObjetivo) {
        Cliente origen = clienteMap.get(nombreOrigen);
        if (origen == null) {
            System.out.println("Cliente no encontrado.");
            return;
        }

        if (nivelObjetivo < 0) {
            System.out.println("El nivel no puede ser negativo.");
            return;
        }

        System.out.println("\n--- ANÁLISIS DE RED (SEGUIDORES): NIVEL " + nivelObjetivo + " desde " + nombreOrigen + " ---");

        Queue<Cliente> cola = new LinkedList<>();
        Map<Cliente, Integer> niveles = new HashMap<>();
        Set<Cliente> visitados = new HashSet<>();

        cola.add(origen);
        niveles.put(origen, 0);
        visitados.add(origen);

        List<Cliente> nivelEncontrado = new ArrayList<>();

        while (!cola.isEmpty()) {
            Cliente actual = cola.poll();
            int nivelActual = niveles.get(actual);

            // Si llegamos al nivel que el usuario pidió, lo guardamos
            if (nivelActual == nivelObjetivo) {
                nivelEncontrado.add(actual);
                continue; // Detener profundidad en este nivel
            }

            for (Cliente vecino : actual.getSiguiendo()) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    niveles.put(vecino, nivelActual + 1);
                    cola.add(vecino);
                }
            }
        }

        if (nivelEncontrado.isEmpty()) {
            System.out.println("No hay nadie en el nivel " + nivelObjetivo + ".");
        } else {
            System.out.println("Clientes en el nivel " + nivelObjetivo + ":");
            for (Cliente c : nivelEncontrado) {
                System.out.println(" -> " + c.getNombre() + " (Scoring: " + c.getScoring() + ")");
            }
        }
    }

    // ==============================================================
    // --- ITERACIÓN 3: GRAFOS GENERALES (AMISTADES Y DISTANCIAS) ---
    // ==============================================================

    @Override
    public void crearAmistad(String nombre1, String nombre2) throws ClienteNoEncontradoException {
        if (nombre1.equals(nombre2)) {
            throw new IllegalArgumentException("Un usuario no puede ser amigo de sí mismo.");
        }

        Cliente c1 = clienteMap.get(nombre1);
        Cliente c2 = clienteMap.get(nombre2);

        if (c1 == null) throw new ClienteNoEncontradoException(nombre1);
        if (c2 == null) throw new ClienteNoEncontradoException(nombre2);

        // La amistad es estrictamente bidireccional (Grafo no dirigido O(1))
        c1.agregarAmigo(c2);
        c2.agregarAmigo(c1);

        System.out.println("🤝 Nueva amistad creada: " + nombre1 + " y " + nombre2);
        assert repOK();
    }

    @Override
    public int calcularDistancia(String origen, String destino) throws ClienteNoEncontradoException {
        if (origen.equals(destino)) return 0; // Distancia a sí mismo es 0

        Cliente nodoOrigen = clienteMap.get(origen);
        Cliente nodoDestino = clienteMap.get(destino);

        if (nodoOrigen == null) throw new ClienteNoEncontradoException(origen);
        if (nodoDestino == null) throw new ClienteNoEncontradoException(destino);

        // BFS Estricto para el camino más corto
        Queue<Cliente> cola = new LinkedList<>();
        Map<Cliente, Integer> distancias = new HashMap<>();

        cola.add(nodoOrigen);
        distancias.put(nodoOrigen, 0);

        while (!cola.isEmpty()) {
            Cliente actual = cola.poll();
            int distanciaActual = distancias.get(actual);

            if (actual.equals(nodoDestino)) {
                return distanciaActual; // Se encontró la distancia mínima
            }

            // Exploramos usando el Set de amigos generales (O(1) acceso a vecinos)
            for (Cliente vecino : actual.getAmigos()) {
                if (!distancias.containsKey(vecino)) {
                    distancias.put(vecino, distanciaActual + 1);
                    cola.add(vecino);
                }
            }
        }
        return -1; // No hay ruta que los conecte
    }

    // ==========================================
    // --- REQ 2: HISTORIAL, DESHACER Y MEMENTO ---
    // ==========================================

    @Override
    public void deshacerUltimaAccion() {
        if (history.isEmpty()) {
            System.out.println("Info: El historial está vacío.");
            return;
        }

        Accion ultima = history.deshacer();
        System.out.println("Deshaciendo: " + ultima);

        switch (ultima.getTipo()) {
            case AGREGAR_CLIENTE:
                Cliente c = clienteMap.get(ultima.getSujeto());
                if (c != null) {
                    scoringTree.eliminar(c.getScoring(), c);
                    clienteMap.remove(ultima.getSujeto());
                    System.out.println("LOG: Cliente eliminado por deshacer.");
                }
                break;

            case SEGUIR_USUARIO:
                String sujeto = ultima.getSujeto();
                String objeto = ultima.getObjeto();
                Cliente sol = clienteMap.get(sujeto);
                Cliente obj = clienteMap.get(objeto);

                if (sol != null && obj != null) {
                    boolean borrado = sol.getSiguiendo().remove(obj);
                    if(borrado) System.out.println("LOG: Se dejó de seguir a " + objeto);
                }
                break;

            case ELIMINAR_CLIENTE:
                // Memento (Resurrección)
                String nombreResucitar = ultima.getSujeto();
                BackupData datos = (BackupData) ultima.getRespaldo();

                if (datos != null) {
                    try {
                        agregarCliente(nombreResucitar, datos.scoring);
                        history.deshacer(); // Borrar evento AGREGAR generado arriba

                        Cliente resucitado = clienteMap.get(nombreResucitar);

                        // Restaurar Salientes (Seguidores Iteración 2)
                        for (Cliente aSeguir : datos.aQuienSeguia) {
                            if (clienteMap.containsKey(aSeguir.getNombre())) {
                                resucitado.agregarSeguido(aSeguir);
                            }
                        }

                        // Restaurar Entrantes (Seguidores Iteración 2)
                        for (String nombreSeguidor : datos.quienesLoSeguian) {
                            Cliente seguidor = clienteMap.get(nombreSeguidor);
                            if (seguidor != null) {
                                seguidor.agregarSeguido(resucitado);
                            }
                        }

                        // Restaurar Amistades (Iteración 3)
                        for (String nombreAmigo : datos.amigos) {
                            Cliente amigo = clienteMap.get(nombreAmigo);
                            if (amigo != null) {
                                resucitado.agregarAmigo(amigo);
                                amigo.agregarAmigo(resucitado);
                            }
                        }
                        System.out.println("✨ ÉXITO: " + nombreResucitar + " ha sido restaurado con todas sus conexiones.");

                    } catch (Exception e) {
                        System.out.println("⚠️ Error al intentar restaurar: " + e.getMessage());
                    }
                }
                break;
        }
        assert repOK();
    }

    @Override
    public void eliminarClienteTotalmente(String nombre) throws ClienteNoEncontradoException {
        Cliente aBorrar = clienteMap.get(nombre);
        if (aBorrar == null) throw new ClienteNoEncontradoException(nombre);

        // 1. Memento (Backup Completo)
        List<String> seguidores = new ArrayList<>();
        for (Cliente otro : clienteMap.values()) {
            if (otro.getSiguiendo().contains(aBorrar)) {
                seguidores.add(otro.getNombre());
            }
        }
        List<String> nombresAmigos = new ArrayList<>();
        for (Cliente amigo : aBorrar.getAmigos()) {
            nombresAmigos.add(amigo.getNombre());
        }

        BackupData backup = new BackupData(aBorrar.getScoring(), aBorrar.getSiguiendo(), seguidores, nombresAmigos);

        // 2. BORRADO EN CASCADA

        // A. Quitar referencias de "Seguidores" (Iteración 2)
        for (Cliente otro : clienteMap.values()) {
            otro.getSiguiendo().remove(aBorrar);
        }

        // B. Quitar referencias de "Amistades" (Iteración 3 - Bidireccional)
        for (Cliente amigo : aBorrar.getAmigos()) {
            amigo.eliminarAmigo(aBorrar);
        }

        // C. Quitar del Árbol AVL
        scoringTree.eliminar(aBorrar.getScoring(), aBorrar);

        // D. Quitar del Mapa Principal
        clienteMap.remove(nombre);

        history.registrarAccion(new Accion(Accion.TipoAccion.ELIMINAR_CLIENTE, nombre, backup));
        System.out.println("🗑️ Cliente '" + nombre + "' eliminado de todas las redes.");
        assert repOK();
    }

    // ==========================================
    // --- CONSULTAS Y REPORTES DE CONSOLA ---
    // ==========================================

    @Override
    public void mostrarConexionesDe(String nombre) throws ClienteNoEncontradoException {
        Cliente c = clienteMap.get(nombre);
        if (c == null) throw new ClienteNoEncontradoException(nombre);

        System.out.println("\n=== PERFIL DE: " + c.getNombre().toUpperCase() + " ===");
        System.out.println("📊 Scoring actual: " + c.getScoring());

        // AMISTADES GENERALES (Iteración 3)
        System.out.println("\n--- 🤝 AMISTADES GENERALES (No Dirigidas) ---");
        Set<Cliente> amigos = c.getAmigos();
        if (amigos.isEmpty()) {
            System.out.println("   No tiene amigos agregados.");
        } else {
            System.out.println("   Amigos (" + amigos.size() + "):");
            for (Cliente amigo : amigos) {
                System.out.println("   - " + amigo.getNombre() + " (Score: " + amigo.getScoring() + ")");
            }
        }

        // SEGUIDORES (Iteración 2)
        System.out.println("\n--- 📱 RED DE SEGUIMIENTO (Dirigida) ---");

        List<Cliente> sigueA = c.getSiguiendo();
        if (sigueA.isEmpty()) {
            System.out.println("➡  No sigue a nadie.");
        } else {
            System.out.println("➡  Sigue a (" + sigueA.size() + "/2):");
            for (Cliente seguido : sigueA) {
                System.out.println("   - " + seguido.getNombre());
            }
        }

        List<String> seguidores = new ArrayList<>();
        for (Cliente otro : clienteMap.values()) {
            if (otro.getSiguiendo().contains(c)) seguidores.add(otro.getNombre());
        }

        if (seguidores.isEmpty()) {
            System.out.println("⬅  No tiene seguidores.");
        } else {
            System.out.println("⬅  Seguido por (" + seguidores.size() + "):");
            for (String seguidor : seguidores) System.out.println("   - " + seguidor);
        }
        System.out.println("==========================================");
    }

    @Override
    public void mostrarEstadoGeneral() {
        if (clienteMap.isEmpty()) {
            System.out.println("❌ No hay clientes en el sistema.");
            return;
        }

        System.out.println("\n--- ESTADO GENERAL DE LA RED SOCIAL ---");
        for (Cliente c : clienteMap.values()) {
            System.out.println("👤 " + c.getNombre() + " (Scoring: " + c.getScoring() + ")");

            if (!c.getAmigos().isEmpty()) {
                System.out.print("   🤝 Amigos: ");
                for (Cliente amigo : c.getAmigos()) System.out.print(amigo.getNombre() + ", ");
                System.out.println();
            }

            if (!c.getSolicitudesRecibidas().isEmpty()) {
                System.out.println("   📩 Buzón: " + c.getSolicitudesRecibidas().size() + " pendientes");
            }

            if (c.getSiguiendo().isEmpty()) {
                System.out.println("   ➡ Sigue a: (Nadie)");
            } else {
                System.out.print("   ➡ Sigue a: ");
                for (Cliente seguido : c.getSiguiendo()) System.out.print(seguido.getNombre() + ", ");
                System.out.println();
            }
            System.out.println("---------------------------------------");
        }
    }

    @Override
    public void verHistorial() {
        history.mostrarHistorialCompleto();
    }

    // ==========================================
    // --- ESTRUCTURAS INTERNAS Y VALIDACIÓN ---
    // ==========================================

    private class BackupData {
        int scoring;
        List<Cliente> aQuienSeguia;
        List<String> quienesLoSeguian;
        List<String> amigos; // Agregado para Memento Iteración 3

        public BackupData(int s, List<Cliente> siguiendo, List<String> seguidores, List<String> amigos) {
            this.scoring = s;
            this.aQuienSeguia = new ArrayList<>(siguiendo);
            this.quienesLoSeguian = new ArrayList<>(seguidores);
            this.amigos = new ArrayList<>(amigos);
        }
    }

    public boolean repOK() {
        if (clienteMap == null || scoringTree == null || history == null) return false;

        for (Cliente c : clienteMap.values()) {
            if (!c.repOK()) return false;
        }
        return true;
    }
}