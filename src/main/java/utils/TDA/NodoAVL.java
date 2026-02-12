package utils.TDA;

import models.Cliente;
import java.util.ArrayList;
import java.util.List;

public class NodoAVL {
    // El dato clave por el cual ordenamos (Scoring)
    int scoring;

    // La lista de clientes que tienen este scoring (Manejo de duplicados)
    List<Cliente> clientes;

    // Estructura del árbol
    NodoAVL izquierdo;
    NodoAVL derecho;
    int altura;

    public NodoAVL(int scoring, Cliente c) {
        this.scoring = scoring;
        this.clientes = new ArrayList<>();
        this.clientes.add(c);
        this.altura = 1; // Altura inicial de una hoja
    }

    // Métodos auxiliares para agregar más clientes al mismo nodo (mismo scoring)
    public void agregarCliente(Cliente c) {
        this.clientes.add(c);
    }

    public void eliminarCliente(Cliente c) {
        this.clientes.remove(c);
    }

    public boolean estaVacio() {
        return clientes.isEmpty();
    }

    public List<Cliente> getClientes() {
        return clientes;
    }
}