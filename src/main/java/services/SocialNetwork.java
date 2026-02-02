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
                // Logica REAL de deshacer seguir (Iteracion 2)
                String sujeto = ultima.getSujeto();
                String objeto = ultima.getObjeto();
                Cliente sol = clienteMap.get(sujeto);
                Cliente obj = clienteMap.get(objeto);

                if (sol != null && obj != null) {
                    boolean borrado = sol.getSiguiendo().remove(obj);
                    if(borrado) System.out.println("LOG: Se dejó de seguir a " + objeto);
                }
                break;
        }
    }
}