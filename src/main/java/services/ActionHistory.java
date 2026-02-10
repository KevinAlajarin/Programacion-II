package services;

import models.Accion;
import java.util.Stack;

public class ActionHistory {
    private Stack<Accion> historial;

    public ActionHistory() {
        this.historial = new Stack<>();
    }

    // O(1) - Cumple requisito de eficiencia
    public void registrarAccion(Accion accion) {
        historial.push(accion);
    }

    // O(1) - Cumple requisito de eficiencia
    public Accion deshacer() {
        if (historial.isEmpty()) return null;
        return historial.pop();
    }

    public boolean isEmpty() {
        return historial.isEmpty();
    }

    // --- NUEVO: Requisito de "Consultar todas las acciones" ---
    public void mostrarHistorialCompleto() {
        if (historial.isEmpty()) {
            System.out.println("   (Historial vacío)");
            return;
        }
        System.out.println("\n--- HISTORIAL DE ACCIONES (LIFO) ---");
        // Stack hereda de Vector, así que podemos iterarlo.
        // Lo mostramos de arriba a abajo (del más reciente al más antiguo).
        for (int i = historial.size() - 1; i >= 0; i--) {
            System.out.println(historial.get(i));
        }
    }
}