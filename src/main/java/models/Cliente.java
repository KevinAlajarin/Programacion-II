package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Quitamos "implements Comparable<Cliente>"
public class Cliente {
    private String nombre;
    private int scoring;
    private List<Cliente> siguiendo;

    public Cliente(String nombre, int scoring) {
        this.nombre = nombre;
        this.scoring = scoring;
        this.siguiendo = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getScoring() { return scoring; }
    public void setScoring(int scoring) { this.scoring = scoring; }

    public List<Cliente> getSiguiendo() { return siguiendo; }

    public void agregarSeguido(Cliente seguido) {
        if (!siguiendo.contains(seguido)) {
            siguiendo.add(seguido);
        }
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
        // Formato limpio: "Bob (Scoring: 88)"
        return nombre + " (Scoring: " + scoring + ")";
    }
}