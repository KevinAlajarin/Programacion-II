package utils.TDA;

import models.Cliente;
import java.util.ArrayList;
import java.util.List;

public class ArbolAVL {
    private NodoAVL raiz;

    // --- MÉTODOS PÚBLICOS (La Interfaz del Árbol) ---

    public void insertar(int scoring, Cliente c) {
        raiz = insertar(raiz, scoring, c);
    }

    public void eliminar(int scoring, Cliente c) {
        raiz = eliminar(raiz, scoring, c);
    }

    public List<Cliente> buscar(int scoring) {
        NodoAVL nodo = buscar(raiz, scoring);
        if (nodo != null) {
            return nodo.getClientes(); // Retorna la lista de clientes con ese puntaje
        }
        return new ArrayList<>(); // Retorna lista vacía si no encuentra
    }

    // Para depuración y reportes: Imprimir en orden (Menor a Mayor)
    public void imprimirInOrder() {
        imprimirInOrder(raiz);
    }

    // --- MÉTODOS PRIVADOS (Lógica Recursiva y Rotaciones) ---

    // 1. Inserción con Balanceo
    private NodoAVL insertar(NodoAVL nodo, int scoring, Cliente c) {
        if (nodo == null) {
            return new NodoAVL(scoring, c);
        }

        if (scoring < nodo.scoring) {
            nodo.izquierdo = insertar(nodo.izquierdo, scoring, c);
        } else if (scoring > nodo.scoring) {
            nodo.derecho = insertar(nodo.derecho, scoring, c);
        } else {
            // El scoring ya existe en el árbol, solo agregamos el cliente a la lista del nodo
            nodo.agregarCliente(c);
            return nodo; // No cambia la altura, no necesitamos balancear
        }

        // Actualizar altura
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));

        // Obtener factor de equilibrio
        int balance = getBalance(nodo);

        // Caso 1: Izquierda Izquierda (Rotación Simple Derecha)
        if (balance > 1 && scoring < nodo.izquierdo.scoring) {
            return rotarDerecha(nodo);
        }

        // Caso 2: Derecha Derecha (Rotación Simple Izquierda)
        if (balance < -1 && scoring > nodo.derecho.scoring) {
            return rotarIzquierda(nodo);
        }

        // Caso 3: Izquierda Derecha (Rotación Doble: Izq -> Der)
        if (balance > 1 && scoring > nodo.izquierdo.scoring) {
            nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            return rotarDerecha(nodo);
        }

        // Caso 4: Derecha Izquierda (Rotación Doble: Der -> Izq)
        if (balance < -1 && scoring < nodo.derecho.scoring) {
            nodo.derecho = rotarDerecha(nodo.derecho);
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    // 2. Eliminación (Simplificada: Solo borra de la lista, si la lista queda vacía borra el nodo)
    private NodoAVL eliminar(NodoAVL nodo, int scoring, Cliente c) {
        if (nodo == null) return null;

        if (scoring < nodo.scoring) {
            nodo.izquierdo = eliminar(nodo.izquierdo, scoring, c);
        } else if (scoring > nodo.scoring) {
            nodo.derecho = eliminar(nodo.derecho, scoring, c);
        } else {
            // Encontramos el nodo. Eliminamos al cliente de la lista.
            nodo.eliminarCliente(c);

            // Si aún quedan clientes con este puntaje, no borramos el nodo físico del árbol
            if (!nodo.estaVacio()) {
                return nodo;
            }

            // Si la lista quedó vacía, procedemos a borrar el nodo físico (Lógica estándar BST)
            if ((nodo.izquierdo == null) || (nodo.derecho == null)) {
                NodoAVL temp = (null != nodo.izquierdo) ? nodo.izquierdo : nodo.derecho;
                if (temp == null) {
                    temp = nodo;
                    nodo = null;
                } else {
                    nodo = temp;
                }
            } else {
                // Nodo con dos hijos: buscar sucesor (menor del subárbol derecho)
                NodoAVL temp = valorMinimo(nodo.derecho);
                nodo.scoring = temp.scoring;
                nodo.clientes = temp.clientes; // Copiamos la lista entera
                // Eliminamos el sucesor antiguo
                // (Nota: Esta parte es compleja si hay duplicados, para simplificar en TPO
                // asumimos que al borrar nodo físico rebalanceamos lo básico)
                nodo.derecho = eliminar(nodo.derecho, temp.scoring, temp.getClientes().get(0));
            }
        }

        if (nodo == null) return null;

        // Re-balanceo tras eliminación
        nodo.altura = Math.max(altura(nodo.izquierdo), altura(nodo.derecho)) + 1;
        int balance = getBalance(nodo);

        if (balance > 1 && getBalance(nodo.izquierdo) >= 0) return rotarDerecha(nodo);
        if (balance > 1 && getBalance(nodo.izquierdo) < 0) {
            nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            return rotarDerecha(nodo);
        }
        if (balance < -1 && getBalance(nodo.derecho) <= 0) return rotarIzquierda(nodo);
        if (balance < -1 && getBalance(nodo.derecho) > 0) {
            nodo.derecho = rotarDerecha(nodo.derecho);
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    private NodoAVL buscar(NodoAVL nodo, int scoring) {
        if (nodo == null || nodo.scoring == scoring) return nodo;
        if (nodo.scoring > scoring) return buscar(nodo.izquierdo, scoring);
        return buscar(nodo.derecho, scoring);
    }

    // --- UTILIDADES DE AVL ---

    private int altura(NodoAVL N) {
        if (N == null) return 0;
        return N.altura;
    }

    private int getBalance(NodoAVL N) {
        if (N == null) return 0;
        return altura(N.izquierdo) - altura(N.derecho);
    }

    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x = y.izquierdo;
        NodoAVL T2 = x.derecho;
        // Rotación
        x.derecho = y;
        y.izquierdo = T2;
        // Actualizar alturas
        y.altura = Math.max(altura(y.izquierdo), altura(y.derecho)) + 1;
        x.altura = Math.max(altura(x.izquierdo), altura(x.derecho)) + 1;
        return x;
    }

    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y = x.derecho;
        NodoAVL T2 = y.izquierdo;
        // Rotación
        y.izquierdo = x;
        x.derecho = T2;
        // Actualizar alturas
        x.altura = Math.max(altura(x.izquierdo), altura(x.derecho)) + 1;
        y.altura = Math.max(altura(y.izquierdo), altura(y.derecho)) + 1;
        return y;
    }

    private NodoAVL valorMinimo(NodoAVL nodo) {
        NodoAVL actual = nodo;
        while (actual.izquierdo != null) actual = actual.izquierdo;
        return actual;
    }

    private void imprimirInOrder(NodoAVL node) {
        if (node != null) {
            imprimirInOrder(node.izquierdo);
            System.out.println("Scoring " + node.scoring + ": " + node.getClientes());
            imprimirInOrder(node.derecho);
        }
    }
}