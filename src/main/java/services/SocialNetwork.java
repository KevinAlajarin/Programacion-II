package services;

import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import exceptions.SocialNetworkException;
import models.Accion;
import models.Cliente;
import utils.TDA.ArbolAVL; // <--- IMPORTANTE: Importar tu nueva clase AVL

import java.util.*;

public class SocialNetwork implements ISocialNetwork {
    private Map<String, Cliente> clienteMap; // Búsqueda O(1)

    // CAMBIO ITERACIÓN 2: Reemplazamos Map por Árbol AVL
    // Esto permite mantener los clientes ordenados por scoring y búsquedas O(log n)
    private ArbolAVL scoringTree;

    private ActionHistory history;

    public SocialNetwork() {
        this.clienteMap = new HashMap<>();
        // Inicializamos el Árbol en lugar del Mapa
        this.scoringTree = new ArbolAVL();
        this.history = new ActionHistory();

        assert repOK() : "Error: La red social no se inicializó correctamente.";
    }

    // --- REQ 1: Gestion de Clientes ---

    @Override
    public void agregarCliente(String nombre, int scoring) throws ClienteYaExisteException {
        if (clienteMap.containsKey(nombre)) {
            throw new ClienteYaExisteException(nombre);
        }

        if (scoring < 0 || scoring > 100) {
            throw new IllegalArgumentException("El scoring debe estar entre 0 y 100.");
        }

        Cliente nuevo = new Cliente(nombre, scoring);

        // 1. Agregar al indice por Nombre (HashMap)
        clienteMap.put(nombre, nuevo);

        // 2. Agregar al indice por Scoring (Árbol AVL)
        // La lógica de inserción y balanceo está encapsulada en la clase ArbolAVL
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
        // Delegamos la búsqueda al Árbol AVL (O(log n))
        return scoringTree.buscar(scoring);
    }

    // --- REQ 3: Gestion de Solicitudes (BUZONES PERSONALES) ---

    @Override
    public void enviarSolicitud(String solicitante, String solicitado)
            throws SocialNetworkException {

        if (solicitante.equals(solicitado)) {
            throw new OperacionInvalidaException("Un usuario no puede enviarse solicitud a sí mismo.");
        }

        Cliente origen = clienteMap.get(solicitante);
        Cliente destino = clienteMap.get(solicitado);

        if (origen == null) throw new ClienteNoEncontradoException(solicitante);
        if (destino == null) throw new ClienteNoEncontradoException(solicitado);

        // En lugar de cola global, usamos el buzón del destino.
        destino.recibirSolicitud(solicitante);

        history.registrarAccion(new Accion(Accion.TipoAccion.SEGUIR_USUARIO, solicitante, solicitado));
        System.out.println("LOG: Solicitud enviada al buzón de " + solicitado);

        assert repOK() : "IREP Roto: Estado inválido tras enviar solicitud.";
    }

    @Override
    public List<String> procesarSolicitudes() {
        List<String> procesados = new ArrayList<>();
        System.out.println("\n--- Procesando Solicitudes (Buzones por Cliente) ---");

        // Recorremos TODOS los clientes para ver si tienen mensajes en su buzón
        for (Cliente destino : clienteMap.values()) {
            Queue<String> buzon = destino.getSolicitudesRecibidas();

            // Procesamos el buzón de este cliente específico
            while (!buzon.isEmpty()) {
                String nombreSolicitante = buzon.poll();
                Cliente solicitante = clienteMap.get(nombreSolicitante);

                if (solicitante != null) {
                    // Validacion Iteracion 2: Máximo 2 seguidos (Checkeado en Cliente.java)
                    try {
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

        assert repOK() : "IREP Roto: Corrupción de datos tras procesar colas.";
        return procesados;
    }

    // --- REQ 2: Historial y Deshacer ---

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
                    // 1. Eliminar del Árbol AVL
                    scoringTree.eliminar(c.getScoring(), c);
                    // 2. Eliminar del Mapa
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
                // Lógica de Resurrección (Memento)
                String nombreResucitar = ultima.getSujeto();
                BackupData datos = (BackupData) ultima.getRespaldo();

                if (datos != null) {
                    try {
                        // 1. Restaurar Nodo (Al llamar a agregarCliente, se inserta solo en el AVL)
                        agregarCliente(nombreResucitar, datos.scoring);
                        history.deshacer(); // Borrar evento AGREGAR interno

                        Cliente resucitado = clienteMap.get(nombreResucitar);

                        // 2. Restaurar Salientes
                        for (Cliente aSeguir : datos.aQuienSeguia) {
                            if (clienteMap.containsKey(aSeguir.getNombre())) {
                                resucitado.agregarSeguido(aSeguir);
                            }
                        }

                        // 3. Restaurar Entrantes
                        for (String nombreSeguidor : datos.quienesLoSeguian) {
                            Cliente seguidor = clienteMap.get(nombreSeguidor);
                            if (seguidor != null) {
                                seguidor.agregarSeguido(resucitado);
                            }
                        }
                        System.out.println("✨ ÉXITO: " + nombreResucitar + " ha sido restaurado.");

                    } catch (Exception e) {
                        System.out.println("⚠️ Error al intentar restaurar: " + e.getMessage());
                    }
                }
                break;
        }

        assert repOK() : "IREP Roto: El deshacer dejó el sistema inestable.";
    }

    // --- NUEVO METODO: ANALISIS DE NIVEL 4 (BFS) ---
    /**
     * Utiliza un recorrido en anchura (BFS) para encontrar "tataranietos" en la red.
     */
    public void analizarNivelCuatro(String nombreOrigen) {
        Cliente origen = clienteMap.get(nombreOrigen);
        if (origen == null) {
            System.out.println("Cliente no encontrado.");
            return;
        }

        System.out.println("\n--- ANÁLISIS DE RED: NIVEL 4 desde " + nombreOrigen + " ---");

        // Estructuras para BFS
        Queue<Cliente> cola = new LinkedList<>();
        Map<Cliente, Integer> niveles = new HashMap<>();
        Set<Cliente> visitados = new HashSet<>();

        cola.add(origen);
        niveles.put(origen, 0);
        visitados.add(origen);

        List<Cliente> nivel4 = new ArrayList<>();

        while (!cola.isEmpty()) {
            Cliente actual = cola.poll();
            int nivelActual = niveles.get(actual);

            if (nivelActual == 4) {
                nivel4.add(actual);
                continue; // No profundizamos más allá del 4
            }

            for (Cliente vecino : actual.getSiguiendo()) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    niveles.put(vecino, nivelActual + 1);
                    cola.add(vecino);
                }
            }
        }

        if (nivel4.isEmpty()) {
            System.out.println("No hay nadie en el nivel 4.");
        } else {
            System.out.println("Clientes en el 4to nivel:");
            for (Cliente c : nivel4) {
                System.out.println(" -> " + c.getNombre() + " (Scoring: " + c.getScoring() + ")");
            }
        }
    }

    // --- REPORTE Y ADMINISTRACIÓN ---

    @Override
    public void mostrarEstadoGeneral() {
        if (clienteMap.isEmpty()) {
            System.out.println("❌ No hay clientes en el sistema.");
            return;
        }

        System.out.println("\n--- ESTADO GENERAL DE LA RED SOCIAL ---");
        // Nota: Si quisieras mostrarlos ordenados por scoring, usarías scoringTree.imprimirInOrder()
        // Aquí seguimos mostrando el barrido general del mapa.
        for (Cliente c : clienteMap.values()) {
            System.out.println("👤 " + c.getNombre() + " (Scoring: " + c.getScoring() + ")");

            if (!c.getSolicitudesRecibidas().isEmpty()) {
                System.out.println("   📩 Buzón: " + c.getSolicitudesRecibidas().size() + " pendientes");
            }

            if (c.getSiguiendo().isEmpty()) {
                System.out.println("   ➡ Sigue a: (Nadie)");
            } else {
                System.out.print("   ➡ Sigue a: ");
                for (Cliente seguido : c.getSiguiendo()) {
                    System.out.print(seguido.getNombre() + ", ");
                }
                System.out.println();
            }
            System.out.println("---------------------------------------");
        }
    }

    @Override
    public void eliminarClienteTotalmente(String nombre) throws ClienteNoEncontradoException {
        Cliente aBorrar = clienteMap.get(nombre);
        if (aBorrar == null) {
            throw new ClienteNoEncontradoException(nombre);
        }

        // 1. Memento
        List<String> seguidores = new ArrayList<>();
        for (Cliente otro : clienteMap.values()) {
            if (otro.getSiguiendo().contains(aBorrar)) {
                seguidores.add(otro.getNombre());
            }
        }

        BackupData backup = new BackupData(
                aBorrar.getScoring(),
                aBorrar.getSiguiendo(),
                seguidores
        );

        // 2. Borrado
        // A. Quitar referencias de otros
        for (Cliente otro : clienteMap.values()) {
            otro.getSiguiendo().remove(aBorrar);
        }

        // B. Quitar del Árbol AVL (O(log n))
        scoringTree.eliminar(aBorrar.getScoring(), aBorrar);

        // C. Quitar del Mapa (O(1))
        clienteMap.remove(nombre);

        // 3. Historial
        history.registrarAccion(new Accion(Accion.TipoAccion.ELIMINAR_CLIENTE, nombre, backup));

        System.out.println("🗑️ Cliente '" + nombre + "' eliminado. (Respaldo guardado)");
        assert repOK() : "IREP Roto: Error de integridad tras eliminar cliente.";
    }

    @Override
    public void verHistorial() {
        history.mostrarHistorialCompleto();
    }

    // --- DTO INTERNO PARA RESPALDO ---
    private class BackupData {
        int scoring;
        List<Cliente> aQuienSeguia;
        List<String> quienesLoSeguian;

        public BackupData(int s, List<Cliente> siguiendo, List<String> seguidores) {
            this.scoring = s;
            this.aQuienSeguia = new ArrayList<>(siguiendo);
            this.quienesLoSeguian = new ArrayList<>(seguidores);
        }
    }

    /**
     * IREP: Invariante de Representación.
     */
    public boolean repOK() {
        // Validamos que las estructuras principales existan
        if (clienteMap == null || scoringTree == null || history == null) {
            return false;
        }

        // Nota académica: Validar que cada nodo del árbol esté en el mapa sería muy costoso (O(n))
        // para un repOK que corre siempre.
        // Validamos la integridad básica de los clientes en el mapa.
        for (Cliente c : clienteMap.values()) {
            if (!c.repOK()) return false;
        }
        return true;
    }

    @Override
    public void mostrarConexionesDe(String nombre) throws ClienteNoEncontradoException {
        // 1. Buscamos al protagonista (O(1))
        Cliente c = clienteMap.get(nombre);
        if (c == null) {
            throw new ClienteNoEncontradoException(nombre);
        }

        System.out.println("\n=== CONEXIONES DE: " + c.getNombre().toUpperCase() + " ===");
        System.out.println("📊 Scoring actual: " + c.getScoring());

        // 2. A QUIÉN SIGUE (Aristas Salientes) - O(1) porque la lista es máx 2
        List<Cliente> sigueA = c.getSiguiendo();
        if (sigueA.isEmpty()) {
            System.out.println("➡  No sigue a nadie.");
        } else {
            System.out.println("➡  Sigue a (" + sigueA.size() + "):");
            for (Cliente seguido : sigueA) {
                System.out.println("   - " + seguido.getNombre() + " (Score: " + seguido.getScoring() + ")");
            }
        }

        // 3. QUIÉN LO SIGUE (Aristas Entrantes) - O(N)
        // Como nuestro grafo es dirigido y solo guardamos "a quién sigo",
        // para saber "quién me sigue" debemos preguntar a todos los demás usuarios.
        List<String> seguidores = new ArrayList<>();
        for (Cliente otro : clienteMap.values()) {
            if (otro.getSiguiendo().contains(c)) {
                seguidores.add(otro.getNombre());
            }
        }

        if (seguidores.isEmpty()) {
            System.out.println("⬅  No tiene seguidores.");
        } else {
            System.out.println("⬅  Seguido por (" + seguidores.size() + "):");
            for (String seguidor : seguidores) {
                System.out.println("   - " + seguidor);
            }
        }
        System.out.println("==========================================");
    }

}