package services;

import exceptions.ClienteNoEncontradoException;
import exceptions.ClienteYaExisteException;
import exceptions.OperacionInvalidaException;
import models.Accion;
import models.Cliente;
import models.Solicitud;
import structures.ScoringBST;

import java.util.*;

public class SocialNetwork {
    private Map<String, Cliente> clienteMap;
    private ScoringBST scoringIndex;
    private ActionHistory history;
    private Queue<Solicitud> requestQueue;

    public SocialNetwork() {
        this.clienteMap = new HashMap<>();
        this.scoringIndex = new ScoringBST();
        this.history = new ActionHistory();
        this.requestQueue = new LinkedList<>();
    }

    // --- REQ 1: Gestión de Clientes ---

    // CAMBIO: Ahora declara que puede lanzar una excepción checked
    public void agregarCliente(String nombre, int scoring) throws ClienteYaExisteException {
        if (clienteMap.containsKey(nombre)) {
            throw new ClienteYaExisteException(nombre);
        }

        // Validación extra: Scoring no negativo (Defensive Programming)
        if (scoring < 0 || scoring > 100) {
            // Usamos IllegalArgumentException porque es un error de argumento, no de negocio
            throw new IllegalArgumentException("El scoring debe estar entre 0 y 100.");
        }

        Cliente nuevo = new Cliente(nombre, scoring);

        clienteMap.put(nombre, nuevo);
        scoringIndex.insert(nuevo);

        history.registrarAccion(new Accion(Accion.TipoAccion.AGREGAR_CLIENTE, nombre, null));
        System.out.println("LOG: Cliente agregado -> " + nombre);
    }

    public Cliente buscarPorNombre(String nombre) {
        return clienteMap.get(nombre);
    }

    public Cliente buscarPorScoring(int scoring, String nombre) {
        return scoringIndex.search(scoring, nombre);
    }

    // --- REQ 3: Gestión de Solicitudes ---

    public void enviarSolicitud(String solicitante, String solicitado)
            throws ClienteNoEncontradoException, OperacionInvalidaException {

        // Validación 1: No seguirse a sí mismo
        if (solicitante.equals(solicitado)) {
            throw new OperacionInvalidaException("Un usuario no puede enviarse solicitud a sí mismo.");
        }

        // Validación 2: Existencia
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
        System.out.println("\n--- Procesando Solicitudes ---");

        while (!requestQueue.isEmpty()) {
            Solicitud sol = requestQueue.poll();
            String log = sol.getSolicitante() + " -> " + sol.getSolicitado();
            System.out.println("Procesando: " + log);
            procesados.add(log);
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
                clienteMap.remove(ultima.getSujeto());
                System.out.println("LOG: Cliente eliminado por deshacer.");
                break;
            case SEGUIR_USUARIO:
                // En una implementación completa, aquí buscaríamos y removeríamos la solicitud de la cola si aun no se procesó
                System.out.println("LOG: Acción de seguimiento revertida (simulado).");
                break;
        }
    }
}