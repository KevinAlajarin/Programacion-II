package models;

import java.util.Objects;

public class Cliente implements Comparable<Cliente> {
    private String nombre;
    private int scoring;

    public Cliente(String nombre, int scoring) {
        this.nombre = nombre;
        this.scoring = scoring;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getScoring() { return scoring; }
    public void setScoring(int scoring) { this.scoring = scoring; }

    // Vital para HashMap O(1)
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
        return "Cliente{nombre='" + nombre + "', scoring=" + scoring + "}";
    }

    // Vital para BST O(log n)
    @Override
    public int compareTo(Cliente otro) {
        int result = Integer.compare(this.scoring, otro.scoring);
        if (result == 0) {
            return this.nombre.compareTo(otro.nombre); // Desempate por nombre
        }
        return result;
    }
}