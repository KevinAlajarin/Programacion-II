package models;

import java.util.ArrayList;
import java.util.HashSet;    // Importar HashSet
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;        // Importar Set

public class Cliente {
    private String nombre;
    private int scoring;

    // Grafo dirigido acotado (Máximo 2)
    private List<Cliente> siguiendo;

    // Buzón de solicitudes pendientes
    private Queue<String> solicitudesRecibidas;

    // Grafo No Dirigido General
    // Usamos HashSet para garantizar O(1) al buscar vecinos/amigos
    private Set<Cliente> amigos;

    public Cliente(String nombre, int scoring) {
        this.nombre = nombre;
        this.scoring = scoring;
        this.siguiendo = new ArrayList<>();
        this.solicitudesRecibidas = new LinkedList<>();

        // Inicializamos el Set de amigos
        this.amigos = new HashSet<>();

        assert repOK() : "Error al crear cliente: Invariante inválido inicial.";
    }

    // --- MÉTODOS DE AMISTAD ---

    /**
     * Devuelve los vecinos (amigos) del cliente.
     * Al devolver un Set, garantizamos accesos O(1).
     */

    public Set<Cliente> getAmigos() {
        return amigos;
    }

    /**
     * Agrega un amigo a la lista de conexiones generales.
     * Complejidad: O(1) amortizado.
     */

    public void agregarAmigo(Cliente amigo) {
        if (amigo == null) {
            throw new IllegalArgumentException("No se puede ser amigo de un cliente nulo.");
        }
        if (this.equals(amigo)) {
            throw new IllegalStateException("No puedes ser amigo de ti mismo.");
        }

        // El Set ignora automáticamente los duplicados, pero lo validamos por prolijidad
        this.amigos.add(amigo);

        assert repOK() : "Error de IREP tras agregar amigo.";
    }

    /**
     * Elimina un amigo de las conexiones. Necesario para borrados en cascada.
     * Complejidad: O(1) amortizado.
     */

    public void eliminarAmigo(Cliente amigo) {
        if (amigo != null) {
            this.amigos.remove(amigo);
        }
        assert repOK();
    }


    // ==========================================
    // --- MÉTODOS PARA LA COLA PERSONAL (BUZÓN) ---
    // ==========================================

    public void recibirSolicitud(String nombreSolicitante) {
        if (!solicitudesRecibidas.contains(nombreSolicitante)) {
            solicitudesRecibidas.add(nombreSolicitante);
        }
        assert repOK();
    }

    public Queue<String> getSolicitudesRecibidas() {
        return solicitudesRecibidas;
    }


    // ==========================================
    // --- MÉTODOS DE RELACIÓN (SEGUIDORES) ---
    // ==========================================

    public List<Cliente> getSiguiendo() {
        return siguiendo;
    }

    public void agregarSeguido(Cliente seguido) {
        if (seguido == null) {
            throw new IllegalArgumentException("No se puede seguir a un cliente nulo.");
        }
        if (this.equals(seguido)) {
            throw new IllegalStateException("No puedes seguirte a ti mismo.");
        }
        if (siguiendo.size() >= 2) {
            throw new IllegalStateException("El cliente " + this.nombre + " ya sigue al máximo de 2 personas.");
        }

        if (!siguiendo.contains(seguido)) {
            siguiendo.add(seguido);
        }

        assert repOK() : "Error de IREP tras agregar seguido.";
    }


    // ==========================================
    // --- GETTERS, SETTERS Y STANDARD ---
    // ==========================================

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


    // ==========================================
    // --- IREP (INVARIANTE DE REPRESENTACIÓN) ---
    // ==========================================

    public boolean repOK() {
        // 1. Validaciones básicas de atributos
        if (nombre == null || nombre.trim().isEmpty()) return false;
        if (scoring < 0 || scoring > 100) return false;

        // Validación de nulidad de estructuras
        if (siguiendo == null) return false;
        if (solicitudesRecibidas == null) return false;
        if (amigos == null) return false; // NUEVO Iteración 3

        // 2. Validación de Topología (Iteración 2)
        if (siguiendo.size() > 2) return false;

        // 3. Validación de contenido de Seguidores
        for (Cliente c : siguiendo) {
            if (c == null) return false;
            if (c.getNombre().equals(this.nombre)) return false;
        }

        // 4. Validación de contenido de Amigos (NUEVO Iteración 3)
        for (Cliente amigo : amigos) {
            if (amigo == null) return false; // No nulos en el Set
            if (amigo.getNombre().equals(this.nombre)) return false; // No auto-bucles de amistad
        }

        return true;
    }
}