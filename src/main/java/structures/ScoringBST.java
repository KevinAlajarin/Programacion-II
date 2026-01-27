package structures;

import models.Cliente;

public class ScoringBST {
    private class Node {
        Cliente data;
        Node left, right;

        Node(Cliente data) {
            this.data = data;
        }
    }

    private Node root;

    public void insert(Cliente cliente) {
        root = insertRec(root, cliente);
    }

    private Node insertRec(Node root, Cliente cliente) {
        if (root == null) return new Node(cliente);

        if (cliente.compareTo(root.data) < 0)
            root.left = insertRec(root.left, cliente);
        else if (cliente.compareTo(root.data) > 0)
            root.right = insertRec(root.right, cliente);

        return root;
    }

    public Cliente search(int scoring, String nombre) {
        Cliente target = new Cliente(nombre, scoring);
        return searchRec(root, target);
    }

    private Cliente searchRec(Node root, Cliente target) {
        if (root == null) return null;
        if (target.compareTo(root.data) == 0) return root.data;

        if (target.compareTo(root.data) < 0)
            return searchRec(root.left, target);
        else
            return searchRec(root.right, target);
    }

    // Método auxiliar para la Iteración 2 (Nivel 4), lo dejamos preparado.
    public void printLevel4() {
        // Implementación pendiente para fase 2
        System.out.println("Funcionalidad BST Nivel 4 pendiente para Iteración 2");
    }
}