package models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class Cliente {
    private String nombre;
    private int scoring;

    // En la Iteración 2, esta lista está limitada a máximo 2 elementos (Grafo acotado / Árbol Binario)
    private List<Cliente> siguiendo;

    // Buzón de solicitudes pendientes (Iteración 1 - Cambio de Arquitectura)
    private Queue<String> solicitudesRecibidas;

    public Cliente(String nombre, int scoring) {
        this.nombre = nombre;
        this.scoring = scoring;
        this.siguiendo = new ArrayList<>();
        // Inicializamos la cola personal
        this.solicitudesRecibidas = new LinkedList<>();

        assert repOK() : "Error al crear cliente: Invariante inválido inicial.";
    }

    // --- MÉTODOS PARA LA COLA PERSONAL (BUZÓN) ---

    /**
     * Alguien quiere seguirme. Agrego su nombre a mi cola.
     * Complejidad: O(1)
     */
    public void recibirSolicitud(String nombreSolicitante) {
        // Evitamos duplicados en la cola de pendientes
        if (!solicitudesRecibidas.contains(nombreSolicitante)) {
            solicitudesRecibidas.add(nombreSolicitante);
        }
        assert repOK();
    }

    /**
     * Devuelve la cola para que el servicio la procese.
     */
    public Queue<String> getSolicitudesRecibidas() {
        return solicitudesRecibidas;
    }

    // --- MÉTODOS DE RELACIÓN (CORE ITERACIÓN 2) ---

    public List<Cliente> getSiguiendo() {
        return siguiendo;
    }

    /**
     * Agrega una conexión dirigida hacia otro cliente.
     * AHORA VALIDA LA RESTRICCIÓN DE MÁXIMO 2 SEGUIDOS.
     */
    public void agregarSeguido(Cliente seguido) {
        // Validación de Integridad Referencial
        if (seguido == null) {
            throw new IllegalArgumentException("No se puede seguir a un cliente nulo.");
        }

        // Validación de Auto-bucle
        if (this.equals(seguido)) {
            throw new IllegalStateException("No puedes seguirte a ti mismo.");
        }

        // --- REGLA DE NEGOCIO ITERACIÓN 2: MÁXIMO 2 CONEXIONES ---
        if (siguiendo.size() >= 2) {
            throw new IllegalStateException("El cliente " + this.nombre + " ya sigue al máximo de 2 personas.");
        }

        if (!siguiendo.contains(seguido)) {
            siguiendo.add(seguido);
        }

        assert repOK() : "Error de IREP tras agregar seguido.";
    }

    // --- GETTERS, SETTERS Y STANDARD ---

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        assert repOK();
    }

    public int getScoring() { return scoring; }

    public void setScoring(int scoring) {
        this.scoring = scoring;
        assert repOK();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(nombre, cliente.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return nombre + " (Scoring: " + scoring + ")";
    }

    // --- IREP ACTUALIZADO (INVARIANTE DE REPRESENTACIÓN) ---
    /**
     * Verifica la consistencia interna del objeto Cliente.
     * @return true si el estado es válido, false si está corrupto.
     */
    public boolean repOK() {
        // 1. Validaciones básicas de atributos
        if (nombre == null || nombre.trim().isEmpty()) return false;
        if (scoring < 0 || scoring > 100) return false;
        if (siguiendo == null) return false;
        if (solicitudesRecibidas == null) return false;

        // 2. Validación de Topología (Iteración 2)
        // El cliente no puede seguir a más de 2 personas.
        if (siguiendo.size() > 2) return false;

        // 3. Validación de contenido de la lista
        for (Cliente c : siguiendo) {
            if (c == null) return false; // No puede haber nulos en la lista
            if (c.getNombre().equals(this.nombre)) return false; // No auto-bucles
        }

        return true;
    }
}