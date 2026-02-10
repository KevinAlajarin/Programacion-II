package services;

import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import models.Accion;
import models.Cliente;
import models.Solicitud;

import java.util.*;

public class SocialNetwork {
    private Map<String, Cliente> clienteMap;
    // Mapea Puntaje -> Lista de personas con ese puntaje.
    private Map<Integer, List<Cliente>> scoringMap;

    private ActionHistory history;
    private Queue<Solicitud> requestQueue;

    public SocialNetwork() {
        this.clienteMap = new HashMap<>();
        this.scoringMap = new HashMap<>();
        this.history = new ActionHistory();
        this.requestQueue = new LinkedList<>();
    }

    // --- REQ 1: Gestion de Clientes ---

    public void agregarCliente(String nombre, int scoring) throws ClienteYaExisteException {
        if (clienteMap.containsKey(nombre)) {
            throw new ClienteYaExisteException(nombre);
        }

        if (scoring < 0 || scoring > 100) {
            throw new IllegalArgumentException("El scoring debe estar entre 0 y 100.");
        }

        Cliente nuevo = new Cliente(nombre, scoring);

        // 1. Agregar al indice por Nombre
        clienteMap.put(nombre, nuevo);

        // 2. Agregar al indice por Scoring
        scoringMap.putIfAbsent(scoring, new ArrayList<>());
        scoringMap.get(scoring).add(nuevo);

        history.registrarAccion(new Accion(Accion.TipoAccion.AGREGAR_CLIENTE, nombre, null));
        System.out.println("LOG: Cliente agregado -> " + nombre);
    }

    public Cliente buscarPorNombre(String nombre) {
        return clienteMap.get(nombre);
    }

    /**
     * Busca TODOS los clientes con un scoring especifico.
     * @param scoring El puntaje a buscar.
     * @return Una lista de clientes (vacia si nadie tiene ese puntaje).
     */
    public List<Cliente> buscarPorScoring(int scoring) {
        return scoringMap.getOrDefault(scoring, new ArrayList<>());
    }

    // --- REQ 3: Gestion de Solicitudes ---

    public void enviarSolicitud(String solicitante, String solicitado)
            throws ClienteNoEncontradoException, OperacionInvalidaException {

        if (solicitante.equals(solicitado)) {
            throw new OperacionInvalidaException("Un usuario no puede enviarse solicitud a sí mismo.");
        }

        if (!clienteMap.containsKey(solicitante)) {
            throw new ClienteNoEncontradoException(solicitante);
        }
        if (!clienteMap.containsKey(solicitado)) {
            throw new ClienteNoEncontradoException(solicitado);
        }

        Solicitud sol = new Solicitud(solicitante, solicitado);
        requestQueue.add(sol);

        history.registrarAccion(new Accion(Accion.TipoAccion.SEGUIR_USUARIO, solicitante, solicitado));
        System.out.println("LOG: Solicitud encolada -> " + solicitante + " a " + solicitado);
    }

    public List<String> procesarSolicitudes() {
        List<String> procesados = new ArrayList<>();
        System.out.println("\n--- Procesando Solicitudes (FIFO) ---");

        while (!requestQueue.isEmpty()) {
            Solicitud sol = requestQueue.poll();

            Cliente origen = clienteMap.get(sol.getSolicitante());
            Cliente destino = clienteMap.get(sol.getSolicitado());

            if (origen != null && destino != null) {
                // Validacion Iteracion 2: Máximo 2 seguidos
                if (origen.getSiguiendo().size() >= 2) {
                    System.out.println("⚠️ Solicitud rechazada: " + origen.getNombre() +
                            " ya sigue al máximo de 2 personas.");
                } else {
                    origen.agregarSeguido(destino);
                    String log = "✅ Aceptada: " + origen.getNombre() + " ahora sigue a " + destino.getNombre();
                    System.out.println(log);
                    procesados.add(log);
                }
            }
        }
        return procesados;
    }

    // --- REQ 2: Historial y Deshacer (Con lógica avanzada de restauración) ---

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
                    // Remover del indice de scoring (Lista)
                    List<Cliente> lista = scoringMap.get(c.getScoring());
                    if (lista != null) {
                        lista.remove(c);
                        if (lista.isEmpty()) {
                            scoringMap.remove(c.getScoring());
                        }
                    }
                    // Remover del indice de nombre
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
                // --- LÓGICA DE RESURRECCIÓN (Restaurar Backup) ---
                String nombreResucitar = ultima.getSujeto();
                BackupData datos = (BackupData) ultima.getRespaldo();

                if (datos != null) {
                    try {
                        // 1. Restaurar el Nodo (Cliente)
                        // Usamos agregarCliente para que lo meta en los mapas,
                        // pero debemos eliminar la accion de "AGREGAR" que esto genera en el historial
                        // para no ensuciar la pila de deshacer.
                        agregarCliente(nombreResucitar, datos.scoring);
                        history.deshacer(); // Borramos el registro "AGREGAR" interno

                        Cliente resucitado = clienteMap.get(nombreResucitar);

                        // 2. Restaurar Aristas Salientes (A quién seguía David)
                        for (Cliente aSeguir : datos.aQuienSeguia) {
                            if (clienteMap.containsKey(aSeguir.getNombre())) {
                                resucitado.agregarSeguido(aSeguir);
                            }
                        }

                        // 3. Restaurar Aristas Entrantes (Quiénes seguían a David)
                        for (String nombreSeguidor : datos.quienesLoSeguian) {
                            Cliente seguidor = clienteMap.get(nombreSeguidor);
                            if (seguidor != null) {
                                seguidor.agregarSeguido(resucitado);
                            }
                        }
                        System.out.println("✨ ÉXITO: " + nombreResucitar + " ha sido restaurado con todas sus conexiones.");

                    } catch (Exception e) {
                        System.out.println("⚠️ Error al intentar restaurar: " + e.getMessage());
                    }
                } else {
                    System.out.println("⚠️ Error: No se encontró respaldo para restaurar.");
                }
                break;
        }
    }

    // --- NUEVOS MÉTODOS PARA REPORTE Y ADMINISTRACIÓN ---

    public void mostrarEstadoGeneral() {
        if (clienteMap.isEmpty()) {
            System.out.println("❌ No hay clientes en el sistema.");
            return;
        }

        System.out.println("\n--- ESTADO GENERAL DE LA RED SOCIAL ---");
        for (Cliente c : clienteMap.values()) {
            System.out.println("👤 " + c.getNombre() + " (Scoring: " + c.getScoring() + ")");

            // 1. Mostrar a quién sigue
            if (c.getSiguiendo().isEmpty()) {
                System.out.println("   ➡ Sigue a: (Nadie)");
            } else {
                System.out.print("   ➡ Sigue a: ");
                for (Cliente seguido : c.getSiguiendo()) {
                    System.out.print(seguido.getNombre() + ", ");
                }
                System.out.println();
            }

            // 2. Mostrar quién le sigue (Calculado)
            List<String> seguidores = new ArrayList<>();
            for (Cliente posibleSeguidor : clienteMap.values()) {
                if (posibleSeguidor.getSiguiendo().contains(c)) {
                    seguidores.add(posibleSeguidor.getNombre());
                }
            }

            if (seguidores.isEmpty()) {
                System.out.println("   ⬅ Seguido por: (Nadie)");
            } else {
                System.out.println("   ⬅ Seguido por: " + seguidores);
            }
            System.out.println("---------------------------------------");
        }
    }

    /**
     * Elimina un cliente guardando un respaldo completo para poder deshacer.
     */
    public void eliminarClienteTotalmente(String nombre) throws ClienteNoEncontradoException {
        Cliente aBorrar = clienteMap.get(nombre);
        if (aBorrar == null) {
            throw new ClienteNoEncontradoException(nombre);
        }

        // --- PASO 1: PREPARAR EL RESPALDO (MEMENTO) ---
        // Calculamos quiénes lo siguen antes de borrarlo
        List<String> seguidores = new ArrayList<>();
        for (Cliente otro : clienteMap.values()) {
            if (otro.getSiguiendo().contains(aBorrar)) {
                seguidores.add(otro.getNombre());
            }
        }

        // Creamos el objeto de respaldo con toda la info
        BackupData backup = new BackupData(
                aBorrar.getScoring(),
                aBorrar.getSiguiendo(),
                seguidores
        );

        // --- PASO 2: BORRADO REAL ---
        // Quitarlo de las listas de otros
        for (Cliente otro : clienteMap.values()) {
            otro.getSiguiendo().remove(aBorrar);
        }

        // Quitarlo del Mapa de Scoring
        List<Cliente> listaScoring = scoringMap.get(aBorrar.getScoring());
        if (listaScoring != null) {
            listaScoring.remove(aBorrar);
            if (listaScoring.isEmpty()) {
                scoringMap.remove(aBorrar.getScoring());
            }
        }

        // Quitarlo del Mapa Principal
        clienteMap.remove(nombre);

        // --- PASO 3: REGISTRAR EN HISTORIAL CON RESPALDO ---
        history.registrarAccion(new Accion(Accion.TipoAccion.ELIMINAR_CLIENTE, nombre, backup));

        System.out.println("🗑️ Cliente '" + nombre + "' eliminado. (Respaldo guardado para deshacer)");
    }

    // --- CLASE INTERNA PARA EL RESPALDO (DTO Privado) ---
    private class BackupData {
        int scoring;
        List<Cliente> aQuienSeguia;    // Copia de a quiénes seguía
        List<String> quienesLoSeguian; // Copia de quiénes lo seguían a él

        public BackupData(int s, List<Cliente> siguiendo, List<String> seguidores) {
            this.scoring = s;
            // Hacemos copias superficiales de las listas (New ArrayList) para proteger la referencia
            this.aQuienSeguia = new ArrayList<>(siguiendo);
            this.quienesLoSeguian = new ArrayList<>(seguidores);
        }
    }

    public void verHistorial() {
        history.mostrarHistorialCompleto();
    }
}